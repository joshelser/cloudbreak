package com.sequenceiq.it.cloudbreak.action.v4.credential;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.EnvironmentClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.credential.CredentialTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;

public class CredentialCreateAction implements Action<CredentialTestDto, EnvironmentClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialCreateAction.class);

    @Override
    public CredentialTestDto action(TestContext testContext, CredentialTestDto testDto, EnvironmentClient environmentClient) throws Exception {
        LOGGER.info(" Credential create request: {}", testDto.getRequest());
        testDto.setResponse(
                environmentClient.getEnvironmentClient()
                        .credentialV1Endpoint()
                        .post(testDto.getRequest()));
        Log.logJSON(LOGGER, " Credential created successfully:\n", testDto.getResponse());
        Log.log(LOGGER, format(" CRN: %s", testDto.getResponse().getCrn()));

        return testDto;
    }
}
