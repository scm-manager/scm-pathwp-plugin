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

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.pathwp.api.PathWritePermissionResource;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, RepositoryServiceFactory serviceFactory) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.serviceFactory = serviceFactory;
  }


  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (PathWritePermissionService.isPermitted(repository)) {
      LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), PathWritePermissionResource.class);
      if (doesSupportBranches(repository)) {
        appender.appendLink("pathWpConfigWithBranches", linkBuilder.method("get").parameters(repository.getNamespace(), repository.getName()).href());
      } else {
        appender.appendLink("pathWpConfig", linkBuilder.method("get").parameters(repository.getNamespace(), repository.getName()).href());
      }
    }
  }

  private boolean doesSupportBranches(Repository repository) {
    try (RepositoryService service = serviceFactory.create(repository)) {
      return service.isSupported(Command.BRANCHES);
    }
  }
}
