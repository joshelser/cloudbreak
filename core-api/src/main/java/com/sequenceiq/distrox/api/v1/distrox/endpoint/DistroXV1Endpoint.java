package com.sequenceiq.distrox.api.v1.distrox.endpoint;

import static com.sequenceiq.cloudbreak.doc.OperationDescriptions.ClusterOpDescription.SET_MAINTENANCE_MODE;
import static com.sequenceiq.cloudbreak.doc.OperationDescriptions.WorkspaceOpDescription.DELETE_BY_NAME;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.CREATE;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.DELETE_BY_CRN;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.DELETE_INSTANCE_BY_ID;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.DELETE_WITH_KERBEROS;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.GET_BY_CRN;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.GET_BY_NAME;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.GET_STACK_REQUEST;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.GET_STATUS_BY_NAME;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.LIST;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.POST_STACK_FOR_BLUEPRINT;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.REPAIR_CLUSTER;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.RETRY_BY_NAME;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.SCALE_BY_NAME;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.START_BY_NAME;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.STOP_BY_NAME;
import static com.sequenceiq.distrox.api.v1.distrox.doc.DistroXOpDescription.SYNC_BY_NAME;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.GeneratedBlueprintV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.StackStatusV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.StackV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.StackViewV4Responses;
import com.sequenceiq.cloudbreak.doc.Notes;
import com.sequenceiq.distrox.api.v1.distrox.model.DistroXMaintenanceModeV1Request;
import com.sequenceiq.distrox.api.v1.distrox.model.DistroXRepairV1Request;
import com.sequenceiq.distrox.api.v1.distrox.model.DistroXScaleV1Request;
import com.sequenceiq.distrox.api.v1.distrox.model.DistroXV1Request;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/v1/distrox")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/v1/distrox", protocols = "http,https")
public interface DistroXV1Endpoint {

    @GET
    @Path("")
    @ApiOperation(value = LIST, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES,
            nickname = "listDistroXV1")
    StackViewV4Responses list(@QueryParam("environmentName") String environmentName, @QueryParam("environmentCrn") String environmentCrn);

    @POST
    @Path("")
    @ApiOperation(value = CREATE, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "postDistroXV1")
    StackV4Response post(@Valid DistroXV1Request request);

    @GET
    @Path("name/{name}")
    @ApiOperation(value = GET_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "getDistroXV1ByName")
    StackV4Response getByName(@PathParam("name") String name, @QueryParam("entries") Set<String> entries);

    @GET
    @Path("crn/{crn}")
    @ApiOperation(value = GET_BY_CRN, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "getDistroXV1ByCrn")
    StackV4Response getByCrn(@PathParam("crn") String crn, @QueryParam("entries") Set<String> entries);

    @DELETE
    @Path("name/{name}")
    @ApiOperation(value = DELETE_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "deleteDistroXV1ByName")
    void deleteByName(@PathParam("name") String name, @QueryParam("forced") @DefaultValue("false") Boolean forced);

    @DELETE
    @Path("crn/{crn}")
    @ApiOperation(value = DELETE_BY_CRN, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "deleteDistroXByCrnV1")
    void deleteByCrn(@PathParam("crn") String crn, @QueryParam("forced") @DefaultValue("false") Boolean forced);

    @PUT
    @Path("name/{name}/sync")
    @ApiOperation(value = SYNC_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "syncDistroXV1")
    void putSyncByName(@PathParam("name") String name);

    @PUT
    @Path("crn/{crn}/sync")
    @ApiOperation(value = SYNC_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "syncDistroXV1")
    void putSyncByCrn(@PathParam("crn") String crn);

    @PUT
    @Path("name/{name}/retry")
    @ApiOperation(value = RETRY_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.RETRY_STACK_NOTES, nickname = "retryDistroXV1")
    void putRetryByName(@PathParam("name") String name);

    @PUT
    @Path("crn/{crn}/retry")
    @ApiOperation(value = RETRY_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.RETRY_STACK_NOTES, nickname = "retryDistroXV1")
    void putRetryByCrn(@PathParam("crn") String crn);

    @PUT
    @Path("name/{name}/stop")
    @ApiOperation(value = STOP_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "stopDistroXV1")
    void putStopByName(@PathParam("name") String name);

    @PUT
    @Path("crn/{crn}/stop")
    @ApiOperation(value = STOP_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "stopDistroXV1")
    void putStopByCrn(@PathParam("crn") String crn);

    @PUT
    @Path("name/{name}/start")
    @ApiOperation(value = START_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "startDistroXV1")
    void putStartByName(@PathParam("name") String name);

    @PUT
    @Path("crn/{crn}/start")
    @ApiOperation(value = START_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "startDistroXV1")
    void putStartByCrn(@PathParam("crn") String crn);

    @PUT
    @Path("name/{name}/scaling")
    @ApiOperation(value = SCALE_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "putScalingDistroXV1")
    void putScalingByName(@PathParam("name") String name, @Valid DistroXScaleV1Request updateRequest);

