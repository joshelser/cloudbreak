package com.sequenceiq.cloudbreak.service.image;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableSet;
import com.sequenceiq.cloudbreak.cloud.CloudConstant;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.cloud.model.catalog.CloudbreakImageCatalogV2;
import com.sequenceiq.cloudbreak.cloud.model.catalog.CloudbreakVersion;
import com.sequenceiq.cloudbreak.cloud.model.catalog.Image;
import com.sequenceiq.cloudbreak.cloud.model.catalog.Images;
import com.sequenceiq.cloudbreak.cloud.model.catalog.Versions;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.core.CloudbreakImageCatalogException;
import com.sequenceiq.cloudbreak.core.flow2.stack.image.update.StackImageUpdateService;
import com.sequenceiq.cloudbreak.domain.ImageCatalog;
import com.sequenceiq.cloudbreak.domain.UserProfile;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.repository.ImageCatalogRepository;
import com.sequenceiq.cloudbreak.service.AuthenticatedUserService;
import com.sequenceiq.cloudbreak.service.AuthorizationService;
import com.sequenceiq.cloudbreak.service.account.AccountPreferencesService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.user.UserProfileService;
import com.sequenceiq.cloudbreak.util.FileReaderUtils;
import com.sequenceiq.cloudbreak.util.JsonUtil;

@RunWith(MockitoJUnitRunner.class)
public class ImageCatalogServiceTest {

    public static final String USER_ID = "userId";

    public static final String USERNAME = "username";

    public static final String ACCOUNT = "account";

    private static final String GIVEN_CB_VERSION = "2.8.0";

    private static final String DEFAULT_CATALOG_URL = "http://localhost/imagecatalog-url";

    private static final String CUSTOM_IMAGE_CATALOG_URL = "http://localhost/custom-imagecatalog-url";

    private static final String V2_CATALOG_FILE = "com/sequenceiq/cloudbreak/service/image/cb-image-catalog-v2.json";

    private static final String PROD_CATALOG_FILE = "com/sequenceiq/cloudbreak/service/image/cb-prod-image-catalog.json";

    private static final String DEV_CATALOG_FILE = "com/sequenceiq/cloudbreak/service/image/cb-dev-image-catalog.json";

    private static final String RC_CATALOG_FILE = "com/sequenceiq/cloudbreak/service/image/cb-rc-image-catalog.json";

    public static final String PROVIDER_AWS = "AWS";

    public static final String STACK_NAME = "stackName";

    public static final String IMAGE_CATALOG_NAME = "anyImageCatalog";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private ImageCatalogProvider imageCatalogProvider;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Spy
    private ImageCatalogVersionFilter versionFilter;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private ImageCatalogRepository imageCatalogRepository;

    @Mock
    private AccountPreferencesService accountPreferencesService;

    @Mock
    private StackService stackService;

    @Mock
    private StackImageUpdateService stackImageUpdateService;

    @InjectMocks
    private ImageCatalogService underTest;

    @Spy
    private final List<CloudConstant> constants = new ArrayList<>();

    @Before
    public void beforeTest() throws Exception {
        setupImageCatalogProvider(CUSTOM_IMAGE_CATALOG_URL, V2_CATALOG_FILE);

        IdentityUser user = getIdentityUser();
        when(authenticatedUserService.getCbUser()).thenReturn(user);
        when(accountPreferencesService.enabledPlatforms()).thenReturn(new HashSet<>(Arrays.asList("AZURE", "AWS", "GCP", "OPENSTACK")));

        constants.addAll(Collections.singletonList(new AwsCloudConstant()));

        ReflectionTestUtils.setField(underTest, ImageCatalogService.class, "defaultCatalogUrl", DEFAULT_CATALOG_URL, null);
        setMockedCbVersion("cbVersion", "unspecified");
    }

    private void setMockedCbVersion(String cbVersion, String versionValue) {
        ReflectionTestUtils.setField(underTest, ImageCatalogService.class, cbVersion, versionValue, String.class);
    }

