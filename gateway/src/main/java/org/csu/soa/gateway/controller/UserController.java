package org.csu.soa.gateway.controller;

import org.csu.soa.gateway.mapper.UserMapper;
import org.csu.soa.gateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 确保导入 List

@RestController
@RequestMapping("/api") // 1. 将所有用户 API 移动到 /api 路径下
public class UserController {

    @Autowired
    private UserMapper userMapper; // 注入 UserMapper

    // 2. (修改) 人员管理 - 创建用户
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        userMapper.insert(user);
        return user;
    }

    // 3. (修改) 人员管理 - 按 ID 获取用户
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id); // 使用 MP 的 selectById
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. (新增) 人员管理 - 获取所有用户
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    // 5. (修改) 根路径 Hello
    @GetMapping("/")
    public String hello() {
        return "Hello from Java (MyBatis-Plus) Gateway & User Service (Port 8080)";
    }
}