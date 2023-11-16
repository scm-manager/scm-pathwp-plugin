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
