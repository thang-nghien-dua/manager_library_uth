# 📚 Boooks — Hướng Dẫn Chạy Dự Án

## Yêu cầu duy nhất
Chỉ cần cài **[Docker Desktop](https://www.docker.com/products/docker-desktop/)** là đủ. Không cần cài Java, Maven, hay MySQL.

---

## ⚡ Chạy dự án (3 bước)

### Bước 1 — Mở Terminal trong thư mục dự án
Chuột phải vào thư mục **`Boooks-main`** (thư mục chứa file `docker-compose.yml`) → **Open in Terminal**

> Hoặc mở Terminal / PowerShell rồi gõ lệnh điều hướng:
> ```
> cd đường\dẫn\đến\Boooks-main
> ```

### Bước 2 — Khởi động toàn bộ hệ thống
```bash
docker-compose up -d
```
> ⏳ Lần đầu chạy sẽ mất **3-5 phút** để tải image và build ứng dụng.  
> Các lần sau chỉ mất **khoảng 30 giây**.

### Bước 3 — Mở trình duyệt
| Dịch vụ | Địa chỉ | Mô tả |
|---|---|---|
| 🌐 **Ứng dụng** | http://localhost:8080 | Trang web chính |
| 🗄️ **phpMyAdmin** | http://localhost:8082 | Quản lý database |

---

## 🔑 Tài khoản đăng nhập

### Trang web (http://localhost:8080)
| Username | Password |
|---|---|
| `admin` | `123456` |
| `librarian1` | `123456` |

### phpMyAdmin (http://localhost:8082)
| Username | Password |
|---|---|
| `root` | `root_password` |

---

## 🛑 Tắt dự án
```bash
docker-compose down
```

## 🔄 Xóa sạch data và chạy lại từ đầu
```bash
docker-compose down -v
docker-compose up -d
```
> ⚠️ Lệnh `-v` sẽ xóa toàn bộ dữ liệu trong database. Chỉ dùng khi muốn reset hoàn toàn.

---

## ❓ Xử lý lỗi thường gặp

### Lỗi: Port đã bị dùng
```
Bind for 0.0.0.0:8080 failed: port is already allocated
```
👉 Có phần mềm khác đang dùng cổng 8080. Tắt phần mềm đó hoặc đổi cổng trong `docker-compose.yml`.

### Ứng dụng chưa hiển thị sau khi `docker-compose up`
👉 Chờ thêm 1-2 phút. Kiểm tra trạng thái bằng lệnh:
```bash
docker-compose logs app --tail=20
```
Khi thấy chữ `Started BoooksApplication` là ứng dụng đã sẵn sàng.

### Lỗi port 3308 (MySQL)
```
Bind for 0.0.0.0:3308 failed
```
👉 Máy đang có MySQL khác chạy. Tắt MySQL local hoặc đổi port trong `docker-compose.yml`:
```yaml
ports:
  - "3309:3306"   # đổi 3308 thành 3309 (hoặc số khác)
```

---

## 📊 Cơ sở dữ liệu mẫu đã có sẵn
- **10 cuốn sách** (có ISBN, mô tả, nhà xuất bản)
- **10 thành viên** (độc giả)
- **15 lịch sử mượn/trả** (bao gồm các trường hợp quá hạn)
- **2 đặt trước sách** đang chờ xử lý

---

*Dự án: Hệ thống Quản lý Thư viện — Đồ án môn Hệ Quản Trị Cơ Sở Dữ Liệu*
