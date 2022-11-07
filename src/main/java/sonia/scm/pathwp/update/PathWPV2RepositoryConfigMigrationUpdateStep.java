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
package sonia.scm.pathwp.update;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.pathwp.service.PathWritePermission;
import sonia.scm.pathwp.service.PathWritePermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.pathwp.service.PathWritePermission.Type;
import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class PathWPV2RepositoryConfigMigrationUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(PathWPV2RepositoryConfigMigrationUpdateStep.class);

  private final V1PropertyDAO v1PropertyDAO;
  private final ConfigurationStoreFactory storeFactory;

  private static final String PATHWP_ENABLED = "pathwp.enabled";
  private static final String PATHWP_PERMISSIONS = "pathwp.permissions";

  private static final String STORE_NAME = "pathWritePermission";

  @Inject
  public PathWPV2RepositoryConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
    this.v1PropertyDAO = v1PropertyDAO;
  }

  @Override
  public void doUpdate() {
    v1PropertyDAO
      .getProperties(REPOSITORY_PROPERTY_READER)
      .havingAnyOf(PATHWP_ENABLED, PATHWP_PERMISSIONS)
      .forEachEntry((key, properties) -> buildConfig(key, properties).ifPresent(configuration ->
        createConfigStore(key).set(configuration)));
  }

  private ConfigurationStore<PathWritePermissions> createConfigStore(String repositoryId) {
    return storeFactory.withType(PathWritePermissions.class).withName(STORE_NAME).forRepository(repositoryId).build();
  }

  private Optional<PathWritePermissions> buildConfig(String repositoryId, V1Properties properties) {
    LOG.debug("migrating repository specific pathwp configuration for repository id {}", repositoryId);

    String v1Permissions = properties.get(PATHWP_PERMISSIONS);
    if (Strings.isNullOrEmpty(v1Permissions) || v1Permissions.equals("false")) {
      return empty();
    }

    String[] splitV1Permissions = v1Permissions.replace("[", "").split("]");

    List<PathWritePermission> mappedPermissions = new ArrayList<>();
    for (String v1Permission : splitV1Permissions) {
      createV2Permission(v1Permission).ifPresent(mappedPermissions::add);
    }

    PathWritePermissions v2Permissions = new PathWritePermissions();
    v2Permissions.setEnabled(Boolean.parseBoolean(properties.get(PATHWP_ENABLED)));
    v2Permissions.setPermissions(mappedPermissions);

    return of(v2Permissions);
  }

  private Optional<PathWritePermission> createV2Permission(String v1Permission) {
    String[] splitV1Permission = v1Permission.split(",");
    if (splitV1Permission.length != 2) {
      return empty();
    }

    String path = splitV1Permission[0];
    String name = splitV1Permission[1].replace("@", "");
    boolean group = splitV1Permission[1].contains("@");

    return of(new PathWritePermission(path, "*", PathWritePermission.BranchScope.INCLUDE, name, group, Type.ALLOW));
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.pathwp.config.repository.xml";
  }
}
