package com.parkcore.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Raw WebSocket handler that broadcasts simulation state to connected clients.
 * This bypasses STOMP and sends JSON directly.
 */
@Component
public class RawWebSocketHandler extends TextWebSocketHandler {
    
    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("✓ WebSocket client connected: " + session.getId() + " (total: " + sessions.size() + ")");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("✓ WebSocket client disconnected: " + session.getId() + " (total: " + sessions.size() + ")");
    }

    /**
     * Broadcast a message to all connected WebSocket clients.
     */
    public void broadcastToAll(Map<String, Object> message) throws Exception {
        String jsonMessage = mapper.writeValueAsString(message);
        TextMessage textMessage = new TextMessage(jsonMessage);
        
        // Remove dead sessions and send to live ones
        Set<WebSocketSession> deadSessions = new HashSet<>();
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (Exception e) {
                deadSessions.add(session);
                System.err.println("✗ Failed to send to client: " + e.getMessage());
            }
        }
        
        // Clean up dead sessions
        sessions.removeAll(deadSessions);
    }

    public int getConnectedClientCount() {
        return sessions.size();
    }
}
