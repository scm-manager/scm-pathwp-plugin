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

import * as React from "react";
import { useTranslation } from "react-i18next";
import { Configuration } from "@scm-manager/ui-components";
import { Subtitle, useRepositoryFromParams } from "@scm-manager/ui-core";
import PathWPsForm from "./PathWPsForm";
import {Link, Repository} from "@scm-manager/ui-types";

const PathWPsContainer: React.FC = ({}) => {
  const { t } = useTranslation("plugins");
  const repository = useRepositoryFromParams();

  let link = (repository._links?.["pathWpConfigWithBranches"] as Link)?.href
    ?? (repository._links?.["pathWpConfig"] as Link)?.href;

  return (
    <>
      <Subtitle subtitle={t("scm-pathwp-plugin.subtitle")} />
      <Configuration link={link} render={(props) => <PathWPsForm {...props} repository={repository} />} />
    </>
  );
};

export default PathWPsContainer;
