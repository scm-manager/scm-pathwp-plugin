/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.pathwp.api;

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.pathwp.service.PathWritePermission;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.pathwp.service.PathWritePermissions;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathWritePermissionResourceTest {

  public static final String PATH = "dir1/subDir/file1.txt";
  public static final String BRANCH = "main";
  public static final String PERMISSIONS_JSON = "{\"permissions\":" +
    "[{\"path\":\"" + PATH + "\"," +
    "\"branch\":\"main\"," +
    "\"branchScope\":\"INCLUDE\"," +
    "\"name\":\"user_1\"," +
    "\"group\":false," +
    "\"type\":\"ALLOW\"" +
    "}" +
    "]," +
    "\"enabled\":true," +
    "\"_links\":{" +
    "\"self\":{" +
    "\"href\":\"/v2/plugins/pathwp/space/repo\"}," +
    "\"update\":{" +
    "\"href\":\"/v2/plugins/pathwp/space/repo\"}" +
    "}" +
    "}";

  @Mock
  PathWritePermissionService service;

  private final PathWritePermissionMapper mapper = new PathWritePermissionMapperImpl();

  private RestDispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();


  @BeforeEach
  public void init() {
    PathWritePermissionResource resource = new PathWritePermissionResource(service, mapper);
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldGetPathWritePermissions() throws URISyntaxException, UnsupportedEncodingException {
    PathWritePermissions permissions = new PathWritePermissions();
    permissions.setEnabled(true);
    permissions.getPermissions().add(new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, "user_1", false, PathWritePermission.Type.ALLOW));
    when(service.getPermissions("space", "repo")).thenReturn(permissions);

    MockHttpRequest request = MockHttpRequest
      .get("/" + PathWritePermissionResource.PATH + "/space/repo")
      .accept(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);
    assertThat(response.getStatus())
      .isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .isEqualTo(PERMISSIONS_JSON);
  }

  @Test
  void shouldPUTPathWritePermissions() throws URISyntaxException {

    MockHttpRequest request = MockHttpRequest
      .put("/" + PathWritePermissionResource.PATH + "/space/repo")
      .contentType(MediaType.APPLICATION_JSON)
      .content(PERMISSIONS_JSON.getBytes());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus())
      .isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(service).setPermissions(eq("space"), eq("repo"), argThat(pathWritePermissions -> {
      PathWritePermissions permissions = new PathWritePermissions();
      permissions.setEnabled(true);
      permissions.getPermissions().add(new PathWritePermission(PATH, BRANCH, PathWritePermission.BranchScope.INCLUDE, "user_1", false, PathWritePermission.Type.ALLOW));
      assertThat(pathWritePermissions).usingRecursiveComparison().isEqualTo(permissions);
      return true;
    }));
  }

}
