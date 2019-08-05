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

    if (PathWritePermissionService.isPermitted(repository)) {
      log.debug("skip pathwp check for {}, because the user has modify privileges to the repository", repository.getNamespaceAndName());
      return;
    }

    if (!service.isPluginEnabled(repository)){
      log.trace("pathwp plugin is disabled.");
      return;
    }

    log.trace("received hook for repository {}", repository.getName());
    Set<String> paths = collectPath(context, repository);

    checkIfUserIsPrivileged(repository, paths);
  }

  private void checkIfUserIsPrivileged(Repository repository, Set<String> paths) {
    Subject subject = SecurityUtils.getSubject();
    PrincipalCollection principals = subject.getPrincipals();

    User user = principals.oneByType(User.class);

    for (String path : paths) {
      if (!service.isPrivileged(user, repository, path)) {
        throw new PathWritePermissionException("Permission denied for the path " + path);
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
