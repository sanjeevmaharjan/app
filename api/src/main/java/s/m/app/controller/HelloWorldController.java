package s.m.app.controller;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
    @GetMapping
    String greet() {
        SecurityContext context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        String username = authentication.getName();

        String name = StringUtils.hasText(username) ? username : "World";
        return "Hello, " + name + "!";
    }
}
