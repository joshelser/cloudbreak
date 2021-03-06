package com.sequenceiq.cloudbreak.cm.util;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.cloudera.api.swagger.UsersResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.sequenceiq.cloudbreak.client.HttpClientConfig;
import com.sequenceiq.cloudbreak.cm.client.ClouderaManagerClientFactory;

@Component
public class UsersResourceApiProvider {

    @Inject
    private ClouderaManagerClientFactory clouderaManagerClientFactory;

    public UsersResourceApi getDefaultUsersResourceApi(Integer gatewayPort, HttpClientConfig clientConfig) {
        ApiClient defaultClient = clouderaManagerClientFactory.getDefaultClient(gatewayPort, clientConfig);
        return new UsersResourceApi(defaultClient);
    }

    public UsersResourceApi getResourceApi(Integer gatewayPort, String user, String password, HttpClientConfig clientConfig) {
        ApiClient newClient = clouderaManagerClientFactory.getClient(gatewayPort, user, password, clientConfig);
        return new UsersResourceApi(newClient);
    }

}
