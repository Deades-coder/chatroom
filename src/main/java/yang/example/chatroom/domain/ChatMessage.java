package yang.example.chatroom.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:08:55
 */
@Data
public class ChatMessage {
    private String type; // broadcast, group, private
    private String sender; // 发送者用户名
    private String recipient; // 接收者（私聊）或群组ID（多播）
    private String content; // 消息内容
    private LocalDateTime timestamp; // 发送时间
}
