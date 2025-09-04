package com.example.docs.handler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentWebSocketHandlerTest {

    @LocalServerPort
    private int port;

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

        // wait for connection
        Thread.sleep(200);

        // Send message from client1
        client1.sendMessage(new TextMessage("Hello from client1"));

        // Both should receive echo
        String msg1 = messagesClient1.poll(2, TimeUnit.SECONDS);
        String msg2 = messagesClient2.poll(2, TimeUnit.SECONDS);

        assertNotNull(msg1, "Client1 should receive a message");
        assertNotNull(msg2, "Client2 should receive a message");
        assertTrue(msg1.contains("Hello from client1"));
        assertTrue(msg2.contains("Hello from client1"));

        client1.close();
        client2.close();
    }
}