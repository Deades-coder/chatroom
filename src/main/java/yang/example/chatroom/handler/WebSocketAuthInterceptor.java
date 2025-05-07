package yang.example.chatroom.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import yang.example.chatroom.service.ChatService;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:12:37
 */
@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final ChatService chatService;

    public WebSocketAuthInterceptor(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String username = accessor.getFirstNativeHeader("username");
                log.info("Received CONNECT with username: {}", username);
                if (username == null || username.trim().isEmpty()) {
                    log.error("Username is required");
                    throw new IllegalArgumentException("Username is required");
                }
                // 清理旧会话并加入新会话
                if (chatService.isOnline(username)) {
                    log.error("User {} is already online", username);
                    throw new IllegalArgumentException("Username is already online");
                }
                chatService.joinChat(username, null);
                // 设置用户身份
                accessor.setUser(() -> username);
                log.info("User {} authenticated and joined chat successfully", username);
            }
        } catch (Exception e) {
            log.error("Error in WebSocket authentication: ", e);
            throw e;
        }
        return message;
    }
}