package org.csu.soa.gateway.controller;

import org.csu.soa.gateway.mapper.UserMapper;
import org.csu.soa.gateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper; // 注入 UserMapper

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        userMapper.insert(user);
        return user;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id); // 使用 MP 的 selectById
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/")
    public String hello() {
        return "Hello from Java (MyBatis-Plus) Gateway & User Service (Port 8080)";
    }
}