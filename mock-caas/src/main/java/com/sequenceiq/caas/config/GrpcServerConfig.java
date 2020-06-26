package com.sequenceiq.caas.config;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sequenceiq.caas.grpc.GrpcServer;
import com.sequenceiq.caas.grpc.service.audit.MockAuditLogService;
import com.sequenceiq.caas.grpc.service.auth.MockAuthorizationService;
import com.sequenceiq.caas.grpc.service.auth.MockUserManagementService;

@Configuration
public class GrpcServerConfig {

    @Inject
    private MockUserManagementService mockUserManagementService;

    @Inject
    private MockAuthorizationService mockAuthorizationService;

    @Inject
    private MockAuditLogService mockAuditLogService;

    @Value("${grpc.server.port:8982}")
    private int grpcServerPort;

    @Bean
    public GrpcServer grpcServer() {
        return new GrpcServer(
                grpcServerPort,
                mockUserManagementService.bindService(),
                mockAuthorizationService.bindService(),
                mockAuditLogService.bindService());
    }
}
