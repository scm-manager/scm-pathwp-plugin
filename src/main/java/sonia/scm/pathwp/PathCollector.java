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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.api.RepositoryService;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class PathCollector implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(PathCollector.class);

  private final RepositoryService repositoryService;
  private final Set<String> paths = new HashSet<>();

  PathCollector(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  Set<String> collect(Iterable<Changeset> changesets) throws IOException {
    for (Changeset c : changesets) {
      collect(c);
    }
    return paths;
  }

  private void collect(Changeset changeset) throws IOException {
    Modifications modifications = repositoryService.getModificationsCommand()
      .revision(changeset.getId())
      .getModifications();

    if (modifications != null) {
      collect(modifications);
    } else {
      LOG.warn("there is no modifications for the changeset {}", changeset.getId());
    }
  }

  private void collect(Modifications modifications) {
    append(modifications.getEffectedPaths());
  }

  private void append(Iterable<String> modifiedPaths) {
    for (String path : modifiedPaths) {
      paths.add(normalizePath(path));
    }
  }

  private String normalizePath(String path) {
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  @Override
  public void close() {
    repositoryService.close();
  }
}
