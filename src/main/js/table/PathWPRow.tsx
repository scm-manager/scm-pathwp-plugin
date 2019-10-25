import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { confirmAlert, Icon } from "@scm-manager/ui-components";
import { PathWP } from "../types/PathWP";

type Props = WithTranslation & {
  permission: PathWP;
  onDelete: (p: PathWP) => void;
};

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

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
    const { permission, t } = this.props;

    const iconType =
      permission && permission.group ? (
        <Icon title={t("scm-pathwp-plugin.table.group")} name="user-friends" />
      ) : (
        <Icon title={t("scm-pathwp-plugin.table.user")} name="user" />
      );

    return (
      <tr>
        <VCenteredTd>
          {iconType} {permission.name}
        </VCenteredTd>
        <td>{permission.path}</td>
        <td>{permission.type}</td>
        <VCenteredTd className="is-darker">
          <a className="level-item" onClick={this.confirmDelete} title={t("scm-pathwp-plugin.table.delete")}>
            <span className="icon is-small">
              <Icon name="trash" color="inherit" />
            </span>
          </a>
        </VCenteredTd>
      </tr>
    );
  }
}

export default withTranslation("plugins")(PathWPRow);
