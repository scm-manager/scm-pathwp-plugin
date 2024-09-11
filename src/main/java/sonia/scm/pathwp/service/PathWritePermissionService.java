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

package sonia.scm.pathwp.service;

import org.apache.shiro.SecurityUtils;
import sonia.scm.group.GroupCollector;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.GlobUtil;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Store the path write permissions in the repository store.
 *
 * @author Mohamed Karray
 */
public class PathWritePermissionService {

  public static final String PERMISSION_VERB = "pathwp";
  private final ConfigurationStoreFactory storeFactory;
  private final RepositoryManager repositoryManager;
  private final GroupCollector groupCollector;
  private static final String STORE_NAME = "pathWritePermission";

  @Inject
  public PathWritePermissionService(ConfigurationStoreFactory storeFactory, RepositoryManager repositoryManager, GroupCollector groupCollector) {
    this.storeFactory = storeFactory;
    this.repositoryManager = repositoryManager;
    this.groupCollector = groupCollector;
  }

  /**
   * The user is privileged for the given path
   * if he or one of his groups has not the DENY permission
   * and
   * if he or one of his groups has the ALLOW permission
   * <p>
   * The user is not privileged if there is no permission found for him or one of his groups.
   *
   * @param user user or group which should be permitted
   * @param repository the repository on which this rule is applied
   * @param branch the branch or pattern which will be affected
   * @param path the path or pattern which will be affected
   * @return true if the user is permitted to write the path
   */
  public boolean isPrivileged(User user, Repository repository, String branch, String path) {
    AssertUtil.assertIsNotNull(user);

    PathWritePermissions permissions = getPermissions(repository);
    if (!isPluginEnabled(permissions)) {
      return true;
    }

    Set<String> userGroups = groupCollector.collect(SecurityUtils.getSubject().getPrincipal().toString());

    BooleanSupplier userAllowed = () -> hasUserPermission(user, branch, path, permissions, PathWritePermission.Type.ALLOW);
    BooleanSupplier anyUserGroupsAllowed = () -> hasAnyGroupPermission(userGroups, branch, path, permissions, PathWritePermission.Type.ALLOW);
    BooleanSupplier userDenied = () -> hasUserPermission(user, branch, path, permissions, PathWritePermission.Type.DENY);
    BooleanSupplier anyUserGroupsDenied = () -> hasAnyGroupPermission(userGroups, path, branch, permissions, PathWritePermission.Type.DENY);

    return !userDenied.getAsBoolean() && !anyUserGroupsDenied.getAsBoolean() && (userAllowed.getAsBoolean() || anyUserGroupsAllowed.getAsBoolean());
  }

  private boolean isPluginEnabled(PathWritePermissions permissions) {
    return permissions.isEnabled();
  }

  public boolean isPluginEnabled(Repository repository) {
    PathWritePermissions permissions = getPermissions(repository);
    return isPluginEnabled(permissions);
  }

  public static boolean isPermitted(Repository repository) {
    return RepositoryPermissions.custom(PERMISSION_VERB, repository).isPermitted();
  }

  public void checkPermission(Repository repository) {
    RepositoryPermissions.custom(PERMISSION_VERB, repository).check();
  }

  private boolean hasAnyGroupPermission(Set<String> userGroups, String branch, String path, PathWritePermissions permissions, PathWritePermission.Type type) {
    return permissions.getPermissions().stream()
      .filter(pathWritePermission -> matchPath(path, pathWritePermission))
      .filter(pathWritePermission -> matchBranch(branch, pathWritePermission))
      .filter(PathWritePermission::isGroup)
      .filter(pathWritePermission -> userGroups.contains(pathWritePermission.getName()))
      .anyMatch(pathWritePermission -> pathWritePermission.getType().equals(type));
  }

  private boolean hasUserPermission(User user, String branch, String path, PathWritePermissions permissions, PathWritePermission.Type type) {
    return permissions.getPermissions().stream()
      .filter(pathWritePermission -> matchPath(path, pathWritePermission))
      .filter(pathWritePermission -> matchBranch(branch, pathWritePermission))
      .filter(pathWritePermission -> !pathWritePermission.isGroup())
      .filter(pathWritePermission -> user.getName().equals(pathWritePermission.getName()))
      .anyMatch(pathWritePermission -> pathWritePermission.getType().equals(type));
  }

  private boolean matchBranch(String branch, PathWritePermission pathWritePermission) {
    if (pathWritePermission.getBranchScope().equals(PathWritePermission.BranchScope.INCLUDE)) {
      return GlobUtil.matches(pathWritePermission.getBranch(), branch);
    }
    return !GlobUtil.matches(pathWritePermission.getBranch(), branch);
  }

  private boolean matchPath(String path, PathWritePermission pathWritePermission) {
    String pathPattern = pathWritePermission.getPath();
    return GlobUtil.matches(pathPattern, path);
  }

  private ConfigurationStore<PathWritePermissions> getStore(Repository repository) {
    return storeFactory.withType(PathWritePermissions.class).withName(STORE_NAME).forRepository(repository).build();
  }

  private Repository getRepository(String namespace, String name) {
    return repositoryManager.get(new NamespaceAndName(namespace, name));
  }

  public PathWritePermissions getPermissions(String namespace, String name) {
    Repository repository = getRepository(namespace, name);
    checkPermission(repository);
    return getPermissions(repository);
  }

  private PathWritePermissions getPermissions(Repository repository) {
    ConfigurationStore<PathWritePermissions> store = getStore(repository);
    PathWritePermissions permissions = store.get();
    if (permissions == null) {
      permissions = new PathWritePermissions();
      store.set(permissions);
    }
    return permissions;
  }

  public void setPermissions(String namespace, String name, PathWritePermissions permissions) {
    setPermissions(getRepository(namespace, name), permissions);

  }

  public void setPermissions(Repository repository, PathWritePermissions permissions) {
    checkPermission(repository);
    ConfigurationStore<PathWritePermissions> store = getStore(repository);
    store.set(permissions);
  }
}
