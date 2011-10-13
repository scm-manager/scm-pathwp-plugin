/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.pathwp;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pathwp.permissions = [path, username][path, @groupname]
 *
 * @author Sebastian Sdorra
 */
public class PermissionParser
{

  /** the logger for PermissionParser */
  private static final Logger logger =
    LoggerFactory.getLogger(PermissionParser.class);

  /** Field description */
  public static Pattern PATTERN = Pattern.compile("\\[([^,]+),([^,]+)\\]");

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permissionString
   *
   * @return
   */
  public static List<PathWPPermission> getPermissions(String permissionString)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("parse permission string: {}", permissionString);
    }

    List<PathWPPermission> permissions = new ArrayList<PathWPPermission>();

    parsePermissions(permissions, permissionString);

    if (logger.isDebugEnabled())
    {
      logger.debug("found {} permissions", permissions.size());
    }

    return permissions;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permissions
   * @param permissionString
   */
  private static void parsePermissions(List<PathWPPermission> permissions,
          String permissionString)
  {
    Matcher m = PATTERN.matcher(permissionString);

    while (m.find())
    {
      String path = m.group(1).trim();
      boolean group = false;
      String name = m.group(2).trim();

      if (name.startsWith("@"))
      {
        group = true;
        name = name.substring(1);
      }

      PathWPPermission permission = new PathWPPermission(path, name, group);

      if (logger.isTraceEnabled())
      {
        logger.trace("parse permission: {}", permission);
      }

      permissions.add(permission);
    }
  }
}
