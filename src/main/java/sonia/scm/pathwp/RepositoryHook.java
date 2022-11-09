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

import com.github.legman.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import sonia.scm.EagerSingleton;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Receive repository events and Verify the write permission on every path found in the event.
 *
 * @author Mohamed Karray
 */
@Slf4j
@Extension
@EagerSingleton
public class RepositoryHook {

  private final PathWritePermissionService service;
  private final PathCollectorFactory pathCollectorFactory;

  @Inject
  public RepositoryHook(PathWritePermissionService service, PathCollectorFactory pathCollectorFactory) {
    this.service = service;
    this.pathCollectorFactory = pathCollectorFactory;
  }

  @Subscribe(async = false)
  public void onEvent(PreReceiveRepositoryHookEvent event) throws IOException {
    HookContext context = event.getContext();
    if (context == null) {
      log.warn("there is no context in the received repository hook");
      return;
    }
    Repository repository = event.getRepository();
    if (repository == null) {
      log.warn("there is no repository in the received repository hook");
      return;
    }

    if (!service.isPluginEnabled(repository)) {
      log.trace("pathwp plugin is disabled.");
      return;
    }

    log.trace("received hook for repository {}", repository.getName());
    Set<String> paths = collectPath(context, repository);
    Set<String> branches = new HashSet<>();
    branches.addAll(context.getBranchProvider().getCreatedOrModified());
    branches.addAll(context.getBranchProvider().getDeletedOrClosed());

    Subject subject = SecurityUtils.getSubject();
    PrincipalCollection principals = subject.getPrincipals();

    User user = principals.oneByType(User.class);

    checkIfUserIsPrivileged(repository, user, branches, paths);
  }

  private void checkIfUserIsPrivileged(Repository repository, User user, Set<String> branches, Set<String> paths) {
    for (String branch : branches) {
      for (String path : paths) {
        if (!service.isPrivileged(user, repository, branch, path)) {
          throw new PathWritePermissionException("Permission denied for the path " + path + " on branch " + branch);
        }
      }
    }
  }

  private Set<String> collectPath(HookContext eventContext, Repository repository) throws IOException {
    if (eventContext.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)) {
      try (PathCollector collector = pathCollectorFactory.create(repository)) {
        return collector.collect(eventContext.getChangesetProvider().getChangesets());
      }
    }
    return Collections.emptySet();
  }

}
