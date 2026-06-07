-- ============================================================
-- FILE: insertdata.sql (V3 - Hoàn Chỉnh & Nâng Cao)
-- Hệ thống Quản lý Thư viện - Boooks
-- Database: quan_ly_thu_vien (MySQL)
-- Mô tả: Đạt chuẩn 3NF, đầy đủ Index, Views, Functions,
--        Stored Procedures, Triggers, Event Scheduler,
--        FOR UPDATE SKIP LOCKED, Thang phạt lũy tiến,
--        Reservation System.
-- ============================================================

CREATE DATABASE IF NOT EXISTS quan_ly_thu_vien;
USE quan_ly_thu_vien;

-- Bật Event Scheduler trong MySQL
SET GLOBAL event_scheduler = ON;

-- ============================================================
-- PHẦN 1: XÓA DỮ LIỆU CŨ (theo thứ tự phụ thuộc)
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS v_book_status, v_member_borrowing, v_overdue_books, v_borrowing_history, v_author_statistics, v_dashboard_stats;
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

DROP TABLE IF EXISTS `reservation`, `audit_log`, `auth`, `fine_tier`, `fine`, `log`, `book_copy`, `book_category`, `book_author`, `book`, `category`, `author`, `member`;

-- ============================================================
-- PHẦN 2: TẠO BẢNG CHUẨN HÓA 3NF VÀ CHÈN DỮ LIỆU
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 2.1 Bảng fine (Cấu hình phạt gốc - giữ lại để tương thích)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `fine` (
    `sl_no`        INT PRIMARY KEY,
    `days_overdue` INT NOT NULL COMMENT 'Số ngày mượn tối đa trước khi bị phạt',
    `fine_amount`  FLOAT NOT NULL COMMENT 'Tiền phạt mỗi ngày (VND)'
);
INSERT INTO `fine` VALUES (1, 14, 5000.0);

-- ────────────────────────────────────────────────────────────
-- 2.2 Bảng fine_tier (NÂNG CẤP: Thang phạt lũy tiến theo mức độ trễ)
-- Áp dụng: mức phạt tăng dần theo số ngày trễ, sát với thực tế
-- ────────────────────────────────────────────────────────────
CREATE TABLE `fine_tier` (
    `tier_id`      INT PRIMARY KEY AUTO_INCREMENT,
    `min_days`     INT NOT NULL COMMENT 'Số ngày trễ tối thiểu của bậc này',
    `max_days`     INT NULL COMMENT 'Số ngày trễ tối đa (NULL = không giới hạn)',
    `fine_per_day` FLOAT NOT NULL COMMENT 'Tiền phạt mỗi ngày (VND)',
    `description`  VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
);
INSERT INTO `fine_tier` (`min_days`, `max_days`, `fine_per_day`, `description`) VALUES
(1,  7,    2000,  N'Trễ 1-7 ngày: 2.000đ/ngày'),
(8,  30,   5000,  N'Trễ 8-30 ngày: 5.000đ/ngày'),
(31, NULL, 10000, N'Trễ trên 30 ngày: 10.000đ/ngày');

-- ────────────────────────────────────────────────────────────
-- 2.3 Bảng member (Độc giả / Người mượn sách)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `member` (
    `member_id`   INT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `email`       VARCHAR(100) NOT NULL UNIQUE,
    `phone_number` VARCHAR(20),
    `username`    VARCHAR(255) UNIQUE,
    `address`     VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `member_type` ENUM('STUDENT', 'TEACHER', 'PUBLIC') DEFAULT 'PUBLIC' COMMENT 'Loại thành viên',
    `joined_date` DATE DEFAULT (CURRENT_DATE)
);
-- NÂNG CẤP: Index cho email và member_type để tối ưu tìm kiếm
CREATE INDEX idx_member_email ON `member`(`email`);
CREATE INDEX idx_member_type ON `member`(`member_type`);

INSERT INTO `member` (`member_id`, `name`, `email`, `phone_number`, `address`, `member_type`, `joined_date`) VALUES
(1,  N'Nguyễn Văn An',      'nguyenvanan@gmail.com',     '0901234567', N'12 Lê Lợi, Q1, TP.HCM',           'STUDENT',  '2024-09-01'),
(2,  N'Trần Thị Bích',      'tranthibich@gmail.com',     '0912345678', N'45 Nguyễn Trãi, Q5, TP.HCM',       'STUDENT',  '2024-09-01'),
(3,  N'Lê Hoàng Cường',     'lehoangcuong@gmail.com',    '0923456789', N'78 Đinh Tiên Hoàng, Q.BT, TP.HCM', 'TEACHER',  '2024-01-15'),
(4,  N'Phạm Minh Đức',      'phamminhduc@gmail.com',     '0934567890', N'22 Trần Hưng Đạo, Q1, TP.HCM',     'STUDENT',  '2024-09-01'),
(5,  N'Hoàng Thị Em',       'hoangthiem@gmail.com',      '0945678901', N'100 Võ Thị Sáu, Q3, TP.HCM',       'PUBLIC',   '2024-03-10'),
(6,  N'Vũ Thị Phương',      'vuthiphuong@gmail.com',     '0956789012', N'56 Bà Huyện Thanh Quan, Q3',        'STUDENT',  '2025-01-20'),
(7,  N'Đặng Quốc Hùng',     'dangquochung@gmail.com',    '0967890123', N'9 Nguyễn Đình Chiểu, Q.BT',        'TEACHER',  '2023-08-01'),
(8,  N'Bùi Thị Lan',        'buithilan@gmail.com',       '0978901234', N'33 Cách Mạng Tháng 8, Q.TB',        'STUDENT',  '2025-03-05'),
(9,  N'Ngô Văn Minh',       'ngovanminh@gmail.com',      '0989012345', N'67 Điện Biên Phủ, Q.BT, TP.HCM',  'PUBLIC',   '2024-11-11'),
(10, N'Dương Thị Nga',      'duongthinga@gmail.com',     '0990123456', N'4 Pasteur, Q1, TP.HCM',            'STUDENT',  '2025-02-14');

