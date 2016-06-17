package com.schremser.spring.jms.client;

import com.schremser.spring.jms.core.JndiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
@Import(JndiConfiguration.class)
@PropertySource("classpath:default.properties")
public class JmsClient {
    private final static Logger log = LoggerFactory.getLogger(JmsClient.class);

    @Autowired JndiConfiguration jndi;
    @Autowired JmsTemplate jmsTemplate;

    @Bean
    public JmsTemplate jmsSenderTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(jndi.connectionFactoryProxy());
        jmsTemplate.setSessionTransacted(false);
        jmsTemplate.setReceiveTimeout(5000);
        jmsTemplate.setDefaultDestination((Destination) jndi.importQueue().getObject());
        return jmsTemplate;
    }

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

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(JmsClient.class);
        JmsClient client = context.getBean(JmsClient.class);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("Your message: ");
            String message = reader.readLine();
            client.postMessage(message);
        }
    }

}
