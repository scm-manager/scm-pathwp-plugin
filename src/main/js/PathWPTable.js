// @flow

import React from "react";
import type { PathWP } from "./PathWP";
import PathWPTableRow from "./PathWPTableRow";
import { translate } from "react-i18next";

type Props = {
  permissions: PathWP[],
  // permissionListChanged: (permissions: PathWP[]) => void
  onDelete: PathWP => void,
  // context prop
  t: string => string
};

class PathWPTable extends React.Component<Props> {
  render() {
    const { t } = this.props;
    const tableRows = this.props.permissions.map(pathWP => {
      return (
        <>
          <PathWPTableRow
            permission={pathWP}
            onDelete={permission => {
              this.props.onDelete(permission);
            }}
          />
        </>
      );
    });
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
        <th>{t("scm-pathwp-plugin.table.path")}</th>
        <th>{t("scm-pathwp-plugin.table.name")}</th>
        <th>{t("scm-pathwp-plugin.table.type")}</th>
        <th>{t("scm-pathwp-plugin.table.group")}</th>
        <th>{t("scm-pathwp-plugin.table.delete")}</th>
        </thead>
        {tableRows}
      </table>
    );
  }
}

export default translate("plugins")( PathWPTable);
