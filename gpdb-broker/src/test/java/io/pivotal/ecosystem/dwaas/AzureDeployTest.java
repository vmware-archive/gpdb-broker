package io.pivotal.ecosystem.dwaas;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import io.pivotal.ecosystem.dwaas.azure.AzureDeployClient;
import io.pivotal.ecosystem.dwaas.azure.AzureDeployClientConfig;
import io.pivotal.ecosystem.dwaas.azure.AzureDeployClientProperties;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.AssertTrue;
import java.io.IOException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes={AzureDeployClientConfig.class, AzureDeployClientProperties.class})
@TestPropertySource(locations="classpath:test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // preserve the context between tests
public class AzureDeployTest {

    private static final Logger log = LoggerFactory.getLogger(AzureDeployTest.class);

    @Autowired
    public AzureDeployClient azureClient;

    @Autowired
    private AzureDeployClientProperties properties;

    @Value("${cognition.pivnetapitoken}")
    private String pivnetApiToken;

    @Value("${cognition.azure.templateurl}")
    private String templateUrl;

    @Value("${cognition.sshadmin.username}")
    private String adminUsername;

    @Value("${cognition.sshadmin.password}")
    private String adminPassword;

    @Value("${cognition.sshadmin.pubkey}")
    private String adminPubkey;

    private Azure azureService;

    private JSONObject clusterParams;

    private String deploymentName = "azure-java-deployment-test";

    private String targetRegion = Region.US_EAST.toString();
    private String resourceGroupName = "azure-gpdb-java-test";

    private static String contentVersion = "1.0.0.0";

    static ResourceGroups resourceGroups;


    @Before
    public void setup() throws JSONException {

        try {
            azureService = Azure.authenticate(azureClient.restClient(), properties.getTenantId()).withDefaultSubscription();
        } catch (com.microsoft.azure.CloudException ae) {
            log.error(ae.toString());
            Assert.fail();
        } catch (IOException ie) {
            log.error(ie.toString());
            Assert.fail();
        } catch (NoSuchMethodError nsme) {
            log.error(nsme.toString());
            Assert.fail();
        }


        Assert.assertNotNull(azureService);
        resourceGroups = azureService.resourceGroups();

        clusterParams = new JSONObject();
        clusterParams.put("artifactsBaseUrl", new JSONObject().put("value", "https://gpdbdemo.blob.core.windows.net/marketplace-testing"));

        clusterParams.put("location", new JSONObject().put("value", targetRegion));
        clusterParams.put("pivotalNetworkToken", new JSONObject().put("value", pivnetApiToken));
        clusterParams.put("clusterName", new JSONObject().put("value", "javatest")); // has a default
        clusterParams.put("adminUserName", new JSONObject().put("value", adminUsername)); // has a default
        clusterParams.put("authenticationType", new JSONObject().put("value", "password"));

        clusterParams.put("adminPassword", new JSONObject().put("value", adminPassword));
        clusterParams.put("sshPublicKey", new JSONObject().put("value", adminPubkey));
        clusterParams.put("gpadminPassword", new JSONObject().put("value", adminPassword));
        clusterParams.put("standbyMaster", new JSONObject().put("value", "1")); // has a default
        clusterParams.put("vmSize", new JSONObject().put("value", "Standard_DS13_v2")); // has a default

        clusterParams.put("segmentHostsCount", new JSONObject().put("value", 2));
        clusterParams.put("dataDiskCount", new JSONObject().put("value", 2)); // has a default
        clusterParams.put("dataDiskSizeGB", new JSONObject().put("value", 512)); // has a default

        //clusterParams.put("vnetNewOrExisting", "new"); // has a default
        clusterParams.put("vnetResourceGroup", new JSONObject().put("value", resourceGroupName));
        //clusterParams.put("vnetName", "azure-gpdb-test-vnet"); // has a default
        //clusterParams.put("vnetAddressSpace", "10.0.0.0/16"); // has a default

        //clusterParams.put("subnet1Name", "subnet-data"); // has a default
        //clusterParams.put("subnet1AddressSpace", "10.0.1.0/24"); // has a default
        //clusterParams.put("subnet1StartAddress", "10.0.1.4"); // has a default
        clusterParams.put("publicIPAddressNewOrExisting", new JSONObject().put("value", "new")); // has a default

        clusterParams.put("publicIPAddressResourceGroup", new JSONObject().put("value", resourceGroupName));
        //clusterParams.put("publicIPAddressName", "gpdb-publicip"); // has a default
        clusterParams.put("publicIPAddressDomainNameLabel", new JSONObject().put("value", "gpdbjunittest"));
        clusterParams.put("allowedIPAddressPrefix", new JSONObject().put("value", "0.0.0.0/0"));

        clusterParams.put("releaseName", new JSONObject().put("value", "4.3.16.0")); // has a default
    }

    @Test
    public void contextSanityCheck() {

        try {
            Assert.assertNotNull(azureClient);
            Assert.assertNotNull(clusterParams);
            Assert.assertNotNull(resourceGroups);
        } catch (Exception ae) {
            log.info(ae.toString());
            Assert.fail();
        }


    }

    @Test
    public void findExistingResourceGroupWithTheDefaultTestName() {

        //azureService.resourceGroups().
        try {
            Assert.assertFalse(azureService.resourceGroups().checkExistence(resourceGroupName));
        } catch (com.microsoft.azure.CloudException ae) {
            log.info(ae.toString());
            return;
        }


    }

    @Test
    public void findExistingDeploymentsInResourceGroupWithTheDefaultTestName() {

        PagedList<Deployment> deployments;
        // List
        try {
            deployments = azureService.deployments().listByResourceGroup(resourceGroupName);
        }
        catch (com.microsoft.azure.CloudException ae) {
            log.info(ae.toString());
            Assert.fail();
            return;
        }

        boolean found = false;
        String deploymentId = "";
        for (Deployment deployment : deployments) {
            if (deployment.name().equals(deploymentName)) {
                found = true;
                log.info("Found " + deployment.name());
                // {gpCommandCenterUrl={type=String, value=http://gpdbjunittest.eastus.cloudapp.azure.com:28080/}}
                //JSONObject output = new JSONObject(deployment.outputs().toString().replace("=", ":"));

                if (deployment.outputs() != null) {
                    log.info(deployment.outputs().toString());
                }
                 //output.getJSONObject("gpCommandCenterUrl").getString("value"));
            }
        }

        Assert.assertFalse(found);
    }

    @Test(timeout=1800000) // 30 minutes
    public void performDeployAndCleanup() throws Exception {
        final String jsonStringOfParameters = clusterParams.toString();

        log.info(jsonStringOfParameters);

        azureService.resourceGroups().define(resourceGroupName)
                .withRegion(targetRegion)
                .withTag("context", "java-deploy-test")
                .create();

        // List
        ResourceGroup groupResult = null;
        for (ResourceGroup rg : azureService.resourceGroups().listByTag("context", "java-deploy-test")) {
            if (rg.name().equals(resourceGroupName)) {
                groupResult = rg;
                break;
            }
        }
        Assert.assertNotNull(groupResult);

        azureService.deployments()
                .define(deploymentName)
                .withExistingResourceGroup(resourceGroupName)
                .withTemplateLink(templateUrl, contentVersion)
                .withParameters(jsonStringOfParameters)
                .withMode(DeploymentMode.INCREMENTAL)
                .create();

        // List
        PagedList<Deployment> deployments = azureService.deployments().listByResourceGroup(resourceGroupName);
        boolean found = false;
        String deploymentId = "";
        for (Deployment deployment : deployments) {
            if (deployment.name().equals(deploymentName)) {
                found = true;

                // {gpCommandCenterUrl={type=String, value=http://gpdbjunittest.eastus.cloudapp.azure.com:28080/}}
                //JSONObject output = new JSONObject(deployment.outputs().toString().replace("=", ":"));

                log.info(deployment.outputs().toString()); //output.getJSONObject("gpCommandCenterUrl").getString("value"));

                Assert.assertTrue(deployment.outputs().toString().contains("gpCommandCenterUrl"));
            }
        }

        Assert.assertEquals("Succeeded", azureService.resourceGroups().getByName(resourceGroupName).provisioningState());

        // List
        groupResult = null;
        for (ResourceGroup rg : azureService.resourceGroups().listByTag("context", "java-deploy-test")) {
            log.info("Found resource group " + rg.name());
            log.info(rg.provisioningState());
            if (rg.name().equals(resourceGroupName)) {

                log.info("Deleting resource group " + resourceGroupName);
                azureService.resourceGroups().beginDeleteByName(resourceGroupName);
                log.info("Current status is " + azureService.resourceGroups().getByName(resourceGroupName).provisioningState());
            }
        }

        Assert.assertEquals("Deleting", azureService.resourceGroups().getByName(resourceGroupName).provisioningState());

    }

    //@After
    public void teardown() throws Exception {
        //Azure azureService = Azure.authenticate(azureDeployClient.restClient(), properties.getTenantId()).withDefaultSubscription();

        Assert.assertNotNull(azureService);
        Assert.assertNotNull(azureService.resourceGroups());
        //Assert.assertNotNull(azureService.resourceGroups().checkExistence(resourceGroupName));

        try {
            log.info(azureService.resourceGroups().getByName(resourceGroupName).provisioningState());

            for (ResourceGroup rg : azureService.resourceGroups().listByTag("context", "java-deploy-test")) {
                log.info("Found resource group " + rg.name());
                log.info(rg.provisioningState());
                if (rg.name().equals(resourceGroupName)) {

                    log.info("Deleting resource group " + resourceGroupName);
                    azureService.resourceGroups().beginDeleteByName(resourceGroupName);
                    log.info("Current status is " + azureService.resourceGroups().getByName(resourceGroupName).provisioningState());
                }
            }

            Assert.assertEquals("Deleting", azureService.resourceGroups().getByName(resourceGroupName).provisioningState());
        } catch (NullPointerException npe) {
            log.info(npe.toString());
        }
    }

}

