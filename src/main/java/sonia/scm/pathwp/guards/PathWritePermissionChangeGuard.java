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

package sonia.scm.pathwp.guards;

import com.cloudogu.scm.editor.ChangeGuard;
import com.cloudogu.scm.editor.ChangeObstacle;
import org.apache.shiro.SecurityUtils;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Stream.concat;

@Extension
@Requires("scm-editor-plugin")
public class PathWritePermissionChangeGuard implements ChangeGuard {

  private final PathWritePermissionService service;
  private final RepositoryManager repositoryManager;

  @Inject
  public PathWritePermissionChangeGuard(PathWritePermissionService service, RepositoryManager repositoryManager) {
    this.service = service;
    this.repositoryManager = repositoryManager;
  }

  @Override
  public Collection<ChangeObstacle> getObstacles(NamespaceAndName namespaceAndName, String branch, Changes changes) {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    Repository repository = repositoryManager.get(namespaceAndName);
    return concat(
      changes.getFilesToCreate().stream(),
      concat(
        changes.getFilesToDelete().stream(),
        concat(
          changes.getFilesToModify().stream(),
          changes.getPathForCreate().map(path -> path + "/something").map(Stream::of).orElse(Stream.empty())
        )
      )
    ).map(path -> getObstacles(repository, user, branch, path))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

  private Optional<ChangeObstacle> getObstacles(Repository repository, User user, String branch, String path) {
    if (service.isPrivileged(user, repository, branch, path)) {
      return empty();
    } else {
      return of(new ChangeObstacle() {
        @Override
        public String getMessage() {
          return "The user has no privileges to write path " + path + " on branch " + branch;
        }

        @Override
        public String getKey() {
          return "scm-pathwp-plugin.obstacle";
        }
      });
    }
  }
}