    private IdentityUser getIdentityUser() {
        return new IdentityUser(USER_ID, USERNAME, ACCOUNT,
                Collections.emptyList(), "givenName", "familyName", new Date());
    }

    @Test
    public void testGetLatestBaseImageDefaultPreferredWithNoDefaultsLatest() throws Exception {
        String name = "img-name";
        setupUserProfileService();
        setupImageCatalogProvider(DEFAULT_CATALOG_URL, V2_CATALOG_FILE);
        ReflectionTestUtils.setField(underTest, ImageCatalogService.class, "cbVersion", "2.1.0-dev.100", null);

        StatedImage image = underTest.getLatestBaseImageDefaultPreferred("AWS", null);
        assertEquals("7aca1fa6-980c-44e2-a75e-3144b18a5993", image.getImage().getUuid());
        Assert.assertFalse(image.getImage().isDefaultImage());
    }

    @Test
    public void testGetLatestBaseImageDefaultPreferredWithNoDefaultsLatestNoVersionMatch() throws Exception {
        String name = "img-name";
        setupUserProfileService();
        setupImageCatalogProvider(DEFAULT_CATALOG_URL, V2_CATALOG_FILE);
        ReflectionTestUtils.setField(underTest, ImageCatalogService.class, "cbVersion", "2.1.0-dev.200", null);

        StatedImage image = underTest.getLatestBaseImageDefaultPreferred("AWS", null);
        assertEquals("7aca1fa6-980c-44e2-a75e-3144b18a5993", image.getImage().getUuid());
        Assert.assertTrue(image.getImage().isDefaultImage());
    }

    @Test
    public void testGetLatestBaseImageDefaultPreferredWithMultipleDefaults() throws Exception {
        String name = "img-name";
        setupUserProfileService();
        setupImageCatalogProvider(DEFAULT_CATALOG_URL, V2_CATALOG_FILE);
        ReflectionTestUtils.setField(underTest, ImageCatalogService.class, "cbVersion", "2.1.0-dev.1", null);

        StatedImage image = underTest.getLatestBaseImageDefaultPreferred("AWS", null);
        assertEquals("7aca1fa6-980c-44e2-a75e-3144b18a5993", image.getImage().getUuid());
        Assert.assertTrue(image.getImage().isDefaultImage());
    }

    private void setupUserProfileService() {
        IdentityUser user = getIdentityUser();
        UserProfile userProfile = new UserProfile();
        when(userProfileService.getOrCreate(user.getAccount(), user.getUserId())).thenReturn(userProfile);
    }

    @Test
    public void testGetLatestBaseImageDefaultPreferredWenNotLatestSelected() throws Exception {
        String name = "img-name";
        setupUserProfileService();
        setupImageCatalogProvider(DEFAULT_CATALOG_URL, V2_CATALOG_FILE);
        ReflectionTestUtils.setField(underTest, ImageCatalogService.class, "cbVersion", "2.1.0-dev.2", null);

        StatedImage image = underTest.getLatestBaseImageDefaultPreferred("AWS", null);
        assertEquals("f6e778fc-7f17-4535-9021-515351df3691", image.getImage().getUuid());
        Assert.assertTrue(image.getImage().isDefaultImage());
    }

