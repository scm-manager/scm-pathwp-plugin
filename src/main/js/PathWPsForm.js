//@flow
import React from "react";
import {translate} from "react-i18next";
import type {PathWPs} from "./PathWP";
import {Button, Checkbox} from "@scm-manager/ui-components";
import PathWPComponent from "./PathWPComponent";


type Props = {
  initialConfiguration: PathWPs,
  readOnly: boolean,
  onConfigurationChange: (PathWPs, boolean) => void,
  // context prop
  t: (string) => string
};

type State = PathWPs & {};

class PathWPsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {...props.initialConfiguration};
  };

  isValid() {
    const {permissions} = this.state;
    let valid = true;
    permissions.map((pathWP) => {
      valid = valid && pathWP.name.trim() != "" && pathWP.path.trim() != "" && pathWP.type.trim() != "";
    });
    return valid;
  }

  updatePathWPs(permissions) {
    this.setState({permissions}, () => this.props.onConfigurationChange(this.state, this.isValid()));
  }

  onDelete = (deletedPathWP) => {
    const {permissions} = this.state;
    let index = permissions.indexOf(deletedPathWP);
    permissions.splice(index, 1);
    this.updatePathWPs(permissions);
  };

  onChange = (changedPathWP, index) => {
    const {permissions} = this.state;
    permissions[index] = changedPathWP;
    this.updatePathWPs(permissions);
  };

  onChangeEnabled = (isEnabled) => {
    this.setState({enabled: isEnabled}
    , () => this.props.onConfigurationChange(this.state, this.isValid()))
  };

  render() {
    const {permissions, enabled} = this.state;
    const {t, readOnly} = this.props;
    let defaultUserPathWP = {
      path: "",
      name: "",
      group: false,
      type: "ALLOW"
    };
    let defaultGroupPathWP = {
      path: "",
      name: "",
      group: true,
      type: "ALLOW"
    };

    const buttons = (<article className="media">
      <Button disabled={readOnly}
              label={t("scm-pathwp-plugin.add-user-permission")}
              action={() => {
                permissions.push(defaultUserPathWP);
                this.updatePathWPs(permissions);
              }
              }/>
      <Button disabled={readOnly}
              label={t("scm-pathwp-plugin.add-group-permission")}
              action={() => {
                permissions.push(defaultGroupPathWP);
                this.updatePathWPs(permissions);
              }
              }/>
    </article>);


    const form = permissions.map((pathWP, index) => {
      return <PathWPComponent
        pathWP={pathWP}
        readOnly={readOnly}
        onDelete={this.onDelete}
        onChange={(changedPathWP) => this.onChange(changedPathWP, index)}
      />
    }) ;


    return (
      <>
        <Checkbox checked={enabled} onChange={this.onChangeEnabled}
                  label={t("scm-pathwp-plugin.is-enabled")}
                  helpText={t("scm-pathwp-plugin.is-enabled-help-text")}/>
        {enabled ? (form): ("")}
        {enabled ? (buttons): ("")}
      </>
    );
  }
}

export default translate("plugins")(PathWPsForm);
