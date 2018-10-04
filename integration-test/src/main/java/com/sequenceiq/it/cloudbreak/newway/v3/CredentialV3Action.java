package com.sequenceiq.it.cloudbreak.newway.v3;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakTest;
import com.sequenceiq.it.cloudbreak.newway.CredentialEntity;
import com.sequenceiq.it.cloudbreak.newway.Entity;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.log.Log;

public class CredentialV3Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialV3Action.class);

    private CredentialV3Action() {
    }

    public static void post(IntegrationTestContext integrationTestContext, Entity entity) {
        CredentialEntity credentialEntity = (CredentialEntity) entity;
        CloudbreakClient client;
        client = integrationTestContext.getContextParam(CloudbreakClient.CLOUDBREAK_CLIENT,
                CloudbreakClient.class);
        Long workspaceId = integrationTestContext.getContextParam(CloudbreakTest.WORKSPACE_ID, Long.class);
        Log.log(" post "
                .concat(credentialEntity.getName())
                .concat(" private credential. "));
        credentialEntity.setResponse(
                client.getCloudbreakClient()
                        .credentialV3Endpoint()
                        .createInWorkspace(workspaceId, credentialEntity.getRequest()));
    }

    public static void get(IntegrationTestContext integrationTestContext, Entity entity) throws IOException {
        CredentialEntity credentialEntity = (CredentialEntity) entity;
        CloudbreakClient client;
        client = integrationTestContext.getContextParam(CloudbreakClient.CLOUDBREAK_CLIENT,
                CloudbreakClient.class);
        Long workspaceId = integrationTestContext.getContextParam(CloudbreakTest.WORKSPACE_ID, Long.class);
        Log.log(" get "
                .concat(credentialEntity.getName())
                .concat(" private credential. "));
        credentialEntity.setResponse(
                client.getCloudbreakClient()
                        .credentialV3Endpoint()
                        .getByNameInWorkspace(workspaceId, credentialEntity.getName()));
        Log.logJSON(" get credential response: ", credentialEntity.getResponse());
    }

    public static void get(IntegrationTestContext integrationTestContext, Entity entity, CloudbreakClient client) throws IOException {
        CredentialEntity credentialEntity = (CredentialEntity) entity;
        Long workspaceId = integrationTestContext.getContextParam(CloudbreakTest.WORKSPACE_ID, Long.class);
        Log.log(" get "
                .concat(credentialEntity.getName())
                .concat(" private credential. "));
        credentialEntity.setResponse(
                client.getCloudbreakClient()
                        .credentialV3Endpoint()
                        .getByNameInWorkspace(workspaceId, credentialEntity.getName()));
        Log.logJSON(" get credential response: ", credentialEntity.getResponse());
    }

    public static void getAll(IntegrationTestContext integrationTestContext, Entity entity) {
        CredentialEntity credentialEntity = (CredentialEntity) entity;
        CloudbreakClient client;
        client = integrationTestContext.getContextParam(CloudbreakClient.CLOUDBREAK_CLIENT,
                CloudbreakClient.class);
        Long workspaceId = integrationTestContext.getContextParam(CloudbreakTest.WORKSPACE_ID, Long.class);
        Log.log(" get all private credential. ");
        credentialEntity.setResponses(
                client.getCloudbreakClient().credentialV3Endpoint().listByWorkspace(workspaceId));
    }

    public static void delete(IntegrationTestContext integrationTestContext, Entity entity, CloudbreakClient client) {
        CredentialEntity credentialEntity = (CredentialEntity) entity;
        Long workspaceId = integrationTestContext.getContextParam(CloudbreakTest.WORKSPACE_ID, Long.class);
        Log.log(" delete "
                .concat(credentialEntity.getName())
                .concat(" private credential. "));
        client.getCloudbreakClient().credentialV3Endpoint()
                .deleteInWorkspace(workspaceId, credentialEntity.getName());
    }

    public static void safeDelete(IntegrationTestContext integrationTestContext, Entity entity, CloudbreakClient client) {
        try {
            get(integrationTestContext, entity, client);
            delete(integrationTestContext, entity, client);
        } catch (Exception e) {
            Log.log("Could not delete credential, probably already deleted.");
        }
    }

    public static void delete(IntegrationTestContext integrationTestContext, Entity entity) {
        CredentialEntity credentialEntity = (CredentialEntity) entity;
        CloudbreakClient client;
        client = integrationTestContext.getContextParam(CloudbreakClient.CLOUDBREAK_CLIENT,
                CloudbreakClient.class);
        Long workspaceId = integrationTestContext.getContextParam(CloudbreakTest.WORKSPACE_ID, Long.class);
        Log.log(" delete %s private credential. ", credentialEntity.getName());
        client.getCloudbreakClient().credentialV3Endpoint()
                .deleteInWorkspace(workspaceId, credentialEntity.getName());
    }

    public static CredentialEntity deleteV2(TestContext testContext, CredentialEntity entity, CloudbreakClient cloudbreakClient) {
        Log.log(LOGGER, "Delete %s credential. ", entity.getName());
        cloudbreakClient.getCloudbreakClient().credentialV3Endpoint()
                .deleteInWorkspace(cloudbreakClient.getWorkspaceId(), entity.getName());
        return entity;
    }

    public static void createInGiven(IntegrationTestContext integrationTestContext, Entity entity) {
        if (getWithoutException(integrationTestContext, entity)) {
            return;
        }
        if (postWithoutException(integrationTestContext, entity)) {
            return;
        }
        try {
            get(integrationTestContext, entity);
        } catch (IOException iox) {
            throw new RuntimeException(iox);
        }
    }

    public static void createDeleteInGiven(IntegrationTestContext integrationTestContext, Entity entity) {
        try {
            get(integrationTestContext, entity);
        } catch (Exception e) {
            post(integrationTestContext, entity);
            delete(integrationTestContext, entity);
        }
    }

    private static boolean getWithoutException(IntegrationTestContext integrationTestContext, Entity entity) {
        try {
            get(integrationTestContext, entity);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean postWithoutException(IntegrationTestContext integrationTestContext, Entity entity) {
        try {
            post(integrationTestContext, entity);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
