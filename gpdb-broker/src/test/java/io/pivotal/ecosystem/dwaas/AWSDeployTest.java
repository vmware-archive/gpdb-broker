package io.pivotal.ecosystem.dwaas;

import com.amazonaws.auth.*;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.*;
import io.pivotal.ecosystem.dwaas.aws.AWSCloudFormationConfig;
import io.pivotal.ecosystem.dwaas.aws.AWSCloudFormationDeployer;
import io.pivotal.ecosystem.dwaas.aws.AWSProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes={AWSCloudFormationConfig.class, AWSCloudFormationDeployer.class, AWSProperties.class})
//@TestPropertySource(properties = {"logging.level.root=DEBUG"})
@TestPropertySource(locations="classpath:test.properties")
public class AWSDeployTest {

    private static final Logger log = LoggerFactory.getLogger(AWSDeployTest.class);

    private AmazonCloudFormation cloudformationClient;

    @Autowired
    AWSCloudFormationDeployer deployer;

    @Autowired
    AWSProperties properties;

    @Value("${cognition.pivnetapitoken}")
    private String pivnetApiToken = "";

    private String templateUrl;
    private String stackName;

    @Before
    public void setup() {
        templateUrl = "";
        stackName = "java-greenplum-cloudformation-test";

        AWSCredentials c = new BasicAWSCredentials(properties.getAccessKey(), properties.getSecretKey());
        AWSStaticCredentialsProvider p = new AWSStaticCredentialsProvider(c);

        this.cloudformationClient = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(p)
                .withRegion(properties.getRegion())
                .build();


        this.deployer.setDeploymentOptions(stackName, "1", properties.getAvailabilityZone(), pivnetApiToken, properties.getKeypairName(), "d2.4xlarge-Ephemeral-24TB", "0.0.0.0/0");


    }

    @Test
    public void showCurrentStacks_beforeCreation() {

        String existingStacks = cloudformationClient.describeStacks(new DescribeStacksRequest()).getStacks().toString();
        log.info(existingStacks);

        Assert.assertEquals("[]", existingStacks);
    }

    @Test(timeout=1200000) // 20 minutes
    public void makeThenDeleteStackCreate() {

        Assert.assertNotNull(cloudformationClient);

        Assert.assertNotNull(cloudformationClient.describeStacks(new DescribeStacksRequest()));

        // Make sure we have a clean slate
        Assert.assertEquals(0, cloudformationClient.describeStacks(new DescribeStacksRequest()).getStacks().size());

        try {
            String createOutput = deployer.makeStack(cloudformationClient);
            log.info(createOutput);
            Assert.assertEquals("CREATE_COMPLETE ()", createOutput);

            Assert.assertNotNull(cloudformationClient.describeStacks(new DescribeStacksRequest()));

            Assert.assertEquals(1, cloudformationClient.describeStacks(new DescribeStacksRequest()).getStacks().size());

            // Show all the stacks for this account along with the resources for each stack
            for (Stack stack : cloudformationClient.describeStacks(new DescribeStacksRequest()).getStacks()) {
                log.info("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");

                Assert.assertEquals(stackName, stack.getStackName());

                Assert.assertEquals("CREATE_COMPLETE", stack.getStackStatus().toString());

                log.info(stack.getOutputs().get(0).getClass().toString());

                Output stackOutput = stack.getOutputs().get(0);

                log.info(stackOutput.getOutputValue());
                //log.info(stack.getOutputs().get(0).toString());

            }
        }
        catch (Exception e) {
            log.error(e.toString());
        }
    }

    @After
    public void after() throws Exception {
        String deleteOutput = deployer.deleteStack(cloudformationClient);
        log.info(deleteOutput);
        Assert.assertEquals("DELETE_COMPLETE", deleteOutput);
    }
}
