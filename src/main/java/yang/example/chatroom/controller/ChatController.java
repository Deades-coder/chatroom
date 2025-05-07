package yang.example.chatroom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import yang.example.chatroom.domain.ChatMessage;
import yang.example.chatroom.service.ChatService;

import java.time.LocalDateTime;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:08:42
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/broadcast")
    public void broadcast(@Payload ChatMessage message) {
        log.info("Broadcast message from {}: {}", message.getSender(), message.getContent());
        message.setType("broadcast");
        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/broadcast", message);
    }

    @MessageMapping("/chat/group")
    public void groupMessage(@Payload ChatMessage message) {
        log.info("Group message from {} to group {}: {}", message.getSender(), message.getRecipient(), message.getContent());
        message.setType("group");
        message.setTimestamp(LocalDateTime.now());
        String groupId = message.getRecipient();
        if (groupId == null || groupId.trim().isEmpty()) {
            log.warn("Group ID is empty in group message from {}", message.getSender());
            return;
        }
        messagingTemplate.convertAndSend("/topic/group/" + groupId, message);
    }

    @MessageMapping("/chat/private")
    public void privateMessage(@Payload ChatMessage message) {
        log.info("Private message from {} to {}: {}", message.getSender(), message.getRecipient(), message.getContent());
        message.setType("private");
        message.setTimestamp(LocalDateTime.now());
        String recipient = message.getRecipient();
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("Recipient is empty in private message from {}", message.getSender());
            return;
        }
        if (chatService.isOnline(recipient)) {
            messagingTemplate.convertAndSendToUser(recipient, "/private", message);
            System.out.println("User1 online.....");
        } else {
            log.warn("Recipient {} is offline, message not sent from {}", recipient, message.getSender());
        }
    }
}