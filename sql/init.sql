DROP DATABASE IF EXISTS campus_trade;
CREATE DATABASE campus_trade
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE campus_trade;

SET FOREIGN_KEY_CHECKS = 0;

-- Drop possible child tables from current and earlier schema versions first.
DROP TABLE IF EXISTS goods_image;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS browse_history;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS trade_order;
DROP TABLE IF EXISTS goods;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(30) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  avatar VARCHAR(255),
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  credit_score INT NOT NULL DEFAULT 100,
  status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_credit_score CHECK (credit_score BETWEEN 0 AND 150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  sort_order INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE goods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  description TEXT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  original_price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(500),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  view_count INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_goods_status (status),
  INDEX idx_goods_category (category_id),
  INDEX idx_goods_seller (seller_id),
  CONSTRAINT fk_goods_seller FOREIGN KEY (seller_id) REFERENCES users(id),
  CONSTRAINT fk_goods_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite_user_goods (user_id, goods_id),
  INDEX idx_favorite_goods (goods_id),
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_favorite_goods FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE browse_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_history_user_goods (user_id, goods_id),
  INDEX idx_history_user_time (user_id, create_time),
  INDEX idx_history_goods (goods_id),
  CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_history_goods FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trade_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL UNIQUE,
  goods_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_order_buyer (buyer_id),
  INDEX idx_order_seller (seller_id),
  CONSTRAINT fk_order_goods FOREIGN KEY (goods_id) REFERENCES goods(id),
  CONSTRAINT fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
  CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  goods_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  target_user_id BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_review_target (target_user_id),
  CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES trade_order(id),
  CONSTRAINT fk_review_goods FOREIGN KEY (goods_id) REFERENCES goods(id),
  CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
  CONSTRAINT fk_review_target FOREIGN KEY (target_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- All three accounts use password: 123456 (BCrypt).
INSERT INTO users (username, password, nickname, phone, email, role, credit_score, status) VALUES
('admin', '$2a$10$DXFwAXIU5Y8B7ZLjREW49elGX.J5jmyRq.E8A07hGtRmOjv/hX9ba', '平台管理员', '13800000000', 'admin@campus.local', 'ADMIN', 120, 'NORMAL'),
('student1', '$2a$10$DXFwAXIU5Y8B7ZLjREW49elGX.J5jmyRq.E8A07hGtRmOjv/hX9ba', '小林同学', '13800000001', 'student1@campus.local', 'USER', 105, 'NORMAL'),
('student2', '$2a$10$DXFwAXIU5Y8B7ZLjREW49elGX.J5jmyRq.E8A07hGtRmOjv/hX9ba', '阿青同学', '13800000002', 'student2@campus.local', 'USER', 98, 'NORMAL');

INSERT INTO category (name, sort_order) VALUES
('数码电子', 1), ('教材资料', 2), ('生活用品', 3),
('服饰鞋包', 4), ('运动器材', 5), ('其他', 6);

INSERT INTO goods (seller_id, category_id, title, description, price, original_price, image_url, status, view_count) VALUES
(2, 1, '九成新机械键盘', '青轴机械键盘，灯光和按键均正常，宿舍自提。', 129.00, 299.00, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=900', 'ON_SALE', 86),
(3, 2, 'Java Web 课程教材与笔记', '教材无缺页，附个人整理的重点笔记，适合期末复习。', 28.00, 69.00, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=900', 'ON_SALE', 63),
(2, 5, '入门羽毛球拍一对', '轻微使用痕迹，送三只球和拍套，适合新手。', 55.00, 139.00, 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=900', 'ON_SALE', 41),
(3, 3, '宿舍桌面收纳架', '白色双层收纳架，尺寸约 45x20cm，干净无破损。', 19.90, 49.00, 'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=900', 'PENDING', 12);

SET FOREIGN_KEY_CHECKS = 1;
