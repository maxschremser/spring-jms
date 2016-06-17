package com.schremser.spring.jms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(value = "Jms Server", objectName = "mbeans:name=Jms Server Bean")
public class JmsServerMgmt {
    @Autowired JmsServer server;

    @ManagedAttribute(description = "JNDI Configuration")
    public String jndiConfiguration() {
        return server.jndi.toString();
    }
}
