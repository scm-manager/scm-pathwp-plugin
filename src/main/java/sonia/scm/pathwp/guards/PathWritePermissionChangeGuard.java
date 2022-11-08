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
import org.apache.shiro.SecurityUtils;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;

import javax.inject.Inject;
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