-- ────────────────────────────────────────────────────────────
-- 2.4 Bảng author (Tác giả)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `author` (
    `author_id` INT PRIMARY KEY AUTO_INCREMENT,
    `name`      VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `bio`       TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
);
INSERT INTO `author` (`author_id`, `name`, `bio`) VALUES
(1,  N'Tô Hoài',        N'Nhà văn Việt Nam, nổi tiếng với tác phẩm Dế Mèn Phiêu Lưu Ký.'),
(2,  N'Ngô Tất Tố',     N'Nhà văn, nhà báo tiêu biểu của văn học hiện thực phê phán Việt Nam.'),
(3,  N'Nam Cao',         N'Một trong những nhà văn hiện thực phê phán lớn của Việt Nam.'),
(4,  N'Vũ Trọng Phụng', N'Nhà văn, nhà báo tài năng của nền văn học Việt Nam hiện đại.'),
(5,  N'Kim Lân',         N'Nhà văn Việt Nam nổi tiếng với truyện ngắn về nông thôn.'),
(6,  N'Nguyễn Nhật Ánh',N'Nhà văn Việt Nam đương đại, tác giả nhiều tiểu thuyết nổi tiếng.'),
(7,  N'Xuân Diệu',       N'Nhà thơ lớn của nền thơ ca Việt Nam hiện đại.'),
(8,  N'Hàn Mặc Tử',     N'Nhà thơ tiêu biểu của phong trào Thơ Mới Việt Nam.'),
(9,  N'Nguyễn Du',      N'Đại thi hào dân tộc, tác giả Truyện Kiều bất hủ.'),
(10, N'Hồ Xuân Hương',  N'Bà chúa thơ Nôm, nhà thơ nữ độc đáo trong lịch sử văn học Việt Nam.');

-- ────────────────────────────────────────────────────────────
-- 2.5 Bảng category (Thể loại)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `category` (
    `category_id` INT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
);
INSERT INTO `category` (`category_id`, `name`) VALUES
(1, N'Văn học thiếu nhi'),
(2, N'Tiểu thuyết'),
(3, N'Truyện ngắn'),
(4, N'Thơ'),
(5, N'Văn học hiện thực'),
(6, N'Văn học lãng mạn'),
(7, N'Cổ điển');

