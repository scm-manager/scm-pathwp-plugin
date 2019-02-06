// @flow
import React from "react";
import {
  Autocomplete,
  Button,
  InputField,
  DropDown
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { PathWP } from "./PathWP";
import type { SelectValue } from "@scm-manager/ui-types";
import LabelWithHelpIcon from "@scm-manager/ui-components/src/forms/LabelWithHelpIcon";

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
  selectedValue: SelectValue
};

const defaultState = {
  pathProtectionPermission: {
    name: "",
    type: "ALLOW",
    path: "",
    group: false
  },
  selectedValue: {
    label: "",
    value: { id: "", displayName: "" }
  }
};

class AddPermissionFormComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = defaultState;
  }

  loadUserSuggestions = (inputValue: string) => {
    return this.loadAutocompletion(this.props.userAutocompleteLink, inputValue);
  };

  loadGroupSuggestions = (inputValue: string) => {
    return this.loadAutocompletion(
      this.props.groupAutocompleteLink,
      inputValue
    );
  };

  loadAutocompletion = (url: string, inputValue: string) => {
    const link = url + "?q=";
    return fetch(link + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          const label = element.displayName
            ? `${element.displayName} (${element.id})`
            : element.id;
          return {
            value: element,
            label
          };
        });
      });
  };

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
      }
    });
  };

  render() {
    const { t, readOnly } = this.props;
    const { pathProtectionPermission } = this.state;
    const { path } = pathProtectionPermission;
    return (
      <>
        <h1 className="subtitle">{t("scm-pathwp-plugin.form.add-permission")}</h1>
        <div className="control">
          <LabelWithHelpIcon
            label={t("scm-pathwp-plugin.form.permission-type")}
            helpText={t("scm-pathwp-plugin.form.permission-type-help-text")}
          />
          <label className="radio">
            <input
              type="radio"
              name="permission_scope"
              checked={!this.state.pathProtectionPermission.group}
              value="USER_PERMISSION"
              onChange={this.permissionScopeChanged}
            />
            {t("scm-pathwp-plugin.form.user-permission")}
          </label>
          <label className="radio">
            <input
              type="radio"
              name="permission_scope"
              value="GROUP_PERMISSION"
              checked={this.state.pathProtectionPermission.group}
              onChange={this.permissionScopeChanged}
            />
            {t("scm-pathwp-plugin.form.group-permission")}
          </label>
        </div>
        <InputField
          name={"path"}
          placeholder={t("scm-pathwp-plugin.form.path")}
          label={t("scm-pathwp-plugin.form.path")}
          helpText={t("scm-pathwp-plugin.form.path-help-text")}
          value={path}
          onChange={this.handlePathExpressionChange}
          disabled={readOnly}
        />
        <div className="columns">
          <div className="column is-two-thirds">{this.renderAutocomplete()}</div>

          <div className="column is-one-third">
            <LabelWithHelpIcon
              label={t("scm-pathwp-plugin.form.permission")}
              helpText={t("scm-pathwp-plugin.form.permission-help-text")}
            />
            <DropDown
              options={["ALLOW", "DENY"]}
              optionSelected={this.handleDropDownChange}
              preselectedOption={this.state.pathProtectionPermission.type}
              disabled={readOnly}
            />
          </div>
        </div>
        <Button
          label={t("scm-pathwp-plugin.form.add")}
          disabled={this.props.readOnly}
          action={() => {
            this.props.onAdd(this.state.pathProtectionPermission);
            this.setState({ ...defaultState });
          }}
        />
      </>
    );
  }

  renderAutocomplete = () => {
    const { t } = this.props;
    const group = this.state.pathProtectionPermission.group;
    const label = group
      ? t("scm-pathwp-plugin.form.group-name")
      : t("scm-pathwp-plugin.form.user-name");
    const helpText = group
      ? t("scm-pathwp-plugin.form.group-name-help-text")
      : t("scm-pathwp-plugin.form.user-name-help-text");
    const placeholder = group
      ? t("scm-pathwp-plugin.form.group-name")
      : t("scm-pathwp-plugin.form.user-name");
    const loadSuggestions = group
      ? this.loadGroupSuggestions
      : this.loadUserSuggestions;
    return (
      <Autocomplete
        label={label}
        loadSuggestions={loadSuggestions}
        helpText={helpText}
        valueSelected={this.selectName}
        value={this.state.selectedValue}
        placeholder={placeholder}
      />
    );
  };
}

export default translate("plugins")(AddPermissionFormComponent);
