package yang.example.chatroom.domain;

import lombok.Data;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:08:55
 */
@Data
public class UserJoinRequest {
    private String username;
    private String groupId; // 可选，加入的群组
}
