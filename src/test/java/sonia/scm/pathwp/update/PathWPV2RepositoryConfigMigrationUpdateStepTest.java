package sonia.scm.pathwp.update;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.pathwp.service.PathWritePermission;
import sonia.scm.pathwp.service.PathWritePermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.update.V1PropertyDaoTestUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class PathWPV2RepositoryConfigMigrationUpdateStepTest {

  private final static String REPO_NAME = "repo";
  private static final String STORE_NAME = "pathWritePermission";

  private final V1PropertyDaoTestUtil testUtil = new V1PropertyDaoTestUtil();

  private final ConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  private PathWPV2RepositoryConfigMigrationUpdateStep updateStep;

  @Before
  public void init() {
    updateStep = new PathWPV2RepositoryConfigMigrationUpdateStep(testUtil.getPropertyDAO(), storeFactory);
  }

  @Test
  public void shouldMigratingMultiplePermissionsForRepository() {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "pathwp.permissions","[/test/777/,Tony][/test/path/,@Edi][/java/git/,Steve]",
        "pathwp.enabled", "true"
      );

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    PathWritePermission permission1 = new PathWritePermission("/test/777/", "Tony", false, PathWritePermission.Type.ALLOW);
    PathWritePermission permission2 = new PathWritePermission("/test/path/", "Edi", true, PathWritePermission.Type.ALLOW);

    assertThat(getConfigStore().get().getPermissions().get(0).getPath()).isEqualToIgnoringCase(permission1.getPath());
    assertThat(getConfigStore().get().getPermissions().get(0).getName()).isEqualToIgnoringCase(permission1.getName());
    assertThat(getConfigStore().get().getPermissions().get(0).isGroup()).isEqualTo(permission1.isGroup());
    assertThat(getConfigStore().get().getPermissions().get(0).getType()).isEqualTo(permission1.getType());
    assertThat(getConfigStore().get().getPermissions().get(1).getPath()).isEqualToIgnoringCase(permission2.getPath());
    assertThat(getConfigStore().get().getPermissions().get(1).getName()).isEqualToIgnoringCase(permission2.getName());
    assertThat(getConfigStore().get().getPermissions().get(1).isGroup()).isEqualTo(permission2.isGroup());
    assertThat(getConfigStore().get().getPermissions().get(1).getType()).isEqualTo(permission2.getType());
    assertThat(getConfigStore().get().isEnabled()).isTrue();
    assertThat(getConfigStore().get().getPermissions().size()).isEqualTo(3);
  }

  @Test
  public void shouldSkipRepositoriesIfPermissionsAreEmpty() {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "pathwp.permissions", ""
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    assertThat(getConfigStore().get()).isNull();
  }

  private ConfigurationStore<PathWritePermissions> getConfigStore() {
    return storeFactory.withType(PathWritePermissions.class).withName(STORE_NAME).forRepository(REPO_NAME).build();
  }
}
