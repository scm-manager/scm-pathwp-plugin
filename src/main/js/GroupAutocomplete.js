// @flow
import React from "react";
import { translate } from "react-i18next";
import { Autocomplete } from "@scm-manager/ui-components";
import type { SelectValue } from "@scm-manager/ui-types";

type Props = {
  groupAutocompleteLink: string,
  valueSelected: SelectValue => void,
  value: string,

  // Context props
  t: string => string
};

class GroupAutocomplete extends React.Component<Props> {
  loadGroupSuggestions = (inputValue: string) => {
    const url = this.props.groupAutocompleteLink;
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
        label={t("scm-pathwp-plugin.form.groupLabel")}
        loadSuggestions={this.loadGroupSuggestions}
        helpText={t("scm-pathwp-plugin.form.groupHelpText")}
        valueSelected={this.selectName}
        value={value}
      />
    );
  }
}

export default translate("plugins")(GroupAutocomplete);
