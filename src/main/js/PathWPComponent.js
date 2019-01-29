//@flow
import React from "react";
import {connect} from "react-redux";
import {confirmAlert, DropDown, LabelWithHelpIcon, InputField, Autocomplete} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import type {PathWP} from "./PathWP";

type Props = {
  pathWP: PathWP,
  readOnly: boolean,
  onChange: (PathWP) => void,
  onDelete: (PathWP) => void,
  userAutocompleteLink: string,
  groupAutocompleteLink: string,
  // context prop
  t: string => string
};

type State = PathWP;

class PathWPComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = props.pathWP;
  }

  componentWillReceiveProps(nextProps) {
    // update the pathwp in the state if the prop are changed
    // The prop can be modified if pathwps are deleted
    if (nextProps.pathWP !== this.props.pathWP) {
      this.state = nextProps.pathWP;
    }
  }

  handleChange = (value: any, name: string) => {
    this.setState({
      [name]: value
    }, () => this.props.onChange(this.state));
  };


  handleDropDownChange = (selection: string) => {
    this.setState({...this.state, type: selection});
    this.handleChange(selection, "type");
  };

  confirmDelete = () => {
    const {t} = this.props;
    confirmAlert({
      title: t("scm-pathwp-plugin.confirm-delete.title"),
      message: t("scm-pathwp-plugin.confirm-delete.message"),
      buttons: [
        {
          label: t("scm-pathwp-plugin.confirm-delete.submit"),
          onClick: () => this.props.onDelete(this.state)
        },
        {
          label: t("scm-pathwp-plugin.confirm-delete.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  loadSuggestions = (inputValue: string) => {
    const {group} = this.state;
    return this.loadAutocompletion(group? this.props.groupAutocompleteLink:this.props.userAutocompleteLink, inputValue);
  };

  loadAutocompletion(url: string, inputValue: string) {
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

  selectName = (value: SelectValue) => {
    let name = value.value.id;
    this.setState({
      name
    }, () => this.props.onChange(this.state));
  };

  render() {
    const {t, readOnly} = this.props;
    const {path, name, group, type} = this.state;
    const deleteIcon = readOnly ? "" :
      <a className="level-item"
         onClick={this.confirmDelete}
      >
        <span className="icon is-small">
          <i className="fas fa-trash">
          </i>
        </span>
      </a>
    ;

    return (
      <article className="media">
        <div className="media-content">
          <LabelWithHelpIcon
            label={group? t("scm-pathwp-plugin.form.group") : t("scm-pathwp-plugin.form.user")}
            helpText={t("scm-pathwp-plugin.form.permission-help-text")}
          />
            <DropDown
              options={["ALLOW", "DENY"]}
              optionSelected={this.handleDropDownChange}
              preselectedOption={this.state.type}
              disabled={readOnly}
            />
          <Autocomplete
            label={group? t("scm-pathwp-plugin.form.group-name"): t("scm-pathwp-plugin.form.user-name")}
            loadSuggestions={this.loadSuggestions}
            helpText={group? t("scm-pathwp-plugin.form.group-name-help-text" ): t("scm-pathwp-plugin.form.user-name-help-text")}
            valueSelected={this.selectName}
            value={name}
            placeholder={name}
          />
            <InputField
              name={"path"}
              placeholder={t("scm-pathwp-plugin.form.path")}
              label={t("scm-pathwp-plugin.form.path")}
              helpText={t("scm-pathwp-plugin.form.path-help-text")}
              value={path}
              onChange={this.handleChange}
              disabled={readOnly}
            />
        </div>
        <div className="media-right">
          {deleteIcon}
        </div>
      </article>
    );
  };
}

function getUserAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "users"
  );
  if (link) {
    return link.href;
  }
  return "";
}
function getGroupAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "groups"
  );
  if (link) {
    return link.href;
  }
  return "";
}

function getLinkCollection(state: Object, name: string): Link[] {
  if (state.indexResources.links && state.indexResources.links[name]) {
    return state.indexResources.links[name];
  }
  return [];
}

const mapStateToProps = (state) => {
  const userAutocompleteLink = getUserAutoCompleteLink(state);
  const groupAutocompleteLink = getGroupAutoCompleteLink(state);
  return {
    userAutocompleteLink,
    groupAutocompleteLink
  };
};

export default connect(mapStateToProps)(translate("plugins")(PathWPComponent));
