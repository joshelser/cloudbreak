package com.sequenceiq.distrox.v1.distrox.converter;

import static com.sequenceiq.cloudbreak.util.NullUtil.getIfNotNull;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.StackType;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.StackV4Request;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.network.NetworkV4Request;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.tags.TagsV4Request;
import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.exception.BadRequestException;
import com.sequenceiq.cloudbreak.service.environment.EnvironmentClientService;
import com.sequenceiq.cloudbreak.service.network.DefaultNetworkRequiredService;
import com.sequenceiq.cloudbreak.service.workspace.WorkspaceService;
import com.sequenceiq.distrox.api.v1.distrox.model.DistroXV1Request;
import com.sequenceiq.distrox.api.v1.distrox.model.network.NetworkV1Request;
import com.sequenceiq.distrox.api.v1.distrox.model.tags.TagsV1Request;
import com.sequenceiq.environment.api.v1.environment.model.response.DetailedEnvironmentResponse;
import com.sequenceiq.environment.api.v1.environment.model.response.EnvironmentStatus;

@Component
public class DistroXV1RequestToStackV4RequestConverter {

    @Inject
    private DistroXEnvironmentV1ToEnvironmentSettingsConverter environmentConverter;

    @Inject
    private DistroXAuthenticationToStaAuthenticationConverter authenticationConverter;

    @Inject
    private DistroXImageToImageSettingsConverter imageConverter;

    @Inject
    private DistroXClusterToClusterConverter clusterConverter;

    @Inject
    private InstanceGroupV1ToInstanceGroupV4Converter instanceGroupConverter;

    @Inject
    private NetworkV1ToNetworkV4Converter networkConverter;

    @Inject
    private DistroXParameterConverter stackParameterConverter;

    @Inject
    private EnvironmentClientService environmentClientService;

    @Inject
    private WorkspaceService workspaceService;

    @Inject
    private DefaultNetworkRequiredService defaultNetworkRequiredService;

    @Inject
    private SdxConverter sdxConverter;

    public StackV4Request convert(DistroXV1Request source) {
        DetailedEnvironmentResponse environment = environmentClientService.getByName(source.getEnvironmentName());
        if (environment.getEnvironmentStatus() != EnvironmentStatus.AVAILABLE) {
            throw new BadRequestException(String.format("Environment state is %s instead of AVAILABLE", environment.getEnvironmentStatus()));
        }
        StackV4Request request = new StackV4Request();
        request.setName(source.getName());
        request.setType(StackType.WORKLOAD);
        request.setCloudPlatform(getCloudPlatform(environment));
        request.setEnvironmentCrn(environment.getCrn());
        request.setAuthentication(getIfNotNull(environment.getAuthentication(), authenticationConverter::convert));
        request.setImage(getIfNotNull(source.getImage(), imageConverter::convert));
        request.setCluster(getIfNotNull(source.getCluster(), clusterConverter::convert));
        request.setInstanceGroups(getIfNotNull(source.getInstanceGroups(), igs -> instanceGroupConverter.convertTo(igs, environment)));
        request.setNetwork(getNetwork(source.getNetwork(), environment));
        request.setAws(getIfNotNull(source.getAws(), stackParameterConverter::convert));
        request.setAzure(getIfNotNull(source.getAzure(), stackParameterConverter::convert));
        request.setYarn(getIfNotNull(source.getYarn(), stackParameterConverter::convert));
        request.setInputs(source.getInputs());
        request.setTags(getIfNotNull(source.getTags(), this::getTags));
        request.setSharedService(sdxConverter.getSharedService(source.getSdx(), environment.getCrn()));
        request.setCustomDomain(null);
        request.setTimeToLive(source.getTimeToLive());
        return request;
    }

