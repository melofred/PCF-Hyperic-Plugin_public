
package com.pivotal.cloudfoundry.monitoring.hyperic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.AutoServerDetector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerDetector;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.ServiceResource;
import org.hyperic.util.config.ConfigResponse;

import com.pivotal.cloudfoundry.monitoring.hyperic.services.CFService;


public class Discovery extends ServerDetector implements AutoServerDetector {

    private static Log log = LogFactory.getLog(Discovery.class);

    public List getServerResources(ConfigResponse platformConfig) throws PluginException {
        
       	
    	log.info("[getServerResources] platfromConfig=" + platformConfig);
    	    	
        
        List servers = new ArrayList();
        for (int i = 1; i <= 2; i++) {
            ServerResource server = createServerResource("Remote PCF");
            server.setName(server.getName());
            ConfigResponse productConfig = new ConfigResponse();
            productConfig.setValue("process.ID", i);
            setProductConfig(server, productConfig);
            setMeasurementConfig(server, new ConfigResponse());
            servers.add(server);
        }
        return servers;
    }

    @Override
    protected List discoverServices(ConfigResponse serverConfig) throws PluginException {
        
    	log.info("[discoverServices] serverConfig=" + serverConfig);
        
    	String jmxURL = serverConfig.getValue("JMX URL");
    	String username = serverConfig.getValue("Username");
    	String password = serverConfig.getValue("Password");
    	
    	log.info("JMX URL=" + jmxURL);
    	log.info("username=" + username);
    	log.info("password=" + password);
        
    	//jmxURL="service:jmx:rmi:///jndi/rmi://10.103.44.105:44444/jmxrmi";
    	//username="admin";
    	//password="password";
    	
    	if (jmxURL==null || username==null || password==null){
    		log.warn("JMX conn parameters are null");
    		serverConfig.setValue("Availability", false);
			return new ArrayList();
    	}
    	
    	JMXClient client = JMXClient.getInstance();
    	try {
			client.connect(jmxURL, username, password);
			serverConfig.setValue("Availability", true);
	    	log.info("[discoverServices] Connected to JMX");

		} catch (Exception e) {
			e.printStackTrace();
			log.info("[discoverServices] "+e.getMessage());
			log.info("[discoverServices] EXCEPTION CONNECTING TO JMX. WILL SET AVAILABILITY TO FALSE");

			serverConfig.setValue("Availability", false);
			return new ArrayList();
		}
    	
    	
    	
    	// HERE USE JMX CONN TO PULL JMX SERVICES AVAILABLE (DEA, HEALTH MANAGER, HA PROXY, ROUTER, ...) AND SHOW THEM
    	
    	
    	List<ServiceResource> services = new ArrayList<ServiceResource>();
    	
    	Iterator<CFService> cfServices = client.getServices().iterator();
    	while (cfServices.hasNext()){    		
    		CFService cfService = cfServices.next();
          
    		ServiceResource service = createServiceResource(cfService.getClass().getSimpleName());
            service.setName(cfService.getClass().getSimpleName() + " " + cfService.getIndex());
            ConfigResponse productConfig = new ConfigResponse();
            productConfig.setValue("service.ID", cfService.getIndex());
            productConfig.setValue("service.IP", cfService.getIp());
            setProductConfig(service, productConfig);
            setMeasurementConfig(service, new ConfigResponse());
            services.add(service);    		
    	}
    	
    	log.info("Returning services #: "+services.size());

    	return services;
    	
    }
}
