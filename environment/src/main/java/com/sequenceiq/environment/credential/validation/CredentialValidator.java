package com.sequenceiq.environment.credential.validation;

import static com.sequenceiq.environment.CloudPlatform.AZURE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.BadRequestException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.util.ValidationResult;
import com.sequenceiq.cloudbreak.util.ValidationResult.ValidationResultBuilder;
import com.sequenceiq.environment.credential.domain.Credential;
import com.sequenceiq.environment.credential.validation.definition.CredentialDefinitionService;

@Component
public class CredentialValidator {

    private final Set<String> enabledPlatforms;

    private final CredentialDefinitionService credentialDefinitionService;

    private final Map<String, ProviderCredentialValidator> providerValidators = new HashMap<>();

    private final EntitlementService entitlementService;

    public CredentialValidator(@Value("${environment.enabledplatforms}") Set<String> enabledPlatforms,
            CredentialDefinitionService credentialDefinitionService,
            List<ProviderCredentialValidator> providerCredentialValidators,
            EntitlementService entitlementService) {
        this.enabledPlatforms = enabledPlatforms;
        this.credentialDefinitionService = credentialDefinitionService;
        providerCredentialValidators.forEach(validator -> providerValidators.put(validator.supportedProvider(), validator));
        this.entitlementService = entitlementService;
    }

    public void validateParameters(Platform platform, Json json) {
        credentialDefinitionService.checkPropertiesRemoveSensitives(platform, json);
    }

    public void validateCredentialCloudPlatform(String cloudPlatform, String userCrn) {
        if (!enabledPlatforms.contains(cloudPlatform)) {
            throw new BadRequestException(String.format("There is no such cloud platform as '%s'", cloudPlatform));
        }
        if (AZURE.name().equalsIgnoreCase(cloudPlatform) && !entitlementService.azureEnabled(userCrn)) {
            throw new BadRequestException("Provisioning in Microsoft Azure is not enabled for this account.");
        }
    }

    public ValidationResult validateCredentialUpdate(Credential original, Credential newCred) {
        ValidationResultBuilder resultBuilder = new ValidationResultBuilder();
        if (!original.getCloudPlatform().equals(newCred.getCloudPlatform())) {
            resultBuilder.error(String.format("CloudPlatform of the credential cannot be changed! Original: '%s' New: '%s'.",
                    original.getCloudPlatform(), newCred.getCloudPlatform()));
            return resultBuilder.build();
        }
        return Optional.ofNullable(providerValidators.get(newCred.getCloudPlatform()))
                .map(validator -> validator.validateUpdate(original, newCred, resultBuilder))
                .orElse(resultBuilder.build());
    }

}
