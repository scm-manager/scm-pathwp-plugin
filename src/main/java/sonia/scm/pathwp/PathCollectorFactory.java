package sonia.scm.pathwp;

import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;

public class PathCollectorFactory {

  private final RepositoryServiceFactory repositoryServiceFactory;

  @Inject
  public PathCollectorFactory(RepositoryServiceFactory repositoryServiceFactory) {
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  PathCollector create(Repository repository) {
    return new PathCollector(repositoryServiceFactory.create(repository));
  }
}
