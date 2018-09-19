package com.schremser.spring.jms.server.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class QueueMessageReceiver implements MessageListener {
    private final static Logger log = LoggerFactory.getLogger(QueueMessageReceiver.class);
    @Value("${jms.queue.request}") private String requestQueue;
    int processed;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage;
        try {
            textMessage = (TextMessage) message;
            log.info("Queue{\"queue\": \"{}\", \"content\": \"{}\"}", requestQueue, textMessage.getText());
            processed++;
        } catch (Exception e) {
            log.error("Message could not be read. {0}", new Object[]{e});
        }
    }

    public int getProcessed() {
        return processed;
    }
}
