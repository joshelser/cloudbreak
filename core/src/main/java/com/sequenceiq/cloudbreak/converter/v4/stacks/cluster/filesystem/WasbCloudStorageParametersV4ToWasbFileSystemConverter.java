package com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.filesystem;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.common.api.cloudstorage.WasbCloudStorageV1Parameters;
import com.sequenceiq.common.api.filesystem.WasbFileSystem;

@Component
public class WasbCloudStorageParametersV4ToWasbFileSystemConverter
        extends AbstractConversionServiceAwareConverter<WasbCloudStorageV1Parameters, WasbFileSystem> {

    @Override
    public WasbFileSystem convert(WasbCloudStorageV1Parameters source) {
        WasbFileSystem fileSystemConfigurations = new WasbFileSystem();
        fileSystemConfigurations.setSecure(source.isSecure());
        fileSystemConfigurations.setAccountName(source.getAccountName());
        fileSystemConfigurations.setAccountKey(source.getAccountKey());
        return fileSystemConfigurations;
    }
}
