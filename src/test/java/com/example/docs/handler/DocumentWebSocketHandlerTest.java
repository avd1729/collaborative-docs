package com.example.docs.handler;

import com.example.docs.ot.model.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentWebSocketHandlerTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebSocketSession connectClient(BlockingQueue<String> messages) throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        return client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                messages.offer(message.getPayload());
            }
        }, "ws://localhost:" + port + "/ws/docs").get();
    }

    @Test
    void testMultipleClientsBroadcast() throws Exception {
        BlockingQueue<String> messagesClient1 = new LinkedBlockingQueue<>();
        BlockingQueue<String> messagesClient2 = new LinkedBlockingQueue<>();

        WebSocketSession client1 = connectClient(messagesClient1);
        WebSocketSession client2 = connectClient(messagesClient2);

        // both clients should receive init
        String init1 = messagesClient1.poll(2, TimeUnit.SECONDS);
        String init2 = messagesClient2.poll(2, TimeUnit.SECONDS);
        assertNotNull(init1, "Client1 should receive init message");
        assertNotNull(init2, "Client2 should receive init message");
        assertTrue(init1.contains("\"type\":\"init\""));
        assertTrue(init2.contains("\"type\":\"init\""));

        // Send an operation from client1
        Operation op = new Operation();
        op.setType(Operation.Type.INSERT);
        op.setPosition(0);
        op.setCharacter("Hello");
        op.setVersion(0);

        String opJson = objectMapper.writeValueAsString(
                new OperationMessage("operation", op, 0)
        );
        client1.sendMessage(new TextMessage(opJson));


        // both clients should receive the broadcasted operation
        String msg1 = messagesClient1.poll(2, TimeUnit.SECONDS);
        String msg2 = messagesClient2.poll(2, TimeUnit.SECONDS);

        assertNotNull(msg1, "Client1 should receive operation message");
        assertNotNull(msg2, "Client2 should receive operation message");

        assertTrue(msg1.contains("\"type\":\"operation\""));
        assertTrue(msg2.contains("\"type\":\"operation\""));
        assertTrue(msg1.contains("Hello"));
        assertTrue(msg2.contains("Hello"));

        client1.close();
        client2.close();
    }

    // helper inner class to construct operation messages
    static class OperationMessage {
        public String type;
        public Operation op;
        public int version;

        public OperationMessage(String type, Operation op, int version) {
            this.type = type;
            this.op = op;
            this.version = version;
        }
    }
}
