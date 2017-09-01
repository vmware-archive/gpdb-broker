package io.pivotal.ecosystem.dwaas.vmware;

import com.vmware.cis.Session;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.cis.authn.ProtocolFactory;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.ExecutionContext;
import com.vmware.vapi.protocol.ProtocolConnection;
import com.vmware.vapi.security.SessionSecurityContext;
import com.vmware.vcenter.VMTypes.FilterSpec.Builder;
import com.vmware.vcenter.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class vSphereClientConfig {

    @Value("${cognition.vsphere.host}")
    String server;

    @Value("${cognition.truststorePath}")
    String trustStorePath;

    @Value("${cognition.truststorePassword}")
    String trustStorePassword;

    @Value("${cognition.vsphere.username}")
    String username;

    @Value("${cognition.vsphere.password}")
    String password;

    @Value("${cognition.vsphere.datacenter:Datacenter}")
    String datacenterName;

    @Value("${cognition.vsphere.cluster:Cluster}")
    String clusterName;

    @Value("${cognition.vsphere.datastore:LUN01}")
    String datastoreName;

    @Value("${cognition.vsphere.vmfolder:cognition-vms}")
    String vmFolderName;

    private Session sessionSvc;
    private StubFactory stubFactory;

    public static final String VAPI_PATH = "/api";

    private static final Logger log = LoggerFactory.getLogger(vSphereClientConfig.class);

    @Bean
    public StubFactory stubFactory(KeyStore trustStore) throws Exception {
        return createApiStubFactory(server, trustStore);
    }

    // wget -O vspherecerts.zip https://vcsa-01.haas-53.pez.pivotal.io/certs/download
    // unzip vspherecerts.zip
    // sudo keytool -import -alias pezHaasVSphere -file certs/*.0 -keystore ${JAVA_HOME}/jre/lib/security/truststore.ts
    @Bean
    public KeyStore trustStore()
            throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream truststoreStream = new FileInputStream(trustStorePath);
        try {
            trustStore.load(truststoreStream, trustStorePassword.toCharArray());
            log.info("Successfully created TrustStore bean");
            return trustStore;
        } finally {
            truststoreStream.close();
        }
    }

    @Bean
    public StubConfiguration sessionStubConfig(KeyStore trustStore) throws Exception {
        if(this.sessionSvc != null) {
            throw new Exception("Session already created");
        }

        this.stubFactory = createApiStubFactory(server, trustStore);

        // Create a security context for username/password authentication
        ExecutionContext.SecurityContext securityContext =
                SecurityContextFactory.createUserPassSecurityContext(
                        username, password.toCharArray());

        // Create a stub configuration with username/password security context
        StubConfiguration stubConfig = new StubConfiguration(securityContext);

        // Create a session stub using the stub configuration.
        Session session =
                this.stubFactory.createStub(Session.class, stubConfig);

        // Login and create a session
        char[] sessionId = session.create();

        // Initialize a session security context from the generated session id
        SessionSecurityContext sessionSecurityContext =
                new SessionSecurityContext(sessionId);

        // Update the stub configuration to use the session id
        stubConfig.setSecurityContext(sessionSecurityContext);

        /*
         * Create a stub for the session service using the authenticated
         * session
         */
        this.sessionSvc =
                this.stubFactory.createStub(Session.class, stubConfig);

        log.info("Successfully created stubConfig bean");
        return stubConfig;
    }

    @Bean
    public VM vmService(StubFactory stubFactory, StubConfiguration sessionStubConfig) {
        return stubFactory.createStub(VM.class, sessionStubConfig);
    }

    @Bean Cluster clusterService(StubFactory stubFactory, StubConfiguration sessionStubConfig) {
        Cluster clusterService = stubFactory.createStub(Cluster.class,
                sessionStubConfig);

        log.info("Successfully created clusterService bean");
        //log.debug(clusterService.get("Cluster").toString());
        return clusterService;
    }

    /**
     * Returns the identifier of a cluster
     *
     * Note: The method assumes that there is only one cluster and datacenter
     * with the mentioned names.
     *
     * @param clusterService
     * @param datacenterId
     * @return identifier of a cluster
     */
    public String getClusterId(Cluster clusterService, String datacenterId) {

        Set<String> clusters = Collections.singleton(clusterName);

        ClusterTypes.FilterSpec.Builder clusterFilterBuilder =
                new ClusterTypes.FilterSpec.Builder().setNames(clusters);

        if (null != datacenterName) {
            // Get the datacenter
            Set<String> datacenters = Collections.singleton(datacenterId);

            clusterFilterBuilder.setDatacenters(datacenters);
        }

        List<ClusterTypes.Summary> clusterSummaries =
                clusterService.list(clusterFilterBuilder.build());

        assert clusterSummaries.size() > 0 : "Cluster " + clusterName
                + "not found in datacenter: "
                + datacenterName;

        return clusterSummaries.get(0).getCluster();
    }

    @Bean
    public Datacenter datacenterService(StubFactory stubFactory, StubConfiguration sessionStubConfig) {
        Datacenter datacenterService = stubFactory.createStub(Datacenter.class,
                sessionStubConfig);

        log.info("Successfully created datacenterService bean");
        return datacenterService;
    }

    /**
     * Returns the identifier of a datacenter
     *
     * Note: The method assumes only one datacenter with the
     * mentioned name.
     * @param datacenterService
     * @return identifier of a datacenter
     */
    public String getDatacenterId(Datacenter datacenterService) {

        Set<String> datacenterNames = Collections.singleton(datacenterName);

        log.info(datacenterNames.toString());

        DatacenterTypes.FilterSpec dcFilterSpec =
                new DatacenterTypes.FilterSpec.Builder().setNames(
                        datacenterNames).build();

        log.info(datacenterService.toString());

        List<DatacenterTypes.Summary> dcSummaries = datacenterService.list(
                dcFilterSpec);

        assert dcSummaries.size() > 0 : "Datacenter with name " + datacenterName
                + " not found.";

        log.info(dcSummaries.get(0).getDatacenter());

        return dcSummaries.get(0).getDatacenter();
    }

    public static String getDatacenter(
            StubFactory stubFactory, StubConfiguration sessionStubConfig,
            String datacenterName) {

        Datacenter datacenterService = stubFactory.createStub(Datacenter.class,
                sessionStubConfig);

        Set<String> datacenterNames = Collections.singleton(datacenterName);
        DatacenterTypes.FilterSpec dcFilterSpec =
                new DatacenterTypes.FilterSpec.Builder().setNames(
                        datacenterNames).build();
        List<DatacenterTypes.Summary> dcSummaries = datacenterService.list(
                dcFilterSpec);

        assert dcSummaries.size() > 0 : "Datacenter with name " + datacenterName
                + " not found.";

        return dcSummaries.get(0).getDatacenter();
    }

    @Bean
    public Datastore datastoreService(StubFactory stubFactory, StubConfiguration sessionStubConfig) {
        Datastore datastoreService = stubFactory.createStub(Datastore.class, sessionStubConfig);

        log.debug("Successfully created datastoreService bean");
        return datastoreService;
    }

    /**
     * Returns the identifier of a datastore
     *
     * Note: The method assumes that there is only one datastore and datacenter
     * with the mentioned names.
     *
     * @param datastoreService
     * @param datacenterId
     * @return identifier of a datastore
     */
    public String getDatastoreId(Datastore datastoreService, String datacenterId) {

        // Get the datacenter
        Set<String> datacenters = Collections.singleton(datacenterId);

        // Get the datastore
        Set<String> datastores = Collections.singleton(datastoreName);

        DatastoreTypes.FilterSpec datastoreFilterSpec =
                new DatastoreTypes.FilterSpec.Builder().setNames(datastores)
                        .setDatacenters(datacenters)
                        .build();

        List<DatastoreTypes.Summary> datastoreSummaries = datastoreService.list(
                datastoreFilterSpec);

        assert datastoreSummaries.size() > 0 : "Datastore " + datastoreName
                + "not found in datacenter : "
                + datacenterName;
        return datastoreSummaries.get(0).getDatastore();
    }

    @Bean
    public Folder folderService(StubFactory stubFactory, StubConfiguration sessionStubConfig) {
        Folder folderService = stubFactory.createStub(Folder.class,
                sessionStubConfig);

        log.debug("Successfully created folderService bean");
        return folderService;
    }

    /**
     * Returns the identifier of a folder
     *
     * Note: The method assumes that there is only one folder and datacenter
     * with the specified names.
     *
     * @param folderService name of the datacenter
     * @param datacenterId name of the datacenter
     * @param folderName name of the folder
     * @return identifier of a folder
     */
    public static String getFolderId(Folder folderService, String datacenterId, String folderName) {

        // Get the folder
        Set<String> vmFolders = Collections.singleton(folderName);

        FolderTypes.FilterSpec.Builder vmFolderFilterSpecBuilder =
                new FolderTypes.FilterSpec.Builder().setNames(vmFolders);

        if (datacenterId != null) {
            // Get the datacenter
            Set<String> datacenters = Collections.singleton(datacenterId);

            vmFolderFilterSpecBuilder.setDatacenters(datacenters);
        }

        List<FolderTypes.Summary> folderSummaries = folderService.list(
                vmFolderFilterSpecBuilder.build());

        assert folderSummaries.size() > 0 : "Folder " + folderName
                + "not found in datacenter: "
                + datacenterId;

        return folderSummaries.get(0).getFolder();
    }

    @Bean
    public Network networkService(StubFactory stubFactory, StubConfiguration sessionStubConfig) {
        Network networkService = stubFactory.createStub(Network.class,
                sessionStubConfig);

        log.debug("Successfully created networkService bean");
        return networkService;
    }

    /**
     * Returns a VM placement spec for a cluster. Ensures that the
     * cluster, resource pool, vm folder and datastore are all in the same
     * datacenter which is specified.
     *
     * Note: The method assumes that there is only one of each resource type
     * (i.e. datacenter, resource pool, cluster, folder, datastore) with the
     * mentioned names.
     */
    @Bean
    public VMTypes.PlacementSpec vmPlacementSpec(StubFactory stubFactory, StubConfiguration sessionStubConfig,
                                                 Cluster clusterService,
                                                 Datacenter datacenterService,
                                                 Datastore datastoreService,
                                                 Folder folderService) {
        /*
         *  Create the vm placement spec with the datastore, resource pool,
         *  cluster and vm folder
         */
        VMTypes.PlacementSpec vmPlacementSpec = new VMTypes.PlacementSpec();

        String datacenterId = getDatacenter(stubFactory, sessionStubConfig, datacenterName);

        String datastoreId = getDatastoreId(datastoreService, datacenterId);

        vmPlacementSpec.setDatastore(datastoreId);
        vmPlacementSpec.setCluster(getClusterId(clusterService, clusterName));
        vmPlacementSpec.setFolder(getFolderId(folderService, datastoreId, vmFolderName));

        log.debug("Successfully created vmPlacementSpec bean");
        return vmPlacementSpec;
    }

    /*
     * Connects to the server using https protocol and returns the factory
     * instance that can be used for creating the client side stubs.
     *
     * @param server hostname or ip address of the server
     * @return factory for the client side stubs
     */
    private StubFactory createApiStubFactory(String server,
                                             KeyStore trustStore)
            throws Exception {
        // Create a https connection with the vapi url
        ProtocolFactory pf = new ProtocolFactory();
        String apiUrl = "https://" + server + VAPI_PATH;

        // Get a connection to the vapi url
        ProtocolConnection connection = pf.getConnection("http",
                apiUrl,
                trustStore);

        // Initialize the stub factory with the api provider
        ApiProvider provider = connection.getApiProvider();
        StubFactory stubFactory = new StubFactory(provider);
        return stubFactory;
    }
}