    public StackV4Request convertAsTemplate(DistroXV1Request source) {
        StackV4Request request = new StackV4Request();
        DetailedEnvironmentResponse environment = null;
        if (source.getEnvironmentName() != null) {
            environment = environmentClientService.getByName(source.getEnvironmentName());
            if (environment.getEnvironmentStatus() != EnvironmentStatus.AVAILABLE) {
                throw new BadRequestException(String.format("Environment state is %s instead of AVAILABLE", environment.getEnvironmentStatus()));
            }
            request.setCloudPlatform(getCloudPlatform(environment));
            request.setEnvironmentCrn(environment.getCrn());
        }
        request.setName(source.getName());
        request.setType(StackType.WORKLOAD);
        request.setImage(getIfNotNull(source.getImage(), imageConverter::convert));
        request.setCluster(getIfNotNull(source.getCluster(), clusterConverter::convert));
        DetailedEnvironmentResponse environmentRef = environment;
        request.setInstanceGroups(getIfNotNull(source.getInstanceGroups(), instanceGroups -> instanceGroupConverter.convertTo(instanceGroups, environmentRef)));
        if (environment != null) {
            request.setNetwork(getNetwork(source.getNetwork(), environment));
        }
        request.setAws(getIfNotNull(source.getAws(), stackParameterConverter::convert));
        request.setAzure(getIfNotNull(source.getAzure(), stackParameterConverter::convert));
        request.setYarn(getIfNotNull(source.getYarn(), stackParameterConverter::convert));
        request.setInputs(source.getInputs());
        request.setTags(getIfNotNull(source.getTags(), this::getTags));
        request.setSharedService(getIfNotNull(source.getSdx(), sdxConverter::getSharedService));
        request.setTimeToLive(source.getTimeToLive());
        return request;
    }

    private NetworkV4Request getNetwork(NetworkV1Request networkRequest, DetailedEnvironmentResponse environment) {
        NetworkV4Request network = getIfNotNull(networkRequest, networkConverter::convert);
        if (defaultNetworkRequiredService.shouldAddNetwork(environment.getCloudPlatform(), network)) {
            network = getIfNotNull(environment.getNetwork(), networkConverter::convert);
        }
        validateSubnetIds(network, environment);
        return network;
    }

    private void validateSubnetIds(NetworkV4Request network, DetailedEnvironmentResponse environment) {
        switch (environment.getCloudPlatform()) {
            case "AWS":
                validateSubnet(network, environment, network.getAws().getSubnetId());
                validateVpc(environment, environment.getNetwork().getAws().getVpcId(), network.getAws().getVpcId());
                break;
            case "AZURE":
                validateSubnet(network, environment, network.getAzure().getSubnetId());
                validateVpc(environment, environment.getNetwork().getAzure().getNetworkId(), network.getAzure().getNetworkId());
                break;
            default:
        }

    }

    private void validateVpc(DetailedEnvironmentResponse environment, String environmentVpcId, String vpcId) {
        if (!vpcId.equals(environmentVpcId)) {
            throw new BadRequestException(String.format("The given vpc id (%s) is not match with the Environment vpc id (%s)",
                    vpcId, environmentVpcId));
        }
    }

    private void validateSubnet(NetworkV4Request network, DetailedEnvironmentResponse environment, String subnetId) {
        if (!environment.getNetwork().getSubnetIds().contains(subnetId)) {
            throw new BadRequestException(String.format("The given subnet id (%s) is not attached to the Environment (%s)",
                    network.getAws().getSubnetId(), environment.getName()));
        }
    }

    public DistroXV1Request convert(StackV4Request source) {
        DistroXV1Request request = new DistroXV1Request();
        request.setName(source.getName());
        request.setEnvironmentName(getIfNotNull(source.getEnvironmentCrn(), crn -> environmentClientService.getByCrn(crn).getName()));
        request.setImage(getIfNotNull(source.getImage(), imageConverter::convert));
        request.setCluster(getIfNotNull(source.getCluster(), clusterConverter::convert));
        request.setInstanceGroups(getIfNotNull(source.getInstanceGroups(), instanceGroupConverter::convertFrom));
        request.setNetwork(getIfNotNull(source.getNetwork(), networkConverter::convert));
        request.setAws(getIfNotNull(source.getAws(), stackParameterConverter::convert));
        request.setAzure(getIfNotNull(source.getAzure(), stackParameterConverter::convert));
        request.setYarn(getIfNotNull(source.getYarn(), stackParameterConverter::convert));
        request.setInputs(source.getInputs());
        request.setTags(getIfNotNull(source.getTags(), this::getTags));
        request.setSdx(getIfNotNull(source.getSharedService(), sdxConverter::getSdx));
        return request;
    }

    private CloudPlatform getCloudPlatform(DetailedEnvironmentResponse environment) {
        return CloudPlatform.valueOf(environment.getCloudPlatform());
    }

    private TagsV4Request getTags(TagsV1Request source) {
        TagsV4Request response = new TagsV4Request();
        response.setApplication(source.getApplication());
        response.setUserDefined(source.getUserDefined());
        response.setDefaults(source.getDefaults());
        return response;
    }

    private TagsV1Request getTags(TagsV4Request source) {
        TagsV1Request response = new TagsV1Request();
        response.setApplication(source.getApplication());
        response.setUserDefined(source.getUserDefined());
        response.setDefaults(source.getDefaults());
        return response;
    }
}
