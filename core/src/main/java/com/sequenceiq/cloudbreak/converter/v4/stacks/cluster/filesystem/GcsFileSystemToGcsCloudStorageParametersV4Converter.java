package com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.filesystem;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.common.api.cloudstorage.GcsCloudStorageV1Parameters;
import com.sequenceiq.common.api.filesystem.GcsFileSystem;

@Component
public class GcsFileSystemToGcsCloudStorageParametersV4Converter
        extends AbstractConversionServiceAwareConverter<GcsFileSystem, GcsCloudStorageV1Parameters> {

    @Override
    public GcsCloudStorageV1Parameters convert(GcsFileSystem source) {
        GcsCloudStorageV1Parameters fileSystemConfigurations = new GcsCloudStorageV1Parameters();
        fileSystemConfigurations.setServiceAccountEmail(source.getServiceAccountEmail());
        return fileSystemConfigurations;
    }
}
