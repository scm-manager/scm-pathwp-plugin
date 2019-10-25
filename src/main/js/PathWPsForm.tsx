import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Checkbox, Subtitle } from "@scm-manager/ui-components";
import { PathWPs, PathWP } from "./types/PathWP";
import PathWPTable from "./table/PathWPTable";
import AddPermissionFormComponent from "./AddPermissionFormComponent";

type Props = WithTranslation & {
  initialConfiguration: PathWPs;
  readOnly: boolean;
  onConfigurationChange: (p1: PathWPs, p2: boolean) => void;
  userAutocompleteLink: string;
  groupAutocompleteLink: string;
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
          label={t("scm-pathwp-plugin.enable")}
          helpText={t("scm-pathwp-plugin.enableHelpText")}
        />
        {enabled ? (
          <>
            <hr />
            <Subtitle subtitle={t("scm-pathwp-plugin.editSubtitle")} />
            <PathWPTable permissions={this.state.permissions} onDelete={this.onDelete} />
            {this.renderAddUserFormComponent()}
          </>
        ) : null}
      </>
    );
  }
}

export default withTranslation("plugins")(PathWPsForm);
