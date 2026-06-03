-- ============================================================
-- FILE: insertdata.sql (V2 - Nâng Cấp Chuẩn Hóa & Nâng Cao)
-- Hệ thống Quản lý Thư viện - Boooks
-- Database: quan_ly_thu_vien (MySQL)
-- Mô tả: File đã được nâng cấp đạt chuẩn 3NF, áp dụng
-- Index, Event Scheduler, Triggers bẫy lỗi và FOR UPDATE SKIP LOCKED
-- ============================================================

CREATE DATABASE IF NOT EXISTS quan_ly_thu_vien;
USE quan_ly_thu_vien;

-- Bật Event Scheduler trong MySQL
SET GLOBAL event_scheduler = ON;

-- ============================================================
-- PHẦN 1: XÓA DỮ LIỆU CŨ (nếu có) - theo thứ tự phụ thuộc
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS v_book_status, v_member_borrowing, v_overdue_books, v_borrowing_history, v_author_statistics;
DROP FUNCTION IF EXISTS fn_calculate_fine;
DROP FUNCTION IF EXISTS fn_count_books_by_member;
DROP FUNCTION IF EXISTS fn_is_book_available;
DROP PROCEDURE IF EXISTS sp_issue_book_safe;
DROP PROCEDURE IF EXISTS sp_return_book;
DROP PROCEDURE IF EXISTS sp_library_report;
DROP PROCEDURE IF EXISTS sp_search_books;
DROP EVENT IF EXISTS evt_auto_calculate_fine;
DROP TRIGGER IF EXISTS trg_check_max_borrow;
DROP TRIGGER IF EXISTS trg_book_copy_after_update;
DROP TRIGGER IF EXISTS trg_member_before_delete;
DROP TRIGGER IF EXISTS trg_book_copy_before_delete;

DROP TABLE IF EXISTS `audit_log`, `auth`, `fine`, `log`, `book_copy`, `book_category`, `book_author`, `book`, `category`, `author`, `member`;

-- ============================================================
-- PHẦN 2: TẠO BẢNG CHUẨN HÓA 3NF VÀ CHÈN DỮ LIỆU
-- ============================================================

-- 2.1 Bảng fine (Cấu hình phạt)
CREATE TABLE `fine` (
    `sl_no` INT PRIMARY KEY,
    `days_overdue` INT NOT NULL,
    `fine_amount` FLOAT NOT NULL
);
INSERT INTO `fine` VALUES (1, 14, 5000.0);

-- 2.2 Bảng member
CREATE TABLE `member` (
    `member_id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone_number` VARCHAR(20)
);
-- NÂNG CẤP: Tạo Index cho email để tối ưu tìm kiếm
CREATE INDEX idx_member_email ON `member`(`email`);

INSERT INTO `member` (`member_id`, `name`, `email`, `phone_number`) VALUES
(1, N'Nguyễn Văn An',      'nguyenvanan@gmail.com',     '0901234567'),
(2, N'Trần Thị Bích',      'tranthibich@gmail.com',     '0912345678'),
(3, N'Lê Hoàng Cường',     'lehoangcuong@gmail.com',    '0923456789'),
(4, N'Phạm Minh Đức',      'phamminhduc@gmail.com',     '0934567890'),
(5, N'Hoàng Thị Em',       'hoangthiem@gmail.com',      '0945678901');

-- 2.3 Bảng author (Tác giả)
CREATE TABLE `author` (
    `author_id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `bio` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
);
INSERT INTO `author` (`author_id`, `name`) VALUES
(1, N'Tô Hoài'), (2, N'Ngô Tất Tố'), (3, N'Nam Cao'), (4, N'Vũ Trọng Phụng'), (5, N'Kim Lân'), (6, N'Nguyễn Nhật Ánh');

-- 2.4 Bảng category (Thể loại)
CREATE TABLE `category` (
    `category_id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
);
INSERT INTO `category` (`category_id`, `name`) VALUES
(1, N'Văn học thiếu nhi'), (2, N'Tiểu thuyết'), (3, N'Truyện ngắn'), (4, N'Thơ');

-- 2.5 Bảng book (Thông tin đầu sách - Không lưu trạng thái vật lý ở đây)
CREATE TABLE `book` (
    `book_id` INT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `publication` DATE
);
-- NÂNG CẤP: FULLTEXT Index cho việc tìm kiếm tự do siêu tốc
CREATE FULLTEXT INDEX idx_book_title ON `book`(`title`);

