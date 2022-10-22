package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/* ********************************************************************************************************************************************
 * https://rabbitmq.github.io/rabbitmq-stream-java-client/stable/htmlsingle/#outbound-message-deduplication
 * 
 * RabbitMQ Stream provides publisher confirms to avoid losing messages: 
 *   once the broker has persisted a message it sends a confirmation for this message. 
 * But this can lead to duplicate messages: 
 *   imagine the connection closes because of a network glitch after the message has been persisted but before the confirmation reaches the producer. 
 * Once reconnected, the producer will retry to send the same message, as it never received the confirmation. 
 * So the message will be persisted twice.
 * 
 * Luckily RabbitMQ Stream can detect and filter out duplicated messages, based on 2 client-side elements: 
 * the producer name and the message publishing ID.
 */
class MessageDeduplicationTest {

    @Test
    void test() {
        fail("Not yet implemented");
    }

}
