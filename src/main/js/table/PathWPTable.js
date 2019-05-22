// @flow
import React from "react";
import { translate } from "react-i18next";
import { LabelWithHelpIcon, Notification } from "@scm-manager/ui-components";
import type { PathWP } from "../types/PathWP";
import PathWPRow from "./PathWPRow";

type Props = {
  permissions: PathWP[],
  onDelete: PathWP => void,

  // context prop
  t: string => string
};

class PathWPTable extends React.Component<Props> {
  render() {
    const { permissions, onDelete, t } = this.props;

    if (permissions && permissions[0]) {
      const tableRows = permissions.map(pathWP => {
        return (
          <>
            <PathWPRow
              permission={pathWP}
              onDelete={permission => {
                onDelete(permission);
              }}
            />
          </>
        );
      });

      return (
        <table className="has-background-light table is-hoverable is-fullwidth">
          <thead>
            <tr>
              <th>
                <LabelWithHelpIcon
                  label={t("scm-pathwp-plugin.table.name")}
                  helpText={t("scm-pathwp-plugin.table.nameHelpText")}
                />
              </th>
              <th>{t("scm-pathwp-plugin.table.path")}</th>
              <th>{t("scm-pathwp-plugin.table.permission")}</th>
              <th />
            </tr>
          </thead>
          <tbody>{tableRows}</tbody>
        </table>
      );
    }
    return (
      <Notification type="info">
        {t("scm-pathwp-plugin.noPermissions")}
      </Notification>
    );
  }
}

export default translate("plugins")(PathWPTable);