INSERT INTO `book` (`book_id`, `title`, `publication`) VALUES
(1, N'Dế Mèn Phiêu Lưu Ký', '1941-06-01'),
(2, N'Tắt Đèn', '1939-01-01'),
(3, N'Chí Phèo', '1941-01-01'),
(4, N'Mắt Biếc', '1990-01-01');

-- 2.6 Các bảng trung gian Nhiều-Nhiều (N-N)
CREATE TABLE `book_author` (
    `book_id` INT,
    `author_id` INT,
    PRIMARY KEY (`book_id`, `author_id`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`book_id`) ON DELETE CASCADE,
    FOREIGN KEY (`author_id`) REFERENCES `author`(`author_id`) ON DELETE CASCADE
);
INSERT INTO `book_author` VALUES (1, 1), (2, 2), (3, 3), (4, 6);

CREATE TABLE `book_category` (
    `book_id` INT,
    `category_id` INT,
    PRIMARY KEY (`book_id`, `category_id`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`book_id`) ON DELETE CASCADE,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`category_id`) ON DELETE CASCADE
);
INSERT INTO `book_category` VALUES (1, 1), (2, 2), (3, 3), (4, 2);

-- 2.7 Bảng book_copy (Bản sao vật lý của từng cuốn sách)
CREATE TABLE `book_copy` (
    `copy_id` INT PRIMARY KEY AUTO_INCREMENT,
    `book_id` INT NOT NULL,
    `status` ENUM('AVAILABLE', 'BORROWED', 'LOST', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    FOREIGN KEY (`book_id`) REFERENCES `book`(`book_id`) ON DELETE CASCADE
);
INSERT INTO `book_copy` (`copy_id`, `book_id`, `status`) VALUES
(1, 1, 'AVAILABLE'), (2, 1, 'AVAILABLE'), (3, 1, 'BORROWED'), -- 3 bản sao của Dế Mèn (ID=1)
(4, 2, 'BORROWED'), (5, 2, 'AVAILABLE'),                      -- 2 bản sao của Tắt Đèn (ID=2)
(6, 3, 'AVAILABLE'), (7, 4, 'BORROWED');                      -- Chí Phèo & Mắt Biếc

-- 2.8 Bảng log (Giao dịch mượn trả - Liên kết thẳng tới bản sao)
CREATE TABLE `log` (
    `log_id` INT PRIMARY KEY AUTO_INCREMENT,
    `copy_id` INT NOT NULL,
    `member_id` INT NOT NULL,
    `issue_date` DATE NOT NULL,
    `return_date` DATE DEFAULT NULL,
    `fine` FLOAT DEFAULT 0,
    FOREIGN KEY (`copy_id`) REFERENCES `book_copy`(`copy_id`),
    FOREIGN KEY (`member_id`) REFERENCES `member`(`member_id`)
);
-- NÂNG CẤP: Thêm Index cho ngày mượn giúp tính toán tiền phạt nhanh hơn
CREATE INDEX idx_log_issue_date ON `log`(`issue_date`);

INSERT INTO `log` (`log_id`, `copy_id`, `member_id`, `issue_date`, `return_date`, `fine`) VALUES
(1, 1, 2, '2025-04-01', '2025-04-10', 0),
(2, 2, 4, '2025-04-05', '2025-04-25', 25000),
(3, 3, 1, '2025-05-25', NULL, 0), -- Đang mượn (copy 3)
(4, 4, 3, '2025-05-28', NULL, 0), -- Đang mượn (copy 4)
(5, 7, 5, '2025-06-01', NULL, 0); -- Đang mượn (copy 7)

-- 2.9 Bảng auth (Tài khoản)
CREATE TABLE `auth` (
    `username` VARCHAR(50) PRIMARY KEY,
    `password` VARCHAR(255) NOT NULL,
    `creation_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `last_login` TIMESTAMP,
    `refresh_token` VARCHAR(255),
    `reset_token` VARCHAR(255),
    `is_activated` BOOLEAN DEFAULT TRUE
);
INSERT INTO `auth` (`username`, `password`) VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- 2.10 Bảng audit_log
CREATE TABLE `audit_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `table_name` VARCHAR(50) NOT NULL,
    `action` VARCHAR(20) NOT NULL,
    `record_id` VARCHAR(50) NOT NULL,
    `old_values` TEXT,
    `new_values` TEXT,
    `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `description` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
);

SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- PHẦN 3: VIEWS (Các khung nhìn)
-- ============================================================

-- VIEW 1: v_book_status (Thống kê số lượng sách)
CREATE VIEW v_book_status AS
SELECT 
    b.book_id,
    b.title,
    GROUP_CONCAT(DISTINCT a.name SEPARATOR ', ') AS authors,
    GROUP_CONCAT(DISTINCT c.name SEPARATOR ', ') AS categories,
    COUNT(bc.copy_id) AS total_copies,
    SUM(CASE WHEN bc.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies
FROM `book` b
LEFT JOIN `book_author` ba ON b.book_id = ba.book_id
LEFT JOIN `author` a ON ba.author_id = a.author_id
LEFT JOIN `book_category` bcat ON b.book_id = bcat.book_id
LEFT JOIN `category` c ON bcat.category_id = c.category_id
LEFT JOIN `book_copy` bc ON b.book_id = bc.book_id
GROUP BY b.book_id, b.title;

-- VIEW 2: v_member_borrowing
CREATE VIEW v_member_borrowing AS
SELECT 
    m.member_id,
    m.name AS member_name,
    m.email,
    bc.copy_id,
    b.title AS book_title,
    l.issue_date,
    DATEDIFF(CURDATE(), l.issue_date) AS days_borrowed
FROM `member` m
INNER JOIN `log` l ON m.member_id = l.member_id AND l.return_date IS NULL
INNER JOIN `book_copy` bc ON l.copy_id = bc.copy_id
INNER JOIN `book` b ON bc.book_id = b.book_id;


-- ============================================================
-- PHẦN 4: FUNCTIONS (Hàm)
-- ============================================================
DELIMITER $$
CREATE FUNCTION fn_calculate_fine(p_issue_date DATE, p_return_date DATE)
RETURNS FLOAT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_days_overdue INT DEFAULT 14;
    DECLARE v_fine_amount FLOAT DEFAULT 5000.0;
    DECLARE v_expected_return DATE;
    DECLARE v_days_late INT;
    DECLARE v_total_fine FLOAT DEFAULT 0.0;

    SELECT days_overdue, fine_amount INTO v_days_overdue, v_fine_amount FROM fine LIMIT 1;
    SET v_expected_return = DATE_ADD(p_issue_date, INTERVAL v_days_overdue DAY);
    
    IF p_return_date > v_expected_return THEN
        SET v_days_late = DATEDIFF(p_return_date, v_expected_return);
        SET v_total_fine = v_days_late * v_fine_amount;
    END IF;

    RETURN v_total_fine;
END$$

CREATE FUNCTION fn_count_books_by_member(p_member_id INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COUNT(*) INTO v_count FROM `log` WHERE member_id = p_member_id AND return_date IS NULL;
    RETURN v_count;
END$$
DELIMITER ;


-- ============================================================
-- PHẦN 5: TRIGGERS (Bẫy sự kiện - Nghiệp vụ)
-- ============================================================
DELIMITER $$

-- 1. TRG: Kiểm tra mượn tối đa 3 cuốn (Quy tắc nghiệp vụ khắt khe)
CREATE TRIGGER trg_check_max_borrow
BEFORE INSERT ON `log`
FOR EACH ROW
BEGIN
    DECLARE v_current_borrowed INT;
    SET v_current_borrowed = fn_count_books_by_member(NEW.member_id);
    
    IF v_current_borrowed >= 3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loi Nghiep Vu: Thanh vien da muon toi da 3 quyen sach, khong the muon them!';
    END IF;
    
    IF NEW.issue_date IS NULL THEN
        SET NEW.issue_date = CURDATE();
    END IF;
END$$

-- 2. TRG: Chặn xóa thành viên đang giữ sách
CREATE TRIGGER trg_member_before_delete
BEFORE DELETE ON `member`
FOR EACH ROW
BEGIN
    DECLARE v_active_loans INT DEFAULT 0;
    SELECT COUNT(*) INTO v_active_loans FROM `log` WHERE member_id = OLD.member_id AND return_date IS NULL;
    IF v_active_loans > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loi: Khong the xoa thanh vien dang giu sach!';
    END IF;
END$$

-- 3. TRG: Chặn xóa bản sao đang cho mượn
CREATE TRIGGER trg_book_copy_before_delete
BEFORE DELETE ON `book_copy`
FOR EACH ROW
BEGIN
    IF OLD.status = 'BORROWED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loi: Khong the xoa ban sao dang duoc muon!';
    END IF;
END$$

-- 4. TRG: Kiểm toán thay đổi trạng thái bản sao
CREATE TRIGGER trg_book_copy_after_update
AFTER UPDATE ON `book_copy`
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO `audit_log` (`table_name`, `action`, `record_id`, `old_values`, `new_values`, `description`)
        VALUES (
            'book_copy', 'UPDATE', CAST(NEW.copy_id AS CHAR),
            CONCAT('status=', OLD.status), CONCAT('status=', NEW.status),
            CONCAT('Trang thai ban sao ID ', NEW.copy_id, ' thay doi tu ', OLD.status, ' sang ', NEW.status)
        );
    END IF;
END$$

DELIMITER ;


-- ============================================================
-- PHẦN 6: STORED PROCEDURES (Giao tác & Khóa đồng thời)
-- ============================================================
DELIMITER $$

-- SP 1: Mượn sách an toàn với FOR UPDATE SKIP LOCKED
CREATE PROCEDURE sp_issue_book_safe(
    IN p_book_id INT,
    IN p_member_id INT,
    OUT p_result VARCHAR(255)
)
BEGIN
    DECLARE v_available_copy_id INT DEFAULT NULL;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'LOI: He thong xay ra loi, da Rollback giao tac!';
    END;

    START TRANSACTION;
    
    -- KIẾN THỨC NÂNG CAO: Tìm 1 bản sao đang rảnh, dùng SKIP LOCKED
    -- Nếu 2 người cùng gọi hàm mượn cuốn "Dế Mèn", hệ thống tự cấp 2 copy_id khác nhau mà không bị chờ (deadlock).
    SELECT copy_id INTO v_available_copy_id
    FROM `book_copy`
    WHERE book_id = p_book_id AND status = 'AVAILABLE'
    LIMIT 1
    FOR UPDATE SKIP LOCKED;
    
    IF v_available_copy_id IS NULL THEN
        ROLLBACK;
        SET p_result = 'LOI: Khong con ban sao nao kha dung cho cuon sach nay!';
    ELSE
        -- Update trạng thái (Trigger audit sẽ bắt)
        UPDATE `book_copy` SET status = 'BORROWED' WHERE copy_id = v_available_copy_id;
        
        -- Insert log (Trigger max_borrow sẽ kiểm tra tự động giới hạn 3 cuốn)
        INSERT INTO `log` (copy_id, member_id, issue_date) VALUES (v_available_copy_id, p_member_id, CURDATE());
        
        COMMIT;
        SET p_result = CONCAT('THANH CONG: Da cho muon ban sao ID = ', v_available_copy_id);
    END IF;
END$$

-- SP 2: Tìm kiếm sách bằng Full-Text Search
CREATE PROCEDURE sp_search_books(IN p_keyword VARCHAR(255))
BEGIN
    -- NÂNG CẤP: Sử dụng MATCH AGAINST thay vì LIKE để tối ưu tốc độ
    SELECT 
        b.book_id,
        b.title,
        COUNT(bc.copy_id) AS available_copies
    FROM `book` b
    LEFT JOIN `book_copy` bc ON b.book_id = bc.book_id AND bc.status = 'AVAILABLE'
    WHERE MATCH(b.title) AGAINST(p_keyword IN BOOLEAN MODE)
    GROUP BY b.book_id, b.title;
END$$

DELIMITER ;


-- ============================================================
-- PHẦN 7: EVENT SCHEDULER (Lập lịch tự động hàng ngày)
-- ============================================================
DELIMITER $$
-- Tự động tính phạt mỗi đêm lúc 00:00 cho các sách quá hạn
CREATE EVENT evt_auto_calculate_fine
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY) -- Bắt đầu từ 00:00 ngày mai
DO
BEGIN
    -- Cập nhật tiền phạt tự động cho các bản ghi mượn quá hạn (chưa trả)
    UPDATE `log` l
    JOIN `fine` f ON f.sl_no = 1
    SET l.fine = DATEDIFF(CURDATE(), DATE_ADD(l.issue_date, INTERVAL f.days_overdue DAY)) * f.fine_amount
    WHERE l.return_date IS NULL 
      AND CURDATE() > DATE_ADD(l.issue_date, INTERVAL f.days_overdue DAY);
END$$
DELIMITER ;

-- ============================================================
-- KẾT THÚC
-- ============================================================
