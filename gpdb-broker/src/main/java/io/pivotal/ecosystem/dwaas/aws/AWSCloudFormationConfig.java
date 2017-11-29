package io.pivotal.ecosystem.dwaas.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cloudformation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class AWSCloudFormationConfig {


    final String GPDB_CLOUDFORMATION_URL = "https://s3.amazonaws.com/awsmp-fulfillment-cf-templates-prod/a11bec62-52d1-4fce-9a09-ea8e38ddcd3b.5b93ff4f-cc7f-49e2-a568-e6e9881a0fc8.template";

    @Value("${cognition.aws.secret}")
    private String awsSecret = "";

    @Value("${cognition.aws.key}")
    private String awsKey = "";


    @Value("${cognition.aws.region:us-west-2}")
    private String region = "";

    @Bean
    public AWSCredentialsProvider awsCredentials() throws AmazonClientException {

        try {
            return new EnvironmentVariableCredentialsProvider();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials. " +
                            "Please make sure that your credentials environment variables " +
                            "are set. (AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY)",
                    e);
        }
    }

    @Bean
    public AmazonCloudFormation cloudformationClient(AWSCredentialsProvider credentials) {
        AWSCredentialsProvider p = new EnvironmentVariableCredentialsProvider();
        AmazonCloudFormation client = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(p)
                .withRegion(region)
                .build();

        return client;
    }
}
