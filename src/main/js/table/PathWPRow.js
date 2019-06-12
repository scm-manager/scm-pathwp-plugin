// @flow
import React from "react";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import { confirmAlert, Icon } from "@scm-manager/ui-components";
import type { PathWP } from "../types/PathWP";

type Props = {
  permission: PathWP,
  onDelete: PathWP => void,

  // context props
  classes: Object,
  t: string => string
};

const styles = {
  centerMiddle: {
    display: "table-cell",
    verticalAlign: "middle !important"
  }
};

class PathWPRow extends React.Component<Props> {
  confirmDelete = () => {
    const { t, onDelete, permission } = this.props;
    confirmAlert({
      title: t("scm-pathwp-plugin.confirmDeleteAlert.title"),
      message: t("scm-pathwp-plugin.confirmDeleteAlert.message"),
      buttons: [
        {
          label: t("scm-pathwp-plugin.confirmDeleteAlert.submit"),
          onClick: () => onDelete(permission)
        },
        {
          label: t("scm-pathwp-plugin.confirmDeleteAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  render() {
    const { permission, classes, t } = this.props;

    const iconType =
      permission && permission.group ? (
        <Icon title={t("scm-pathwp-plugin.table.group")} name="user-friends" />
      ) : (
        <Icon title={t("scm-pathwp-plugin.table.user")} name="user" />
      );

    return (
      <tr>
        <td className={classes.centerMiddle}>
          {iconType} {permission.name}
        </td>
        <td>{permission.path}</td>
        <td>{permission.type}</td>
        <td>
          <a
            className="level-item"
            onClick={this.confirmDelete}
            title={t("scm-pathwp-plugin.table.delete")}
          >
            <span className="icon is-small">
              <i className="fas fa-trash" />
            </span>
          </a>
        </td>
      </tr>
    );
  }
}

export default translate("plugins")(injectSheet(styles)(PathWPRow));
