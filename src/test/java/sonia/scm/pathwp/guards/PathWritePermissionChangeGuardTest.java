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
package sonia.scm.pathwp.guards;

import com.cloudogu.scm.editor.ChangeGuard;
import com.cloudogu.scm.editor.ChangeObstacle;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathWritePermissionChangeGuardTest {

  static final Repository REPOSITORY = new Repository("1", "git", "space", "X");
  static final User USER = new User();

  @Mock
  PathWritePermissionService service;
  @Mock
  RepositoryManager repositoryManager;
  @InjectMocks
  PathWritePermissionChangeGuard changeGuard;

  @BeforeEach
  void initRepositoryManager() {
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
  }

  @BeforeEach
  void mockUser() {
    Subject subject = mock(Subject.class);
    when(subject.getPrincipals()).thenReturn(new SimplePrincipalCollection(USER, "user"));
    ThreadContext.bind(subject);
  }

  @BeforeEach
  void mockService() {
    lenient().doReturn(true).when(service).isPrivileged(eq(USER), eq(REPOSITORY), argThat(argument -> !argument.contains("invalid")), argThat(argument -> !argument.contains("invalid")));
    lenient().doReturn(false).when(service).isPrivileged(eq(USER), eq(REPOSITORY), argThat(argument -> argument.contains("invalid")), argThat(argument -> argument.contains("invalid")));
  }

  @Test
  void shouldNotFailIfEverythingIsOk() {
    ChangeGuard.Changes changes = mock(ChangeGuard.Changes.class);
    when(changes.getFilesToCreate()).thenReturn(singleton("valid_create"));
    when(changes.getFilesToDelete()).thenReturn(singleton("valid_delete"));
    when(changes.getFilesToModify()).thenReturn(singleton("valid_modify"));
    when(changes.getPathForCreate()).thenReturn(of("valid_path"));

    Collection<ChangeObstacle> obstacles = changeGuard.getObstacles(REPOSITORY.getNamespaceAndName(), "irrelevant", changes);

    assertThat(obstacles).isEmpty();
  }

  @Test
  void shouldCheckFilesToCreate() {
    ChangeGuard.Changes changes = mock(ChangeGuard.Changes.class);
    when(changes.getFilesToCreate()).thenReturn(asList("valid", "invalid"));

    Collection<ChangeObstacle> obstacles = changeGuard.getObstacles(REPOSITORY.getNamespaceAndName(), "irrelevant", changes);

    assertThat(obstacles).hasSize(1);
  }

  @Test
  void shouldCheckFilesToDelete() {
    ChangeGuard.Changes changes = mock(ChangeGuard.Changes.class);
    when(changes.getFilesToDelete()).thenReturn(asList("valid", "invalid"));

    Collection<ChangeObstacle> obstacles = changeGuard.getObstacles(REPOSITORY.getNamespaceAndName(), "irrelevant", changes);

    assertThat(obstacles).hasSize(1);
  }

  @Test
  void shouldCheckFilesToModify() {
    ChangeGuard.Changes changes = mock(ChangeGuard.Changes.class);
    when(changes.getFilesToModify()).thenReturn(asList("valid", "invalid"));

    Collection<ChangeObstacle> obstacles = changeGuard.getObstacles(REPOSITORY.getNamespaceAndName(), "irrelevant", changes);

    assertThat(obstacles).hasSize(1);
  }

  @Test
  void shouldCheckPathsForCreate() {
    ChangeGuard.Changes changes = mock(ChangeGuard.Changes.class);
    when(changes.getPathForCreate()).thenReturn(of("invalid"));

    Collection<ChangeObstacle> obstacles = changeGuard.getObstacles(REPOSITORY.getNamespaceAndName(), "irrelevant", changes);

    assertThat(obstacles).hasSize(1);
  }
}
