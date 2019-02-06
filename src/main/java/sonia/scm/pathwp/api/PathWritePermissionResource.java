package sonia.scm.pathwp.api;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import sonia.scm.pathwp.service.PathWritePermissionService;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path(PathWritePermissionResource.PATH)
public class PathWritePermissionResource {
  public static final String PATH = "v2/plugins/pathwp";

  private PathWritePermissionService service;
  private PathWritePermissionMapper mapper;

  @Inject
  public PathWritePermissionResource(PathWritePermissionService service, PathWritePermissionMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public PathWritePermissionsDto get(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    return mapper.using(uriInfo).map(service.getPermissions(namespace, name), new NamespaceAndName(namespace, name));
  }


  @PUT
  @Path("/{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "no content"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public void put(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name, PathWritePermissionsDto permissions) {
    service.setPermissions(namespace, name, mapper.using(uriInfo).map(permissions));
  }

}
