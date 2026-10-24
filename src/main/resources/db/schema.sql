-- 个人记账管理系统 建库建表脚本
CREATE DATABASE IF NOT EXISTS account_book
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE account_book;

CREATE TABLE IF NOT EXISTS t_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
  password    VARCHAR(100) NOT NULL COMMENT '密码(MD5)',
  nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) COMMENT '用户表';

CREATE TABLE IF NOT EXISTS t_record (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  user_id     BIGINT       NOT NULL COMMENT '所属用户ID',
  type        TINYINT      NOT NULL COMMENT '类型: 1支出 2收入',
  category    VARCHAR(30)  NOT NULL COMMENT '分类',
  amount      DECIMAL(10,2) NOT NULL COMMENT '金额(元)',
  record_date DATE         NOT NULL COMMENT '发生日期',
  remark      VARCHAR(200) DEFAULT NULL COMMENT '备注',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_user_date (user_id, record_date) COMMENT '按用户+日期查询的索引'
) COMMENT '收支记录表';
