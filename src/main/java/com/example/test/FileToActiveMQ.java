package com.example.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import static java.util.Collections.singletonList;

public class FileToActiveMQ {

    public static void main(String[] args) throws Exception {

        try (CamelContext context = new DefaultCamelContext()) {

            ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();

            context.addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

            context.addRoutes(new MyRouteBuilder());

            context.start();

            try (ProducerTemplate template = context.createProducerTemplate()) {
                for (int i = 0; i < 5; i++) {
                    template.sendBody("file:sendbox", "Test Message: " + i);
                }
            }

            Thread.sleep(3000);

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
            from("file:sendbox?noop=true").convertBodyTo(String.class).to("activemq:queue:my_queue");
        }
    }
}
