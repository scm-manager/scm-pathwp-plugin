// @flow
import React from "react";
import { translate } from "react-i18next";
import type { SelectValue } from "@scm-manager/ui-types";
import {
  Button,
  InputField,
  Radio,
  DropDown,
  Subtitle,
  LabelWithHelpIcon,
  GroupAutocomplete,
  UserAutocomplete
} from "@scm-manager/ui-components";
import type { PathWP } from "./types/PathWP";

type Props = {
  userAutocompleteLink: string,
  groupAutocompleteLink: string,
  readOnly: boolean,
  onAdd: PathWP => void,

  // Context props
  t: string => string
};

type State = {
  pathProtectionPermission: PathWP,
  selectedValue?: SelectValue
};

const defaultState = {
  pathProtectionPermission: {
    name: "",
    type: "ALLOW",
    path: "",
    group: false
  },
  selectedValue: undefined
};

class AddPermissionFormComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = defaultState;
  }

  handleDropDownChange = (type: string) => {
    this.setState({
      ...this.state,
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        type
      }
    });
  };

  selectName = (selection: SelectValue) => {
    this.setState({
      ...this.state,
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        name: selection.value.id
      },
      selectedValue: selection
    });
  };

  handlePathExpressionChange = (path: string) => {
    this.setState({
      ...this.state,
      pathProtectionPermission: {
        ...this.state.pathProtectionPermission,
        path
      }
    });
  };

  permissionScopeChanged = event => {
    const group = event.target.value === "GROUP_PERMISSION";
    this.setState({
      ...this.state,
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
    const { path } = pathProtectionPermission;
    return (
      <>
        <hr />
        <Subtitle subtitle={t("scm-pathwp-plugin.addSubtitle")} />
        <div className="columns is-multiline">
          <div className="column is-full">
            <label className="label">
              {t("scm-pathwp-plugin.form.permissionType")}
            </label>
            <div className="field is-grouped">
              <div className="control">
                <Radio
                  label={t("scm-pathwp-plugin.form.userPermission")}
                  name="permission_scope"
                  value="USER_PERMISSION"
                  checked={!this.state.pathProtectionPermission.group}
                  onChange={this.permissionScopeChanged}
                />
                <Radio
                  label={t("scm-pathwp-plugin.form.groupPermission")}
                  name="permission_scope"
                  value="GROUP_PERMISSION"
                  checked={this.state.pathProtectionPermission.group}
                  onChange={this.permissionScopeChanged}
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
                  optionSelected={this.handleDropDownChange}
                  preselectedOption={this.state.pathProtectionPermission.type}
                  disabled={readOnly}
                />
              </div>
              <div className="column">
                <Button
                  label={t("scm-pathwp-plugin.form.add")}
                  disabled={
                    this.props.readOnly ||
                    !path ||
                    !(
                      this.state.selectedValue && this.state.selectedValue.label
                    )
                  }
                  action={() => {
                    this.props.onAdd(this.state.pathProtectionPermission);
                    this.setState({ ...defaultState });
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

export default translate("plugins")(AddPermissionFormComponent);
