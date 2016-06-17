package com.schremser.spring.jms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(description = "Jms Server Bean")
public class JmsServerMgmt {
    @Autowired JmsServer server;
    String jndiConfiguration;


    @ManagedAttribute(description = "JNDI Configuration")
    public String getJndiConfiguration() {
        if (jndiConfiguration == null)
            jndiConfiguration = server.jndi.toString();
        return jndiConfiguration;
    }

    @ManagedAttribute(description = "Processed Messages")
    public int getProcessed() {
        return server.queueMessageReceiver().getProcessed();
    }
}
