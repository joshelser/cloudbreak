package com.sequenceiq.freeipa.api.v1.freeipa.dns;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.service.api.doc.ModelDescriptions;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("AddDnsZoneForSubnetIdsV1Request")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDnsZoneForSubnetIdsRequest {
    @NotNull
    @ApiModelProperty(value = ModelDescriptions.ENVIRONMENT_CRN, required = true)
    private String environmentCrn;

    @NotNull
    private AddDnsZoneNetwork addDnsZoneNetwork;

    public String getEnvironmentCrn() {
        return environmentCrn;
    }

    public void setEnvironmentCrn(String environmentCrn) {
        this.environmentCrn = environmentCrn;
    }

    public AddDnsZoneNetwork getAddDnsZoneNetwork() {
        return addDnsZoneNetwork;
    }

    public void setAddDnsZoneNetwork(AddDnsZoneNetwork addDnsZoneNetwork) {
        this.addDnsZoneNetwork = addDnsZoneNetwork;
    }

    @Override
    public String toString() {
        return "AddDnsZoneForSubnetIdsRequest{"
                + "environmentCrn='" + environmentCrn + '\''
                + ", addDnsZoneNetwork=" + addDnsZoneNetwork
                + '}';
    }
}
