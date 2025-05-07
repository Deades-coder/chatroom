package yang.example.chatroom.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import yang.example.chatroom.domain.UserJoinRequest;
import yang.example.chatroom.service.ChatService;

/**
 * User:小小星仔
 * Date:2025-05-07
 * Time:09:27
 */
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatPageController {

    private final ChatService chatService;

    @GetMapping
    public String chatPage(@RequestParam(value = "username", required = false) String username, Model model, HttpServletRequest request) {
        System.out.println("Received request for /chat with username: " + username);
        System.out.println("Query string: " + request.getQueryString());
        if (username != null) {
            System.out.println("Passing username to view: " + username);
        } else {
            System.out.println("No username provided in request");
        }
        model.addAttribute("username", username);
        return "chat";
    }

    @PostMapping("/login")
    public String login(UserJoinRequest request, Model model, HttpServletResponse response) {
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "用户名不能为空");
            return "chat";
        }
        try {
            System.out.println("User attempting to join chat: " + username);
            chatService.joinChat(username, null);
            System.out.println("User " + username + " joined chat successfully");
            String redirectUrl = UriComponentsBuilder.fromPath("/chat")
                    .queryParam("username", username)
                    .build()
                    .encode()
                    .toUriString();
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // 防止缓存
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Failed to join chat for user: " + username);
            e.printStackTrace();
            model.addAttribute("error", "加入聊天失败：" + e.getMessage());
            return "chat";
        }
    }

    @PostMapping("/logout")
    public String logout(@RequestParam String username, Model model) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "用户名不能为空");
            return "chat";
        }
        chatService.leaveChat(username);
        return "redirect:/chat";
    }
}
