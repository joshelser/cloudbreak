package com.sequenceiq.cloudbreak.template.filesystem.gcs;

import java.util.Collection;

import com.sequenceiq.cloudbreak.template.filesystem.BaseFileSystemConfigurationsView;
import com.sequenceiq.cloudbreak.template.filesystem.StorageLocationView;
import com.sequenceiq.common.model.FileSystemType;
import com.sequenceiq.common.api.filesystem.GcsFileSystem;

public class GcsFileSystemConfigurationsView extends BaseFileSystemConfigurationsView {

    private String serviceAccountEmail;

    public GcsFileSystemConfigurationsView(GcsFileSystem gcsFileSystem, Collection<StorageLocationView> locations, boolean deafultFs) {
        super(gcsFileSystem.getStorageContainer(), deafultFs, locations);
        serviceAccountEmail = gcsFileSystem.getServiceAccountEmail();
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }

    @Override
    public String getType() {
        return FileSystemType.GCS.name();
    }
}
