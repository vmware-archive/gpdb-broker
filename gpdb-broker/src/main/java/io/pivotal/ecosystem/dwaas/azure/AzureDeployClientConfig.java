package io.pivotal.ecosystem.dwaas.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import org.springframework.context.annotation.Profile;
@Profile("azure")
@Configuration
public class AzureDeployClientConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureDeployClientConfig.class);

    @Autowired
    private AzureDeployClientProperties properties;

    private ServiceClientCredentials credentials;

    @Bean
    public ServiceClientCredentials azureCredentials() {
        // from : https://github.com/Azure/azure-sdk-for-java/blob/master/AUTH.md#using-applicationtokencredentials
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                properties.getClientId(), properties.getTenantId(), properties.getKey(), AzureEnvironment.AZURE);

        this.credentials = credentials;

        log.debug("Got Azure credentials.");

        return this.credentials;
    }

    @Bean
    public AzureDeployClient azureClient(ServiceClientCredentials credentials) {
        try {
            return new AzureDeployClient(properties.getBaseUrl(), credentials, new OkHttpClient.Builder(), new Retrofit.Builder());
        } catch (com.microsoft.azure.CloudException ae) {
            log.error(ae.toString());
            return null;
        }

    }

}
