package com.schremser.spring.jms.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.connection.TransactionAwareConnectionFactoryProxy;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import java.util.Properties;

@Configuration
@PropertySource("classpath:default.properties")
public class JndiConfiguration {
    private final static Logger log = LoggerFactory.getLogger(JndiConfiguration.class);

    // JMS Connection properties
    @Value("${" + Context.INITIAL_CONTEXT_FACTORY + "}") private String initialContextFactory;
    @Value("${" + Context.PROVIDER_URL + "}") private String providerUrl;
    @Value("${" + Context.SECURITY_PRINCIPAL + "}") private String securityPrincipal;
    @Value("${" + Context.SECURITY_CREDENTIALS + "}") private String securityCredentials;

    // JMS ConnectionFactory
    @Value("${jms.connection.factory}") private String connectionFactory;

    // Queues
    @Value("${jms.queue.import}") private String importQueue;
    @Value("${jms.queue.export}") private String exportQueue;

    @Bean
    public JndiTemplate jndiTemplate() {
        log.info(toString());
        Properties environment = new Properties();
        environment.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        environment.setProperty(Context.PROVIDER_URL, providerUrl);
        environment.setProperty(Context.SECURITY_PRINCIPAL, securityPrincipal);
        environment.setProperty(Context.SECURITY_CREDENTIALS, securityCredentials);

        JndiTemplate jndiTemplate = new JndiTemplate();
        jndiTemplate.setEnvironment(environment);
        return jndiTemplate;
    }

    @Bean
    public JndiObjectFactoryBean connectionFactory() {
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiTemplate(jndiTemplate());
        factoryBean.setJndiName(connectionFactory);
        return factoryBean;
    }

    @Bean
    public JndiObjectFactoryBean importQueue() {
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiTemplate(jndiTemplate());
        factoryBean.setJndiName(importQueue);
        return factoryBean;
    }

    @Bean
    public JndiObjectFactoryBean exportQueue() {
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiTemplate(jndiTemplate());
        factoryBean.setJndiName(exportQueue);
        return factoryBean;
    }

    @Bean
    public TransactionAwareConnectionFactoryProxy connectionFactoryProxy() {
        return new TransactionAwareConnectionFactoryProxy((ConnectionFactory) connectionFactory().getObject());
    }

    @Override
    public String toString() {
        return "JndiConfiguration{jms.queue.import=" + importQueue +
                ", jms.queue.export=" + exportQueue +
                ", java.naming.factory.initial=" + initialContextFactory +
                ", java.naming.provider.url=" + providerUrl +
                ", java.naming.security.principal=" + securityPrincipal +
                ", jms.connection.factory=" + connectionFactory +
                "}";
    }

}
