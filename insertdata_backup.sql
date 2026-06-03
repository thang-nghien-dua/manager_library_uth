-- ============================================================
-- FILE: insertdata.sql
-- Hệ thống Quản lý Thư viện - Boooks
-- Database: quan_ly_thu_vien (MySQL)
-- Bao gồm: INSERT dữ liệu mẫu, VIEW, STORED PROCEDURE,
--           FUNCTION, TRIGGER, TRANSACTION (giao tác)
-- ============================================================

USE quan_ly_thu_vien;

-- ============================================================
-- PHẦN 1: XÓA DỮ LIỆU CŨ (nếu có) - theo thứ tự phụ thuộc
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `log`;
DELETE FROM `book`;
DELETE FROM `member`;
DELETE FROM `fine`;
DELETE FROM `auth`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- PHẦN 2: CHÈN DỮ LIỆU MẪU (INSERT)
-- ============================================================

-- -----------------------------------------------
-- 2.1. Bảng fine (Cấu hình phạt)
-- -----------------------------------------------
INSERT INTO `fine` (`sl_no`, `days_overdue`, `fine_amount`) VALUES
(1, 14, 5000.0);   -- Quá hạn 14 ngày thì bắt đầu phạt, mỗi ngày 5000đ

-- -----------------------------------------------
-- 2.2. Bảng member (Thành viên thư viện)
-- -----------------------------------------------
INSERT INTO `member` (`member_id`, `name`, `email`, `phone_number`) VALUES
(1, N'Nguyễn Văn An',      'nguyenvanan@gmail.com',     '0901234567'),
(2, N'Trần Thị Bích',      'tranthibich@gmail.com',     '0912345678'),
(3, N'Lê Hoàng Cường',     'lehoangcuong@gmail.com',    '0923456789'),
(4, N'Phạm Minh Đức',      'phamminhduc@gmail.com',     '0934567890'),
(5, N'Hoàng Thị Em',       'hoangthiem@gmail.com',      '0945678901'),
(6, N'Võ Quốc Phong',      'voquocphong@gmail.com',     '0956789012'),
(7, N'Đặng Thùy Giang',    'dangthuyGiang@gmail.com',   '0967890123'),
(8, N'Bùi Thanh Hải',      'buithanhai@gmail.com',      '0978901234'),
(9, N'Ngô Khánh Linh',     'ngokhanhlinh@gmail.com',    '0989012345'),
(10, N'Đỗ Xuân Khôi',      'doxuankhoi@gmail.com',      '0990123456');

-- -----------------------------------------------
-- 2.3. Bảng book (Sách)
-- -----------------------------------------------
INSERT INTO `book` (`book_id`, `title`, `author`, `genre`, `publication`, `is_available`, `holder`) VALUES
(1,  N'Dế Mèn Phiêu Lưu Ký',           N'Tô Hoài',              N'Văn học thiếu nhi',  '1941-06-01', TRUE,  NULL),
(2,  N'Tắt Đèn',                         N'Ngô Tất Tố',           N'Tiểu thuyết',        '1939-01-01', TRUE,  NULL),
(3,  N'Chí Phèo',                         N'Nam Cao',              N'Truyện ngắn',        '1941-01-01', TRUE,  NULL),
(4,  N'Số Đỏ',                            N'Vũ Trọng Phụng',      N'Tiểu thuyết',        '1936-01-01', TRUE,  NULL),
(5,  N'Vợ Nhặt',                          N'Kim Lân',              N'Truyện ngắn',        '1962-01-01', TRUE,  NULL),
(6,  N'Lão Hạc',                          N'Nam Cao',              N'Truyện ngắn',        '1943-01-01', TRUE,  NULL),
(7,  N'Truyện Kiều',                      N'Nguyễn Du',            N'Thơ',                '1820-01-01', TRUE,  NULL),
(8,  N'Nhật Ký Trong Tù',                 N'Hồ Chí Minh',         N'Thơ',                '1960-01-01', TRUE,  NULL),
(9,  N'Bắt Trẻ Đồng Xanh',               N'J.D. Salinger',       N'Tiểu thuyết',        '1951-07-16', TRUE,  NULL),
(10, N'Đắc Nhân Tâm',                     N'Dale Carnegie',       N'Kỹ năng sống',       '1936-10-01', TRUE,  NULL),
(11, N'Nhà Giả Kim',                      N'Paulo Coelho',        N'Tiểu thuyết',        '1988-01-01', TRUE,  NULL),
(12, N'Tuổi Trẻ Đáng Giá Bao Nhiêu',     N'Rosie Nguyễn',        N'Kỹ năng sống',       '2016-05-01', TRUE,  NULL),
(13, N'Đời Thừa',                          N'Nam Cao',             N'Truyện ngắn',        '1943-01-01', TRUE,  NULL),
(14, N'Tôi Thấy Hoa Vàng Trên Cỏ Xanh',  N'Nguyễn Nhật Ánh',     N'Văn học thiếu nhi',  '2010-12-01', TRUE,  NULL),
(15, N'Mắt Biếc',                          N'Nguyễn Nhật Ánh',     N'Tiểu thuyết',        '1990-01-01', TRUE,  NULL),
(16, N'Cho Tôi Xin Một Vé Đi Tuổi Thơ',   N'Nguyễn Nhật Ánh',    N'Văn học thiếu nhi',  '2008-01-01', TRUE,  NULL),
(17, N'Giết Con Chim Nhại',                N'Harper Lee',          N'Tiểu thuyết',        '1960-07-11', TRUE,  NULL),
(18, N'1984',                              N'George Orwell',       N'Tiểu thuyết',        '1949-06-08', TRUE,  NULL),
(19, N'Sapiens: Lược Sử Loài Người',      N'Yuval Noah Harari',   N'Khoa học',           '2011-01-01', TRUE,  NULL),
(20, N'Clean Code',                         N'Robert C. Martin',   N'Công nghệ',          '2008-08-01', TRUE,  NULL);

