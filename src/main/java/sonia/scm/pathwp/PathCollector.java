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
