// @flow
import React from "react";
import { Configuration, Subtitle } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { Repository } from "@scm-manager/ui-types";
import PathWPsForm from "./PathWPsForm";

type Props = {
  repository: Repository,
  link: string,
  indexLinks: Object,
  t: string => string
};

class PathWPsContainer extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  render() {
    const { t, link, indexLinks } = this.props;
    const userAutoCompleteLink = indexLinks.autocomplete.find(
      link => link.name === "users"
    ).href;
    const groupsAutoCompleteLink = indexLinks.autocomplete.find(
      link => link.name === "groups"
    ).href;
    return (
      <>
        <Subtitle subtitle={t("scm-pathwp-plugin.form.header")} />
        <br />
        <Configuration
          link={link}
          render={props => (
            <PathWPsForm
              {...props}
              userAutocompleteLink={userAutoCompleteLink}
              groupAutocompleteLink={groupsAutoCompleteLink}
            />
          )}
        />
      </>
    );
  }
}

export default translate("plugins")(PathWPsContainer);
