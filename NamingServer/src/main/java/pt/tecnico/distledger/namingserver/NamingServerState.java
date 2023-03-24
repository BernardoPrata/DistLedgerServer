package pt.tecnico.distledger.namingserver;

import pt.tecnico.distledger.namingserver.domain.ServiceEntry;
import pt.tecnico.distledger.namingserver.exceptions.*;
import pt.tecnico.distledger.namingserver.domain.ServerEntry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class NamingServerState {

    ConcurrentHashMap<String, ServiceEntry> serviceEntries;
    private boolean toDebug;

    public NamingServerState(boolean toDebug) {
        this.serviceEntries  = new ConcurrentHashMap<String, ServiceEntry>();
        this.toDebug = toDebug;

    }

    private void debug(String debugMessage) {
        if (toDebug)
            System.err.println(debugMessage);
    }

    public void addServiceEntry(ServiceEntry serviceEntry) {
        this.serviceEntries.put(serviceEntry.getServiceName(), serviceEntry);
    }

    public ServiceEntry getServiceEntry(String serviceName) {
        return this.serviceEntries.get(serviceName);
    }

    public void removeServiceEntry(String serviceName) {
        this.serviceEntries.remove(serviceName);
    }

    public void register(String serviceName, String qualifier, String serverAddress) throws NotPossibleToRegisterServerException {
        ServerEntry serverEntry = new ServerEntry(serverAddress,qualifier);

        if (!this.serviceEntries.containsKey(serviceName)) {
            // create service and add this serviceEntry to it
            debug("register: Service does not exist, creating new service");
            ServiceEntry serviceEntry = new ServiceEntry(serviceName);
            serviceEntry.addServerEntry(serverEntry);
            this.serviceEntries.put(serviceName,serviceEntry);

        }
        else {
            debug("register: Service exists");
            ServiceEntry serviceEntry = getServiceEntry(serviceName);
            if (serviceEntry.duplicate(serverAddress))
                throw new NotPossibleToRegisterServerException();
            debug("register: Service exists and adding server entry");
            serviceEntry.addServerEntry(serverEntry);

        }
        debug("register: Current state for service " + serviceName + " is " + getServiceEntry(serviceName).getServerEntries().toString());
    }

    public void delete(String serviceName, String serverAddress) throws NotPossibleToRemoveServerException {
        if (!this.serviceEntries.containsKey(serviceName)){
            debug("delete: Service does not exist");
            throw new NotPossibleToRemoveServerException();
        }

        ServiceEntry serviceEntry = this.getServiceEntry(serviceName);
        if (!serviceEntry.removeServerEntry(serverAddress)){
            debug("delete: Server does not exist for given Service");
            throw new NotPossibleToRemoveServerException();
        }

        if (serviceEntry.getServerEntries().size() == 0){
            debug("delete: Service has no more servers, removing service");
            this.removeServiceEntry(serviceName);
        }
        debug("delete: ServerEntries for service " + serviceEntry.getServiceName() +" are " + serviceEntry.getServerEntries().toString());


    }

    public List<String> lookup(String serviceName, String qualifier) {
        ServiceEntry serviceEntry = getServiceEntry(serviceName);
        if (serviceEntry == null) {
            debug("lookup: Service " + serviceName + " not found");
            return new ArrayList<String>();
        }

        List<String> serviceEntriesWithQualifier = serviceEntry.lookup(qualifier);
        debug("lookup: Service " + serviceName + " found in servers: " + serviceEntriesWithQualifier);

        return serviceEntriesWithQualifier;
    }
}
