package yang.example.chatroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import yang.example.chatroom.domain.ChatMessage;
import yang.example.chatroom.domain.UserJoinRequest;
import yang.example.chatroom.service.ChatService;

import java.time.LocalDateTime;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:08:42
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class RestChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/join")
    public ResponseEntity<String> joinChat(@RequestBody UserJoinRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        chatService.joinChat(request.getUsername(), request.getGroupId());
        ChatMessage message = new ChatMessage();
        message.setType("system");
        message.setSender("System");
        message.setContent(request.getUsername() + " 加入了聊天");
        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/broadcast", message);
        return ResponseEntity.ok("加入成功");
    }
}