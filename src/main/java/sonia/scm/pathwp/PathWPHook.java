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

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PermissionUtil;
import sonia.scm.repository.PreReceiveRepositoryHook;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.GlobUtil;
import sonia.scm.util.SecurityUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class PathWPHook extends PreReceiveRepositoryHook
{

  /** Field description */
  public static final String PROPERTY_ENABLE = "pathwp.enabled";

  /** Field description */
  public static final String PROPERTY_PERMISSIONS = "pathwp.permissions";

  /** the logger for PathWPHook */
  private static final Logger logger =
    LoggerFactory.getLogger(PathWPHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   */
  @Inject
  public PathWPHook(Provider<WebSecurityContext> securityContextProvider)
  {
    this.securityContextProvider = securityContextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Override
  public void onEvent(RepositoryHookEvent event)
  {
    if (!SecurityUtil.isAdmin(securityContextProvider)
        || PermissionUtil.hasPermission(event.getRepository(),
          securityContextProvider, PermissionType.OWNER))
    {
      handleEvent(event);
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("skip pathwp permissions for admins and owners");
    }
  }

  /**
   * Method description
   *
   *
   * @param total
   * @param append
   */
  private void append(Collection<String> total, Collection<String> append)
  {
    if (append != null)
    {
      total.addAll(append);
    }
  }

  /**
   * Method description
   *
   *
   * @param changesets
   *
   * @return
   */
  private Set<String> createPathSet(Collection<Changeset> changesets)
  {
    Set<String> pathSet = new HashSet<String>();

    if (changesets != null)
    {
      for (Changeset c : changesets)
      {
        Modifications m = c.getModifications();

        if (m != null)
        {
          append(pathSet, m.getAdded());
          append(pathSet, m.getModified());
          append(pathSet, m.getRemoved());
        }
      }
    }

    return pathSet;
  }

  /**
   * Method description
   *
   *
   * @param event
   */
  private void handleEvent(RepositoryHookEvent event)
  {
    Repository repository = event.getRepository();

    if (isEnabled(repository))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("path write protection is enabled");
      }

      String permissionString = repository.getProperty(PROPERTY_PERMISSIONS);

      if (Util.isNotEmpty(permissionString))
      {
        List<PathWPPermission> permissions =
          PermissionParser.getPermissions(permissionString);

        handlePermissions(permissions, event.getChangesets());
      }
      else
      {
        if (logger.isWarnEnabled())
        {
          logger.debug("no pathwp permissions to handle, access denied");
        }

        throw new ScmSecurityException("not enough permissions");
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("path write protection is disabled");
    }
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param path
   */
  private void handlePermission(List<PathWPPermission> permissions, String path)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("handle pathwp permission for {}", path);
    }

    boolean privileged = false;

    for (PathWPPermission permission : permissions)
    {
      String permPath = permission.getPath();

      if (isMatching(permPath, path))
      {
        if (isPrivileged(permission))
        {
          privileged = true;

          break;
        }
      }
      else if (logger.isTraceEnabled())
      {
        logger.trace("permission {} does not match for {}", permission, path);
      }
    }

    if (!privileged)
    {
      throw new SecurityException("not enough permissions");
    }
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param changesets
   */
  private void handlePermissions(List<PathWPPermission> permissions,
                                 Collection<Changeset> changesets)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("handle permissions");
    }

    Set<String> pathSet = createPathSet(changesets);

    if (logger.isDebugEnabled())
    {
      logger.debug("handle permission for {} paths", pathSet.size());
    }

    for (String path : pathSet)
    {
      handlePermission(permissions, path);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private boolean isEnabled(Repository repository)
  {
    return Boolean.valueOf(repository.getProperty(PROPERTY_ENABLE));
  }

  /**
   * TODO: handle patterns
   *
   *
   * @param permPath
   * @param path
   *
   * @return
   */
  private boolean isMatching(String permPath, String path)
  {
    return GlobUtil.matches(permPath, path);
  }

  /**
   * Method description
   *
   *
   * @param permission
   *
   * @return
   */
  private boolean isPrivileged(PathWPPermission permission)
  {
    boolean privileged = false;
    WebSecurityContext context = securityContextProvider.get();

    if (permission.isGroup())
    {
      privileged = context.getGroups().contains(permission.getName());
    }
    else
    {
      privileged = permission.getName().equals(context.getUser().getName());
    }

    return privileged;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
