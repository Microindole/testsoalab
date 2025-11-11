package org.csu.soa.gateway.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("users")
@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String role;
}