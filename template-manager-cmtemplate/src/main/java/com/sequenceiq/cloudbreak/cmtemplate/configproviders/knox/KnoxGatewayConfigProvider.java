package com.sequenceiq.cloudbreak.cmtemplate.configproviders.knox;

import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.ConfigUtils.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.cloudera.api.swagger.model.ApiClusterTemplateRoleConfigGroup;
import com.cloudera.api.swagger.model.ApiClusterTemplateService;
import com.sequenceiq.cloudbreak.cmtemplate.CmTemplateProcessor;
import com.sequenceiq.cloudbreak.cmtemplate.configproviders.AbstractRoleConfigProvider;
import com.sequenceiq.cloudbreak.dto.KerberosConfig;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;
import com.sequenceiq.cloudbreak.template.views.GatewayView;
import com.sequenceiq.cloudbreak.template.views.HostgroupView;
import com.sequenceiq.common.api.type.InstanceGroupType;

@Component
public class KnoxGatewayConfigProvider extends AbstractRoleConfigProvider {

    private static final String KNOX_SERVICE_REF_NAME = "knox";

    private static final String KNOX_GATEWAY_REF_NAME = "knox-KNOX_GATEWAY-BASE";

    private static final String KNOX_MASTER_SECRET = "gateway_master_secret";

    private static final String GATEWAY_PATH = "gateway_path";

    private static final String SIGNING_KEYSTORE_NAME = "gateway_signing_keystore_name";

    private static final String SIGNING_KEYSTORE_TYPE = "gateway_signing_keystore_type";

    private static final String SIGNING_KEY_ALIAS = "gateway_signing_key_alias";

    private static final String SIGNING_JKS = "signing.jks";

    private static final String JKS = "JKS";

    private static final String SIGNING_IDENTITY = "signing-identity";

    private static final String GATEWAY_WHITELIST = "gateway_dispatch_whitelist";

    // TODO Move over to TemplatePreparationObject and its initializer class; replace with a more general value
    private static final String ID_BROKER_DATA_ACCESS_ROLE = "arn:aws:iam::980678866538:role/lrodek-ec2-role-user-lrodek";

    // TODO Move over to TemplatePreparationObject and its initializer class; replace with a more general value
    private static final String ID_BROKER_CLUSTER_CREATOR_USER = "lrodek";

    // TODO Move over to TemplatePreparationObject and its initializer class; incomplete
    private static final Set<String> ID_BROKER_DATA_ACCESS_SERVICES = Set.of("yarn", "hdfs", "hive", "ranger", "atlas", "kafka", "solr");

    @Override
    protected List<ApiClusterTemplateConfig> getRoleConfigs(String roleType, TemplatePreparationObject source) {
        GatewayView gateway = source.getGatewayView();
        String masterSecret = gateway != null ? gateway.getMasterSecret() : source.getGeneralClusterConfigs().getPassword();

        List<ApiClusterTemplateConfig> config = new ArrayList<>();
        switch (roleType) {
            case KnoxRoles.KNOX_GATEWAY:
                config.add(config(KNOX_MASTER_SECRET, masterSecret));
                Optional<KerberosConfig> kerberosConfig = source.getKerberosConfig();
                if (gateway != null) {
                    config.add(config(GATEWAY_PATH, gateway.getPath()));
                    config.add(config(SIGNING_KEYSTORE_NAME, SIGNING_JKS));
                    config.add(config(SIGNING_KEYSTORE_TYPE, JKS));
                    config.add(config(SIGNING_KEY_ALIAS, SIGNING_IDENTITY));
                    if (kerberosConfig.isPresent()) {
                        String domain = kerberosConfig.get().getDomain();
                        config.add(config(GATEWAY_WHITELIST, "^/.*$;^https?://(.+." + domain + "):[0-9]+/?.*$"));
                    } else {
                        config.add(config(GATEWAY_WHITELIST, "^*.*$"));
                    }
                }
                return config;
            case KnoxRoles.IDBROKER:
                config.add(config("idbroker_master_secret", masterSecret));
                // TODO Fetch mappings from TemplatePreparationObject
                // TODO Determine cloud platform from TemplatePreparationObject
                // TODO Set mappings for Azure and GCP
                String userMapping = Stream.concat(List.of(ID_BROKER_CLUSTER_CREATOR_USER).stream(), ID_BROKER_DATA_ACCESS_SERVICES.stream())
                        .map(s -> String.format("%s=%s", s, ID_BROKER_DATA_ACCESS_ROLE))
                        .collect(Collectors.joining(";"));
                config.add(config("idbroker_aws_user_mapping", userMapping));
                config.add(config("idbroker_aws_group_mapping", ""));
                return config;
            default:
                return List.of();
        }
    }

    @Override
    public Map<String, ApiClusterTemplateService> getAdditionalServices(CmTemplateProcessor cmTemplateProcessor, TemplatePreparationObject source) {
        if (source.getGatewayView() != null && cmTemplateProcessor.getServiceByType(KnoxRoles.KNOX).isEmpty()) {
            ApiClusterTemplateService knox = createBaseKnoxService();
            Set<HostgroupView> hostgroupViews = source.getHostgroupViews();
            return hostgroupViews.stream()
                    .filter(hg -> InstanceGroupType.GATEWAY.equals(hg.getInstanceGroupType()))
                    .collect(Collectors.toMap(HostgroupView::getName, v -> knox));
        }
        return Map.of();
    }

    private ApiClusterTemplateService createBaseKnoxService() {
        ApiClusterTemplateService knox = new ApiClusterTemplateService().serviceType(KnoxRoles.KNOX).refName(KNOX_SERVICE_REF_NAME);
        ApiClusterTemplateRoleConfigGroup knoxGateway = new ApiClusterTemplateRoleConfigGroup()
                .roleType(KnoxRoles.KNOX_GATEWAY).base(true).refName(KNOX_GATEWAY_REF_NAME);
        knox.roleConfigGroups(List.of(knoxGateway));
        return knox;
    }

    @Override
    public String getServiceType() {
        return KnoxRoles.KNOX;
    }

    @Override
    public List<String> getRoleTypes() {
        return List.of(KnoxRoles.KNOX_GATEWAY, KnoxRoles.IDBROKER);
    }
}
