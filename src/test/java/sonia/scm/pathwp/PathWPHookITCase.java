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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class PathWPHookITCase extends AbstractTestBase
{

  /**
   * Constructs ...
   *
   *
   * @param type
   */
  public PathWPHookITCase(String type)
  {
    super(type);
    System.out.println("====================================================");
    System.out.append("start tests for type ").println(type);
    System.out.println("====================================================");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test(expected = RepositoryClientException.class)
  public void onlyEnabledTest() throws RepositoryClientException, IOException
  {
    setPathWPPermissions("");

    RepositoryClient client = createRepositoryClient();
    File directory = client.getLocalRepository();
    File file = new File(directory, "test.txt");

    addContent(file);
    client.add("test.txt");
    client.commit("added test.txt");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void simpleAllowTest() throws RepositoryClientException, IOException
  {
    StringBuilder perms = new StringBuilder();

    perms.append("[test.txt,").append(user.getName()).append("]");
    setPathWPPermissions(perms.toString());

    RepositoryClient client = createRepositoryClient();
    File directory = client.getLocalRepository();
    File file = new File(directory, "test.txt");

    addContent(file);
    client.add("test.txt");
    client.commit("added test.txt");
  }

  /**
   * Method description
   *
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test(expected = RepositoryClientException.class)
  public void simpleDenyTest()
          throws RepositoryClientException, FileNotFoundException, IOException
  {
    StringBuilder perms = new StringBuilder();

    perms.append("[bla/*,").append(user.getName()).append("]");
    perms.append("[blub/*,").append(user.getName()).append("]");
    setPathWPPermissions(perms.toString());

    RepositoryClient client = createRepositoryClient();
    File directory = client.getLocalRepository();
    File file = new File(directory, "test.txt");

    addContent(file);
    client.add("test.txt");
    client.commit("added test.txt");
  }
}
