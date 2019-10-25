import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { LabelWithHelpIcon, Notification } from "@scm-manager/ui-components";
import { PathWP } from "../types/PathWP";
import PathWPRow from "./PathWPRow";

type Props = WithTranslation & {
  permissions: PathWP[];
  onDelete: (p: PathWP) => void;
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
        <table className="card-table table is-hoverable is-fullwidth">
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
    return <Notification type="info">{t("scm-pathwp-plugin.noPermissions")}</Notification>;
  }
}

export default withTranslation("plugins")(PathWPTable);
