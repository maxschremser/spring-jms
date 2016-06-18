package com.schremser.spring.jms.server;

import com.schremser.spring.jms.core.JndiConfiguration;
import com.schremser.spring.jms.server.receiver.QueueMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.JmxUtils;

import javax.jms.Destination;
import javax.management.MBeanServer;
import java.io.IOException;

@SpringBootApplication
@Import(JndiConfiguration.class)
@PropertySource("classpath:default.properties")
@ComponentScan("com.schremser.spring.jms.server")
public class JmsServer {
    private final static Logger log = LoggerFactory.getLogger(JmsServer.class);

    @Autowired JndiConfiguration jndi;

    @Bean
    public QueueMessageReceiver queueMessageReceiver() {
        return new QueueMessageReceiver();
    }

    @Bean
    DefaultMessageListenerContainer queueMessageListener() {
        DefaultMessageListenerContainer defaultMessageListenerContainer = new DefaultMessageListenerContainer();
        defaultMessageListenerContainer.setConnectionFactory(jndi.connectionFactoryProxy());
        defaultMessageListenerContainer.setDestination((Destination) jndi.importQueue().getObject());
        defaultMessageListenerContainer.setSessionTransacted(true);
        defaultMessageListenerContainer.setConcurrentConsumers(1);
        defaultMessageListenerContainer.setMaxConcurrentConsumers(7);
        defaultMessageListenerContainer.setMessageListener(queueMessageReceiver());
        defaultMessageListenerContainer.afterPropertiesSet();
        defaultMessageListenerContainer.start();

        return defaultMessageListenerContainer;
    }

    @Bean
    public JmsTemplate jmsSenderTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(jndi.connectionFactoryProxy());
        jmsTemplate.setSessionTransacted(false);
        jmsTemplate.setReceiveTimeout(5000);
        jmsTemplate.setDefaultDestination((Destination) jndi.importQueue().getObject());
        return jmsTemplate;
    }

    @Bean
    MBeanExporter mBeanExporter() {
        MBeanExporter exporter = new MBeanExporter();
        AnnotationJmxAttributeSource attributeSource = new AnnotationJmxAttributeSource();
        exporter.setAutodetect(true);
        exporter.setAssembler(new MetadataMBeanInfoAssembler(attributeSource));
        exporter.setNamingStrategy(new MetadataNamingStrategy(attributeSource));
        MBeanServer server = JmxUtils.locateMBeanServer();
        log.info("JMX Server is " + server.getDefaultDomain());
        exporter.setServer(server);
        return exporter;
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(JmsServer.class);
        log.info("Waiting for requests ...");
    }
}
