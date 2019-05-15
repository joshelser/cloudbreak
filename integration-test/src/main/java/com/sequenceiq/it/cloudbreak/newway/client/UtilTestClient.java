package com.sequenceiq.it.cloudbreak.newway.client;

import org.springframework.stereotype.Service;

import com.sequenceiq.it.cloudbreak.newway.action.IntegrationTestAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.CloudStorageMatrixAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.DeploymentPreferencesAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.RepoConfigValidationAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.SecurityRulesAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.StackMatrixAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.SubscriptionAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.TagSpecificationsAction;
import com.sequenceiq.it.cloudbreak.newway.action.v4.util.VersionCheckAction;
import com.sequenceiq.it.cloudbreak.newway.dto.util.CloudStorageMatrixTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.util.DeploymentPreferencesTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.util.RepoConfigValidationTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.util.StackMatrixTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.util.SubscriptionTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.util.TagSpecificationsTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.util.VersionCheckTestDto;
import com.sequenceiq.it.cloudbreak.newway.entity.util.SecurityRulesTestDto;

@Service
public class UtilTestClient {

    public IntegrationTestAction<CloudStorageMatrixTestDto> cloudStorageMatrix() {
        return new CloudStorageMatrixAction();
    }

    public IntegrationTestAction<DeploymentPreferencesTestDto> deploymentPreferencesV4() {
        return new DeploymentPreferencesAction();
    }

    public IntegrationTestAction<RepoConfigValidationTestDto> repoConfigValidationV4() {
        return new RepoConfigValidationAction();
    }

    public IntegrationTestAction<SecurityRulesTestDto> securityRulesV4() {
        return new SecurityRulesAction();
    }

    public IntegrationTestAction<StackMatrixTestDto> stackMatrixV4() {
        return new StackMatrixAction();
    }

    public IntegrationTestAction<SubscriptionTestDto> subscriptionV4() {
        return new SubscriptionAction();
    }

    public IntegrationTestAction<TagSpecificationsTestDto> tagSpecificationsV4() {
        return new TagSpecificationsAction();
    }

    public IntegrationTestAction<VersionCheckTestDto> versionChecker() {
        return new VersionCheckAction();
    }

}