-- -----------------------------------------------
-- 2.4. Mô phỏng một số sách đang được mượn
-- -----------------------------------------------
-- Sách ID 2 (Tắt Đèn) đang được thành viên 1 (Nguyễn Văn An) mượn
UPDATE `book` SET `is_available` = FALSE, `holder` = 1 WHERE `book_id` = 2;
-- Sách ID 5 (Vợ Nhặt) đang được thành viên 3 (Lê Hoàng Cường) mượn
UPDATE `book` SET `is_available` = FALSE, `holder` = 3 WHERE `book_id` = 5;
-- Sách ID 10 (Đắc Nhân Tâm) đang được thành viên 5 (Hoàng Thị Em) mượn
UPDATE `book` SET `is_available` = FALSE, `holder` = 5 WHERE `book_id` = 10;
-- Sách ID 15 (Mắt Biếc) đang được thành viên 7 (Đặng Thùy Giang) mượn
UPDATE `book` SET `is_available` = FALSE, `holder` = 7 WHERE `book_id` = 15;

-- -----------------------------------------------
-- 2.5. Bảng log (Nhật ký mượn/trả sách)
-- -----------------------------------------------
-- Các bản ghi đã trả (có return_date)
INSERT INTO `log` (`log_id`, `book_id`, `member_id`, `issue_date`, `return_date`, `fine`) VALUES
(1,  1,  2, '2025-04-01', '2025-04-10', 0),        -- Trả đúng hạn
(2,  3,  4, '2025-04-05', '2025-04-25', 25000),     -- Trả trễ 6 ngày → phạt 6 x 5000 = 30000 (nhưng ghi 25000 ví dụ)
(3,  7,  1, '2025-04-10', '2025-04-20', 0),         -- Trả đúng hạn
(4,  9,  6, '2025-05-01', '2025-05-12', 0),         -- Trả đúng hạn
(5, 11,  8, '2025-05-05', '2025-05-25', 30000),     -- Trả trễ → phạt
(6, 14,  9, '2025-05-10', '2025-05-20', 0),         -- Trả đúng hạn
(7,  4, 10, '2025-05-15', '2025-06-01', 10000),     -- Trả trễ → phạt
(8, 18,  2, '2025-05-20', '2025-06-02', 0);         -- Trả đúng hạn

-- Các bản ghi đang mượn (chưa trả - return_date = NULL)
INSERT INTO `log` (`log_id`, `book_id`, `member_id`, `issue_date`, `return_date`, `fine`) VALUES
(9,  2,  1, '2025-05-25', NULL, NULL),               -- Đang mượn
(10, 5,  3, '2025-05-28', NULL, NULL),               -- Đang mượn
(11, 10, 5, '2025-06-01', NULL, NULL),               -- Đang mượn
(12, 15, 7, '2025-05-20', NULL, NULL);               -- Đang mượn