    @Test
    public void testGetImagesWhenExactVersionExistsInCatalog() throws Exception {
        String cbVersion = "1.16.4";
        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", cbVersion);

        boolean exactImageIdMatch = images.getImages().getHdpImages().stream()
                .anyMatch(img -> "2.5.1.9-4-ccbb32dc-6c9f-43f1-8a09-64b598fda733-2.6.1.4-2".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required Ambari image with id.", exactImageIdMatch);
    }

    private ImageCatalog getImageCatalog() {
        ImageCatalog imageCatalog = new ImageCatalog();
        imageCatalog.setImageCatalogUrl(CUSTOM_IMAGE_CATALOG_URL);
        imageCatalog.setImageCatalogName("default");
        return imageCatalog;
    }

    @Test
    public void testGetImagesWhenExactVersionExistsInCatalogAndMorePlatformRequested() throws Exception {
        String cbVersion = "1.12.0";
        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, ImmutableSet.of("aws", "azure"), cbVersion);
        boolean awsAndAzureWerePresentedInTheTest = false;
        assertEquals(2L, images.getImages().getHdpImages().size());
        for (Image image : images.getImages().getHdpImages()) {
            boolean containsAws = images.getImages().getHdpImages().stream()
                    .anyMatch(img -> img.getImageSetsByProvider().entrySet().stream().anyMatch(
                            platformImages -> "aws".equals(platformImages.getKey())));
            boolean containsAzure = images.getImages().getHdpImages().stream()
                    .anyMatch(img -> img.getImageSetsByProvider().entrySet().stream().anyMatch(
                            platformImages -> "azure_rm".equals(platformImages.getKey())));
            if (image.getImageSetsByProvider().size() == 2) {
                awsAndAzureWerePresentedInTheTest = true;
                Assert.assertTrue("Result doesn't contain the required Ambari image with id.", containsAws && containsAzure);
            } else if (image.getImageSetsByProvider().size() == 1) {
                Assert.assertTrue("Result doesn't contain the required Ambari image with id.", containsAws || containsAzure);

            }
        }
        Assert.assertTrue(awsAndAzureWerePresentedInTheTest);
    }

