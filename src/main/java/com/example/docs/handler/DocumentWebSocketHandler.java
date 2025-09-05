package com.example.docs.handler;

import com.example.docs.ot.engine.OTEngine;
import com.example.docs.ot.model.Operation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DocumentWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final OTEngine otEngine = new OTEngine();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.add(session);
        System.out.println("New client connected: " + session.getId());

        // Send current document + version to new client
        InitMessage init = new InitMessage("init", otEngine.getDocument(), otEngine.getVersion());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(init)));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Received: " + message.getPayload());

        JsonNode node = objectMapper.readTree(message.getPayload());
        String type = node.get("type").asText();

        if("operation".equals(type)){
            Operation op = objectMapper.treeToValue(node.get("op"), Operation.class);
            otEngine.applyOperation(op);

            OperationMessage update = new OperationMessage("operation", op, otEngine.getVersion());
            broadcast(objectMapper.writeValueAsString(update));
        }
    }

    private void broadcast(String msg) throws Exception {
        for (WebSocketSession s: sessions){
            if(s.isOpen()) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("Client disconnected: " + session.getId());
    }

    static class InitMessage {
        public String type;
        public String doc;
        public int version;

        public InitMessage(String type, String doc, int version) {
            this.type = type;
            this.doc = doc;
            this.version = version;
        }
    }

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

