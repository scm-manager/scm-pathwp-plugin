// @flow
import React from "react";
import {Configuration, Title} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import type {Repository} from "@scm-manager/ui-types";
import PathWPsForm from "./PathWPsForm";

type Props = {
  repository: Repository,
  link: string,
  t: string => string
};

class PathWPsContainer extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
  };

  render() {
    const {t, link} = this.props;
    return (
      <>
        <Title title={t("scm-pathwp-plugin.form.header")} />
        <br/>
        <Configuration link={link} render={props => <PathWPsForm {...props} />}/>
      </>
    );
  };
}

export default translate("plugins")(PathWPsContainer);
