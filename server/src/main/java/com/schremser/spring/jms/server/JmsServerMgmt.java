package com.schremser.spring.jms.server;

import com.schremser.spring.jms.core.JndiConfiguration;
import com.schremser.spring.jms.server.receiver.QueueMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Component
@ManagedResource(description = "Jms Server Bean")
public class JmsServerMgmt {
    private final static Logger log = LoggerFactory.getLogger(JmsServerMgmt.class);

    @Autowired JmsServer server;
    @Autowired JndiConfiguration jndi;
    @Autowired JmsTemplate jmsTemplate;
    @Autowired QueueMessageReceiver queueMessageReceiver;

    @ManagedAttribute(description = "JNDI Configuration")
    public String getJndiConfiguration() {
        return jndi.toString();
    }


    @ManagedAttribute(description = "Processed Messages")
    public int getProcessed() {
        return queueMessageReceiver.getProcessed();
    }

    @ManagedOperation(description = "Post a Message")
    public void postMessage(String message) {
        try {
            jmsTemplate.send(wrapMessage(message));
        } catch (Exception e) {
            log.error("Could not send message to queue. {}", e);
        }
    }

    private MessageCreator wrapMessage(final String message) {
        return new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        };
    }

}
