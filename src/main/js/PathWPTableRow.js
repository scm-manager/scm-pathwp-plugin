// @flow

import React from "react";
import type { PathWP } from "./PathWP";

type Props = {
  permission: PathWP,
  onDelete: PathWP => void
};
type State = {};

class PathWPTableRow extends React.Component<Props, State> {
  render() {
    const { permission } = this.props;
    return (
      <tr>
        <td>{permission.path}</td>
        <td>{permission.name}</td>
        <td>{permission.type}</td>
        <td>
          <input
            type="checkbox"
            id="group"
            checked={permission.group}
            readOnly
          />
        </td>
        <td>
          <a
            className="level-item"
            onClick={() => {
              this.props.onDelete(this.props.permission);
            }}
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

export default PathWPTableRow;
