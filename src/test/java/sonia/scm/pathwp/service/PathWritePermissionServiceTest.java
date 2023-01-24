/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.pathwp.service;

import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.GroupCollector;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PathWritePermissionServiceTest {

  public static final String MAIL = "email@d.de";
  public static final String USERNAME = "user_1";
  public static final User USER = new User(USERNAME, "User 1", MAIL);
  public static final String PATH = "dir1/subDir/file1.txt";
  public static final String BRANCH = "main";
  public static final PathWritePermission.Type TYPE = PathWritePermission.Type.ALLOW;
  public static final boolean GROUP = false;
  public static final String GROUP_NAME = "group1";

  ConfigurationStore<PathWritePermissions> store;
  ConfigurationStoreFactory storeFactory;

  @Mock
  private GroupCollector groupCollector;
  @Mock
  private Subject subject;

  PathWritePermissionService service;
  public static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  static {
    REPOSITORY.setId("id-1");
  }

  @BeforeEach
  public void init() {
    storeFactory = new InMemoryConfigurationStoreFactory();
    service = new PathWritePermissionService(storeFactory, null, groupCollector);
    store = storeFactory.withType(PathWritePermissions.class).withName("pathWritePermission").forRepository(REPOSITORY).build();
    lenient().when(groupCollector.collect(anyString())).thenReturn(ImmutableSet.of(GROUP_NAME));

    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithOwnerPermission {

    @BeforeEach
    void setPermissions() {
      lenient().when(subject.isPermitted("repository:pathwp:id-1")).thenReturn(true);
    }

    @Test
    @SubjectAware(username = "owner", password = "secret")
    void shouldStorePermissionForOwner() {
      PathWritePermissions permissions = new PathWritePermissions();

      PathWritePermission permission = createPathWritePermission();
      permissions.getPermissions().add(permission);
      service.setPermissions(REPOSITORY, permissions);

      assertThat(store.get()).isSameAs(permissions);
    }

    @Test
    @SubjectAware(username = "owner", password = "secret")
    void shouldBePermittedAsOwner() {
      boolean privileged = PathWritePermissionService.isPermitted(REPOSITORY);
      assertThat(privileged).isTrue();
    }
  }

  @Nested
  class WithAdminPermission {

    @BeforeEach
    void setPermissions() {
      lenient().when(subject.isPermitted("repository:pathwp:id-1")).thenReturn(true);
      lenient().when(subject.getPrincipal()).thenReturn("admin");
    }

    @Test
    void shouldBePermittedAsAdmin() {
      boolean privileged = PathWritePermissionService.isPermitted(REPOSITORY);
      assertThat(privileged).isTrue();
    }

    @Test
    void shouldAllowAnyUserIfTheConfigIsDisabled() {
      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isTrue();
    }

    @Test
    void shouldPrivilegeUserBecauseThePathIsAllowedToTheUser() {
      PathWritePermissions permissions = new PathWritePermissions();
      permissions.getPermissions().add(createPathWritePermission());
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isTrue();
    }

    @Test
    void shouldPrivilegeUserBecauseAllPathsAreAllowedToTheUser() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission pathWritePermission = new PathWritePermission("*", BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), false, PathWritePermission.Type.ALLOW);
      permissions.getPermissions().add(pathWritePermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isTrue();
    }

    @Test
    void shouldPrivilegeUserBecauseAllPathsAreAllowedToOneOfHisGroups() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission pathWritePermission = new PathWritePermission("*", BRANCH, PathWritePermission.BranchScope.INCLUDE, GROUP_NAME, true, PathWritePermission.Type.ALLOW);
      permissions.getPermissions().add(pathWritePermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isTrue();
    }

    @Test
    void shouldPrivilegeUserBecauseTheSearchedPathIsAllowedToOneOfHisGroups() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission pathWritePermission = new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, GROUP_NAME, true, PathWritePermission.Type.ALLOW);
      permissions.getPermissions().add(pathWritePermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isTrue();
    }

    @Test
    void shouldDenyPermissionBecauseAllPathsAreAllowedToTheUserButTheSearchedPathIsDenied() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission pathWritePermission = new PathWritePermission("*", BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), false, PathWritePermission.Type.ALLOW);
      PathWritePermission deniedPermission = new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), false, PathWritePermission.Type.DENY);
      permissions.getPermissions().add(pathWritePermission);
      permissions.getPermissions().add(deniedPermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isFalse();
    }

    @Test
    void shouldDenyPermissionBecauseAllPathsAreAllowedToOneOfTheUserGroupsButTheSearchedPathIsDenied() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission pathWritePermission = new PathWritePermission("*", BRANCH, PathWritePermission.BranchScope.INCLUDE, GROUP_NAME, true, PathWritePermission.Type.ALLOW);
      PathWritePermission deniedPermission = new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), false, PathWritePermission.Type.DENY);
      permissions.getPermissions().add(pathWritePermission);
      permissions.getPermissions().add(deniedPermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isFalse();
    }

    @Test
    void shouldDenyPermissionBecauseTheSearchedPathIsDenied() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission deniedPermission = new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), false, PathWritePermission.Type.DENY);
      permissions.getPermissions().add(deniedPermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isFalse();
    }

    @Test
    void shouldDenyPermissionBecauseTheSearchedBranchIsExcluded() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission deniedPermission = new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.EXCLUDE, USER.getName(), false, PathWritePermission.Type.ALLOW);
      permissions.getPermissions().add(deniedPermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isFalse();
    }

    @Test
    void shouldAllowPermissionBecauseTheSearchedBranchIsIncluded() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission deniedPermission = new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), false, PathWritePermission.Type.ALLOW);
      permissions.getPermissions().add(deniedPermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isTrue();
    }

    @Test
    void shouldDenyPermissionBecauseTheSearchedPathIsDeniedToOneOfTheUserGroups() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission deniedPermission = new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, GROUP_NAME, true, PathWritePermission.Type.DENY);
      permissions.getPermissions().add(deniedPermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isFalse();
    }

    @Test
    @SubjectAware(username = "admin", password = "secret")
    void shouldDenyPermissionBecauseThereIsNoStoredPermissionForTheSearchedPath() {
      PathWritePermissions permissions = new PathWritePermissions();
      PathWritePermission deniedPermission = new PathWritePermission("other_path", BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), false, PathWritePermission.Type.ALLOW);
      permissions.getPermissions().add(deniedPermission);
      permissions.setEnabled(true);
      service.setPermissions(REPOSITORY, permissions);

      boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH, PATH);

      assertThat(privileged).isFalse();
    }
  }

  @Test
  void shouldFailOnStoringPermissionForNotAdminOrOwnerUsers() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:pathwp:id-1");
    PathWritePermissions permissions = new PathWritePermissions();

    PathWritePermission permission = createPathWritePermission();
    permissions.getPermissions().add(permission);

    assertThrows(AuthorizationException.class, () -> service.setPermissions(REPOSITORY, permissions));
  }

  private PathWritePermission createPathWritePermission() {
    return new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, USER.getName(), GROUP, TYPE);
  }
}
