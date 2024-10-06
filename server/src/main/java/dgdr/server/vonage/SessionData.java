package dgdr.server.vonage;

import org.springframework.web.socket.WebSocketSession;

public class SessionData {
    private volatile WebSocketSession websocketSession;

    public SessionData(WebSocketSession websocketSession) {
        this.websocketSession = websocketSession;
    }

    public WebSocketSession getWebSocketSession() {
        return websocketSession;
    }

    public void setWebSocketSession(WebSocketSession websocketSession) {
        this.websocketSession = websocketSession;
    }
}
