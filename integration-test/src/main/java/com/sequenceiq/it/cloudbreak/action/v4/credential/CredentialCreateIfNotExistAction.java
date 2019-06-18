package com.sequenceiq.it.cloudbreak.action.v4.credential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.EnvironmentClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.credential.CredentialTestDto;
import com.sequenceiq.it.cloudbreak.exception.ProxyMethodInvocationException;
import com.sequenceiq.it.cloudbreak.log.Log;

public class CredentialCreateIfNotExistAction implements Action<CredentialTestDto, EnvironmentClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialCreateIfNotExistAction.class);

    @Override
    public CredentialTestDto action(TestContext testContext, CredentialTestDto testDto, EnvironmentClient environmentClient) throws Exception {
        LOGGER.info("Create Credential with name: {}", testDto.getRequest());
        try {
            testDto.setResponse(
                    environmentClient.getEnvironmentClient().credentialV1Endpoint().post(testDto.getRequest())
            );
            Log.logJSON(LOGGER, "Credential created successfully: ", testDto.getRequest());
        } catch (ProxyMethodInvocationException e) {
            LOGGER.info("Cannot create Credential, fetch existed one: {}", testDto.getRequest().getName());
            testDto.setResponse(
                    environmentClient.getEnvironmentClient().credentialV1Endpoint()
                            .getByName(testDto.getRequest().getName()));
        }
        if (testDto.getResponse() == null) {
            throw new IllegalStateException("Credential could not be created.");
        }
        return testDto;
    }
}
