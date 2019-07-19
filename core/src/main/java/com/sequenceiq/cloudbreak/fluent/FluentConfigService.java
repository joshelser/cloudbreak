package com.sequenceiq.cloudbreak.fluent;

import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.StackType;
import com.sequenceiq.cloudbreak.common.exception.CloudbreakServiceException;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.common.api.cloudstorage.WasbCloudStorageV1Parameters;
import com.sequenceiq.common.api.telemetry.model.Logging;
import com.sequenceiq.common.api.telemetry.model.Telemetry;

@Service
public class FluentConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FluentConfigService.class);

    private static final String[] S3_SCHEME_PREFIXES = {"s3://", "s3a://", "s3n://"};

    private static final String[] WASB_SCHEME_PREFIXES = {"wasb://", "wasbs://"};

    private static final String AZURE_BLOB_DOMAIN_SUFFIX = ".blob.core.windows.net";

    private static final String CLUSTER_TYPE_DISTROX = "distrox";

    private static final String CLUSTER_TYPE_SDX = "sdx";

    private static final String CLUSTER_LOG_PREFIX = "cluster-logs";

    private static final String S3_PROVIDER_PREFIX = "s3";

    private static final String WASB_PROVIDER_PREFIX = "wasb";

    private static final String DEFAULT_PROVIDER_PREFIX = "stdout";

    public FluentConfigView createFluentConfigs(Stack stack, Telemetry telemetry) {
        final FluentConfigView.Builder builder = new FluentConfigView.Builder();
        boolean fluentEnabled = false;
        if (telemetry != null && telemetry.getLogging() != null) {
            Logging logging = telemetry.getLogging();
            String clusterName = stack.getCluster().getName();
            String platform = stack.getCloudPlatform();
            String storageLocation = logging.getStorageLocation();

            String clusterType = StackType.DATALAKE.equals(stack.getType()) ? CLUSTER_TYPE_SDX : CLUSTER_TYPE_DISTROX;
            String logFolderName = Paths.get(CLUSTER_LOG_PREFIX, clusterType, clusterName).toString();

            builder.withPlatform(platform)
                    .withOverrideAttributes(
                            logging.getAttributes() != null ? new HashMap<>(logging.getAttributes()) : new HashMap<>()
                    )
                    .withProviderPrefix(DEFAULT_PROVIDER_PREFIX);

            if (logging.getS3() != null) {
                fillS3Configs(builder, storageLocation, clusterType, clusterName, logFolderName);
                LOGGER.debug("Fluent will be configured to use S3 output.");
                fluentEnabled = true;
            } else if (logging.getWasb() != null) {
                fillWasbConfigs(builder, storageLocation, logging.getWasb(), clusterType, clusterName, logFolderName);
                LOGGER.debug("Fluent will be configured to use WASB output.");
                fluentEnabled = true;
            }
        }
        if (!fluentEnabled) {
            LOGGER.debug("Fluent usage is disabled");
        }

        return builder
                .withEnabled(fluentEnabled)
                .build();
    }

    private void fillS3Configs(FluentConfigView.Builder builder, String storageLocation,
            String clusterType, String clusterName, String logFolderName) {
        BucketFolderPrefixPair bucketFolderPrefixPair = generateBucketFolderPrefixPair(storageLocation);
        logFolderName = resolveLogFolder(logFolderName, bucketFolderPrefixPair.folderPrefix, clusterType, clusterName);

        builder.withProviderPrefix(S3_PROVIDER_PREFIX)
                .withS3LogArchiveBucketName(bucketFolderPrefixPair.bucket)
                .withLogFolderName(logFolderName);
    }

    // TODO: add support for Azure MSI
    private void fillWasbConfigs(FluentConfigView.Builder builder, String storageLocation,
            WasbCloudStorageV1Parameters wasbParams, String clusterType, String clusterName, String logFolderName) {
        AzureBlobContainerParts azureBlobContainerParts = generateAzureBlobContainerParts(storageLocation);
        logFolderName = resolveLogFolder(logFolderName, azureBlobContainerParts.folderPrefix, clusterType, clusterName);
        String storageAccount = StringUtils.isNotEmpty(azureBlobContainerParts.account)
                ? azureBlobContainerParts.account : wasbParams.getAccountName();

        builder.withProviderPrefix(WASB_PROVIDER_PREFIX)
                .withAzureStorageAccessKey(wasbParams.getAccountKey())
                .withAzureContainer(azureBlobContainerParts.storageContainer)
                .withAzureStorageAccount(storageAccount)
                .withLogFolderName(logFolderName);
    }

    private String resolveLogFolder(String logFolderName, String folderPrefix, String clusterType, String clusterName) {
        if (StringUtils.isNotEmpty(folderPrefix)) {
            logFolderName = Paths.get(folderPrefix, clusterType, clusterName).toString();
        }
        return logFolderName;
    }

    private BucketFolderPrefixPair generateBucketFolderPrefixPair(String location) {
        if (StringUtils.isNotEmpty(location)) {
            String locationWithoutScheme = getLocationWithoutSchemePrefixes(location, S3_SCHEME_PREFIXES);
            String[] splitted = locationWithoutScheme.split("/", 2);
            String folderPrefix = splitted.length < 2 ? null :  splitted[1];
            return new BucketFolderPrefixPair(splitted[0], folderPrefix);
        }
        throw new CloudbreakServiceException("Storage location parameter is missing for S3");
    }

    private AzureBlobContainerParts generateAzureBlobContainerParts(String location) {
        if (StringUtils.isNotEmpty(location)) {
            String locationWithoutScheme = getLocationWithoutSchemePrefixes(location, WASB_SCHEME_PREFIXES);
            String[] splitted = locationWithoutScheme.split("@");
            String[] storageWithSuffix = splitted[0].split("/", 2);
            String folderPrefix = storageWithSuffix.length < 2 ? null :  "/" + storageWithSuffix[1];
            if (splitted.length < 2) {
                return new AzureBlobContainerParts(storageWithSuffix[0], folderPrefix, null);
            } else {
                String[] splittedByDomain = splitted[1].split(AZURE_BLOB_DOMAIN_SUFFIX);
                String account = splittedByDomain[0];
                if (splittedByDomain.length > 1) {
                    String folderPrefixAfterDomain = splittedByDomain[1];
                    if (StringUtils.isNoneEmpty(folderPrefix, folderPrefixAfterDomain)) {
                        throw new CloudbreakServiceException(String.format("Invalid WASB path: %s", location));
                    }
                    folderPrefix = StringUtils.isNotEmpty(folderPrefixAfterDomain) ? folderPrefixAfterDomain : folderPrefix;
                }
                return new AzureBlobContainerParts(storageWithSuffix[0], folderPrefix, account);
            }
        }
        throw new CloudbreakServiceException("Storage location parameter is missing for WASB");
    }

    private String getLocationWithoutSchemePrefixes(String input, String... schemePrefixes) {
        for (String schemePrefix : schemePrefixes) {
            if (input.startsWith(schemePrefix)) {
                String[] splitted = input.split(schemePrefix);
                if (splitted.length > 1) {
                    return splitted[1];
                }
            }
        }
        return input;
    }

    private static class BucketFolderPrefixPair {

        private final String bucket;

        private final String folderPrefix;

        BucketFolderPrefixPair(String bucket, String folderPrefix) {
            this.bucket = bucket;
            this.folderPrefix = folderPrefix;
        }
    }

    private static class AzureBlobContainerParts {

        private final String storageContainer;

        private final String folderPrefix;

        private final String account;

        AzureBlobContainerParts(String storageContainer, String folderPrefix, String account) {
            this.storageContainer = storageContainer;
            this.folderPrefix = folderPrefix;
            this.account = account;
        }
    }
}
