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
import { SelectValue } from "@scm-manager/ui-types";
import {
  Button,
  DropDown,
  GroupAutocomplete,
  InputField,
  LabelWithHelpIcon,
  Radio,
  Subtitle,
  UserAutocomplete
} from "@scm-manager/ui-components";
import { PathWP } from "./types/PathWP";

type Props = WithTranslation & {
  userAutocompleteLink: string;
  groupAutocompleteLink: string;
  readOnly: boolean;
  onAdd: (p: PathWP) => void;
};

type State = {
  pathProtectionPermission: PathWP;
  selectedValue?: SelectValue;
};

const defaultState = {
  pathProtectionPermission: {
    name: "",
    type: "ALLOW",
    path: "",
    branch: "*",
    branchScope: "INCLUDE",
    group: false
  },
  selectedValue: undefined
};

class AddPermissionFormComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = defaultState;
  }

  handleTypeChange = (type: string) => {
    this.setState({
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        type
      }
    });
  };

  handleBranchScopeChange = (branchScope: string) => {
    this.setState({
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        branchScope
      }
    });
  };

  selectName = (selection: SelectValue) => {
    this.setState({
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        name: selection.value.id
      },
      selectedValue: selection
    });
  };

  handlePathExpressionChange = (path: string) => {
    this.setState({
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        path
      }
    });
  };

  handleBranchExpressionChange = (branch: string) => {
    this.setState({
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        branch
      }
    });
  };

  choosePermissionUserScope = () => {
    this.changePermissionScope(false);
  };

  choosePermissionGroupScope = () => {
    this.changePermissionScope(true);
  };

  changePermissionScope = (group: boolean) => {
    this.setState({
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        group
      },
      selectedValue: undefined
    });
  };

  render() {
    const { t, readOnly } = this.props;
    const { pathProtectionPermission } = this.state;
    const { path, branch } = pathProtectionPermission;
    return (
      <>
        <hr />
        <Subtitle subtitle={t("scm-pathwp-plugin.addSubtitle")} />
        <div className="columns is-multiline">
          <div className="column is-full">
            <label className="label">{t("scm-pathwp-plugin.form.permissionType")}</label>
            <div className="field is-grouped">
              <div className="control">
                <Radio
                  label={t("scm-pathwp-plugin.form.userPermission")}
                  name="permission_scope"
                  checked={!this.state.pathProtectionPermission.group}
                  onChange={this.choosePermissionUserScope}
                />
                <Radio
                  label={t("scm-pathwp-plugin.form.groupPermission")}
                  name="permission_scope"
                  checked={this.state.pathProtectionPermission.group}
                  onChange={this.choosePermissionGroupScope}
                />
              </div>
            </div>
          </div>
          <div className="column is-full">
            <InputField
              name={"path"}
              placeholder={t("scm-pathwp-plugin.form.path")}
              label={t("scm-pathwp-plugin.form.path")}
              helpText={t("scm-pathwp-plugin.form.pathHelpText")}
              value={path}
              onChange={this.handlePathExpressionChange}
              disabled={readOnly}
            />
          </div>
          <div className="column is-half">
            <InputField
              name={"branch"}
              placeholder={t("scm-pathwp-plugin.form.branch")}
              label={t("scm-pathwp-plugin.form.branch")}
              helpText={t("scm-pathwp-plugin.form.branchHelpText")}
              value={branch}
              onChange={this.handleBranchExpressionChange}
              disabled={readOnly}
            />
          </div>
          <div className="column is-half">
            <LabelWithHelpIcon
              label={t("scm-pathwp-plugin.form.branchScope")}
              helpText={t("scm-pathwp-plugin.form.branchScopeHelpText")}
            />
            <DropDown
              options={["INCLUDE", "EXCLUDE"]}
              optionSelected={this.handleBranchScopeChange}
              preselectedOption={this.state.pathProtectionPermission.branchScope}
              disabled={readOnly}
            />
          </div>
          <div className="column">{this.renderAutocomplete()}</div>
          <div className="column">
            <div className="columns">
              <div className="column">
                <LabelWithHelpIcon
                  label={t("scm-pathwp-plugin.form.permission")}
                  helpText={t("scm-pathwp-plugin.form.permissionHelpText")}
                />
                <DropDown
                  options={["ALLOW", "DENY"]}
                  optionSelected={this.handleTypeChange}
                  preselectedOption={this.state.pathProtectionPermission.type}
                  disabled={readOnly}
                />
              </div>
              <div className="column">
                <Button
                  label={t("scm-pathwp-plugin.form.add")}
                  disabled={
                    this.props.readOnly || !path || !(this.state.selectedValue && this.state.selectedValue.label)
                  }
                  action={() => {
                    this.props.onAdd(this.state.pathProtectionPermission);
                    this.setState({
                      ...defaultState,
                      pathProtectionPermission: {
                        ...defaultState.pathProtectionPermission,
                        path: pathProtectionPermission.path,
                        pathType: pathProtectionPermission.pathType,
                        branch: pathProtectionPermission.branch,
                        branchType: pathProtectionPermission.branchType,
                        group: pathProtectionPermission.group
                      }
                    });
                  }}
                  className="label-icon-spacing"
                />
              </div>
            </div>
          </div>
        </div>
      </>
    );
  }

  renderAutocomplete = () => {
    const group = this.state.pathProtectionPermission.group;
    if (group) {
      return (
        <GroupAutocomplete
          autocompleteLink={this.props.groupAutocompleteLink}
          valueSelected={this.selectName}
          value={this.state.selectedValue ? this.state.selectedValue : ""}
        />
      );
    }
    return (
      <UserAutocomplete
        autocompleteLink={this.props.userAutocompleteLink}
        valueSelected={this.selectName}
        value={this.state.selectedValue ? this.state.selectedValue : ""}
      />
    );
  };
}

export default withTranslation("plugins")(AddPermissionFormComponent);
