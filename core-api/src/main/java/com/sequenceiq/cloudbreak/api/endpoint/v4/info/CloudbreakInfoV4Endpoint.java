package com.sequenceiq.cloudbreak.api.endpoint.v4.info;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sequenceiq.cloudbreak.api.endpoint.v4.info.responses.CloudbreakInfoResponse;
import com.sequenceiq.cloudbreak.doc.ContentType;
import com.sequenceiq.cloudbreak.doc.ControllerDescription;
import com.sequenceiq.cloudbreak.doc.Notes;
import com.sequenceiq.cloudbreak.doc.OperationDescriptions.InfoOpDescription;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/v4/info")
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/v4/info", description = ControllerDescription.INFO_DESCRIPTION, protocols = "http,https")
public interface CloudbreakInfoV4Endpoint {

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = InfoOpDescription.INFO, produces = ContentType.JSON, notes = Notes.INFO_CONFIG_NOTES,
            nickname = "info")
    CloudbreakInfoResponse info();

}