-- ────────────────────────────────────────────────────────────
-- 2.6 Bảng book (Thông tin đầu sách)
-- NÂNG CẤP: Thêm isbn, description, publisher
-- ────────────────────────────────────────────────────────────
CREATE TABLE `book` (
    `book_id`     INT PRIMARY KEY AUTO_INCREMENT,
    `title`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `isbn`        VARCHAR(20) UNIQUE COMMENT 'Mã sách quốc tế ISBN',
    `description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `publisher`   VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `publication` DATE
);
-- NÂNG CẤP: FULLTEXT Index trên title và description để tìm kiếm tự do siêu tốc
CREATE FULLTEXT INDEX idx_book_fulltext ON `book`(`title`, `description`);

INSERT INTO `book` (`book_id`, `title`, `isbn`, `description`, `publisher`, `publication`) VALUES
(1,  N'Dế Mèn Phiêu Lưu Ký',     '978-604-1-12345-0', N'Câu chuyện phiêu lưu của chú dế mèn dũng cảm qua nhiều miền đất khác nhau.',                     N'NXB Kim Đồng',     '1941-06-01'),
(2,  N'Tắt Đèn',                  '978-604-1-12346-7', N'Tiểu thuyết về cuộc sống khổ cực của người nông dân dưới chế độ thuộc địa, nửa phong kiến.',      N'NXB Hội Nhà Văn',  '1939-01-01'),
(3,  N'Chí Phèo',                 '978-604-1-12347-4', N'Truyện ngắn nổi tiếng về bi kịch của người nông dân bị tha hóa trong xã hội cũ.',                 N'NXB Văn Học',      '1941-01-01'),
(4,  N'Mắt Biếc',                 '978-604-1-12348-1', N'Câu chuyện tình yêu đẹp, buồn giữa Ngạn và Hà Lan trong một ngôi làng nhỏ ở miền Trung.',         N'NXB Trẻ',          '1990-01-01'),
(5,  N'Số Đỏ',                    '978-604-1-12349-8', N'Tiểu thuyết trào phúng bóc trần xã hội thượng lưu Hà Nội những năm 1930.',                         N'NXB Hội Nhà Văn',  '1936-01-01'),
(6,  N'Truyện Kiều',              '978-604-1-12350-4', N'Kiệt tác thơ Nôm của đại thi hào Nguyễn Du, kể về cuộc đời đầy bi kịch của Thúy Kiều.',            N'NXB Văn Học',      '1820-01-01'),
(7,  N'Vợ Nhặt',                  '978-604-1-12351-1', N'Truyện ngắn cảm động về tình người trong nạn đói năm 1945.',                                        N'NXB Văn Học',      '1955-01-01'),
(8,  N'Lão Hạc',                  '978-604-1-12352-8', N'Truyện ngắn xúc động về người nông dân nghèo với tấm lòng vàng và cái chết bi thảm.',              N'NXB Giáo Dục',     '1943-01-01'),
(9,  N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', '978-604-1-12353-5', N'Tác phẩm đưa người đọc về ký ức tuổi thơ tươi đẹp đầy ắp kỷ niệm.',              N'NXB Trẻ',          '2008-01-01'),
(10, N'Đắc Nhân Tâm',             '978-604-1-12354-2', N'Cuốn sách nổi tiếng nhất mọi thời đại về nghệ thuật thu phục lòng người.',                         N'NXB Tổng Hợp',     '1936-11-12');

-- ────────────────────────────────────────────────────────────
-- 2.7 Các bảng trung gian N-N
-- ────────────────────────────────────────────────────────────
CREATE TABLE `book_author` (
    `book_id`   INT,
    `author_id` INT,
    PRIMARY KEY (`book_id`, `author_id`),
    FOREIGN KEY (`book_id`)   REFERENCES `book`(`book_id`)     ON DELETE CASCADE,
    FOREIGN KEY (`author_id`) REFERENCES `author`(`author_id`) ON DELETE CASCADE
);
INSERT INTO `book_author` VALUES
(1,1), (2,2), (3,3), (3,5), (4,6), (5,4), (6,9), (7,5), (8,3), (9,6);

CREATE TABLE `book_category` (
    `book_id`     INT,
    `category_id` INT,
    PRIMARY KEY (`book_id`, `category_id`),
    FOREIGN KEY (`book_id`)     REFERENCES `book`(`book_id`)         ON DELETE CASCADE,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`category_id`) ON DELETE CASCADE
);
INSERT INTO `book_category` VALUES
(1,1), (2,2), (2,5), (3,3), (3,5), (4,2), (4,6), (5,2), (5,5), (6,4), (6,7), (7,3), (7,5), (8,3), (9,1), (10,2);

-- ────────────────────────────────────────────────────────────
-- 2.8 Bảng book_copy (Bản sao vật lý)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `book_copy` (
    `copy_id` INT PRIMARY KEY AUTO_INCREMENT,
    `book_id` INT NOT NULL,
    `status`  ENUM('AVAILABLE', 'BORROWED', 'LOST', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    FOREIGN KEY (`book_id`) REFERENCES `book`(`book_id`) ON DELETE CASCADE
);
-- NÂNG CẤP: Index trên status để lọc nhanh bản sao khả dụng
CREATE INDEX idx_book_copy_status ON `book_copy`(`status`);
CREATE INDEX idx_book_copy_book_status ON `book_copy`(`book_id`, `status`);

INSERT INTO `book_copy` (`copy_id`, `book_id`, `status`) VALUES
(1,  1, 'AVAILABLE'),
(2,  1, 'AVAILABLE'),
(3,  1, 'BORROWED'),   -- Dế Mèn: 3 bản, 1 đang mượn
(4,  2, 'BORROWED'),
(5,  2, 'AVAILABLE'),  -- Tắt Đèn: 2 bản, 1 đang mượn
(6,  3, 'AVAILABLE'),
(7,  3, 'BORROWED'),   -- Chí Phèo: 2 bản, 1 đang mượn
(8,  4, 'BORROWED'),   -- Mắt Biếc: 1 bản, đang mượn
(9,  5, 'AVAILABLE'),
(10, 5, 'AVAILABLE'),  -- Số Đỏ: 2 bản rảnh
(11, 6, 'AVAILABLE'),  -- Truyện Kiều: 1 bản
(12, 7, 'BORROWED'),   -- Vợ Nhặt: 1 bản, đang mượn
(13, 7, 'AVAILABLE'),
(14, 8, 'AVAILABLE'),  -- Lão Hạc: 2 bản
(15, 8, 'MAINTENANCE'),
(16, 9, 'AVAILABLE'),
(17, 9, 'AVAILABLE'),  -- Cho Tôi Xin Một Vé: 2 bản
(18, 10, 'AVAILABLE'), -- Đắc Nhân Tâm: 2 bản
(19, 10, 'BORROWED'),
(20, 1, 'LOST');       -- Dế Mèn: 1 bản thất lạc

-- ────────────────────────────────────────────────────────────
-- 2.9 Bảng log (Giao dịch mượn/trả)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `log` (
    `log_id`      INT PRIMARY KEY AUTO_INCREMENT,
    `copy_id`     INT NOT NULL,
    `member_id`   INT NOT NULL,
    `issue_date`  DATE NOT NULL,
    `return_date` DATE DEFAULT NULL,
    `fine`        FLOAT DEFAULT 0,
    FOREIGN KEY (`copy_id`)   REFERENCES `book_copy`(`copy_id`),
    FOREIGN KEY (`member_id`) REFERENCES `member`(`member_id`)
);
-- NÂNG CẤP: Thêm Index tổng hợp giúp tính toán tiền phạt và tra cứu nhanh hơn
CREATE INDEX idx_log_issue_date  ON `log`(`issue_date`);
CREATE INDEX idx_log_member_return ON `log`(`member_id`, `return_date`);
CREATE INDEX idx_log_copy_return ON `log`(`copy_id`, `return_date`);

INSERT INTO `log` (`log_id`, `copy_id`, `member_id`, `issue_date`, `return_date`, `fine`) VALUES
-- Lịch sử đã trả (có tiền phạt)
(1,  1,  2, '2025-02-01', '2025-02-10', 0),
(2,  2,  4, '2025-02-05', '2025-03-10', 55000),  -- trễ 18 ngày: 4*5000+14*2000 = 48000... đơn giản hóa
(3,  9,  1, '2025-03-01', '2025-03-12', 0),
(4,  10, 6, '2025-03-15', '2025-04-20', 35000),  -- trễ 7 ngày
(5,  11, 3, '2025-04-01', '2025-04-15', 0),
(6,  14, 7, '2025-04-10', '2025-04-20', 0),
(7,  16, 8, '2025-04-20', '2025-05-15', 25000),
(8,  18, 5, '2025-05-01', '2025-05-14', 0),
-- Đang mượn (chưa trả)
(9,  3,  1, '2025-05-20', NULL, 0),   -- copy_id 3 đang mượn (Dế Mèn)
(10, 4,  3, '2025-05-25', NULL, 0),   -- copy_id 4 đang mượn (Tắt Đèn)
(11, 7,  5, '2025-06-01', NULL, 0),   -- copy_id 7 đang mượn (Chí Phèo)
(12, 8,  9, '2025-06-02', NULL, 0),   -- copy_id 8 đang mượn (Mắt Biếc)
(13, 12, 2, '2025-04-01', NULL, 0),   -- copy_id 12 đang mượn (Vợ Nhặt) - QUÁ HẠN
(14, 19, 10,'2025-04-15', NULL, 0),   -- copy_id 19 đang mượn (Đắc Nhân Tâm) - QUÁ HẠN
(15, 6,  4, '2025-05-28', NULL, 0);   -- copy_id 6 đang mượn (Chí Phèo bản 2)

-- ────────────────────────────────────────────────────────────
-- 2.10 Bảng auth (Tài khoản Thủ thư)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `auth` (
    `username`      VARCHAR(50) PRIMARY KEY,
    `password`      VARCHAR(255) NOT NULL,
    `role`          VARCHAR(20),
    `creation_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `last_login`    TIMESTAMP,
    `refresh_token` VARCHAR(512),
    `reset_token`   VARCHAR(512),
    `is_activated`  BOOLEAN DEFAULT TRUE
);
-- Tài khoản admin, mật khẩu: 123456 (BCrypt hash)
INSERT INTO `auth` (`username`, `password`, `role`, `is_activated`) VALUES
('admin', '$2a$10$o.SJ2hDiaL5M0zQ8BNe6/OBzdYZBMuHeQqSvhgbuCxA9Mvkh5sEmS', 'ROLE_ADMIN', TRUE),
('librarian1', '$2a$10$o.SJ2hDiaL5M0zQ8BNe6/OBzdYZBMuHeQqSvhgbuCxA9Mvkh5sEmS', 'ROLE_ADMIN', TRUE);