    @Test
    public void testGetImagesWhenLatestVersionDoesntExistInCatalogShouldReturnWithReleasedVersionIfExists() throws Exception {
        setupImageCatalogProvider(CUSTOM_IMAGE_CATALOG_URL, PROD_CATALOG_FILE);

        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", "2.6.0");

        boolean match = images.getImages().getHdpImages().stream()
                .anyMatch(img -> "63cdb3bc-28a6-4cea-67e4-9842fdeeaefb".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required base image with id.", match);
    }

    @Test
    public void testGetImagesWhenLatestDevVersionDoesntExistInCatalogShouldReturnWithReleasedVersionIfExists() throws Exception {
        setupImageCatalogProvider(CUSTOM_IMAGE_CATALOG_URL, DEV_CATALOG_FILE);

        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", "2.6.0-dev.132");

        boolean match = images.getImages().getHdpImages().stream()
                .anyMatch(img -> "b150efce-33ac-49c9-7206-7f148d162744".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required base image with id.", match);
    }

    @Test
    public void testGetImagesWhenLatestRcVersionDoesntExistInCatalogShouldReturnWithReleasedVersionIfExists() throws Exception {
        setupImageCatalogProvider(CUSTOM_IMAGE_CATALOG_URL, RC_CATALOG_FILE);

        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", "2.6.0-rc.13");

        boolean match = images.getImages().getHdpImages().stream()
                .anyMatch(img -> "bbc63453-086c-4bf7-4337-a04c37d51b68".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required base image with id.", match);
    }

    @Test(expected = BadRequestException.class)
    public void testGetImagesWhenLatestRcVersionDoesntExistInDevCatalogShouldThrow() throws Exception {
        setupImageCatalogProvider(CUSTOM_IMAGE_CATALOG_URL, DEV_CATALOG_FILE);

        ImageCatalog imageCatalog = getImageCatalog();
        underTest.getImages(imageCatalog, "aws", "2.6.0-rc.13");
    }

    @Test
    public void testGetImagesWhenSimilarDevVersionDoesntExistInCatalogShouldReturnWithReleasedVersionIfExists() throws Exception {
        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", "1.16.4-dev.132");

        boolean match = images.getImages().getHdpImages().stream()
                .anyMatch(img -> "2.5.1.9-4-ccbb32dc-6c9f-43f1-8a09-64b598fda733-2.6.1.4-2".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required Ambari image with id.", match);
    }

    @Test
    public void testGetImagesWhenSimilarRcVersionDoesntExistInCatalogShouldReturnWithReleasedVersionIfExists() throws Exception {
        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", "1.16.4-rc.13");

        boolean match = images.getImages().getHdpImages().stream()
                .anyMatch(img -> "2.5.1.9-4-ccbb32dc-6c9f-43f1-8a09-64b598fda733-2.6.1.4-2".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required Ambari image with id.", match);
    }

    @Test
    public void testGetImagesWhenSimilarDevVersionExistsInCatalog() throws Exception {
        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", "2.1.0-dev.4000");

        boolean hdfImgMatch = images.getImages().getHdfImages().stream()
                .anyMatch(ambariImage -> "9958938a-1261-48e2-aff9-dbcb2cebf6cd".equals(ambariImage.getUuid()));
        boolean hdpImgMatch = images.getImages().getHdpImages().stream()
                .anyMatch(ambariImage -> "2.5.0.2-65-5288855d-d7b9-4b90-b326-ab4b168cf581-2.6.0.1-145".equals(ambariImage.getUuid()));
        boolean baseImgMatch = images.getImages().getBaseImages().stream()
                .anyMatch(ambariImage -> "f6e778fc-7f17-4535-9021-515351df3691".equals(ambariImage.getUuid()));
        Assert.assertTrue("Result doesn't contain the required Ambari image with id.", hdfImgMatch && hdpImgMatch && baseImgMatch);
    }

    @Test
    public void testGetImagesWhenSimilarRcVersionExistsInCatalog() throws Exception {
        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "aws", "2.0.0-rc.4");

        boolean allMatch = images.getImages().getHdpImages().stream()
                .allMatch(img -> "2.4.2.2-1-9e3ccdca-fa64-42eb-ab29-b1450767bbd8-2.5.0.1-265".equals(img.getUuid())
                        || "2.5.1.9-4-ccbb32dc-6c9f-43f1-8a09-64b598fda733-2.6.1.4-2".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required Ambari image with id.", allMatch);
    }

    @Test
    public void testGetImagesWhenExactVersionExistsInCatalogForPlatform() throws Exception {
        ImageCatalog imageCatalog = getImageCatalog();
        StatedImages images = underTest.getImages(imageCatalog, "AWS", "1.16.4");
        boolean exactImageIdMatch = images.getImages().getHdpImages().stream()
                .anyMatch(img -> "2.5.1.9-4-ccbb32dc-6c9f-43f1-8a09-64b598fda733-2.6.1.4-2".equals(img.getUuid()));
        Assert.assertTrue("Result doesn't contain the required Ambari image with id for the platform.", exactImageIdMatch);
    }

    @Test
    public void testGetImagesWhenExactVersionDoesnotExistInCatalogForPlatform() throws Exception {
        ImageCatalog imageCatalog = getImageCatalog();

        thrown.expectMessage("Platform(s) owncloud are not supported by the current catalog");
        thrown.expect(CloudbreakImageCatalogException.class);

        underTest.getImages(imageCatalog, "owncloud", "1.16.4");
    }

    @Test
    public void testGetImagesWhenCustomImageCatalogExists() throws Exception {
        ImageCatalog ret = new ImageCatalog();
        ret.setImageCatalogUrl(CUSTOM_IMAGE_CATALOG_URL);
        when(imageCatalogRepository.findByName("name", "userId", "account")).thenReturn(ret);
        when(imageCatalogProvider.getImageCatalogV2(CUSTOM_IMAGE_CATALOG_URL)).thenReturn(null);
        underTest.getImages("name", "aws");

        verify(imageCatalogProvider, times(1)).getImageCatalogV2(CUSTOM_IMAGE_CATALOG_URL);

    }

    @Test
    public void testGetImagesWhenCustomImageCatalogDoesNotExists() throws Exception {
        when(imageCatalogRepository.findByName("verycool", "userId", "account")).thenThrow(new AccessDeniedException("denied"));

        thrown.expectMessage("The verycool catalog does not exist or does not belong to your account.");
        thrown.expect(CloudbreakImageCatalogException.class);

        underTest.getImages("verycool", "aws").getImages();

        verify(imageCatalogProvider, times(0)).getImageCatalogV2("");
    }

    @Test
    public void testDeleteImageCatalog() {
        String name = "img-name";
        IdentityUser user = getIdentityUser();
        UserProfile userProfile = new UserProfile();
        ImageCatalog imageCatalog = new ImageCatalog();
        imageCatalog.setImageCatalogName(name);
        imageCatalog.setArchived(false);
        when(authenticatedUserService.getCbUser()).thenReturn(user);
        when(imageCatalogRepository.findByName(name, user.getUserId(), user.getAccount())).thenReturn(imageCatalog);
        when(userProfileService.getOrCreate(user.getAccount(), user.getUserId(), user.getUsername())).thenReturn(userProfile);
        underTest.delete(name);

        verify(imageCatalogRepository, times(1)).save(imageCatalog);

        Assert.assertTrue(imageCatalog.isArchived());
        Assert.assertTrue(imageCatalog.getImageCatalogName().startsWith(name) && imageCatalog.getImageCatalogName().indexOf('_') == name.length());
    }

    @Test
    public void testDeleteImageCatalogWhenEnvDefault() {
        String name = "cloudbreak-default";

        thrown.expectMessage("cloudbreak-default cannot be deleted because it is an environment default image catalog.");
        thrown.expect(BadRequestException.class);

        underTest.delete(name);
    }

    @Test
    public void testGet() {
        String name = "img-name";
        ImageCatalog imageCatalog = new ImageCatalog();
        IdentityUser user = getIdentityUser();
        when(authenticatedUserService.getCbUser()).thenReturn(user);
        when(imageCatalogRepository.findByName(name, user.getUserId(), user.getAccount())).thenReturn(imageCatalog);
        ImageCatalog actual = underTest.get(name);

        assertEquals(actual, imageCatalog);
    }

    @Test
    public void testGetWhenEnvDefault() {
        String name = "cloudbreak-default";
        ImageCatalog actual = underTest.get(name);

        verify(imageCatalogRepository, times(0)).findByName(name, USER_ID, ACCOUNT);

        assertEquals(actual.getImageCatalogName(), name);
        Assert.assertNull(actual.getId());
    }

    @Test
    public void testGetApplicableImages() throws CloudbreakImageCatalogException, IOException {
        Stack stack = getStack();
        when(stackService.getPublicStack(eq(STACK_NAME), any())).thenReturn(stack);
        IdentityUser loggedInUser = setupLoggedInUser();
        ImageCatalog imageCatalog = setupImageCatalog();
        when(imageCatalogProvider.getImageCatalogV2(anyString())).thenReturn(getImageCatalogV2());
        ReflectionTestUtils.setField(underTest, "cbVersion", GIVEN_CB_VERSION);
        when(stackImageUpdateService.isValidImage(any(), anyString(), anyString(), anyString())).thenReturn(true);

        Images images = underTest.getApplicableImages(IMAGE_CATALOG_NAME, STACK_NAME);

        assertEquals("hdp-1", images.getHdpImages().get(0).getUuid());
        assertEquals("base-2", images.getBaseImages().get(0).getUuid());
        assertEquals("hdf-3", images.getHdfImages().get(0).getUuid());
        verify(imageCatalogRepository).findByName(eq(IMAGE_CATALOG_NAME), eq(loggedInUser.getUserId()), eq(loggedInUser.getAccount()));
        InOrder inorder = inOrder(stackImageUpdateService);
        inorder.verify(stackImageUpdateService).isValidImage(eq(stack), eq("hdp-1"), eq(imageCatalog.getImageCatalogName()), eq(imageCatalog.getImageCatalogUrl()));
        inorder.verify(stackImageUpdateService).isValidImage(eq(stack), eq("base-2"), eq(imageCatalog.getImageCatalogName()), eq(imageCatalog.getImageCatalogUrl()));
        inorder.verify(stackImageUpdateService).isValidImage(eq(stack), eq("hdf-3"), eq(imageCatalog.getImageCatalogName()), eq(imageCatalog.getImageCatalogUrl()));
    }

    @Test
    public void testGetApplicableImagesWhenStackImageUpdateServiceRejectsAll() throws CloudbreakImageCatalogException, IOException {
        Stack stack = getStack();
        when(stackService.getPublicStack(eq(STACK_NAME), any())).thenReturn(stack);
        IdentityUser loggedInUser = setupLoggedInUser();
        ImageCatalog imageCatalog = setupImageCatalog();
        when(imageCatalogProvider.getImageCatalogV2(anyString())).thenReturn(getImageCatalogV2());
        ReflectionTestUtils.setField(underTest, "cbVersion", GIVEN_CB_VERSION);
        when(stackImageUpdateService.isValidImage(any(), anyString(), anyString(), anyString())).thenReturn(false);

        Images images = underTest.getApplicableImages(IMAGE_CATALOG_NAME, STACK_NAME);

        assertThat(images.getHdpImages(), empty());
        assertThat(images.getHdfImages(), empty());
        assertThat(images.getBaseImages(), empty());
        verify(imageCatalogRepository).findByName(eq(IMAGE_CATALOG_NAME), eq(loggedInUser.getUserId()), eq(loggedInUser.getAccount()));
        InOrder inorder = inOrder(stackImageUpdateService);
        inorder.verify(stackImageUpdateService).isValidImage(eq(stack), eq("hdp-1"), eq(imageCatalog.getImageCatalogName()), eq(imageCatalog.getImageCatalogUrl()));
        inorder.verify(stackImageUpdateService).isValidImage(eq(stack), eq("base-2"), eq(imageCatalog.getImageCatalogName()), eq(imageCatalog.getImageCatalogUrl()));
        inorder.verify(stackImageUpdateService).isValidImage(eq(stack), eq("hdf-3"), eq(imageCatalog.getImageCatalogName()), eq(imageCatalog.getImageCatalogUrl()));
    }

    private IdentityUser setupLoggedInUser() {
        IdentityUser user = new IdentityUser("", "", "", Collections.emptyList(), "", "", Instant.now().toDate());
        when(authenticatedUserService.getCbUser()).thenReturn(user);
        return user;
    }

    private ImageCatalog setupImageCatalog() {
        ImageCatalog imageCatalog1 = new ImageCatalog();
        imageCatalog1.setImageCatalogName(IMAGE_CATALOG_NAME);
        imageCatalog1.setImageCatalogUrl(CUSTOM_IMAGE_CATALOG_URL);
        ImageCatalog imageCatalog = imageCatalog1;
        when(imageCatalogRepository.findByName(anyString(), anyString(), anyString())).thenReturn(imageCatalog);
        return imageCatalog;
    }

    private CloudbreakImageCatalogV2 getImageCatalogV2() {
        List<String> supportedVersion = Collections.singletonList(GIVEN_CB_VERSION);
        Images images = new Images(
                Arrays.asList(getImage("b", "base-2")),
                Arrays.asList(getImage("a", "hdp-1")),
                Arrays.asList(getImage("c", "hdf-3")),
                new HashSet<>()
        );
        return new CloudbreakImageCatalogV2(images, new Versions(Arrays.asList(new CloudbreakVersion(supportedVersion, new ArrayList<>(), Arrays.asList("hdp-1", "base-2", "hdf-3")))));
    }

    private Image getImage(String os, String id) {
        Map<String, Map<String, String>> imageSetsByProvider = new HashMap<>();
        imageSetsByProvider.put(PROVIDER_AWS, null);
        return new Image("", "", os, id, "", Collections.emptyMap(), imageSetsByProvider, null, "");
    }

    private Stack getStack() {
        Stack stack = new Stack();
        stack.setCloudPlatform(PROVIDER_AWS);
        stack.setName(STACK_NAME);
        return stack;
    }

    private static class AwsCloudConstant implements CloudConstant {
        @Override
        public Platform platform() {
            return Platform.platform(PROVIDER_AWS);
        }

        @Override
        public Variant variant() {
            return Variant.variant(PROVIDER_AWS);
        }
    }

    private void setupImageCatalogProvider(String catalogUrl, String catalogFile) throws IOException, CloudbreakImageCatalogException {
        String catalogJson = FileReaderUtils.readFileFromClasspath(catalogFile);
        CloudbreakImageCatalogV2 catalog = JsonUtil.readValue(catalogJson, CloudbreakImageCatalogV2.class);
        when(imageCatalogProvider.getImageCatalogV2(catalogUrl)).thenReturn(catalog);
    }
}