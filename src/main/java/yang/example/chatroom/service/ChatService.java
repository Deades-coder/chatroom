package yang.example.chatroom.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:08:42
 */
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
    }

    public void leaveChat(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        redisTemplate.opsForSet().remove("online:users", username);

        for (Set<String> members : groups.values()) {
            members.remove(username);
        }

        groups.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public boolean isOnline(String username) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("online:users", username));
    }

    public Set<String> getGroups() {
        return groups.keySet();
    }
}
