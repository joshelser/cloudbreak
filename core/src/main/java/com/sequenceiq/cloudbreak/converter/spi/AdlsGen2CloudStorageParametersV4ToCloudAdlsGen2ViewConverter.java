package com.sequenceiq.cloudbreak.converter.spi;

import org.springframework.stereotype.Component;

import com.sequenceiq.common.api.cloudstorage.AdlsGen2CloudStorageV1Parameters;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudAdlsGen2View;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;

@Component
public class AdlsGen2CloudStorageParametersV4ToCloudAdlsGen2ViewConverter
        extends AbstractConversionServiceAwareConverter<AdlsGen2CloudStorageV1Parameters, CloudAdlsGen2View> {
    @Override
    public CloudAdlsGen2View convert(AdlsGen2CloudStorageV1Parameters source) {
        CloudAdlsGen2View cloudAdlsGen2View = new CloudAdlsGen2View();
        cloudAdlsGen2View.setAccountKey(source.getAccountKey());
        cloudAdlsGen2View.setAccountName(source.getAccountName());
        cloudAdlsGen2View.setSecure(source.isSecure());
        return cloudAdlsGen2View;
    }
}
