package com.parkcore.backend.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Raw WebSocket handler that broadcasts simulation state to connected clients
 * and forwards control messages ({"type":"CONTROL", ...}) to a listener.
 * This bypasses STOMP and sends JSON directly.
 */
@Component
public class RawWebSocketHandler extends TextWebSocketHandler {

    private static final int SEND_TIME_LIMIT_MS = 2000;
    private static final int BUFFER_SIZE_LIMIT = 512 * 1024;

    // Decorated sessions are safe to send to from the simulation thread
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile Consumer<JsonNode> controlListener = msg -> {};

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(),
                new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT));
        System.out.println("✓ WebSocket client connected: " + session.getId() + " (total: " + sessions.size() + ")");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("✓ WebSocket client disconnected: " + session.getId() + " (total: " + sessions.size() + ")");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode msg = mapper.readTree(message.getPayload());
            if ("CONTROL".equals(msg.path("type").asText())) {
                controlListener.accept(msg);
            }
        } catch (Exception e) {
            System.err.println("✗ Bad message from client: " + e.getMessage());
        }
    }

    public void setControlListener(Consumer<JsonNode> listener) {
        this.controlListener = listener;
    }

    /**
     * Broadcast a message to all connected WebSocket clients.
     */
    public void broadcastToAll(Object message) throws Exception {
        if (sessions.isEmpty()) return;
        TextMessage textMessage = new TextMessage(mapper.writeValueAsString(message));

        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                } else {
                    sessions.remove(session.getId());
                }
            } catch (Exception e) {
                sessions.remove(session.getId());
                System.err.println("✗ Failed to send to client: " + e.getMessage());
            }
        }
    }

    public int getConnectedClientCount() {
        return sessions.size();
    }
}
