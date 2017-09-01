package io.pivotal.ecosystem.dwaas.azure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//@ConfigurationProperties("azure")
@Component
public class AzureDeployClientProperties {
    @Value("${cognition.azure.baseUrl:https://management.azure.com}")
    private String baseUrl;

    /*
        # In the Azure cloud shell:

        $ az ad sp create-for-rbac --sdk-auth
        {
          "clientId": "------",
          "clientSecret": "------",
          "subscriptionId": "------",
          "tenantId": "------",
          "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
          "resourceManagerEndpointUrl": "https://management.azure.com/",
          "activeDirectoryGraphResourceId": "https://graph.windows.net/",
          "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
          "galleryEndpointUrl": "https://gallery.azure.com/",
          "managementEndpointUrl": "https://management.core.windows.net/"
        }
    */

    @Value("${cognition.azure.clientId}")
    private String clientId;

    @Value("${cognition.azure.tenantId}")
    private String tenantId;

    @Value("${cognition.azure.clientSecret}")
    private String key;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
