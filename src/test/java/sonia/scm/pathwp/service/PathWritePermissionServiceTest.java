package sonia.scm.pathwp.service;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.group.GroupCollector;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/pathwp/shiro-001.ini", username = "user_1", password = "secret")
public class PathWritePermissionServiceTest {

  public static final String MAIL = "email@d.de";
  public static final String USERNAME = "user_1";
  public static final User USER = new User(USERNAME, "User 1", MAIL);
  public static final String PATH = "dir1/subDir/file1.txt";
  public static final PathWritePermission.Type TYPE = PathWritePermission.Type.ALLOW;
  public static final boolean GROUP = false;
  public static final String GROUP_NAME = "group1";

  @Rule
  public ShiroRule shiro = new ShiroRule();

  ConfigurationStore<PathWritePermissions> store;
  ConfigurationStoreFactory storeFactory;

  private GroupCollector groupCollector = mock(GroupCollector.class);

  PathWritePermissionService service;
  public static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Before
  public void init() {
    storeFactory = new InMemoryConfigurationStoreFactory();
    service = new PathWritePermissionService(storeFactory, null, groupCollector);
    store = storeFactory.withType(PathWritePermissions.class).withName("pathWritePermission").forRepository(REPOSITORY).build();
    when(groupCollector.collect(anyString())).thenReturn(ImmutableSet.of(GROUP_NAME));
  }

  public PathWritePermissionServiceTest() {
    // cleanup state that might have been left by other tests
    ThreadContext.unbindSecurityManager();
    ThreadContext.unbindSubject();
    ThreadContext.remove();
  }

  @Test
  @SubjectAware(username = "owner", password = "secret")
  public void shouldStorePermissionForOwner() {
    PathWritePermissions permissions = new PathWritePermissions();

    PathWritePermission permission = createPathWritePermission();
    permissions.getPermissions().add(permission);
    service.setPermissions(REPOSITORY, permissions);

    assertThat(store.get()).isSameAs(permissions);
  }

  @Test
  public void shouldFailOnStoringPermissionForNotAdminOrOwnerUsers() {
    PathWritePermissions permissions = new PathWritePermissions();

    PathWritePermission permission = createPathWritePermission();
    permissions.getPermissions().add(permission);

    assertThatThrownBy(() -> service.setPermissions(REPOSITORY, permissions)).hasMessage("Subject does not have permission [repository:pathwp:id-1]");
  }

  @Test
  @SubjectAware(username = "owner", password = "secret")
  public void shouldBePermittedAsOwner() {
    boolean privileged = PathWritePermissionService.isPermitted(REPOSITORY);
    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldBePermittedAsAdmin() {
    boolean privileged = PathWritePermissionService.isPermitted(REPOSITORY);
    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldAllowAnyUserIfTheConfigIsDisabled() {
    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldPrivilegeUserBecauseThePathIsAllowedToTheUser() {
    PathWritePermissions permissions = new PathWritePermissions();
    permissions.getPermissions().add(createPathWritePermission());
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldPrivilegeUserBecauseAllPathsAreAllowedToTheUser() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission pathWritePermission = new PathWritePermission("*", USER.getName(), false, PathWritePermission.Type.ALLOW);
    permissions.getPermissions().add(pathWritePermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldPrivilegeUserBecauseAllPathsAreAllowedToOneOfHisGroups() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission pathWritePermission = new PathWritePermission("*", GROUP_NAME, true, PathWritePermission.Type.ALLOW);
    permissions.getPermissions().add(pathWritePermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldPrivilegeUserBecauseTheSearchedPathIsAllowedToOneOfHisGroups() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission pathWritePermission = new PathWritePermission(PATH, GROUP_NAME, true, PathWritePermission.Type.ALLOW);
    permissions.getPermissions().add(pathWritePermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldDenyPermissionBecauseAllPathsAreAllowedToTheUserButTheSearchedPathIsDenied() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission pathWritePermission = new PathWritePermission("*", USER.getName(), false, PathWritePermission.Type.ALLOW);
    PathWritePermission deniedPermission = new PathWritePermission(PATH, USER.getName(), false, PathWritePermission.Type.DENY);
    permissions.getPermissions().add(pathWritePermission);
    permissions.getPermissions().add(deniedPermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isFalse();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldDenyPermissionBecauseAllPathsAreAllowedToOneOfTheUserGroupsButTheSearchedPathIsDenied() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission pathWritePermission = new PathWritePermission("*", GROUP_NAME, true, PathWritePermission.Type.ALLOW);
    PathWritePermission deniedPermission = new PathWritePermission(PATH, USER.getName(), false, PathWritePermission.Type.DENY);
    permissions.getPermissions().add(pathWritePermission);
    permissions.getPermissions().add(deniedPermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isFalse();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldDenyPermissionBecauseTheSearchedPathIsDenied() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission deniedPermission = new PathWritePermission(PATH, USER.getName(), false, PathWritePermission.Type.DENY);
    permissions.getPermissions().add(deniedPermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isFalse();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldDenyPermissionBecauseTheSearchedPathIsDeniedToOneOfTheUserGroups() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission deniedPermission = new PathWritePermission(PATH, GROUP_NAME, true, PathWritePermission.Type.DENY);
    permissions.getPermissions().add(deniedPermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isFalse();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldDenyPermissionBecauseThereIsNoStoredPermissionForTheSearchedPath() {
    PathWritePermissions permissions = new PathWritePermissions();
    PathWritePermission deniedPermission = new PathWritePermission("other_path", USER.getName(), false, PathWritePermission.Type.ALLOW);
    permissions.getPermissions().add(deniedPermission);
    permissions.setEnabled(true);
    service.setPermissions(REPOSITORY, permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, PATH);

    assertThat(privileged).isFalse();
  }

  private PathWritePermission createPathWritePermission() {
    return new PathWritePermission(PATH, USER.getName(), GROUP, TYPE);
  }
}
