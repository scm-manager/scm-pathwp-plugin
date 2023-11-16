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
package sonia.scm.pathwp;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.FeatureNotSupportedException;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.repository.Added;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class RepositoryHookTest {

  @Mock
  private PathWritePermissionService service;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PathCollectorFactory pathCollectorFactory;

  @InjectMocks
  private RepositoryHook hook;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PreReceiveRepositoryHookEvent event;
  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Subject subject;
  private final User user = new User("trillian");

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
    when(subject.getPrincipals().oneByType(User.class)).thenReturn(user);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @BeforeEach
  void prepareBasicEvent() throws IOException {
    when(event.getRepository()).thenReturn(repository);
    when(service.isPluginEnabled(repository)).thenReturn(true);
    when(event.getContext().isFeatureSupported(HookFeature.CHANGESET_PROVIDER)).thenReturn(true);
    Set<Changeset> changesets = singleton(new Changeset());
    when(event.getContext().getChangesetProvider().getChangesets()).thenReturn(changesets);
    when(pathCollectorFactory.create(repository).collect(changesets)).thenReturn(singleton("path"));
  }

  @Nested
  class WithBranchProvider {

    @BeforeEach
    void prepareBranches() {
      when(event.getContext().isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
      when(event.getContext().getBranchProvider().getCreatedOrModified()).thenReturn(singletonList("branch"));
    }

    @Test
    @SuppressWarnings("java:S2699") // we only have to make sure that there is no exception
    void shouldNotFailWithPermission() throws IOException {
      when(service.isPrivileged(
        user,
        repository,
        "branch",
        "path"
      )).thenReturn(true);

      hook.onEvent(event);

      // nothing happens
    }

    @Test
    void shouldFailWithoutPermission() {
      when(service.isPrivileged(
        user,
        repository,
        "branch",
        "path"
      )).thenReturn(false);

      Assertions.assertThrows(PathWritePermissionException.class, () -> hook.onEvent(event));
    }

    @Nested
    class WithModificationsProvider {

      @BeforeEach
      void prepareModifications() {
        when(event.getContext().isFeatureSupported(HookFeature.MODIFICATIONS_PROVIDER)).thenReturn(true);
        Modifications modifications = new Modifications("42", new Added("modifiedPath"));
        when(event.getContext().getModificationsProvider().getModifications("branch"))
          .thenReturn(modifications);
      }

      @Test
      @SuppressWarnings("java:S2699") // we only have to make sure that there is no exception
      void shouldNotFailWithPermission() throws IOException {
        when(service.isPrivileged(
          user,
          repository,
          "branch",
          "modifiedPath"
        )).thenReturn(true);

        hook.onEvent(event);

        // nothing happens
      }

      @Test
      void shouldFailWithoutPermission() {
        when(service.isPrivileged(
          user,
          repository,
          "branch",
          "modifiedPath"
        )).thenReturn(false);

        Assertions.assertThrows(PathWritePermissionException.class, () -> hook.onEvent(event));
      }
    }
  }

  @Nested
  class WithoutBranchProvider {

    @BeforeEach
    void prepareBranches() {
      when(event.getContext().isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(false);
      when(event.getContext().getBranchProvider()).thenThrow(FeatureNotSupportedException.class);
    }

    @Test
    @SuppressWarnings("java:S2699") // we only have to make sure that there is no exception
    void shouldNotFailWithPermission() throws IOException {
      when(service.isPrivileged(
        user,
        repository,
        "*",
        "path"
      )).thenReturn(true);

      hook.onEvent(event);

      // nothing happens
    }

    @Test
    void shouldFailWithoutPermission() {
      when(service.isPrivileged(
        user,
        repository,
        "*",
        "path"
      )).thenReturn(false);

      Assertions.assertThrows(PathWritePermissionException.class, () -> hook.onEvent(event));
    }
  }
}
