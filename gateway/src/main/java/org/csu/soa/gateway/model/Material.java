package org.csu.soa.gateway.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("materials") // 对应你的 'materials' 表
public class Material {

    // --- Getters and Setters ---
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer quantity; // 注意数据库是 int(11)
    private String location;

}