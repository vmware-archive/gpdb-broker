package io.pivotal.ecosystem.dwaas.vmware;

import com.vmware.vcenter.Network;
import com.vmware.vcenter.NetworkTypes;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.vm.GuestOS;
import com.vmware.vcenter.vm.hardware.DiskTypes;
import com.vmware.vcenter.vm.hardware.EthernetTypes;
import com.vmware.vcenter.vm.hardware.ScsiAddressSpec;
import com.vmware.vcenter.vm.hardware.boot.DeviceTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

public class vSphereClient {

    private String vmFolderName;

    private String vmName;

    private String datastoreName;

    private String datacenterName;

    private String clusterName;

    @Value("${cognition.vsphere.portgroup:VM Network}")
    private String standardPortgroupName;

    private String basicVMId;

    private static final String BASIC_VM_NAME = "java-vsphere-test";

    private GuestOS vmGuestOS = GuestOS.RHEL_7_64;

    @Autowired
    private VM vmService;

    @Autowired
    private VMTypes.PlacementSpec vmPlacementSpec;

    @Autowired
    private Network networkService;

    public vSphereClient() {

    }

    public String makeVm() {

        // Get a standard network backing
        String standardNetworkBacking = getStandardNetworkBackingId(this.networkService,
                this.datacenterName, this.standardPortgroupName);

        // Create the scsi disk as a boot disk
        DiskTypes.CreateSpec bootDiskCreateSpec =
                new DiskTypes.CreateSpec.Builder().setType(
                    DiskTypes.HostBusAdapterType.SCSI)
                    .setScsi(new ScsiAddressSpec.Builder(0l).setUnit(0l)
                        .build())
                    .setNewVmdk(new DiskTypes.VmdkCreateSpec())
                    .build();

        // Create a data disk
        DiskTypes.CreateSpec dataDiskCreateSpec =
                new DiskTypes.CreateSpec.Builder().setNewVmdk(
                    new DiskTypes.VmdkCreateSpec()).build();

        List<DiskTypes.CreateSpec> disks = Arrays.asList(bootDiskCreateSpec,
            dataDiskCreateSpec);

        // Create a nic with standard network backing
        EthernetTypes.BackingSpec nicBackingSpec =
                new EthernetTypes.BackingSpec.Builder(
                    EthernetTypes.BackingType.STANDARD_PORTGROUP).setNetwork(
                        standardNetworkBacking).build();

        EthernetTypes.CreateSpec nicCreateSpec =
                new EthernetTypes.CreateSpec.Builder().setStartConnected(true)
                    .setBacking(nicBackingSpec)
                    .build();

        List<EthernetTypes.CreateSpec> nics = Collections.singletonList(
            nicCreateSpec);

        // Specify the boot order
        List<DeviceTypes.EntryCreateSpec> bootDevices = Arrays.asList(
                new DeviceTypes.EntryCreateSpec.Builder(DeviceTypes.Type.CDROM)
                        .build(),
                new DeviceTypes.EntryCreateSpec.Builder(DeviceTypes.Type.DISK)
                        .build());

        VMTypes.CreateSpec vmCreateSpec = new VMTypes.CreateSpec.Builder(this.vmGuestOS)
                .setName(this.vmName)
                .setBootDevices(bootDevices)
                .setPlacement(vmPlacementSpec)
                .setNics(nics)
                .setDisks(disks)
                .build();

        vmService.create(vmCreateSpec);

        return vmService.get(this.vmName).toString();
    }

    /**
     * Returns the identifier of a standard network.
     *
     * Note: The method assumes that there is only one standard portgroup
     * and datacenter with the mentioned names.
     *
     * @param datacenterId name of the datacenter on which the network exists
     * @param stdPortgroupName name of the standard portgroup
     * @return identifier of a standard network.
     */
    public static String getStandardNetworkBackingId(Network networkService,
                                                     String datacenterId, String stdPortgroupName) {

        // Get the datacenter id
        Set<String> datacenters = Collections.singleton(datacenterId);

        // Get the network id
        Set<String> networkNames = Collections.singleton(stdPortgroupName);

        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
                .singletonList(NetworkTypes.Type.STANDARD_PORTGROUP));

        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder().setDatacenters(
                        datacenters)
                        .setNames(networkNames)
                        .setTypes(networkTypes)
                        .build();

        List<NetworkTypes.Summary> networkSummaries = networkService.list(
                networkFilterSpec);

        assert networkSummaries.size() > 0 : "Standard Portgroup with name "
                + stdPortgroupName
                + " not found in datacenter "
                + datacenterId;

        return networkSummaries.get(0).getNetwork();
    }

    /**
     * Returns the identifier of a distributed network
     *
     * Note: The method assumes that there is only one distributed portgroup
     * and datacenter with the mentioned names.
     *
     * @param datacenterId name of the datacenter on which the distributed
     * network exists
     * @param vdPortgroupName name of the distributed portgroup
     * @return identifier of the distributed network
     */
    public static String getDistributedNetworkBackingId(Network networkService,
                                                        String datacenterId, String vdPortgroupName) {

        // Get the datacenter id
        Set<String> datacenters = Collections.singleton(datacenterId);

        // Get the network id
        Set<String> networkNames = Collections.singleton(vdPortgroupName);

        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
                .singletonList(NetworkTypes.Type.DISTRIBUTED_PORTGROUP));

        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder().setDatacenters(datacenters)
                        .setNames(networkNames)
                        .setTypes(networkTypes)
                        .build();

        List<NetworkTypes.Summary> networkSummaries = networkService.list(
                networkFilterSpec);

        assert networkSummaries.size() > 0 : "Distributed Portgroup with name "
                + vdPortgroupName
                + " not found in datacenter "
                + datacenterId;

        return networkSummaries.get(0).getNetwork();
    }
}
