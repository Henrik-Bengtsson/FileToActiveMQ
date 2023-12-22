package com.example.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.File;
import java.nio.file.Files;

import static java.util.Collections.singletonList;

public class FileToActiveMQ {

    public static void main(String[] args) throws Exception {

        try (CamelContext context = new DefaultCamelContext()) {

            ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();

            context.addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

            context.addRoutes(new MyRouteBuilder());

            context.start();

            try (ProducerTemplate template = context.createProducerTemplate()) {
                File[] files = new File("./sendbox").listFiles();
                if (files != null) {
                    for (File file : files) {
                        String fileContent = Files.readString(file.toPath());
                        template.sendBody("activemq:queue:my_queue", fileContent);
                    }
                }
            }

            Thread.sleep(1000);
        }
    }

    static ActiveMQConnectionFactory createActiveMQConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        connectionFactory.setTrustAllPackages(false);
        connectionFactory.setTrustedPackages(singletonList("com.example.test"));
        return connectionFactory;
    }

    static class MyRouteBuilder extends RouteBuilder {

        @Override
        public void configure() {
            from("file:sendbox").convertBodyTo(String.class).to("activemq:queue:my_queue");
        }
    }
}
