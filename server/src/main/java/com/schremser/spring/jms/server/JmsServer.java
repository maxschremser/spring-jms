package com.schremser.spring.jms.server;

import com.schremser.spring.jms.core.JndiConfiguration;
import com.schremser.spring.jms.server.receiver.QueueMessageReceiver;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
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
public class JmsServer implements Runnable, Lifecycle {
    private final static Logger log = LoggerFactory.getLogger(JmsServer.class);
    boolean running;
    ConfigurableApplicationContext context;
    @Autowired StopWatch stopWatch;
    @Autowired JndiConfiguration jndi;

    public JmsServer() {
    }

    public JmsServer(ConfigurableApplicationContext context) {
        this.context = context;
        this.stopWatch = context.getBean(StopWatch.class);
        start();
    }

    @Bean
    public StopWatch stopWatch() {
        return new StopWatch();
    }

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
        JmsServer jmsServer = new JmsServer(SpringApplication.run(JmsServer.class));
        log.info("Waiting for requests ...");
        new Thread(jmsServer).run();
    }

    @Override
    public void start() {
        stopWatch.reset();
        stopWatch.start();
        running = true;
    }

    @Override
    public void stop() {
        stopWatch.stop();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        while (true) {
            System.out.printf("\rJmsServer is running since: %s", DurationFormatUtils.formatDuration(stopWatch.getTime(), "H:mm:ss"));
            try { Thread.sleep(1000); } catch (Exception e) {}
        }
    }
}
