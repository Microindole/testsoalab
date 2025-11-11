package org.csu.soa.gateway.controller;

// 移除不必要的 Mapper
// import org.csu.soa.gateway.mapper.BorrowRecordMapper;
// import org.csu.soa.gateway.mapper.MaterialMapper;

import org.csu.soa.gateway.mapper.UserMapper;
import org.csu.soa.gateway.model.BorrowRecord;
import org.csu.soa.gateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api") // 所有对外 API 都以 /api 开头
public class GatewayController {

    // 1. 只注入 UserMapper (用于本地业务) 和 RestTemplate (用于远程调用)
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RestTemplate restTemplate;

    // 2. 定义其他服务的 URL
    private final String PYTHON_BORROW_SERVICE_URL = "http://localhost:8081";
    private final String NODE_MATERIAL_SERVICE_URL = "http://localhost:8082";


    /**
     * 核心功能：统一的借用操作 (跨服务)
     * 客户端访问: POST http://localhost:8080/api/borrow
     * Body: { "userId": 1, "materialId": 2 }
     */
    @PostMapping("/borrow")
    // @Transactional (不再需要，因为它无法管理跨服务事务)
    public ResponseEntity<?> borrowMaterial(@RequestBody Map<String, Long> payload) {

        Long userId = payload.get("userId");
        Long materialId = payload.get("materialId");

        // 1. 检查用户 (本地业务)
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body("Error: User not found");
        }

        // 2. [操作] 减库存 (调用 Node.js 服务)
        try {
            restTemplate.put(NODE_MATERIAL_SERVICE_URL + "/materials/" + materialId + "/decrement", null);
        } catch (HttpClientErrorException e) {
            // 转发 Node.js 服务返回的错误 (例如 404 "Not Found" 或 400 "Out of Stock")
            return ResponseEntity.status(e.getStatusCode()).body("Error from Material Service: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error calling Material Service: " + e.getMessage());
        }

        // 3. [操作] 创建借用记录 (调用 Python 服务)
        try {
            // 准备要 POST 到 Python 的数据
            // (注意：Python 服务端的 app.py 期望 'userId' 和 'materialId' 字段)
            BorrowRecord recordRequest = new BorrowRecord();
            recordRequest.setUserId(userId);
            recordRequest.setMaterialId(materialId);

            // 发起 POST 请求
            ResponseEntity<BorrowRecord> pythonResponse = restTemplate.postForEntity(
                    PYTHON_BORROW_SERVICE_URL + "/borrows",
                    recordRequest,
                    BorrowRecord.class
            );

            // 4. 返回成功 (返回 Python 服务创建的记录)
            return ResponseEntity.ok(pythonResponse.getBody());

        } catch (Exception pythonError) {
            // !! 关键：补偿事务 (Saga 模式的简单实现) !!
            // Python 服务失败了，必须尝试把 Node.js 服务减掉的库存加回去
            try {
                restTemplate.put(NODE_MATERIAL_SERVICE_URL + "/materials/" + materialId + "/increment", null);
            } catch (Exception compensationError) {
                // 如果补偿失败，这是最坏的情况
                return ResponseEntity.status(500).body("CRITICAL ERROR: Borrow failed AND stock compensation failed. Manual DB correction required. Python Error: " + pythonError.getMessage());
            }

            // 补偿成功，返回 Python 的原始错误
            return ResponseEntity.status(500).body("Error: Borrow record creation failed, stock has been rolled back. Python Error: " + pythonError.getMessage());
        }
    }

    // --- 4. 添加其他服务的代理 (Proxy) ---

    // 代理: 获取所有物资
    @GetMapping("/materials")
    public ResponseEntity<?> getAllMaterials() {
        try {
            // 直接从 Node.js 获取原始 JSON 字符串并转发
            String response = restTemplate.getForObject(NODE_MATERIAL_SERVICE_URL + "/materials", String.class);
            // (返回 Object.class 也可以，Spring 会自动处理 JSON)
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error proxying Material Service: " + e.getMessage());
        }
    }

    // 代理: 获取所有借用记录
    @GetMapping("/borrows")
    public ResponseEntity<?> getAllBorrows() {
        try {
            String response = restTemplate.getForObject(PYTHON_BORROW_SERVICE_URL + "/borrows", String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error proxying Borrow Service: " + e.getMessage());
        }
    }
}