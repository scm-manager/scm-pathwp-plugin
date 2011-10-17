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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.ScmState;
import sonia.scm.Type;
import sonia.scm.client.ScmClient;
import sonia.scm.client.ScmClientSession;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;
import sonia.scm.repository.client.RepositoryClientFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractTestBase
{

  /**
   * Constructs ...
   *
   *
   * @param type
   */
  public AbstractTestBase(String type)
  {
    this.type = type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   *  Method description
   *
   *
   *  @return
   */
  @Parameters
  public static Collection<Object[]> createParameters()
  {
    Collection<Object[]> params = new ArrayList<Object[]>();
    ScmClientSession adminSession = createAdminSession();

    try
    {
      ScmState state = adminSession.getState();

      for (Type t : state.getRepositoryTypes())
      {
        params.add(new Object[] { t.getName() });
      }
    }
    finally
    {
      IOUtil.close(adminSession);
    }

    return params;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private static ScmClientSession createAdminSession()
  {
    return ScmClient.createSession("http://localhost:8081/scm", "scmadmin",
                                   "scmadmin");
  }

  /**
   * Method description
   *
   */
  @After
  public void cleanupAfterTest()
  {
    adminSession.getUserHandler().delete(user);
    adminSession.getRepositoryHandler().delete(repository);
    IOUtil.close(adminSession);
  }

  /**
   * Method description
   *
   */
  @Before
  public void prepareForTest()
  {
    adminSession = createAdminSession();
    user = UserTestData.createTrillian();
    user.setPassword("scmittest");
    adminSession.getUserHandler().create(user);
    repository = RepositoryTestData.createHeartOfGold(type);
    repository.setPermissions(
        new ArrayList<Permission>(
            Arrays.asList(
              new Permission(user.getName(), PermissionType.WRITE))));
    adminSession.getRepositoryHandler().create(repository);
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @throws IOException
   */
  protected void addContent(File file) throws IOException
  {
    PrintWriter writer = null;

    try
    {
      writer = new PrintWriter(file);
      writer.println(UUID.randomUUID().toString());
    }
    finally
    {
      IOUtil.close(writer);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws RepositoryClientException
   */
  protected RepositoryClient createRepositoryClient()
          throws RepositoryClientException
  {
    RepositoryClient client = RepositoryClientFactory.createClient(type,
                                tempFolder.getRoot(), repository.getUrl(),
                                user.getName(), "scmittest");

    client.checkout();

    return client;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param perms
   */
  protected void setPathWPPermissions(String perms)
  {
    repository.setProperty(PathWPHook.PROPERTY_ENABLE, Boolean.TRUE.toString());
    repository.setProperty(PathWPHook.PROPERTY_PERMISSIONS, perms.toString());
    adminSession.getRepositoryHandler().modify(repository);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /** Field description */
  protected ScmClientSession adminSession;

  /** Field description */
  protected Repository repository;

  /** Field description */
  protected User user;

  /** Field description */
  private String type;
}
