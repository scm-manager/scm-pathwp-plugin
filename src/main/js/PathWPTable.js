// @flow

import React from "react";
import type { PathWP } from "./PathWP";
import PathWPTableRow from "./PathWPTableRow";

type Props = {
  permissions: PathWP[],
  // permissionListChanged: (permissions: PathWP[]) => void
  onDelete: PathWP => void
};

class PathWPTable extends React.Component<Props> {
  render() {
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
          <th>Path</th>
          <th>Name</th>
          <th>Type</th>
          <th>Group</th>
          <th>Delete</th>
        </thead>
        {tableRows}
      </table>
    );
  }
}

export default PathWPTable;
