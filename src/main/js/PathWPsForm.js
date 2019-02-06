//@flow
import React from "react";
import { translate } from "react-i18next";
import type { PathWPs, PathWP } from "./PathWP";
import { Checkbox } from "@scm-manager/ui-components";
import PathWPTable from "./PathWPTable";
import AddPermissionFormComponent from "./AddPermissionFormComponent";

type Props = {
  initialConfiguration: PathWPs,
  readOnly: boolean,
  onConfigurationChange: (PathWPs, boolean) => void,
  userAutocompleteLink: string,
  groupAutocompleteLink: string,
  // context prop
  t: string => string
};

type State = PathWPs & {};

class PathWPsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { ...props.initialConfiguration };
  }

  isValid() {
    const { permissions } = this.state;
    let valid = true;
    permissions.map(pathWP => {
      valid =
        valid &&
        pathWP.name.trim() !== "" &&
        pathWP.path.trim() !== "" &&
        pathWP.type.trim() !== "";
    });
    return valid;
  }

  updatePathWPs(permissions) {
    this.setState({ permissions }, () =>
      this.props.onConfigurationChange(this.state, this.isValid())
    );
  }

  onDelete = deletedPathWP => {
    const { permissions } = this.state;
    let index = permissions.indexOf(deletedPathWP);
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
    this.setState({ enabled: isEnabled }, () => {
      this.props.onConfigurationChange(this.state, this.isValid());
    });
  };

  renderAddUserFormComponent = () => {
    const { readOnly } = this.props;
    if (this.props.userAutocompleteLink) {
      return (
        <AddPermissionFormComponent
          userAutocompleteLink={this.props.userAutocompleteLink}
          groupAutocompleteLink={this.props.groupAutocompleteLink}
          onAdd={this.userPathPermissionAdded}
          readOnly={readOnly}
        />
      );
    } else return null;
  };

  render() {
    const { enabled } = this.state;
    const { t } = this.props;

    return (
      <>
        <Checkbox
          checked={enabled}
          onChange={this.onChangeEnabled}
          label={t("scm-pathwp-plugin.is-enabled")}
          helpText={t("scm-pathwp-plugin.is-enabled-help-text")}
        />
        {enabled ? (
          <>
            <PathWPTable
              permissions={this.state.permissions}
              onDelete={this.onDelete}
            />
            {this.renderAddUserFormComponent()}
          </>
        ) : null}
      </>
    );
  }
}

export default translate("plugins")(PathWPsForm);
