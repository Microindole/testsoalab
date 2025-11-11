package org.csu.soa.gateway.controller;


import org.csu.soa.gateway.mapper.BorrowRecordMapper;
import org.csu.soa.gateway.model.BorrowRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class BorrowRecordController {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    // 1. 获取所有借用记录
    @GetMapping("/borrows")
    public List<BorrowRecord> getAllBorrows() {
        return borrowRecordMapper.selectList(null);
    }

    // 2. 创建新借用记录
    @PostMapping("/borrows")
    public BorrowRecord createBorrow(@RequestBody BorrowRecord borrowRecord) {
        // 设置默认值
        if (borrowRecord.getBorrowDate() == null) {
            borrowRecord.setBorrowDate(LocalDateTime.now());
        }
        if (borrowRecord.getStatus() == null) {
            borrowRecord.setStatus("BORROWED");
        }
        
        borrowRecordMapper.insert(borrowRecord);
        return borrowRecord; // 返回包含新 ID 的记录
    }
}