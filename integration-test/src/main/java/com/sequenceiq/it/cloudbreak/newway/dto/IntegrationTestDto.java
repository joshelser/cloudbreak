package com.sequenceiq.it.cloudbreak.newway.dto;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.context.Orderable;
import com.sequenceiq.it.cloudbreak.newway.context.RunningParameter;
import com.sequenceiq.it.cloudbreak.newway.context.ServiceType;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;

public interface IntegrationTestDto extends Orderable {

    Logger LOGGER = LoggerFactory.getLogger(IntegrationTestDto.class);

    IntegrationTestDto valid();

    String getName();

    default void cleanUp(TestContext context, CloudbreakClient cloudbreakClient) {
        LOGGER.warn("Did not clean up resource ({}): name={}", getClass().getSimpleName(), getName());
    }

    default IntegrationTestDto refresh(TestContext context, CloudbreakClient cloudbreakClient) {
        LOGGER.warn("It is not refreshable resource: {}", getName());
        return this;
    }

    default IntegrationTestDto wait(Map<String, Status> desiredStatus, RunningParameter runningParameter) {
        LOGGER.warn("Did not wait: {}", getName());
        return this;
    }

    ServiceType type();

}
