package sonia.scm.pathwp;

import com.github.legman.Subscribe;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import sonia.scm.EagerSingleton;
import sonia.scm.group.GroupNames;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.util.List;

/**
 * Receive repository events and Verify the write permission on every path found in the event.
 *
 * @author Mohamed Karray
 */
@Slf4j
@Extension
@EagerSingleton
public class RepositoryHook {

  private PathWritePermissionService service;
  private RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryHook(PathWritePermissionService service, RepositoryServiceFactory serviceFactory) {
    this.service = service;
    this.serviceFactory = serviceFactory;
  }

  @Subscribe(async = false)
  public void onEvent(PreReceiveRepositoryHookEvent event) {
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

    if (!service.isPluginEnabled(repository)){
      log.trace("The PathWP Plugin is disabled.");
      return ;
    }

    log.trace("received hook for repository {}", repository.getName());
    List<String> paths = getPaths(context, repository);
    for (String path : paths) {
      if (!isCurrentUserPrivileged(repository, path)) {
        throw new PathWritePermissionException("Permission denied for the path " + path);
      }
    }
  }

  public boolean isCurrentUserPrivileged(Repository repository, String path) {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    GroupNames userGroups = SecurityUtils.getSubject().getPrincipals().oneByType(GroupNames.class);
    return service.isPrivileged(user, userGroups, repository, path);
  }

  private List<String> getPaths(HookContext eventContext, Repository repository) {
    List<String> paths = Lists.newArrayList();
    if (eventContext.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)) {
      for (Changeset changeset : eventContext.getChangesetProvider().getChangesets()) {
        paths.addAll(getPaths(changeset, repository));
      }
    }
    return paths;
  }


  private List<String> getPaths(Changeset changeset, Repository repository) {
    List<String> paths = Lists.newArrayList();
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      Modifications modifications = repositoryService.getModificationsCommand()
        .revision(changeset.getId())
        .getModifications();
      if (modifications == null) {
        log.warn("there is no modifications for the changeset {}", changeset.getId());
        return paths;
      }
      if (modifications.getAdded() != null && !modifications.getAdded().isEmpty()) {
        paths.addAll(modifications.getAdded());
      }
      if (modifications.getModified() != null && !modifications.getModified().isEmpty()) {
        paths.addAll(modifications.getModified());
      }
      if (modifications.getRemoved() != null && !modifications.getRemoved().isEmpty()) {
        paths.addAll(modifications.getRemoved());
      }
      // remove leading slash
      paths.replaceAll(path -> {
        if (path.startsWith("/")) {
          return path.substring(1);
        }
        return path;
      });
    } catch (Exception e) {
      log.warn("cannot get modifications from the changeset " + changeset.getId(), e);
    }
    return paths;
  }

}
