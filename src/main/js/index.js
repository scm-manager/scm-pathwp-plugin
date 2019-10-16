// @flow
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import PathWPsContainer from "./PathWPsContainer";

cfgBinder.bindRepositorySetting(
  "/pathwp",
  "scm-pathwp-plugin.navLink",
  "pathWpConfig",
  PathWPsContainer
);