-- -----------------------------------------------
-- 2.6. Bảng auth (Tài khoản thủ thư)
-- -----------------------------------------------
INSERT INTO `auth` (`username`, `password`, `creation_time`, `last_login`, `refresh_token`, `reset_token`, `is_activated`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2025-01-01 00:00:00', '2025-06-01 08:00:00', NULL, NULL, TRUE),
('librarian01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2025-01-15 09:00:00', '2025-06-01 10:00:00', NULL, NULL, TRUE);


-- ============================================================
-- PHẦN 3: VIEWS (Các khung nhìn)
-- ============================================================

-- -----------------------------------------------
-- VIEW 1: v_book_status - Tổng quan trạng thái sách
-- Hiển thị thông tin sách kèm tên người đang mượn (nếu có)
-- -----------------------------------------------
DROP VIEW IF EXISTS v_book_status;
CREATE VIEW v_book_status AS
SELECT 
    b.book_id,
    b.title,
    b.author,
    b.genre,
    b.publication,
    b.is_available,
    b.holder AS holder_id,
    m.name AS holder_name,
    m.email AS holder_email
FROM `book` b
LEFT JOIN `member` m ON b.holder = m.member_id;

-- -----------------------------------------------
-- VIEW 2: v_member_borrowing - Thành viên đang mượn sách
-- Liệt kê các thành viên đang mượn sách và sách họ đang giữ
-- -----------------------------------------------
DROP VIEW IF EXISTS v_member_borrowing;
CREATE VIEW v_member_borrowing AS
SELECT 
    m.member_id,
    m.name AS member_name,
    m.email,
    m.phone_number,
    b.book_id,
    b.title AS book_title,
    l.issue_date,
    DATEDIFF(CURDATE(), l.issue_date) AS days_borrowed
FROM `member` m
INNER JOIN `log` l ON m.member_id = l.member_id AND l.return_date IS NULL
INNER JOIN `book` b ON l.book_id = b.book_id;

-- -----------------------------------------------
-- VIEW 3: v_overdue_books - Sách quá hạn chưa trả
-- Liệt kê sách quá hạn dựa trên cấu hình fine
-- -----------------------------------------------
DROP VIEW IF EXISTS v_overdue_books;
CREATE VIEW v_overdue_books AS
SELECT 
    l.log_id,
    b.book_id,
    b.title,
    m.member_id,
    m.name AS member_name,
    m.phone_number,
    l.issue_date,
    DATE_ADD(l.issue_date, INTERVAL f.days_overdue DAY) AS expected_return_date,
    DATEDIFF(CURDATE(), DATE_ADD(l.issue_date, INTERVAL f.days_overdue DAY)) AS days_overdue_count,
    DATEDIFF(CURDATE(), DATE_ADD(l.issue_date, INTERVAL f.days_overdue DAY)) * f.fine_amount AS estimated_fine
FROM `log` l
INNER JOIN `book` b ON l.book_id = b.book_id
INNER JOIN `member` m ON l.member_id = m.member_id
CROSS JOIN `fine` f
WHERE l.return_date IS NULL
  AND CURDATE() > DATE_ADD(l.issue_date, INTERVAL f.days_overdue DAY);

-- -----------------------------------------------
-- VIEW 4: v_borrowing_history - Lịch sử mượn trả đầy đủ
-- -----------------------------------------------
DROP VIEW IF EXISTS v_borrowing_history;
CREATE VIEW v_borrowing_history AS
SELECT 
    l.log_id,
    b.title AS book_title,
    b.author,
    m.name AS member_name,
    l.issue_date,
    l.return_date,
    CASE 
        WHEN l.return_date IS NULL THEN N'Đang mượn'
        ELSE N'Đã trả'
    END AS status,
    COALESCE(l.fine, 0) AS fine_amount
FROM `log` l
INNER JOIN `book` b ON l.book_id = b.book_id
INNER JOIN `member` m ON l.member_id = m.member_id
ORDER BY l.issue_date DESC;

-- -----------------------------------------------
-- VIEW 5: v_author_statistics - Thống kê theo tác giả
-- -----------------------------------------------
DROP VIEW IF EXISTS v_author_statistics;
CREATE VIEW v_author_statistics AS
SELECT 
    b.author,
    COUNT(*) AS total_books,
    SUM(CASE WHEN b.is_available = TRUE THEN 1 ELSE 0 END) AS available_books,
    SUM(CASE WHEN b.is_available = FALSE THEN 1 ELSE 0 END) AS borrowed_books,
    GROUP_CONCAT(b.title ORDER BY b.title SEPARATOR ', ') AS book_list
FROM `book` b
GROUP BY b.author;


-- ============================================================
-- PHẦN 4: FUNCTIONS (Hàm)
-- ============================================================

-- -----------------------------------------------
-- FUNCTION 1: fn_calculate_fine
-- Tính tiền phạt dựa trên ngày mượn
-- Tham số: p_issue_date (ngày mượn), p_return_date (ngày trả)
-- -----------------------------------------------
DROP FUNCTION IF EXISTS fn_calculate_fine;
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

    -- Lấy cấu hình phạt từ bảng fine
    SELECT days_overdue, fine_amount 
    INTO v_days_overdue, v_fine_amount 
    FROM fine WHERE sl_no = 1
    LIMIT 1;

    SET v_expected_return = DATE_ADD(p_issue_date, INTERVAL v_days_overdue DAY);
    
    IF p_return_date > v_expected_return THEN
        SET v_days_late = DATEDIFF(p_return_date, v_expected_return);
        SET v_total_fine = v_days_late * v_fine_amount;
    END IF;

    RETURN v_total_fine;
END$$
DELIMITER ;

-- -----------------------------------------------
-- FUNCTION 2: fn_count_books_by_member
-- Đếm số sách đang được mượn bởi 1 thành viên
-- -----------------------------------------------
DROP FUNCTION IF EXISTS fn_count_books_by_member;
DELIMITER $$
CREATE FUNCTION fn_count_books_by_member(p_member_id INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_count
    FROM `log`
    WHERE member_id = p_member_id AND return_date IS NULL;
    
    RETURN v_count;
END$$
DELIMITER ;

-- -----------------------------------------------
-- FUNCTION 3: fn_is_book_available
-- Kiểm tra sách có sẵn để mượn không (1 = có, 0 = không)
-- -----------------------------------------------
DROP FUNCTION IF EXISTS fn_is_book_available;
DELIMITER $$
CREATE FUNCTION fn_is_book_available(p_book_id INT)
RETURNS TINYINT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_available TINYINT DEFAULT 0;
    
    SELECT is_available INTO v_available
    FROM `book`
    WHERE book_id = p_book_id
    LIMIT 1;
    
    RETURN v_available;
END$$
DELIMITER ;


-- ============================================================
-- PHẦN 5: STORED PROCEDURES (Thủ tục lưu trữ)
-- ============================================================

-- -----------------------------------------------
-- PROCEDURE 1: sp_issue_book_safe
-- *** GIAO TÁC AN TOÀN CHO MƯỢN SÁCH ***
-- Sử dụng SELECT ... FOR UPDATE để khóa dòng, đảm bảo
-- khi 2 người cùng lúc mượn 1 cuốn sách thì chỉ 1 người
-- được mượn thành công, người còn lại sẽ nhận lỗi.
-- -----------------------------------------------
DROP PROCEDURE IF EXISTS sp_issue_book_safe;
DELIMITER $$
CREATE PROCEDURE sp_issue_book_safe(
    IN p_book_id INT,
    IN p_member_id INT,
    OUT p_result VARCHAR(255),
    OUT p_expected_return DATE
)
BEGIN
    DECLARE v_is_available TINYINT DEFAULT 0;
    DECLARE v_member_exists INT DEFAULT 0;
    DECLARE v_days_overdue INT DEFAULT 14;
    DECLARE v_new_log_id INT DEFAULT 0;
    
    -- Khai báo handler cho lỗi SQL → ROLLBACK
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'LOI: Da xay ra loi he thong, giao tac da duoc ROLLBACK!';
        SET p_expected_return = NULL;
    END;

    -- ========== BẮT ĐẦU GIAO TÁC ==========
    START TRANSACTION;
    
    -- Bước 1: Khóa dòng sách bằng SELECT ... FOR UPDATE
    -- Điều này đảm bảo nếu 2 session cùng lúc gọi procedure này
    -- cho cùng 1 cuốn sách, session thứ 2 phải CHỜ session đầu hoàn thành.
    SELECT is_available INTO v_is_available
    FROM `book`
    WHERE book_id = p_book_id
    FOR UPDATE;
    
    -- Bước 2: Kiểm tra sách có tồn tại không
    IF v_is_available IS NULL THEN
        ROLLBACK;
        SET p_result = 'LOI: Sach khong ton tai!';
        SET p_expected_return = NULL;
    
    -- Bước 3: Kiểm tra sách có sẵn không
    ELSEIF v_is_available = 0 THEN
        ROLLBACK;
        SET p_result = 'LOI: Sach da duoc nguoi khac muon, khong the muon!';
        SET p_expected_return = NULL;
    
    ELSE
        -- Bước 4: Kiểm tra thành viên có tồn tại không
        SELECT COUNT(*) INTO v_member_exists
        FROM `member`
        WHERE member_id = p_member_id;
        
        IF v_member_exists = 0 THEN
            ROLLBACK;
            SET p_result = 'LOI: Thanh vien khong ton tai!';
            SET p_expected_return = NULL;
        ELSE
            -- Bước 5: Lấy cấu hình số ngày cho mượn
            SELECT days_overdue INTO v_days_overdue
            FROM `fine` WHERE sl_no = 1
            LIMIT 1;
            
            -- Bước 6: Cập nhật trạng thái sách → không khả dụng
            UPDATE `book`
            SET is_available = FALSE, holder = p_member_id
            WHERE book_id = p_book_id;
            
            -- Bước 7: Tạo ID mới cho log
            SELECT COALESCE(MAX(log_id), 0) + 1 INTO v_new_log_id FROM `log`;
            
            -- Bước 8: Ghi log mượn sách
            INSERT INTO `log` (log_id, book_id, member_id, issue_date, return_date, fine)
            VALUES (v_new_log_id, p_book_id, p_member_id, CURDATE(), NULL, NULL);
            
            -- ========== COMMIT GIAO TÁC ==========
            COMMIT;
            
            SET p_expected_return = DATE_ADD(CURDATE(), INTERVAL v_days_overdue DAY);
            SET p_result = CONCAT('THANH CONG: Da cho muon sach! Han tra: ', p_expected_return);
        END IF;
    END IF;
    
END$$
DELIMITER ;

-- -----------------------------------------------
-- PROCEDURE 2: sp_return_book
-- Trả sách và tính tiền phạt tự động
-- -----------------------------------------------
DROP PROCEDURE IF EXISTS sp_return_book;
DELIMITER $$
CREATE PROCEDURE sp_return_book(
    IN p_book_id INT,
    IN p_member_id INT,
    OUT p_result VARCHAR(255),
    OUT p_fine_amount FLOAT
)
BEGIN
    DECLARE v_log_id INT;
    DECLARE v_issue_date DATE;
    DECLARE v_total_fine FLOAT DEFAULT 0.0;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'LOI: Da xay ra loi he thong khi tra sach!';
        SET p_fine_amount = 0;
    END;

    START TRANSACTION;
    
    -- Tìm bản ghi mượn sách chưa trả
    SELECT log_id, issue_date INTO v_log_id, v_issue_date
    FROM `log`
    WHERE book_id = p_book_id AND member_id = p_member_id AND return_date IS NULL
    LIMIT 1;
    
    IF v_log_id IS NULL THEN
        ROLLBACK;
        SET p_result = 'LOI: Khong tim thay ban ghi muon sach nay!';
        SET p_fine_amount = 0;
    ELSE
        -- Tính tiền phạt bằng function
        SET v_total_fine = fn_calculate_fine(v_issue_date, CURDATE());
        
        -- Cập nhật log
        UPDATE `log`
        SET return_date = CURDATE(), fine = v_total_fine
        WHERE log_id = v_log_id;
        
        -- Cập nhật trạng thái sách
        UPDATE `book`
        SET is_available = TRUE, holder = NULL
        WHERE book_id = p_book_id;
        
        COMMIT;
        
        SET p_fine_amount = v_total_fine;
        IF v_total_fine > 0 THEN
            SET p_result = CONCAT('DA TRA SACH! Phat: ', v_total_fine, ' VND');
        ELSE
            SET p_result = 'DA TRA SACH! Khong bi phat.';
        END IF;
    END IF;
    
END$$
DELIMITER ;

-- -----------------------------------------------
-- PROCEDURE 3: sp_library_report
-- Báo cáo tổng hợp thư viện
-- -----------------------------------------------
DROP PROCEDURE IF EXISTS sp_library_report;
DELIMITER $$
CREATE PROCEDURE sp_library_report()
BEGIN
    -- Thống kê tổng quan
    SELECT 
        (SELECT COUNT(*) FROM `book`) AS total_books,
        (SELECT COUNT(*) FROM `book` WHERE is_available = TRUE) AS available_books,
        (SELECT COUNT(*) FROM `book` WHERE is_available = FALSE) AS borrowed_books,
        (SELECT COUNT(*) FROM `member`) AS total_members,
        (SELECT COUNT(*) FROM `log` WHERE return_date IS NULL) AS active_loans,
        (SELECT COALESCE(SUM(fine), 0) FROM `log` WHERE fine > 0) AS total_fines_collected;
    
    -- Top 5 thành viên mượn sách nhiều nhất
    SELECT 
        m.member_id,
        m.name,
        COUNT(l.log_id) AS total_borrows,
        COALESCE(SUM(l.fine), 0) AS total_fines
    FROM `member` m
    LEFT JOIN `log` l ON m.member_id = l.member_id
    GROUP BY m.member_id, m.name
    ORDER BY total_borrows DESC
    LIMIT 5;
    
    -- Top 5 sách được mượn nhiều nhất
    SELECT 
        b.book_id,
        b.title,
        b.author,
        COUNT(l.log_id) AS times_borrowed
    FROM `book` b
    LEFT JOIN `log` l ON b.book_id = l.book_id
    GROUP BY b.book_id, b.title, b.author
    ORDER BY times_borrowed DESC
    LIMIT 5;
END$$
DELIMITER ;

-- -----------------------------------------------
-- PROCEDURE 4: sp_search_books
-- Tìm kiếm sách theo từ khóa (title hoặc author)
-- -----------------------------------------------
DROP PROCEDURE IF EXISTS sp_search_books;
DELIMITER $$
CREATE PROCEDURE sp_search_books(IN p_keyword VARCHAR(255))
BEGIN
    SELECT 
        book_id,
        title,
        author,
        genre,
        publication,
        CASE WHEN is_available = TRUE THEN 'Co san' ELSE 'Dang muon' END AS status
    FROM `book`
    WHERE title LIKE CONCAT('%', p_keyword, '%')
       OR author LIKE CONCAT('%', p_keyword, '%')
    ORDER BY title;
END$$
DELIMITER ;


-- ============================================================
-- PHẦN 6: TRIGGERS (Bẫy sự kiện)
-- ============================================================

-- -----------------------------------------------
-- Bảng phụ trợ cho Trigger: audit_log (Nhật ký kiểm toán)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `table_name` VARCHAR(50) NOT NULL,
    `action` VARCHAR(20) NOT NULL,          -- INSERT, UPDATE, DELETE
    `record_id` VARCHAR(50) NOT NULL,
    `old_values` TEXT,
    `new_values` TEXT,
    `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `description` VARCHAR(500)
);

-- -----------------------------------------------
-- TRIGGER 1: trg_book_after_update
-- Ghi log kiểm toán khi trạng thái sách thay đổi (mượn/trả)
-- -----------------------------------------------
DROP TRIGGER IF EXISTS trg_book_after_update;
DELIMITER $$
CREATE TRIGGER trg_book_after_update
AFTER UPDATE ON `book`
FOR EACH ROW
BEGIN
    IF OLD.is_available != NEW.is_available THEN
        INSERT INTO `audit_log` (`table_name`, `action`, `record_id`, `old_values`, `new_values`, `description`)
        VALUES (
            'book',
            'UPDATE',
            CAST(NEW.book_id AS CHAR),
            CONCAT('is_available=', OLD.is_available, ', holder=', COALESCE(CAST(OLD.holder AS CHAR), 'NULL')),
            CONCAT('is_available=', NEW.is_available, ', holder=', COALESCE(CAST(NEW.holder AS CHAR), 'NULL')),
            CASE 
                WHEN NEW.is_available = FALSE THEN CONCAT('Sach "', NEW.title, '" da duoc muon boi member_id=', COALESCE(CAST(NEW.holder AS CHAR), '?'))
                ELSE CONCAT('Sach "', NEW.title, '" da duoc tra lai')
            END
        );
    END IF;
END$$
DELIMITER ;

-- -----------------------------------------------
-- TRIGGER 2: trg_member_before_delete
-- Ngăn xóa thành viên nếu đang mượn sách chưa trả
-- -----------------------------------------------
DROP TRIGGER IF EXISTS trg_member_before_delete;
DELIMITER $$
CREATE TRIGGER trg_member_before_delete
BEFORE DELETE ON `member`
FOR EACH ROW
BEGIN
    DECLARE v_active_loans INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_active_loans
    FROM `log`
    WHERE member_id = OLD.member_id AND return_date IS NULL;
    
    IF v_active_loans > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Khong the xoa thanh vien dang muon sach chua tra!';
    END IF;
END$$
DELIMITER ;

-- -----------------------------------------------
-- TRIGGER 3: trg_book_before_delete
-- Ngăn xóa sách nếu đang được mượn
-- -----------------------------------------------
DROP TRIGGER IF EXISTS trg_book_before_delete;
DELIMITER $$
CREATE TRIGGER trg_book_before_delete
BEFORE DELETE ON `book`
FOR EACH ROW
BEGIN
    IF OLD.is_available = FALSE THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Khong the xoa sach dang duoc muon!';
    END IF;
END$$
DELIMITER ;

-- -----------------------------------------------
-- TRIGGER 4: trg_log_before_insert
-- Tự động gán ngày mượn = ngày hiện tại nếu chưa có,
-- và tự động tạo log_id
-- -----------------------------------------------
DROP TRIGGER IF EXISTS trg_log_before_insert;
DELIMITER $$
CREATE TRIGGER trg_log_before_insert
BEFORE INSERT ON `log`
FOR EACH ROW
BEGIN
    -- Nếu issue_date chưa được gán, tự động gán ngày hiện tại
    IF NEW.issue_date IS NULL THEN
        SET NEW.issue_date = CURDATE();
    END IF;
    
    -- Nếu log_id = 0 (chưa gán), tự động tăng
    IF NEW.log_id = 0 OR NEW.log_id IS NULL THEN
        SET NEW.log_id = (SELECT COALESCE(MAX(log_id), 0) + 1 FROM `log`);
    END IF;
END$$
DELIMITER ;


-- ============================================================
-- PHẦN 7: GIAO TÁC MẪU (TRANSACTION DEMO)
-- Minh họa việc 2 session cùng mượn 1 sách
-- ============================================================

-- -----------------------------------------------
-- Ví dụ gọi giao tác mượn sách an toàn:
-- -----------------------------------------------
-- Giả sử thành viên 2 (Trần Thị Bích) muốn mượn sách 1 (Dế Mèn Phiêu Lưu Ký)
-- Sách 1 hiện đang available = TRUE

-- Session 1 gọi:
-- CALL sp_issue_book_safe(1, 2, @result1, @return_date1);
-- SELECT @result1, @return_date1;

-- Session 2 gọi ĐỒNG THỜI (cùng sách 1, member khác):
-- CALL sp_issue_book_safe(1, 4, @result2, @return_date2);
-- SELECT @result2, @return_date2;

-- KẾT QUẢ:
-- → Session 1: "THANH CONG: Da cho muon sach! Han tra: 2025-06-xx"
-- → Session 2: "LOI: Sach da duoc nguoi khac muon, khong the muon!"
-- → Nhờ SELECT ... FOR UPDATE, dữ liệu luôn NHẤT QUÁN.

-- -----------------------------------------------
-- Demo thực tế: Mượn sách 1 cho thành viên 2
-- -----------------------------------------------
CALL sp_issue_book_safe(1, 2, @result, @return_date);
SELECT @result AS ket_qua, @return_date AS han_tra;


-- ============================================================
-- PHẦN 8: KIỂM TRA DỮ LIỆU ĐÃ CHÈN
-- ============================================================
SELECT '===== DANH SACH SACH =====' AS '';
SELECT * FROM `book`;

SELECT '===== DANH SACH THANH VIEN =====' AS '';
SELECT * FROM `member`;

SELECT '===== NHAT KY MUON TRA =====' AS '';
SELECT * FROM `log`;

SELECT '===== CAU HINH PHAT =====' AS '';
SELECT * FROM `fine`;

SELECT '===== VIEW: SACH QUA HAN =====' AS '';
SELECT * FROM v_overdue_books;

SELECT '===== VIEW: THANH VIEN DANG MUON =====' AS '';
SELECT * FROM v_member_borrowing;

SELECT '===== VIEW: THONG KE TAC GIA =====' AS '';
SELECT * FROM v_author_statistics;

SELECT '===== BAO CAO THU VIEN =====' AS '';
CALL sp_library_report();

SELECT '===== HOAN TAT! =====' AS '';
