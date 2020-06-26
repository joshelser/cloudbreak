package com.sequenceiq.freeipa.api.v1.freeipa.stack.model.common;

public enum DetailedStackStatus {
    UNKNOWN(Status.UNKNOWN),
    // Provision statuses
    PROVISION_REQUESTED(Status.REQUESTED),
    PROVISION_SETUP(Status.CREATE_IN_PROGRESS),
    IMAGE_SETUP(Status.CREATE_IN_PROGRESS),
    CREATING_INFRASTRUCTURE(Status.CREATE_IN_PROGRESS),
    METADATA_COLLECTION(Status.CREATE_IN_PROGRESS),
    TLS_SETUP(Status.CREATE_IN_PROGRESS),
    REGISTERING_WITH_CLUSTER_PROXY(Status.CREATE_IN_PROGRESS),
    STACK_PROVISIONED(Status.STACK_AVAILABLE),
    PROVISIONED(Status.AVAILABLE),
    PROVISION_FAILED(Status.CREATE_FAILED),
    // Orchestration statuses
    BOOTSTRAPPING_MACHINES(Status.UPDATE_IN_PROGRESS),
    COLLECTING_HOST_METADATA(Status.UPDATE_IN_PROGRESS),
    MOUNTING_DISKS(Status.UPDATE_IN_PROGRESS),
    STARTING_FREEIPA_SERVICES(Status.UPDATE_IN_PROGRESS),
    REGISTER_WITH_CLUSTER_PROXY(Status.UPDATE_IN_PROGRESS),
    UPDATE_CLUSTER_PROXY_REGISTRATION(Status.UPDATE_IN_PROGRESS),
    POSTINSTALL_FREEIPA_CONFIGURATION(Status.UPDATE_IN_PROGRESS),
    // Start statuses
    START_REQUESTED(Status.START_REQUESTED),
    START_IN_PROGRESS(Status.START_IN_PROGRESS),
    STARTED(Status.AVAILABLE),
    START_FAILED(Status.START_FAILED),
    // Stop statuses
    STOP_REQUESTED(Status.STOP_REQUESTED),
    STOP_IN_PROGRESS(Status.STOP_IN_PROGRESS),
    STOPPED(Status.STOPPED),
    STOP_FAILED(Status.STOP_FAILED),
    // Upscale statuses
    UPSCALE_REQUESTED(Status.UPDATE_REQUESTED),
    UPSCALE_IN_PROGRESS(Status.UPDATE_IN_PROGRESS),
    UPSCALE_COMPLETED(Status.AVAILABLE),
    UPSCALE_FAILED(Status.AVAILABLE),
    // Downscale statuses
    DOWNSCALE_REQUESTED(Status.UPDATE_REQUESTED),
    DOWNSCALE_IN_PROGRESS(Status.UPDATE_IN_PROGRESS),
    DOWNSCALE_COMPLETED(Status.AVAILABLE),
    DOWNSCALE_FAILED(Status.AVAILABLE),
    // Repair statuses
    REPAIR_REQUESTED(Status.UPDATE_REQUESTED),
    REPAIR_IN_PROGRESS(Status.UPDATE_IN_PROGRESS),
    REPAIR_COMPLETED(Status.AVAILABLE),
    REPAIR_FAILED(Status.AVAILABLE),
    // Termination statuses
    DEREGISTERING_WITH_CLUSTERPROXY(Status.DELETE_IN_PROGRESS),
    DEREGISTERING_CCM_KEY(Status.DELETE_IN_PROGRESS),
    DELETE_IN_PROGRESS(Status.DELETE_IN_PROGRESS),
    DELETE_COMPLETED(Status.DELETE_COMPLETED),
    DELETE_FAILED(Status.DELETE_FAILED),
    // Rollback statuses
    ROLLING_BACK(Status.UPDATE_IN_PROGRESS),
    // The stack is available
    AVAILABLE(Status.AVAILABLE),
    // Instance removing status
    REMOVE_INSTANCE(Status.UPDATE_IN_PROGRESS),
    // Cluster operation is in progress
    CLUSTER_OPERATION(Status.UPDATE_IN_PROGRESS),
    // Wait for sync
    WAIT_FOR_SYNC(Status.WAIT_FOR_SYNC),
    UNREACHABLE(Status.UNREACHABLE),
    DELETED_ON_PROVIDER_SIDE(Status.DELETED_ON_PROVIDER_SIDE),
    UNHEALTHY(Status.UNHEALTHY);

    private final Status status;

    DetailedStackStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
