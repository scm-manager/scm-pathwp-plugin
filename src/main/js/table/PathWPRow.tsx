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

import React, { FC, useState } from "react";
import styled from "styled-components";
import { PathWP } from "../types/PathWP";
import { IconButton, Icon, Dialog, Button } from "@scm-manager/ui-core"
import { useTranslation } from "react-i18next";

type Props = {
  permission: PathWP;
  onDelete: (p: PathWP) => void;
  withBranches: boolean;
};

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

const PathWPRow: FC<Props> = ({ permission, onDelete, withBranches }) => {
  const [t] = useTranslation("plugins");
  const [isOpen, setIsOpen] = useState(false);

  const confirmDelete = () => {
    setIsOpen(false);
    onDelete(permission);
  }

  const iconType = permission && permission.group ? (
    <Icon>user-friends</Icon>
  ) : (
    <Icon>user</Icon>
  );

  return (
    <tr>
      <VCenteredTd>
        {iconType} {permission.name}
      </VCenteredTd>
      <td>{permission.path}</td>
      {withBranches ? (
        <>
          <td>{t("scm-pathwp-plugin.table." + permission.branchScope)}</td>
          <td>{permission.branch}</td>
        </>
      ) : null}
      <td>{t("scm-pathwp-plugin.table." + permission.type)}</td>
      <VCenteredTd className="is-darker">
        <Dialog
          trigger={
            <IconButton title={t("scm-pathwp-plugin.table.delete")}>
              <Icon>trash</Icon>
            </IconButton>
          }
          title={t("scm-pathwp-plugin.confirmDeleteAlert.title")}
          footer={[
            <Button onClick={confirmDelete}>
              {t("scm-pathwp-plugin.confirmDeleteAlert.submit")}
            </Button>,
            <Button variant="primary" autoFocus onClick={() => setIsOpen(false)}>
              {t("scm-pathwp-plugin.confirmDeleteAlert.cancel")}
            </Button>
          ]}
          open={isOpen}
          onOpenChange={setIsOpen}
        >
          {t("scm-pathwp-plugin.confirmDeleteAlert.message")}
        </Dialog>
      </VCenteredTd>
    </tr>
  );
};

export default PathWPRow;
