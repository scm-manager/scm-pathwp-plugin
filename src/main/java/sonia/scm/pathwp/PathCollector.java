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
    append(modifications.getAdded());
    append(modifications.getModified());
    append(modifications.getRemoved());
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