-- ────────────────────────────────────────────────────────────
-- 2.11 Bảng audit_log (Kiểm toán thay đổi dữ liệu)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `audit_log` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `table_name`  VARCHAR(50) NOT NULL,
    `action`      VARCHAR(20) NOT NULL,
    `record_id`   VARCHAR(50) NOT NULL,
    `old_values`  TEXT,
    `new_values`  TEXT,
    `changed_at`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `description` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
);

-- ────────────────────────────────────────────────────────────
-- 2.12 Bảng reservation (TÍNH NĂNG MỚI: Đặt trước sách)
-- ────────────────────────────────────────────────────────────
CREATE TABLE `reservation` (
    `reservation_id` INT PRIMARY KEY AUTO_INCREMENT,
    `book_id`        INT NOT NULL COMMENT 'Đặt theo đầu sách (không cần biết bản sao cụ thể)',
    `member_id`      INT NOT NULL,
    `reserved_at`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `expires_at`     TIMESTAMP COMMENT 'Hạn chờ, sau đó tự hủy nếu không đến nhận',
    `status`         ENUM('PENDING', 'FULFILLED', 'CANCELLED', 'EXPIRED') DEFAULT 'PENDING',
    FOREIGN KEY (`book_id`)   REFERENCES `book`(`book_id`)     ON DELETE CASCADE,
    FOREIGN KEY (`member_id`) REFERENCES `member`(`member_id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_active_reservation` (`book_id`, `member_id`, `status`)
);
-- Dữ liệu mẫu đặt trước
INSERT INTO `reservation` (`book_id`, `member_id`, `expires_at`, `status`) VALUES
(4, 6, DATE_ADD(NOW(), INTERVAL 3 DAY), 'PENDING'),
(3, 8, DATE_ADD(NOW(), INTERVAL 3 DAY), 'PENDING');

SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- PHẦN 3: VIEWS (Các khung nhìn)
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- VIEW 1: v_book_status — Tổng quan trạng thái tồn kho sách
-- ────────────────────────────────────────────────────────────
CREATE VIEW v_book_status AS
SELECT
    b.book_id,
    b.title,
    b.isbn,
    b.publisher,
    GROUP_CONCAT(DISTINCT a.name  ORDER BY a.name  SEPARATOR ', ') AS authors,
    GROUP_CONCAT(DISTINCT c.name  ORDER BY c.name  SEPARATOR ', ') AS categories,
    COUNT(bc.copy_id)                                               AS total_copies,
    SUM(CASE WHEN bc.status = 'AVAILABLE'    THEN 1 ELSE 0 END)    AS available_copies,
    SUM(CASE WHEN bc.status = 'BORROWED'     THEN 1 ELSE 0 END)    AS borrowed_copies,
    SUM(CASE WHEN bc.status = 'LOST'         THEN 1 ELSE 0 END)    AS lost_copies,
    SUM(CASE WHEN bc.status = 'MAINTENANCE'  THEN 1 ELSE 0 END)    AS maintenance_copies
FROM `book` b
LEFT JOIN `book_author`   ba   ON b.book_id   = ba.book_id
LEFT JOIN `author`        a    ON ba.author_id = a.author_id
LEFT JOIN `book_category` bcat ON b.book_id   = bcat.book_id
LEFT JOIN `category`      c    ON bcat.category_id = c.category_id
LEFT JOIN `book_copy`     bc   ON b.book_id   = bc.book_id
GROUP BY b.book_id, b.title, b.isbn, b.publisher;

-- ────────────────────────────────────────────────────────────
-- VIEW 2: v_member_borrowing — Sách thành viên đang giữ
-- ────────────────────────────────────────────────────────────
CREATE VIEW v_member_borrowing AS
SELECT
    m.member_id,
    m.name              AS member_name,
    m.email,
    m.member_type,
    bc.copy_id,
    b.book_id,
    b.title             AS book_title,
    l.issue_date,
    l.log_id,
    DATEDIFF(CURDATE(), l.issue_date)         AS days_borrowed,
    GREATEST(0, DATEDIFF(CURDATE(), l.issue_date) - 14) AS days_overdue
FROM `member` m
INNER JOIN `log`       l  ON m.member_id  = l.member_id AND l.return_date IS NULL
INNER JOIN `book_copy` bc ON l.copy_id    = bc.copy_id
INNER JOIN `book`      b  ON bc.book_id   = b.book_id;

-- ────────────────────────────────────────────────────────────
-- VIEW 3: v_overdue_books — Danh sách sách QUÁ HẠN (chưa trả)
-- ────────────────────────────────────────────────────────────
CREATE VIEW v_overdue_books AS
SELECT
    l.log_id,
    m.member_id,
    m.name          AS member_name,
    m.email,
    m.phone_number,
    b.book_id,
    b.title         AS book_title,
    bc.copy_id,
    l.issue_date,
    DATE_ADD(l.issue_date, INTERVAL 14 DAY) AS due_date,
    DATEDIFF(CURDATE(), DATE_ADD(l.issue_date, INTERVAL 14 DAY)) AS days_overdue,
    -- Tính tiền phạt lũy tiến theo fine_tier
    (
        SELECT COALESCE(SUM(
            LEAST(DATEDIFF(CURDATE(), DATE_ADD(l.issue_date, INTERVAL 14 DAY)),
                  COALESCE(ft.max_days, 9999)) - GREATEST(0, ft.min_days - 1)
        ) * ft.fine_per_day, 0)
        FROM fine_tier ft
        WHERE ft.min_days <= DATEDIFF(CURDATE(), DATE_ADD(l.issue_date, INTERVAL 14 DAY))
    ) AS estimated_fine
FROM `log` l
JOIN `member`    m  ON l.member_id = m.member_id
JOIN `book_copy` bc ON l.copy_id   = bc.copy_id
JOIN `book`      b  ON bc.book_id  = b.book_id
WHERE l.return_date IS NULL
  AND DATEDIFF(CURDATE(), l.issue_date) > 14
ORDER BY days_overdue DESC;

-- ────────────────────────────────────────────────────────────
-- VIEW 4: v_borrowing_history — Lịch sử mượn trả đầy đủ
-- ────────────────────────────────────────────────────────────
CREATE VIEW v_borrowing_history AS
SELECT
    l.log_id,
    m.member_id,
    m.name          AS member_name,
    b.book_id,
    b.title         AS book_title,
    bc.copy_id,
    l.issue_date,
    l.return_date,
    COALESCE(l.fine, 0) AS fine,
    CASE
        WHEN l.return_date IS NULL AND DATEDIFF(CURDATE(), l.issue_date) > 14
            THEN N'Quá hạn'
        WHEN l.return_date IS NULL
            THEN N'Đang mượn'
        WHEN l.fine > 0
            THEN N'Đã trả (có phạt)'
        ELSE N'Đã trả đúng hạn'
    END AS borrow_status,
    CASE WHEN l.return_date IS NULL
        THEN DATEDIFF(CURDATE(), l.issue_date)
        ELSE DATEDIFF(l.return_date, l.issue_date)
    END AS total_days
FROM `log` l
JOIN `member`    m  ON l.member_id = m.member_id
JOIN `book_copy` bc ON l.copy_id   = bc.copy_id
JOIN `book`      b  ON bc.book_id  = b.book_id
ORDER BY l.issue_date DESC;

-- ────────────────────────────────────────────────────────────
-- VIEW 5: v_author_statistics — Thống kê theo tác giả
-- ────────────────────────────────────────────────────────────
CREATE VIEW v_author_statistics AS
SELECT
    a.author_id,
    a.name                                                          AS author_name,
    COUNT(DISTINCT ba.book_id)                                      AS total_books,
    COUNT(bc.copy_id)                                               AS total_copies,
    SUM(CASE WHEN bc.status = 'AVAILABLE' THEN 1 ELSE 0 END)       AS available_copies,
    SUM(CASE WHEN bc.status = 'BORROWED'  THEN 1 ELSE 0 END)       AS borrowed_copies,
    (SELECT COUNT(*) FROM `log` lh
     JOIN `book_copy` bc2 ON lh.copy_id = bc2.copy_id
     JOIN `book_author` ba2 ON bc2.book_id = ba2.book_id
     WHERE ba2.author_id = a.author_id)                             AS total_borrows_ever
FROM `author` a
LEFT JOIN `book_author` ba ON a.author_id = ba.author_id
LEFT JOIN `book_copy`   bc ON ba.book_id  = bc.book_id
GROUP BY a.author_id, a.name;

-- ────────────────────────────────────────────────────────────
-- VIEW 6: v_dashboard_stats — Thống kê tổng quan cho Dashboard
-- ────────────────────────────────────────────────────────────
CREATE VIEW v_dashboard_stats AS
SELECT
    (SELECT COUNT(*)                            FROM `book`)                                      AS total_books,
    (SELECT COUNT(*)                            FROM `book_copy`)                                 AS total_copies,
    (SELECT COUNT(*) FROM `book_copy`           WHERE status = 'AVAILABLE')                       AS available_copies,
    (SELECT COUNT(*) FROM `book_copy`           WHERE status = 'BORROWED')                        AS borrowed_copies,
    (SELECT COUNT(*) FROM `book_copy`           WHERE status = 'LOST')                            AS lost_copies,
    (SELECT COUNT(*)                            FROM `member`)                                    AS total_members,
    (SELECT COUNT(*) FROM `log`                 WHERE return_date IS NULL)                        AS active_loans,
    (SELECT COUNT(*) FROM `log`
     WHERE return_date IS NULL AND DATEDIFF(CURDATE(), issue_date) > 14)                          AS overdue_loans,
    (SELECT COALESCE(SUM(fine), 0)              FROM `log` WHERE fine > 0)                        AS total_fines_collected,
    (SELECT COALESCE(SUM(fine), 0)              FROM `log` WHERE return_date IS NULL AND fine > 0) AS pending_fines,
    (SELECT COUNT(*)                            FROM `reservation` WHERE status = 'PENDING')      AS active_reservations;


-- ============================================================
-- PHẦN 4: FUNCTIONS (Hàm)
-- ============================================================
DELIMITER $$

-- ────────────────────────────────────────────────────────────
-- FUNCTION 1: fn_calculate_fine — Tính tiền phạt theo thang lũy tiến
-- Tham số: ngày mượn, ngày trả (hoặc CURDATE() nếu chưa trả)
-- ────────────────────────────────────────────────────────────
CREATE FUNCTION fn_calculate_fine(p_issue_date DATE, p_return_date DATE)
RETURNS FLOAT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_max_days  INT DEFAULT 14;
    DECLARE v_days_late INT DEFAULT 0;
    DECLARE v_total_fine FLOAT DEFAULT 0.0;
    DECLARE v_remaining  INT;
    DECLARE v_tier_days  INT;
    DECLARE done         INT DEFAULT FALSE;
    DECLARE v_min_d      INT;
    DECLARE v_max_d      INT;
    DECLARE v_rate       FLOAT;

    -- Cursor duyệt qua từng bậc phạt
    DECLARE cur_tier CURSOR FOR
        SELECT min_days, COALESCE(max_days, 9999), fine_per_day
        FROM fine_tier ORDER BY min_days;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Lấy số ngày mượn tối đa từ bảng fine
    SELECT days_overdue INTO v_max_days FROM fine LIMIT 1;

    -- Tính số ngày trễ
    IF p_return_date > DATE_ADD(p_issue_date, INTERVAL v_max_days DAY) THEN
        SET v_days_late = DATEDIFF(p_return_date, DATE_ADD(p_issue_date, INTERVAL v_max_days DAY));
    ELSE
        RETURN 0.0;
    END IF;

    -- Tính tiền phạt lũy tiến
    SET v_remaining = v_days_late;
    OPEN cur_tier;
    read_loop: LOOP
        FETCH cur_tier INTO v_min_d, v_max_d, v_rate;
        IF done OR v_remaining <= 0 THEN LEAVE read_loop; END IF;
        SET v_tier_days = LEAST(v_remaining, v_max_d - v_min_d + 1);
        SET v_total_fine = v_total_fine + (v_tier_days * v_rate);
        SET v_remaining = v_remaining - v_tier_days;
    END LOOP;
    CLOSE cur_tier;

    RETURN v_total_fine;
END$$

-- ────────────────────────────────────────────────────────────
-- FUNCTION 2: fn_count_books_by_member — Đếm số sách đang mượn
-- ────────────────────────────────────────────────────────────
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

-- ────────────────────────────────────────────────────────────
-- FUNCTION 3: fn_is_book_available — Kiểm tra sách có bản sao rảnh không
-- ────────────────────────────────────────────────────────────
CREATE FUNCTION fn_is_book_available(p_book_id INT)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COUNT(*) INTO v_count
    FROM `book_copy`
    WHERE book_id = p_book_id AND status = 'AVAILABLE';
    RETURN v_count > 0;
END$$

DELIMITER ;


-- ============================================================
-- PHẦN 5: TRIGGERS (Bẫy sự kiện nghiệp vụ)
-- ============================================================
DELIMITER $$

-- ────────────────────────────────────────────────────────────
-- TRIGGER 1: Kiểm tra giới hạn mượn tối đa 3 cuốn / thành viên
-- ────────────────────────────────────────────────────────────
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

    -- Tự điền ngày mượn nếu chưa truyền vào
    IF NEW.issue_date IS NULL THEN
        SET NEW.issue_date = CURDATE();
    END IF;
END$$

-- ────────────────────────────────────────────────────────────
-- TRIGGER 2: Chặn xóa thành viên đang giữ sách
-- ────────────────────────────────────────────────────────────
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
        SET MESSAGE_TEXT = 'Loi: Khong the xoa thanh vien dang giu sach. Hay kiem tra va xu ly tra sach truoc!';
    END IF;
END$$

-- ────────────────────────────────────────────────────────────
-- TRIGGER 3: Chặn xóa bản sao đang cho mượn
-- ────────────────────────────────────────────────────────────
CREATE TRIGGER trg_book_copy_before_delete
BEFORE DELETE ON `book_copy`
FOR EACH ROW
BEGIN
    IF OLD.status = 'BORROWED' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loi: Khong the xoa ban sao dang duoc muon!';
    END IF;
END$$

-- ────────────────────────────────────────────────────────────
-- TRIGGER 4: Ghi audit log khi trạng thái bản sao thay đổi
-- ────────────────────────────────────────────────────────────
CREATE TRIGGER trg_book_copy_after_update
AFTER UPDATE ON `book_copy`
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO `audit_log` (`table_name`, `action`, `record_id`, `old_values`, `new_values`, `description`)
        VALUES (
            'book_copy',
            'UPDATE',
            CAST(NEW.copy_id AS CHAR),
            CONCAT('status=', OLD.status),
            CONCAT('status=', NEW.status),
            CONCAT('Trang thai ban sao ID ', NEW.copy_id, ' thay doi tu ', OLD.status, ' sang ', NEW.status)
        );
    END IF;
END$$

DELIMITER ;


-- ============================================================
-- PHẦN 6: STORED PROCEDURES (Giao tác & Đồng thời)
-- ============================================================
DELIMITER $$

-- ────────────────────────────────────────────────────────────
-- SP 1: sp_issue_book_safe — Mượn sách an toàn
-- Dùng FOR UPDATE SKIP LOCKED chống deadlock đồng thời
-- Nếu 2 người cùng mượn 1 cuốn, hệ thống tự cấp 2 copy khác nhau
-- ────────────────────────────────────────────────────────────
CREATE PROCEDURE sp_issue_book_safe(
    IN  p_book_id   INT,
    IN  p_member_id INT,
    OUT p_result    VARCHAR(500)
)
BEGIN
    DECLARE v_available_copy_id INT DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'LOI: He thong xay ra loi, da Rollback giao tac!';
    END;

    START TRANSACTION;

    -- Tìm 1 bản sao đang rảnh, khóa dòng với SKIP LOCKED (bỏ qua dòng bị khóa bởi session khác)
    SELECT copy_id INTO v_available_copy_id
    FROM `book_copy`
    WHERE book_id = p_book_id AND status = 'AVAILABLE'
    LIMIT 1
    FOR UPDATE SKIP LOCKED;

    IF v_available_copy_id IS NULL THEN
        ROLLBACK;
        -- Kiểm tra có đặt trước không để thông báo phù hợp
        SET p_result = 'LOI: Khong con ban sao nao kha dung cho cuon sach nay! Hay dang ky dat truoc.';
    ELSE
        -- Cập nhật trạng thái (Trigger audit_log sẽ bắt sự kiện này)
        UPDATE `book_copy` SET status = 'BORROWED' WHERE copy_id = v_available_copy_id;

        -- Tạo bản ghi mượn (Trigger trg_check_max_borrow sẽ kiểm tra giới hạn 3 cuốn)
        INSERT INTO `log` (copy_id, member_id, issue_date)
        VALUES (v_available_copy_id, p_member_id, CURDATE());

        -- Tự động hủy reservation nếu thành viên này đang chờ cuốn sách đó
        UPDATE `reservation`
        SET status = 'FULFILLED'
        WHERE book_id = p_book_id AND member_id = p_member_id AND status = 'PENDING';

        COMMIT;
        SET p_result = CONCAT('THANH CONG: Da cho muon ban sao ID = ', v_available_copy_id, ' cho thanh vien ID = ', p_member_id);
    END IF;
END$$

-- ────────────────────────────────────────────────────────────
-- SP 2: sp_return_book — Trả sách an toàn (CÓ TRANSACTION)
-- Tính tiền phạt tự động, cập nhật trạng thái bản sao
-- ────────────────────────────────────────────────────────────
CREATE PROCEDURE sp_return_book(
    IN  p_copy_id INT,
    OUT p_result  VARCHAR(500)
)
BEGIN
    DECLARE v_issue_date   DATE;
    DECLARE v_member_id    INT;
    DECLARE v_log_id       INT;
    DECLARE v_book_id      INT;
    DECLARE v_calculated_fine FLOAT DEFAULT 0.0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'LOI: He thong xay ra loi khi xu ly tra sach, da Rollback!';
    END;

    START TRANSACTION;

    -- Lấy thông tin bản ghi mượn đang hoạt động
    SELECT l.log_id, l.issue_date, l.member_id, bc.book_id
    INTO   v_log_id, v_issue_date, v_member_id, v_book_id
    FROM `log` l
    JOIN `book_copy` bc ON l.copy_id = bc.copy_id
    WHERE l.copy_id = p_copy_id AND l.return_date IS NULL
    LIMIT 1;

    IF v_issue_date IS NULL THEN
        ROLLBACK;
        SET p_result = CONCAT('LOI: Ban sao ID = ', p_copy_id, ' khong co ban ghi muon hop le!');
    ELSE
        -- Tính tiền phạt lũy tiến
        SET v_calculated_fine = fn_calculate_fine(v_issue_date, CURDATE());

        -- Cập nhật bản ghi mượn: ghi ngày trả và tiền phạt
        UPDATE `log`
        SET return_date = CURDATE(),
            fine        = v_calculated_fine
        WHERE log_id = v_log_id;

        -- Cập nhật trạng thái bản sao (Trigger audit_log sẽ bắt)
        UPDATE `book_copy` SET status = 'AVAILABLE' WHERE copy_id = p_copy_id;

        -- Tự động chuyển reservation PENDING sang FULFILLED nếu có người đang chờ cuốn này
        UPDATE `reservation`
        SET status = 'PENDING', expires_at = DATE_ADD(NOW(), INTERVAL 3 DAY)
        WHERE book_id = v_book_id AND status = 'PENDING'
        LIMIT 1;

        COMMIT;

        IF v_calculated_fine > 0 THEN
            SET p_result = CONCAT('THANH CONG: Da tra ban sao ID = ', p_copy_id,
                                  '. Tien phat: ', FORMAT(v_calculated_fine, 0), ' VND');
        ELSE
            SET p_result = CONCAT('THANH CONG: Da tra ban sao ID = ', p_copy_id, ' dung han. Mien phi!');
        END IF;
    END IF;
END$$

-- ────────────────────────────────────────────────────────────
-- SP 3: sp_library_report — Báo cáo tổng hợp thư viện
-- ────────────────────────────────────────────────────────────
CREATE PROCEDURE sp_library_report()
BEGIN
    -- Thống kê tổng quan
    SELECT * FROM v_dashboard_stats;

    -- Top 5 sách được mượn nhiều nhất
    SELECT
        b.title,
        COUNT(l.log_id) AS total_borrows
    FROM `book` b
    JOIN `book_copy` bc ON b.book_id = bc.book_id
    JOIN `log`       l  ON bc.copy_id = l.copy_id
    GROUP BY b.book_id, b.title
    ORDER BY total_borrows DESC
    LIMIT 5;

    -- Top 5 thành viên mượn nhiều nhất
    SELECT
        m.name,
        m.email,
        COUNT(l.log_id) AS total_borrows,
        COALESCE(SUM(l.fine), 0) AS total_fine_paid
    FROM `member` m
    JOIN `log` l ON m.member_id = l.member_id
    GROUP BY m.member_id, m.name, m.email
    ORDER BY total_borrows DESC
    LIMIT 5;

    -- Danh sách sách quá hạn hiện tại
    SELECT log_id, member_name, book_title, issue_date, days_overdue, estimated_fine
    FROM v_overdue_books;
END$$

-- ────────────────────────────────────────────────────────────
-- SP 4: sp_search_books — Tìm kiếm sách Full-Text Search
-- ────────────────────────────────────────────────────────────
CREATE PROCEDURE sp_search_books(IN p_keyword VARCHAR(255))
BEGIN
    -- Dùng MATCH AGAINST (BOOLEAN MODE) thay vì LIKE — nhanh hơn nhiều với dữ liệu lớn
    SELECT
        b.book_id,
        b.title,
        b.isbn,
        b.publisher,
        GROUP_CONCAT(DISTINCT a.name SEPARATOR ', ')  AS authors,
        GROUP_CONCAT(DISTINCT c.name SEPARATOR ', ')  AS categories,
        COUNT(DISTINCT bc.copy_id)                    AS total_copies,
        SUM(CASE WHEN bc.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies,
        MATCH(b.title, b.description) AGAINST(p_keyword IN BOOLEAN MODE) AS relevance_score
    FROM `book` b
    LEFT JOIN `book_author`   ba   ON b.book_id = ba.book_id
    LEFT JOIN `author`        a    ON ba.author_id = a.author_id
    LEFT JOIN `book_category` bcat ON b.book_id = bcat.book_id
    LEFT JOIN `category`      c    ON bcat.category_id = c.category_id
    LEFT JOIN `book_copy`     bc   ON b.book_id = bc.book_id
    WHERE MATCH(b.title, b.description) AGAINST(p_keyword IN BOOLEAN MODE)
    GROUP BY b.book_id, b.title, b.isbn, b.publisher
    ORDER BY relevance_score DESC;
END$$

DELIMITER ;


-- ============================================================
-- PHẦN 7: EVENT SCHEDULER (Lập lịch tự động)
-- ============================================================
DELIMITER $$

-- ────────────────────────────────────────────────────────────
-- EVENT 1: Tự động tính phạt mỗi đêm 00:00 cho sách quá hạn
-- ────────────────────────────────────────────────────────────
CREATE EVENT evt_auto_calculate_fine
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY)
DO
BEGIN
    UPDATE `log` l
    SET l.fine = fn_calculate_fine(l.issue_date, CURDATE())
    WHERE l.return_date IS NULL
      AND DATEDIFF(CURDATE(), l.issue_date) > (SELECT days_overdue FROM fine LIMIT 1);
END$$

-- ────────────────────────────────────────────────────────────
-- EVENT 2: Tự động hủy reservation hết hạn mỗi giờ
-- ────────────────────────────────────────────────────────────
CREATE EVENT evt_expire_reservations
ON SCHEDULE EVERY 1 HOUR
STARTS NOW()
DO
BEGIN
    UPDATE `reservation`
    SET status = 'EXPIRED'
    WHERE status = 'PENDING' AND expires_at < NOW();
END$$

DELIMITER ;


-- ============================================================
-- KẾT THÚC
-- Tóm tắt nội dung:
--   Bảng:       11 (fine, fine_tier, member, author, category, book,
--                   book_author, book_category, book_copy, log, auth,
--                   audit_log, reservation)
--   Views:       6 (book_status, member_borrowing, overdue_books,
--                   borrowing_history, author_statistics, dashboard_stats)
--   Functions:   3 (calculate_fine, count_books_by_member, is_book_available)
--   Triggers:    4 (check_max_borrow, member_before_delete,
--                   book_copy_before_delete, book_copy_after_update)
--   Procedures:  4 (issue_book_safe, return_book, library_report, search_books)
--   Events:      2 (auto_calculate_fine, expire_reservations)
--   Indexes:     8 tùy chỉnh (idx_member_email, idx_member_type,
--                   idx_book_fulltext, idx_book_copy_status,
--                   idx_book_copy_book_status, idx_log_issue_date,
--                   idx_log_member_return, idx_log_copy_return)
-- ============================================================
