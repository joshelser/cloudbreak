package com.sequenceiq.cloudbreak.conf;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;
import com.sequenceiq.cloudbreak.auth.caas.CaasClient;
import com.sequenceiq.cloudbreak.auth.uaa.IdentityClient;
import com.sequenceiq.cloudbreak.common.service.token.CachedRemoteTokenService;

@Configuration
public class RemoteTokenConfig {

    @Value("${cb.client.id}")
    private String clientId;

    @Value("${cb.client.secret}")
    private String clientSecret;

    @Inject
    @Named("identityServerUrl")
    private String identityServerUrl;

    @Inject
    private IdentityClient identityClient;

    @Inject
    private GrpcUmsClient umsClient;

    @Inject
    private CaasClient caasClient;

    @Bean
    public ResourceServerTokenServices remoteTokenServices() {
        return new CachedRemoteTokenService(clientId, clientSecret, identityServerUrl, umsClient, caasClient, identityClient);
    }

}
