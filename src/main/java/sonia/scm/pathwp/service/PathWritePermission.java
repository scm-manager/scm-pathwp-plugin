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

package sonia.scm.pathwp.service;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


/**
 * Write Permission for Pathes.
 *
 * @author Sebastian Sdorra
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PathWritePermission implements Serializable {

  private String path;
  private String branch;
  private BranchScope branchScope;
  private String name;
  private boolean group;
  private Type type;

  public String getBranch() {
    return branch == null? "*": branch;
  }

  public sonia.scm.pathwp.service.PathWritePermission.BranchScope getBranchScope() {
    return branchScope == null? BranchScope.INCLUDE: branchScope;
  }

  public enum Type {
    ALLOW, DENY
  }

  public enum BranchScope {
    INCLUDE, EXCLUDE
  }

}
