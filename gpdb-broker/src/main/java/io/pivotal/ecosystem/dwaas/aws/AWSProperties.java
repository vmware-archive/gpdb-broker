package io.pivotal.ecosystem.dwaas.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AWSProperties {

    @Value("${cognition.aws.accessKey}")
    private String accessKey;

    @Value("${cognition.aws.secretKey}")
    private String secretKey;

    @Value("${cognition.aws.templateurl}")
    private String templateurl;

    @Value("${cognition.aws.region}")
    private String region;

    @Value("${cognition.aws.availabilityzone}")
    private String availabilityZone;

    @Value("${cognition.aws.keypairName}")
    private String keypairName;


    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTemplateurl() {
        return templateurl;
    }

    public void setTemplateurl(String templateurl) {
        this.templateurl = templateurl;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
}
