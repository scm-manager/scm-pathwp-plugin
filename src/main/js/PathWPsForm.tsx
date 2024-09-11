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
import { WithTranslation, withTranslation } from "react-i18next";
import { Checkbox, Subtitle } from "@scm-manager/ui-components";
import { PathWP, PathWPs } from "./types/PathWP";
import PathWPTable from "./table/PathWPTable";
import AddPermissionFormComponent from "./AddPermissionFormComponent";
import { Repository } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  initialConfiguration: PathWPs;
  readOnly: boolean;
  onConfigurationChange: (p1: PathWPs, p2: boolean) => void;
  userAutocompleteLink: string;
  groupAutocompleteLink: string;
  repository: Repository;
};

type State = PathWPs & {};

class PathWPsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  isValid() {
    const { permissions } = this.state;
    let valid = true;
    permissions.map(pathWP => {
      valid = valid && pathWP.name.trim() !== "" && pathWP.path.trim() !== "" && pathWP.type.trim() !== "";
    });
    return valid;
  }

  updatePathWPs(permissions) {
    this.setState(
      {
        permissions
      },
      () => this.props.onConfigurationChange(this.state, this.isValid())
    );
  }

  onDelete = deletedPathWP => {
    const { permissions } = this.state;
    const index = permissions.indexOf(deletedPathWP);
    permissions.splice(index, 1);
    this.updatePathWPs(permissions);
  };

  onChange = (changedPathWP, index) => {
    const { permissions } = this.state;
    permissions[index] = changedPathWP;
    this.updatePathWPs(permissions);
  };

  userPathPermissionAdded = (permission: PathWP) => {
    this.setState(
      {
        ...this.state,
        permissions: [...this.state.permissions, permission]
      },
      () => {
        this.props.onConfigurationChange(this.state, this.isValid());
      }
    );
  };

  onChangeEnabled = isEnabled => {
    this.setState(
      {
        enabled: isEnabled
      },
      () => {
        this.props.onConfigurationChange(this.state, this.isValid());
      }
    );
  };

  renderAddUserFormComponent = () => {
    const { readOnly, repository } = this.props;
    if (this.props.userAutocompleteLink) {
      return (
        <AddPermissionFormComponent
          userAutocompleteLink={this.props.userAutocompleteLink}
          groupAutocompleteLink={this.props.groupAutocompleteLink}
          onAdd={this.userPathPermissionAdded}
          readOnly={readOnly}
          withBranches={!!repository._links.pathWpConfigWithBranches}
        />
      );
    } else return null;
  };

  render() {
    const { enabled } = this.state;
    const { t, repository } = this.props;

    const withBranches = !!repository._links.pathWpConfigWithBranches;

    return (
      <>
        <Subtitle subtitle={t("scm-pathwp-plugin.subtitle")} />
        <Checkbox
          checked={enabled}
          onChange={this.onChangeEnabled}
          label={t("scm-pathwp-plugin.enable")}
          helpText={
            withBranches ? t("scm-pathwp-plugin.enableHelpTextWithBranches") : t("scm-pathwp-plugin.enableHelpText")
          }
        />
        {enabled ? (
          <>
            <hr />
            <Subtitle subtitle={t("scm-pathwp-plugin.editSubtitle")} />
            <PathWPTable
              permissions={this.state.permissions}
              onDelete={this.onDelete}
              withBranches={withBranches}
            />
            {this.renderAddUserFormComponent()}
          </>
        ) : null}
      </>
    );
  }
}

export default withTranslation("plugins")(PathWPsForm);
