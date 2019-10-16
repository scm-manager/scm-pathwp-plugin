// @flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { Configuration } from "@scm-manager/ui-components";
import PathWPsForm from "./PathWPsForm";

type Props = {
  repository: Repository,
  link: string,
  indexLinks: Object
};

export default class PathWPsContainer extends React.Component<Props> {
  render() {
    const { link, indexLinks } = this.props;
    const userAutoCompleteLink = indexLinks.autocomplete.find(
      link => link.name === "users"
    ).href;
    const groupsAutoCompleteLink = indexLinks.autocomplete.find(
      link => link.name === "groups"
    ).href;
    return (
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
    );
  }
}
