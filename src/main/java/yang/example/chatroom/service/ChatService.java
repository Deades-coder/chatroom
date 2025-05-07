package yang.example.chatroom.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:08:42
 */
@Slf4j
@Service
public class ChatService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ConcurrentHashMap<String, Set<String>> groups = new ConcurrentHashMap<>();

    public ChatService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void joinChat(String username, String groupId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        redisTemplate.opsForSet().add("online:users", username);
        log.info("User {} added to online:users", username);
        if (groupId != null && !groupId.trim().isEmpty()) {
            joinGroup(username, groupId);
        }
    }

    public void joinGroup(String username, String groupId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("Group ID is required");
        }
        groups.computeIfAbsent(groupId, k -> new HashSet<>()).add(username);
        log.info("User {} joined group {}", username, groupId);
    }

    public void leaveChat(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Cannot leave chat: username is empty");
            return;
        }
        Long removedCount = redisTemplate.opsForSet().remove("online:users", username);
        log.info("User {} removed from online:users, removed count: {}", username, removedCount);

        for (Set<String> members : groups.values()) {
            members.remove(username);
        }

        groups.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        log.info("User {} left chat, updated groups: {}", username, groups.keySet());
    }

    public boolean isOnline(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Cannot check online status: username is empty");
            return false;
        }
        boolean online = redisTemplate.opsForSet().isMember("online:users", username);
        log.info("Checking if user {} is online: {}", username, online);
        return online;
    }

    public Set<String> getGroups() {
        return groups.keySet();
    }

    public Set<String> getOnlineUsers() {
        Set<String> onlineUsers = redisTemplate.opsForSet().members("online:users");
        log.info("Current online users: {}", onlineUsers);
        return onlineUsers;
    }

    public void clearOnlineUsers() {
        redisTemplate.delete("online:users");
        log.info("Cleared online:users in Redis");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : null;
        if (username != null) {
            leaveChat(username);
            log.info("User {} disconnected via WebSocket event", username);
        }
    }
}
