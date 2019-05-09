package com.sequenceiq.it.cloudbreak.newway.testcase.smoke;

import static com.sequenceiq.cloudbreak.api.endpoint.v4.recipes.requests.RecipeV4Type.POST_AMBARI_START;
import static com.sequenceiq.it.cloudbreak.newway.cloud.HostGroupType.MASTER;
import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.key;

import java.io.IOException;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.sequenceiq.it.cloudbreak.newway.client.RecipeTestClient;
import com.sequenceiq.it.cloudbreak.newway.client.StackTestClient;
import com.sequenceiq.it.cloudbreak.newway.cloud.v2.CommonCloudProperties;
import com.sequenceiq.it.cloudbreak.newway.context.Description;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.dto.ClouderaManagerProductTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.ClouderaManagerRepositoryTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.ClouderaManagerTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.ClusterTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.InstanceGroupTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.recipe.RecipeTestDto;
import com.sequenceiq.it.cloudbreak.newway.dto.stack.StackTestDto;
import com.sequenceiq.it.cloudbreak.newway.testcase.e2e.AbstractE2ETest;
import com.sequenceiq.it.cloudbreak.newway.util.ClouderaManagerUtil;
import com.sequenceiq.it.util.ResourceUtil;

public class YarnSmokeTest extends AbstractE2ETest {

    private static final int NODE_COUNT = 1;

    private static final String INSTANCE_GROUP_ID = "ig";

    private static final String CREATE_CM_USER_SCRIPT_FILE = "classpath:/recipes/create-cm-user.sh";

    private static final String REPOSITORY_VERSION = "7.x.0";

    private static final String REPOSITORY_URL = "http://cloudera-build-3-us-central-1.gce.cloudera.com/s3/build/1072240/cm7/7.x.0/redhat7/yum/";

    private static final String PRODUCT_VERSION = "6.0.99-1.cdh6.0.99.p0.181";

    private static final String PRODUCT_PARCEL = "http://cloudera-build-3-us-central-1.gce.cloudera.com/s3/build/1071671/cdh/6.x/parcels/";

    private static final String PRODUCT_NAME = "CDH";

    @Inject
    private StackTestClient stackTestClient;

    @Inject
    private RecipeTestClient recipeTestClient;

    @Inject
    private CommonCloudProperties commonCloudProperties;

    @Test(dataProvider = TEST_CONTEXT)
    @Description(
            given = "a valid YARN workload with POST CLUSTER MANAGER START recipe",
            when = "the workload is created",
            then = "a new Cloudera Manager user has to be created"
    )
    public void testWhenCreatedYARNClusterShouldClouderaManagerUserPresentByPostClusterInstallRecipe(TestContext testContext) throws IOException {
        String postCmStartRecipeName = resourcePropertyProvider().getName();
        String cm = resourcePropertyProvider().getName();
        String cmcluster = resourcePropertyProvider().getName();
        String stack = resourcePropertyProvider().getName();

        testContext
                .given(cm, ClouderaManagerTestDto.class)
                .withClouderaManagerRepository(new ClouderaManagerRepositoryTestDto(testContext)
                        .withVersion(REPOSITORY_VERSION).withBaseUrl(REPOSITORY_URL))
                .withClouderaManagerProduct(new ClouderaManagerProductTestDto(testContext)
                        .withName(PRODUCT_NAME).withParcel(PRODUCT_PARCEL).withVersion(PRODUCT_VERSION))
                .given(cmcluster, ClusterTestDto.class).withValidateBlueprint(Boolean.FALSE).withClouderaManager(cm)
                .given(RecipeTestDto.class)
                .withName(postCmStartRecipeName).withContent(generateCreateCMUserRecipeContent(CREATE_CM_USER_SCRIPT_FILE))
                .withRecipeType(POST_AMBARI_START)
                .when(recipeTestClient.createV4())
                .given(INSTANCE_GROUP_ID, InstanceGroupTestDto.class)
                .withHostGroup(MASTER).withNodeCount(NODE_COUNT).withRecipes(postCmStartRecipeName)
                .given(StackTestDto.class).withCluster(cmcluster)
                .replaceInstanceGroups(INSTANCE_GROUP_ID)
                .when(stackTestClient.createV4(), key(stack))
                .await(STACK_AVAILABLE, key(stack))
                .then(ClouderaManagerUtil::checkClouderaManagerUser)
                .validate();
    }

    private String generateCreateCMUserRecipeContent(String filePath) throws IOException {
        String cmUser = commonCloudProperties.getClouderaManager().getDefaultUser();
        String cmPassword = commonCloudProperties.getClouderaManager().getDefaultPassword();
        String recipeContentFromFile = ResourceUtil.readResourceAsString(applicationContext, filePath);

        recipeContentFromFile = recipeContentFromFile.replaceAll("CM_USER", cmUser);
        recipeContentFromFile = recipeContentFromFile.replaceAll("CM_PASSWORD", cmPassword);
        return Base64.encodeBase64String(recipeContentFromFile.getBytes());
    }

}