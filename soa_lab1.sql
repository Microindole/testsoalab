-- 1. 创建数据库 (如果尚未创建)
CREATE DATABASE IF NOT EXISTS soa_lab1 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2. 使用该数据库
USE soa_lab1;

-- ==========================================
-- 表 1: 人员表 (users)
-- 对应 Java "人员管理" 模块
-- ==========================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID (主键)',
                         `name` varchar(100) NOT NULL COMMENT '姓名',
                         `role` varchar(50) DEFAULT NULL COMMENT '角色 (如: 学生, 教师, 管理员)',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员信息表';

-- 插入测试数据: 人员
INSERT INTO `users` (`name`, `role`) VALUES ('张三', '学生');
INSERT INTO `users` (`name`, `role`) VALUES ('李四', '教师');
INSERT INTO `users` (`name`, `role`) VALUES ('王五', '管理员');


-- ==========================================
-- 表 2: 物资表 (materials)
-- 对应 Node.js "物资管理" 模块
-- (Node.js 将通过 API 调用 Java 来存取此表，或自己连接此库)
-- ==========================================
DROP TABLE IF EXISTS `materials`;
CREATE TABLE `materials` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '物资ID (主键)',
                             `name` varchar(255) NOT NULL COMMENT '物资名称',
                             `quantity` int(11) NOT NULL DEFAULT '0' COMMENT '库存数量',
                             `location` varchar(255) DEFAULT NULL COMMENT '存放位置',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室物资表';

-- 插入测试数据: 物资
INSERT INTO `materials` (`name`, `quantity`, `location`) VALUES ('投影仪', 5, 'A101');
INSERT INTO `materials` (`name`, `quantity`, `location`) VALUES ('示波器', 10, 'B202');
INSERT INTO `materials` (`name`, `quantity`, `location`) VALUES ('3D打印机', 2, 'C303');


-- ==========================================
-- 表 3: 借用记录表 (borrow_records)
-- 对应 Python "借用记录" 模块
-- (Python 将通过 API 调用 Java 来存取此表，或自己连接此库)
-- ==========================================
DROP TABLE IF EXISTS `borrow_records`;
CREATE TABLE `borrow_records` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID (主键)',
                                  `user_id` bigint(20) NOT NULL COMMENT '借用人ID (外键 -> users.id)',
                                  `material_id` bigint(20) NOT NULL COMMENT '物资ID (外键 -> materials.id)',
                                  `borrow_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借用时间',
                                  `return_date` datetime DEFAULT NULL COMMENT '归还时间 (NULL表示未归还)',
                                  `status` varchar(50) DEFAULT 'BORROWED' COMMENT '状态: BORROWED(借出), RETURNED(已还)',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_user_id` (`user_id`),
                                  KEY `idx_material_id` (`material_id`),
                                  CONSTRAINT `fk_borrow_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                  CONSTRAINT `fk_borrow_material` FOREIGN KEY (`material_id`) REFERENCES `materials` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资借用记录表';

-- 插入测试数据: 借用记录
-- 假设: 张三(id=1) 借用了 投影仪(id=1)
INSERT INTO `borrow_records` (`user_id`, `material_id`, `borrow_date`, `status`)
VALUES (1, 1, '2025-11-10 09:00:00', 'BORROWED');

-- 假设: 李四(id=2) 借用了 示波器(id=2)
INSERT INTO `borrow_records` (`user_id`, `material_id`, `borrow_date`, `status`)
VALUES (2, 2, '2025-11-11 14:30:00', 'BORROWED');