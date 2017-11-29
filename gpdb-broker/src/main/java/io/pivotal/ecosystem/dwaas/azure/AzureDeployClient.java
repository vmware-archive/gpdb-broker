package io.pivotal.ecosystem.dwaas.azure;

import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import org.springframework.context.annotation.Profile;
@Profile("azure")
@Component
public class AzureDeployClient extends ServiceClient {

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service base uri
     * @param credentials the credentials
     * @param clientBuilder the http client builder
     * @param restBuilder the retrofit rest client builder
     */
    protected AzureDeployClient(String baseUrl, ServiceClientCredentials credentials, OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        this(new RestClient.Builder(clientBuilder, restBuilder)
                .withBaseUrl(baseUrl)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .build());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param restClient the REST client
     */
    protected AzureDeployClient(RestClient restClient) {
        super(restClient);
    }
}

