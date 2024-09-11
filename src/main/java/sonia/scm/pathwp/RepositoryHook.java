/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.pathwp;

import com.github.legman.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import sonia.scm.ContextEntry;
import sonia.scm.EagerSingleton;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.user.User;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

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

    Subject subject = SecurityUtils.getSubject();
    PrincipalCollection principals = subject.getPrincipals();

    User user = principals.oneByType(User.class);

    if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
      Set<String> branches = new HashSet<>();
      branches.addAll(context.getBranchProvider().getCreatedOrModified());
      branches.addAll(context.getBranchProvider().getDeletedOrClosed());
      checkIfUserIsPrivileged(context, repository, user, branches, paths);
    } else {
      checkIfUserIsPrivileged(repository, user, paths);
    }
  }

  private void checkIfUserIsPrivileged(Repository repository, User user, Set<String> paths) {
    checkIfUserIsPrivileged(
      repository,
      user,
      paths.stream(),
      "*",
      path -> entity("Path", path).in(repository).build(),
      path -> "Permission denied for the path " + path
    );
  }

  private void checkIfUserIsPrivileged(HookContext context, Repository repository, User user, Set<String> branches, Set<String> allPaths) {
    for (String branch : branches) {
      Stream<String> paths = collectPaths(context, branch, allPaths);
      checkIfUserIsPrivileged(
        repository,
        user,
        paths,
        branch,
        path -> entity("Path", path).in("Branch", branch).in(repository).build(),
        path -> "Permission denied for the path " + path + " on branch " + branch
      );
    }
  }

  private Stream<String> collectPaths(HookContext context, String branch, Set<String> allPaths) {
    if (context.isFeatureSupported(HookFeature.MODIFICATIONS_PROVIDER)) {
      return context
        .getModificationsProvider()
        .getModifications(branch)
        .effectedPathsStream();
    } else {
      return allPaths.stream();
    }

  }

  private void checkIfUserIsPrivileged(Repository repository, User user, Stream<String> paths, String branch, Function<String, List<ContextEntry>> context, UnaryOperator<String> errorMessage) {
    paths.forEach(path -> {
      if (!service.isPrivileged(user, repository, branch, path)) {
        throw new PathWritePermissionException(context.apply(path), errorMessage.apply(path));
      }
    });
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
