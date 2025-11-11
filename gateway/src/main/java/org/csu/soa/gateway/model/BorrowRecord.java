package org.csu.soa.gateway.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@TableName("borrow_records")
public class BorrowRecord {

    // --- Getters and Setters ---
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id") // 映射数据库的 user_id
    private Long userId;

    @TableField("material_id") // 映射数据库的 material_id
    private Long materialId;

    @TableField("borrow_date")
    private LocalDateTime borrowDate;

    @TableField("return_date")
    private LocalDateTime returnDate;

    private String status;

}