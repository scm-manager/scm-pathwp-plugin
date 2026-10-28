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

import * as React from "react";
import { useState, ChangeEvent } from "react";
import { useTranslation } from "react-i18next";
import { Autocomplete, InputField, Radio } from "@scm-manager/ui-components";
import { PathWP } from "./types/PathWP";
import { useIndexLinks } from "@scm-manager/ui-api";
import { Link } from "@scm-manager/ui-types";
import { Button, Label, Select, Subtitle } from "@scm-manager/ui-core";
import { Option } from "@scm-manager/ui-types/src/Option";
import { AutocompleteObject } from "@scm-manager/ui-types/src/Autocomplete";

type Props = {
  readOnly: boolean;
  onAdd: (p: PathWP) => void;
  withBranches: boolean;
};

const defaultPermission: PathWP = {
  name: "",
  type: "ALLOW",
  path: "",
  branch: "*",
  branchScope: "INCLUDE",
  group: false,
};

const useAutoCompleteLinks = () => {
  const links = useIndexLinks()?.autocomplete as Link[];
  return {
    groups: links?.find((l) => l.name === "groups"),
    users: links?.find((l) => l.name === "users"),
  };
};

const AddPermissionFormComponent: React.FC<Props> = ({ readOnly, onAdd, withBranches }) => {
  const { t } = useTranslation("plugins");

  const [pathProtectionPermission, setPathProtectionPermission] = useState<PathWP>(defaultPermission);
  const [selectedValue, setSelectedValue] = useState<Option<AutocompleteObject> | undefined>(undefined);
  const links = useAutoCompleteLinks();

  const { path, branch, group, type, branchScope } = pathProtectionPermission;

  // Korrektur: Nutzt das native ChangeEvent und extrahiert den string-Wert (.target.value)
  const handleTypeChange = (event: ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    setPathProtectionPermission((prev) => ({ ...prev, type: value }));
  };

  const handleBranchScopeChange = (event: ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    setPathProtectionPermission((prev) => ({ ...prev, branchScope: value }));
  };

  const selectName = (selection: Option<AutocompleteObject> | null) => {
    if (!selection) {
      setPathProtectionPermission((prev) => ({ ...prev, name: "" }));
      setSelectedValue(undefined);
      return;
    }
    setPathProtectionPermission((prev) => ({ ...prev, name: selection.value.id }));
    setSelectedValue(selection);
  };

  const handlePathExpressionChange = (newPath: string) => {
    setPathProtectionPermission((prev) => ({ ...prev, path: newPath }));
  };

  const handleBranchExpressionChange = (newBranch: string) => {
    setPathProtectionPermission((prev) => ({ ...prev, branch: newBranch }));
  };

  const changePermissionScope = (isGroup: boolean) => {
    setPathProtectionPermission((prev) => ({ ...prev, group: isGroup, name: "" }));
    setSelectedValue(undefined);
  };

  const handleAdd = () => {
    onAdd(pathProtectionPermission);
    setPathProtectionPermission((prev) => ({
      ...defaultPermission,
      path: prev.path,
      type: prev.type,
      branch: prev.branch,
      branchScope: prev.branchScope,
      group: prev.group,
    }));
    setSelectedValue(undefined);
  };

  return (
    <>
      <hr />
      <Subtitle subtitle={t("scm-pathwp-plugin.addSubtitle")} />
      <div className="columns is-multiline">
        <div className="column is-full">
          <label className="label">{t("scm-pathwp-plugin.form.permissionType")}</label>
          <div className="field is-grouped">
            <div className="control">
              <Radio
                label={t("scm-pathwp-plugin.form.userPermission")}
                name="permission_scope"
                checked={!group}
                onChange={() => changePermissionScope(false)}
                disabled={readOnly}
              />
              <Radio
                label={t("scm-pathwp-plugin.form.groupPermission")}
                name="permission_scope"
                checked={group}
                onChange={() => changePermissionScope(true)}
                disabled={readOnly}
              />
            </div>
          </div>
        </div>

        <div className="column is-full">
          <InputField
            name="path"
            placeholder={t("scm-pathwp-plugin.form.path")}
            label={t("scm-pathwp-plugin.form.path")}
            helpText={t("scm-pathwp-plugin.form.pathHelpText")}
            value={path}
            onChange={handlePathExpressionChange}
            disabled={readOnly}
          />
        </div>
        {withBranches && (
          <div className="column">
            <div className="columns is-3">
              <div className="column is-align-items-flex-start">
                <Label>{t("scm-pathwp-plugin.form.branchScope")}</Label>
                <Select
                  options={[
                    { label: t("scm-pathwp-plugin.form.include"), value: "INCLUDE" },
                    { label: t("scm-pathwp-plugin.form.exclude"), value: "EXCLUDE" },
                  ]}
                  value={branchScope}
                  onChange={handleBranchScopeChange}
                  disabled={readOnly}
                />
              </div>
              <div className="column is-flex-grow-3">
                <InputField
                  name="branch"
                  placeholder={t("scm-pathwp-plugin.form.branch")}
                  label={t("scm-pathwp-plugin.form.branch")}
                  helpText={t("scm-pathwp-plugin.form.branchHelpText")}
                  value={branch}
                  onChange={handleBranchExpressionChange}
                  disabled={readOnly}
                />
              </div>
            </div>
          </div>
        )}
      </div>
      <div className="columns">
        <div className="column is-align-items-flex-start">
          <Label>{t("scm-pathwp-plugin.form.permission")}</Label>
          <Select
            options={[
              { label: t("scm-pathwp-plugin.form.allow"), value: "ALLOW" },
              { label: t("scm-pathwp-plugin.form.deny"), value: "DENY" },
            ]}
            value={type}
            onChange={handleTypeChange}
            disabled={readOnly}
          />
        </div>
        <div className="column is-flex-grow-3">
          <Autocomplete
            type={group ? "group" : "user"}
            autocompleteLink={group ? links.groups?.href : links.users?.href}
            onSelect={selectName}
          />
        </div>
      </div>
      <div className="is-flex is-justify-content-flex-end">
        <Button
          className="is-flex is-align-self-flex-end"
          disabled={readOnly || !path || !branch || !selectedValue?.label}
          onClick={handleAdd}
        >
          {t("scm-pathwp-plugin.form.add")}
        </Button>
      </div>
    </>
  );
};

export default AddPermissionFormComponent;
