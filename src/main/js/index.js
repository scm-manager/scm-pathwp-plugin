// @flow

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components";
import PathWPsContainer from "./PathWPsContainer";

cfgBinder.bindRepository(
  "/pathwp",
  "scm-pathwp-plugin.nav-link",
  "pathWpConfig",
  PathWPsContainer
);
