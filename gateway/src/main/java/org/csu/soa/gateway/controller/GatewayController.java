package org.csu.soa.gateway.controller;

import org.csu.soa.gateway.mapper.BorrowRecordMapper;
import org.csu.soa.gateway.mapper.MaterialMapper;
import org.csu.soa.gateway.mapper.UserMapper;
import org.csu.soa.gateway.model.BorrowRecord;
import org.csu.soa.gateway.model.Material;
import org.csu.soa.gateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // 导入事务注解
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api") // 所有对外 API 都以 /api 开头
public class GatewayController {

    // 1. 注入所有需要的 Mapper
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MaterialMapper materialMapper;
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    // (注意：这里不再需要 RestTemplate 和服务 URL 了)

    /**
     * 核心功能：统一的借用操作 (事务性)
     * 客户端访问: POST http://localhost:8080/api/borrow
     * Body: { "userId": 1, "materialId": 2 }
     */
    @PostMapping("/borrow")
    @Transactional // 开启事务！这是本操作成功的关键
    public ResponseEntity<?> borrowMaterial(@RequestBody Map<String, Long> payload) {

        Long userId = payload.get("userId");
        Long materialId = payload.get("materialId");

        // 1. 检查用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body("Error: User not found");
        }

        // 2. 检查物资
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return ResponseEntity.status(404).body("Error: Material not found");
        }

        // 3. 检查库存
        if (material.getQuantity() <= 0) {
            return ResponseEntity.status(400).body("Error: Material out of stock");
        }

        // 4. [操作] 减库存
        material.setQuantity(material.getQuantity() - 1);
        materialMapper.updateById(material);

        // 5. [操作] 创建借用记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setMaterialId(materialId);
        record.setBorrowDate(LocalDateTime.now());
        record.setStatus("BORROWED");
        borrowRecordMapper.insert(record);

        // 6. 返回成功
        // (如果中间任何步骤失败，@Transactional 会自动回滚数据库)
        return ResponseEntity.ok(record);
    }

    // 你也可以在这里添加对其他服务的代理，例如获取所有用户
    // 这个 API 客户端可以直接访问，不需要通过其他服务
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userMapper.selectList(null));
    }
}