    @PUT
    @Path("crn/{crn}/scaling")
    @ApiOperation(value = SCALE_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "putScalingDistroXV1")
    void putScalingByCrn(@PathParam("crn") String crn, @Valid DistroXScaleV1Request updateRequest);

    @POST
    @Path("name/{name}/manual_repair")
    @ApiOperation(value = REPAIR_CLUSTER, produces = MediaType.APPLICATION_JSON, notes = Notes.CLUSTER_REPAIR_NOTES, nickname = "repairDistroXV1")
    void repairClusterByName(@PathParam("name") String name, @Valid DistroXRepairV1Request clusterRepairRequest);

    @POST
    @Path("crn/{name}/manual_repair")
    @ApiOperation(value = REPAIR_CLUSTER, produces = MediaType.APPLICATION_JSON, notes = Notes.CLUSTER_REPAIR_NOTES, nickname = "repairDistroXV1")
    void repairClusterByCrn(@PathParam("crn") String crn, @Valid DistroXRepairV1Request clusterRepairRequest);

    @POST
    @Path("name/{name}/blueprint")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = POST_STACK_FOR_BLUEPRINT, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "postDistroXForBlueprintV1")
    GeneratedBlueprintV4Response postStackForBlueprintByName(@PathParam("name") String name, @Valid DistroXV1Request stackRequest);

    @POST
    @Path("crn/{crn}/blueprint")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = POST_STACK_FOR_BLUEPRINT, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "postDistroXForBlueprintV1")
    GeneratedBlueprintV4Response postStackForBlueprintByCrn(@PathParam("crn") String crn, @Valid DistroXV1Request stackRequest);

    @GET
    @Path("name/{name}/request")
    @ApiOperation(value = GET_STACK_REQUEST, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "getDistroXRequestFromNameV1")
    DistroXV1Request getRequestfromName(@PathParam("name") String name);

    @GET
    @Path("crn/{crn}/request")
    @ApiOperation(value = GET_STACK_REQUEST, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "getDistroXRequestFromNameV1")
    DistroXV1Request getRequestfromCrn(@PathParam("crn") String crn);

    @GET
    @Path("name/{name}/status")
    @ApiOperation(value = GET_STATUS_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "statusDistroXV1")
    StackStatusV4Response getStatusByName(@PathParam("name") String name);

    @GET
    @Path("crn/{crn}/status")
    @ApiOperation(value = GET_STATUS_BY_NAME, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "statusDistroXV1")
    StackStatusV4Response getStatusByCrn(@PathParam("crn") String crn);

    @DELETE
    @Path("name/{name}/instance")
    @ApiOperation(value = DELETE_INSTANCE_BY_ID, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "deleteInstanceDistroXV1")
    void deleteInstanceByName(@PathParam("name") String name,
            @QueryParam("forced") @DefaultValue("false") Boolean forced,
            @QueryParam("instanceId") String instanceId);

    @DELETE
    @Path("crn/{crn}/instance")
    @ApiOperation(value = DELETE_INSTANCE_BY_ID, produces = MediaType.APPLICATION_JSON, notes = Notes.STACK_NOTES, nickname = "deleteInstanceDistroXV1")
    void deleteInstanceByCrn(@PathParam("crn") String crn,
                        @QueryParam("forced") @DefaultValue("false") Boolean forced,
                        @QueryParam("instanceId") String instanceId);

    @PUT
    @Path("name/{name}/maintenance")
    @ApiOperation(value = SET_MAINTENANCE_MODE, produces = MediaType.APPLICATION_JSON, notes = Notes.MAINTENANCE_NOTES, nickname = "setDistroXMaintenanceMode")
    void setClusterMaintenanceModeByName(@PathParam("name") String name,
            @NotNull DistroXMaintenanceModeV1Request maintenanceMode);

    @PUT
    @Path("crn/{crn}/maintenance")
    @ApiOperation(value = SET_MAINTENANCE_MODE, produces = MediaType.APPLICATION_JSON, notes = Notes.MAINTENANCE_NOTES, nickname = "setDistroXMaintenanceMode")
    void setClusterMaintenanceModeByCrn(@PathParam("crn") String crn,
                                   @NotNull DistroXMaintenanceModeV1Request maintenanceMode);

    @DELETE
    @Path("name/{name}/cluster")
    @ApiOperation(value = DELETE_WITH_KERBEROS, produces = MediaType.APPLICATION_JSON, notes = Notes.CLUSTER_NOTES, nickname = "deleteWithKerberosDistroXV1")
    void deleteWithKerberosByName(@PathParam("name") String name);

    @DELETE
    @Path("crn/{crn}/cluster")
    @ApiOperation(value = DELETE_WITH_KERBEROS, produces = MediaType.APPLICATION_JSON, notes = Notes.CLUSTER_NOTES, nickname = "deleteWithKerberosDistroXV1")
    void deleteWithKerberosByCrn(@PathParam("crn") String crn);
}
