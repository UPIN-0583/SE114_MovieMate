# SE114_MovieMate
## 1. Hướng dẫn chạy app
- Vào firebase: https://console.firebase.google.com/project/moviemate-a54ac/overview
![image](https://github.com/user-attachments/assets/35ddd763-d38b-45d8-97bc-b5d0b636cceb)
- Chọn Project Setting -> General -> Your App -> tải file google-services.json
- Clone dự án -> SE114_MovieMate\app dán file google-services.json -> Chạy app

## 2. Cấu hình API key:
### 2.1. Thiết lập file `env`:
- Clone project và tìm thư mục `assets` cùng file `env.template`
- Tạo file mới tên `env` (không có đuôi) và copy toàn bộ nội dung từ `env.template`

![image](https://github.com/user-attachments/assets/3bf5180b-8754-4ec0-82bc-09a72d590870)

> [!CAUTION]
> - Không được thay đổi `KEY_NAME` để tránh gây lỗi
> - Khi thêm key mới, cần cập nhật cả file `env.template` và hướng dẫn trong `README.md`

### 2.2. Cấu trúc file `env`
- API key sẽ được lưu tại `/assets/env` theo format
```env
# Comment start with '#'
KEY_NAME=CONTENT # Comment inline is also supported
# Eg
PAYOS_API_KEY=paste-api-key-here
```

### 2.3. Cách sử dụng:
- Việc load key sẽ sử dụng lib [cdimascio/dotenv-java](https://github.com/cdimascio/dotenv-java)
- Code mẫu để có thể load key (Các ví dụ nâng cao có thể đọc tại repo của lib)
```java
Dotenv dotenv = Dotenv.configure()
   .directory("/assets")
   .filename("env") // Lưu ý: env chứ không phải .env
   .load();

dotenv.get("PAYOS_API_KEY");
```
## 3. Cấu hình PayOS
### 3.1. Lấy API key của PayOS:
1. Đăng nhập [PayOS Dashboard](https://my.payos.vn/)
2. Chọn tổ chức
3. Chọn "Kênh thanh toán"
4. Chọn "MovieMate"

![image](https://github.com/user-attachments/assets/c004934f-aabb-4e2c-853e-22edb5fbf55c)

## 4. Cấu hình Cloudinary
### 4.1. Lấy API key của Cloudinary:
1. Đăng nhập [Cloudinary](https://cloudinary.com/)
2. Mở Settings ở góc dưới, bên trái -> Chọn API Keys (tại đây sẽ có đủ thông tin cần thiết)

![image](https://github.com/user-attachments/assets/4294c2cf-a250-4f3b-a23a-a333a524c30d)

3. Copy các giá trị vào file `/assets/env` theo mẫu trong `/assets/env.template`
