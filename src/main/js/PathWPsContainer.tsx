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

import React from "react";
import { Repository } from "@scm-manager/ui-types";
import { Configuration } from "@scm-manager/ui-components";
import PathWPsForm from "./PathWPsForm";

type Props = {
  repository: Repository;
  link: string;
  indexLinks: object;
};

export default class PathWPsContainer extends React.Component<Props> {
  render() {
    const { link, indexLinks, repository } = this.props;
    const userAutoCompleteLink = indexLinks.autocomplete.find(link => link.name === "users").href;
    const groupsAutoCompleteLink = indexLinks.autocomplete.find(link => link.name === "groups").href;
    return (
      <Configuration
        link={link}
        render={props => (
          <PathWPsForm
            {...props}
            userAutocompleteLink={userAutoCompleteLink}
            groupAutocompleteLink={groupsAutoCompleteLink}
            repository={repository}
          />
        )}
      />
    );
  }
}
