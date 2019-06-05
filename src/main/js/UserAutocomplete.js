// @flow
import React from "react";
import { translate } from "react-i18next";
import { Autocomplete } from "@scm-manager/ui-components";
import type { SelectValue } from "@scm-manager/ui-types";

type Props = {
  userAutocompleteLink: string,
  valueSelected: SelectValue => void,
  value: string,

  // Context props
  t: string => string
};

class UserAutocomplete extends React.Component<Props> {
  loadUserSuggestions = (inputValue: string) => {
    const url = this.props.userAutocompleteLink;
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

  selectName = (selection: SelectValue) => {
    this.props.valueSelected(selection);
  };

  render() {
    const { t, value } = this.props;
    return (
      <Autocomplete
        label={t("scm-pathwp-plugin.form.userLabel")}
        loadSuggestions={this.loadUserSuggestions}
        helpText={t("scm-pathwp-plugin.form.userHelpText")}
        valueSelected={this.selectName}
        value={value}
      />
    );
  }
}

export default translate("plugins")(UserAutocomplete);
