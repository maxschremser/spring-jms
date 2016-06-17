package com.schremser.spring.jms.server;

import com.schremser.spring.jms.core.JndiConfiguration;
import com.schremser.spring.jms.server.receiver.QueueMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.jmx.support.MBeanServerFactoryBean;

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
    QueueMessageReceiver queueMessageReceiver;

    @Bean
    public QueueMessageReceiver queueMessageReceiver() {
        if (queueMessageReceiver == null)
            queueMessageReceiver = new QueueMessageReceiver();
        return queueMessageReceiver;
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

    @Override
    public String toString() {
        return jndi.toString();
    }

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(JmsServer.class);
        log.info("Waiting for requests ...");
        AnnotationJmxAttributeSource ajas = new AnnotationJmxAttributeSource();
        MBeanExporter exporter = new MBeanExporter();
        exporter.setAutodetect(true);
        exporter.setAssembler(new MetadataMBeanInfoAssembler(ajas));
        exporter.setNamingStrategy(new MetadataNamingStrategy(ajas));
        MBeanServer server = JmxUtils.locateMBeanServer();
        log.info("JMX Server is " + server.getDefaultDomain());
        exporter.setServer(server);
    }
}
