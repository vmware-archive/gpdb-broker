package io.pivotal.ecosystem.dwaas.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AWSCloudFormationDeployer {

    final String GPDB_CLOUDFORMATION_URL = "https://s3.amazonaws.com/awsmp-fulfillment-cf-templates-prod/a11bec62-52d1-4fce-9a09-ea8e38ddcd3b.5b93ff4f-cc7f-49e2-a568-e6e9881a0fc8.template";

    private static final Logger log = LoggerFactory.getLogger(AWSCloudFormationDeployer.class);

    @Value("${cognition.pivnetapitoken}")
    private String pivnetApiToken = "";

    @Value("${cognition.aws.stack-name:java-greenplum-cloudformation-test}")
    private String stackName = "";

    @Value("${cognition.cloudformation-url:" + GPDB_CLOUDFORMATION_URL + "}")
    private String cloudformationUrl = "";

    @Value("${cognition.num-nodes:1}")
    private String numNodes = "-1";

    @Value("${cognition.aws.availability-zone:us-west-2a}")
    private String availabilityZone = "";

    @Value("${cognition.aws.keypairn-ame:cognition-testing}")
    private String keypairName = "";

    @Value("${cognition.aws.cluster-node-instance-type:d2.4xlarge-Ephemeral-24TB}")
    private String clusterNodeInstanceType = "";

    @Value("${cognition.aws.logical-resource-name:SampleNotificationTopicTest}")
    private String logicalResourceName = "";

    @Value("${cognition.aws.ssh-locations:0.0.0.0/0}")
    private String sshLocations = "";

    private List<Parameter> params;

    public void setDeploymentOptions(String stackName, String numNodes, String availabilityZone, String pivnetApiToken, String keypairName, String clusterNodeInstanceType, String sshLocations) {
        this.params = new ArrayList<Parameter>();
        this.params.add(new Parameter().withParameterKey("ClusterInstanceCount").withParameterValue(numNodes));
        this.params.add(new Parameter().withParameterKey("AvailabilityZone").withParameterValue(availabilityZone));
        this.params.add(new Parameter().withParameterKey("APIToken").withParameterValue(pivnetApiToken));
        this.params.add(new Parameter().withParameterKey("KeyName").withParameterValue(keypairName));
        this.params.add(new Parameter().withParameterKey("ClusterNodeInstanceType").withParameterValue(clusterNodeInstanceType));
        this.params.add(new Parameter().withParameterKey("SSHLocation").withParameterValue(sshLocations));

        this.stackName = stackName;
        this.numNodes = numNodes;
        this.availabilityZone = availabilityZone;
        this.pivnetApiToken = pivnetApiToken;
        this.keypairName = keypairName;
        this.clusterNodeInstanceType = clusterNodeInstanceType;
        this.sshLocations = sshLocations;
    }

    public String makeStack(AmazonCloudFormation cloudformationClient) throws Exception {

        log.info("===========================================");
        log.info("Getting Started with AWS CloudFormation");
        log.info("===========================================\n");

        try {
            // Create a stack
            CreateStackRequest createRequest = new CreateStackRequest()
                    .withStackName(stackName)
                    .withTemplateURL(cloudformationUrl)
                    .withCapabilities(Capability.CAPABILITY_IAM)
                    .withParameters(params);

            log.info("Creating a stack called " + createRequest.getStackName() + ".");
            cloudformationClient.createStack(createRequest);

            // Wait for stack to be created
            // Note that you could use SNS notifications on the CreateStack call to track the progress of the stack creation
            String status = waitForCompletion(cloudformationClient);
            log.info("Stack creation completed, the stack " + stackName + " completed with " + status);

            // Show all the stacks for this account along with the resources for each stack
            for (Stack stack : cloudformationClient.describeStacks(new DescribeStacksRequest()).getStacks()) {
                log.info("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");

                DescribeStackResourcesRequest stackResourceRequest = new DescribeStackResourcesRequest()
                        .withStackName(stack.getStackName());

                for (StackResource resource : cloudformationClient.describeStackResources(stackResourceRequest).getStackResources()) {
                    //log.debug();
                    log.debug("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId());
                }
            }

            // Lookup a resource by its logical name
            DescribeStackResourcesRequest logicalNameResourceRequest = new DescribeStackResourcesRequest()
                    .withStackName(stackName)
                    .withLogicalResourceId(logicalResourceName);

            log.debug("Looking up resource name %1$s from stack %2$s\n", logicalNameResourceRequest.getLogicalResourceId(), logicalNameResourceRequest.getStackName());
            for (StackResource resource : cloudformationClient.describeStackResources(logicalNameResourceRequest).getStackResources()) {

                log.debug("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId());
            }

            return status;


        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS CloudFormation, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
        }

        return "CREATE FAILED";
    }

    public String deleteStack(AmazonCloudFormation cloudformationClient) throws Exception {

        log.info("===========================================");
        log.info("Getting Started with AWS CloudFormation");
        log.info("===========================================\n");

        try {
            // Delete the stack
            DeleteStackRequest deleteRequest = new DeleteStackRequest()
                    .withStackName(stackName);

            log.info("Deleting the stack called " + deleteRequest.getStackName() + ".");
            cloudformationClient.deleteStack(deleteRequest);

            // Wait for stack to be deleted
            // Note that you could used SNS notifications on the original CreateStack call to track the progress of the stack deletion
            String status = waitForCompletion(cloudformationClient);
            log.info("Stack deletion completed, the stack " + stackName + " completed with " + status);

            return status;

        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS CloudFormation, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
        }

        return "DELETE FAILED";
    }

    // Wait for a stack to complete transitioning
    // End stack states are:
    //    CREATE_COMPLETE
    //    CREATE_FAILED
    //    DELETE_FAILED
    //    ROLLBACK_FAILED
    // OR the stack no longer exists
    public String waitForCompletion(AmazonCloudFormation cloudformationClient) throws Exception {

        DescribeStacksRequest wait = new DescribeStacksRequest()
                .withStackName(stackName);

        //wait.setStackName(stackName);
        Boolean completed = false;
        String stackStatus = "Unknown";
        String stackReason = "";

        log.debug("Waiting");

        while (!completed) {
            List<Stack> stacks = new ArrayList<Stack>();
            // this is horrendous. quick and dirty and needs to be refactored
            try {
                stacks = cloudformationClient.describeStacks(wait).getStacks();
            } catch (AmazonServiceException ae) {
                if (ae.getMessage().contains(stackName + " does not exist")) {
                    return StackStatus.DELETE_COMPLETE.toString();
                }
                else {
                    throw ae;
                }
            }
            if (stacks.isEmpty())
            {
                completed   = true;
                stackStatus = "NO_SUCH_STACK";
                stackReason = "Stack has been deleted";
            } else {
                for (Stack stack : stacks) {
                    if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
                            stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
                        completed = true;
                        stackStatus = stack.getStackStatus();
                        stackReason = stack.getStackStatusReason();
                        if (stackReason == null) {
                            String output = "";
                            for (Output o : stack.getOutputs()) {
                                output += output.toString();
                            }
                            stackReason = output;
                        }
                    }
                }
            }

            // Show we are waiting
            log.debug(".");

            // Not done yet so sleep for 10 seconds.
            if (!completed) Thread.sleep(10000);
        }

        // Show we are done
        log.info("done\n");

        return stackStatus + " (" + stackReason + ")";
    }

    public static Logger getLog() {
        return log;
    }

    public String getPivnetApiToken() {
        return pivnetApiToken;
    }

    public void setPivnetApiToken(String pivnetApiToken) {
        this.pivnetApiToken = pivnetApiToken;
    }

    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public String getCloudformationUrl() {
        return cloudformationUrl;
    }

    public void setCloudformationUrl(String cloudformationUrl) {
        this.cloudformationUrl = cloudformationUrl;
    }

    public String getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(String numNodes) {
        this.numNodes = numNodes;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getKeypairName() {
        return keypairName;
    }

    public void setKeypairName(String keypairName) {
        this.keypairName = keypairName;
    }

    public String getClusterNodeInstanceType() {
        return clusterNodeInstanceType;
    }

    public void setClusterNodeInstanceType(String clusterNodeInstanceType) {
        this.clusterNodeInstanceType = clusterNodeInstanceType;
    }

    public String getLogicalResourceName() {
        return logicalResourceName;
    }

    public void setLogicalResourceName(String logicalResourceName) {
        this.logicalResourceName = logicalResourceName;
    }

}
