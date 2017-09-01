package io.pivotal.ecosystem.dwaas;

import com.vmware.vcenter.VM;
import io.pivotal.ecosystem.dwaas.azure.AzureDeployClientConfig;
import io.pivotal.ecosystem.dwaas.azure.AzureDeployClientProperties;
import io.pivotal.ecosystem.dwaas.vmware.vSphereClient;
import io.pivotal.ecosystem.dwaas.vmware.vSphereClientConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes={vSphereClientConfig.class})
@TestPropertySource(locations="classpath:test.properties")
public class vSphereDeployTest {

    private vSphereClient client;

    /*
     * Note, these tests do not currently function.
     * refer to open issue : https://github.com/vmware/vsphere-automation-sdk-java/issues/12
     */

    //@Before
    public void setup() {
        /*
        Properties systemProps = System.getProperties();
        systemProps.put("javax.net.ssl.keyStorePassword","changeit");
        systemProps.put("javax.net.ssl.keyStore","/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre/lib/security/cacerts");

        // wget -o vspherecerts.zip https://vcsa-01.haas-53.pez.pivotal.io/certs/download
        // unzip vspherecerts.zip
        // sudo keytool -import -alias pezHaasVSphere -file certs/*.0 -keystore ${JAVA_HOME}/jre/lib/security/truststore.ts

        systemProps.put("javax.net.ssl.trustStore", "/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre/lib/security/truststore.ts");
        systemProps.put("javax.net.ssl.trustStorePassword","changeit");
        System.setProperties(systemProps);
        */

        client = new vSphereClient();
    }

    //@Test
    public void testCreatVm() {
        String resultingVm = client.makeVm();

        Assert.assertNotEquals("", resultingVm);
    }
}
