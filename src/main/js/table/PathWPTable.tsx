/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { LabelWithHelpIcon, Notification } from "@scm-manager/ui-components";
import { PathWP } from "../types/PathWP";
import PathWPRow from "./PathWPRow";

type Props = WithTranslation & {
  permissions: PathWP[];
  onDelete: (p: PathWP) => void;
  withBranches: boolean;
};

class PathWPTable extends React.Component<Props> {
  render() {
    const { permissions, onDelete, withBranches, t } = this.props;

    if (permissions && permissions[0]) {
      const tableRows = permissions.map(pathWP => {
        return (
          <>
            <PathWPRow
              permission={pathWP}
              onDelete={permission => {
                onDelete(permission);
              }}
              withBranches={withBranches}
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
              {withBranches ? (
                <>
                  <th>{t("scm-pathwp-plugin.table.branchScope")}</th>
                  <th>{t("scm-pathwp-plugin.table.branch")}</th>
                </>
              ) : null}
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
