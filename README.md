# 🐝 Hệ thống Quản lý Nuôi ong & Phấn hoa (Bee Pollen Management)

Một ứng dụng Spring Boot toàn diện được thiết kế để quản lý các trang trại nuôi ong, bầy ong, hệ thực vật và theo dõi sản lượng thu hoạch phấn hoa. Hệ thống tích hợp mô phỏng dữ liệu IoT để theo dõi thời gian thực, trợ lý ảo AI sử dụng Google Gemini và kiến trúc phân quyền truy cập mạnh mẽ.

Toàn bộ giao diện người dùng (UI) đã được Việt hóa 100%.

---

## 🌟 Tính năng chính

### 🔐 Bảo mật & Quản lý người dùng
*   **Xác thực lai (Hybrid Authentication)**: Hỗ trợ cả đăng nhập theo phiên (Session) cho giao diện Web và token JWT (Stateless Bearer Tokens) cho API REST.
*   **Phân quyền truy cập (RBAC)**:
    *   `ADMIN`: Quyền truy cập toàn bộ các module và cấu hình.
    *   `BEEKEEPER`: Quản lý bầy ong, theo dõi thu hoạch, và xem cơ sở dữ liệu thực vật/phấn hoa.
    *   `RESEARCHER`: Quyền chỉ xem (read-only) đối với dữ liệu thực vật và phấn hoa.
    *   `USER`: Quyền mặc định.
*   **Khôi phục mật khẩu an toàn**: Quy trình "Quên mật khẩu" tích hợp sử dụng token dùng một lần, có thời hạn và mô phỏng gửi email.
*   **Nhật ký hoạt động (Audit Trail)**: Sử dụng Spring Data JPA Auditing, tự động theo dõi người tạo (`CreatedBy`) và người cập nhật (`UpdatedBy`) trên toàn hệ thống để xây dựng nhật ký hoạt động theo thời gian.

### 🌼 Các module quản lý cốt lõi
*   **Hệ Thực vật**: Lưu trữ danh mục dữ liệu thực vật, mùa ra hoa, và khu vực phân bố.
*   **Hồ sơ Phấn hoa**: Quản lý các loại phấn hoa, hình dạng vi mô, và mã màu. Liên kết nhiều-nhiều (Many-to-Many) với Thực vật.
*   **Trang trại & Bầy ong**: Theo dõi tình trạng sức khoẻ (`HEALTHY`, `SICK`, `WEAK`), loài ong, toạ độ GPS, và số lượng ong ước tính.
*   **Theo dõi Thu hoạch**: Ghi nhận và tổng hợp sản lượng phấn hoa thu hoạch (tính bằng gram) liên kết với từng bầy ong và loại phấn hoa cụ thể.

### 🤖 Tính năng thông minh
*   **Mô phỏng IoT**: Một bộ lập lịch chạy ngầm (`IotDeviceSimulator`) mô phỏng các cân/cảm biến thông minh tại trại ong, tự động báo cáo dữ liệu thu hoạch phấn hoa ngẫu nhiên theo chu kỳ.
*   **Trợ lý AI**: Tích hợp với **Google Gemini AI** để cung cấp khả năng hỏi đáp qua chatbot, nhận diện hình ảnh khung cầu ong, nhận diện thực vật trực tiếp trong ứng dụng.

### 📊 Bảng điều khiển & Giao diện
*   **Giao diện Responsive**: Được xây dựng với Thymeleaf Layout Dialect, Bootstrap 5, và Bootstrap Icons.
*   **Bảng điều khiển phân tích (Analytics Dashboard)**: Trực quan hoá các loại phấn hoa thu hoạch nhiều nhất, tổng sản lượng, và biểu đồ phân bổ.
*   **Điều hướng động**: Các thành phần giao diện và nút thao tác (Thêm, Sửa, Xoá) tự động điều chỉnh dựa trên quyền (role) của người dùng.

---

## 🛠️ Công nghệ sử dụng

**Backend**
*   **Java 17**
*   **Spring Boot 3.2.x** (Web, Data JPA, Security, Mail, Validation)
*   **Spring Security 6** (JWT + BCrypt Password Encoding)
*   **Hibernate** (ORM)
*   **MySQL** (Cơ sở dữ liệu quan hệ)
*   **Lombok** (Giảm thiểu code boilerplate)

**Frontend**
*   **Thymeleaf** (Render phía server)
*   **Bootstrap 5** (CSS Framework)
*   **Thymeleaf Extras Spring Security 6** (Thẻ bảo mật cho UI)

---

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống
*   **JDK 17** trở lên
*   **Maven** 3.8+
*   **MySQL Server** (Chạy trên `localhost:3306`)

### Cấu hình
1. Mở file `src/main/resources/application.yml`.
2. Cấu hình thông tin kết nối MySQL của bạn:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/bee_pollen_db?createDatabaseIfNotExist=true
       username: root
       password: yourpassword
   ```
3. *(Tuỳ chọn)* Cấu hình **Gemini API Key** để kích hoạt các tính năng AI:
   ```yaml
   gemini:
     api:
       key: YOUR_GEMINI_API_KEY
   ```
4. *(Tuỳ chọn)* Bật tính năng Gửi Email cho quy trình Quên mật khẩu bằng cách bỏ comment khối `spring.mail` trong `application.yml` và thêm thông tin SMTP của bạn.

### Chạy ứng dụng

Để sử dụng đầy đủ các tính năng AI, hãy đặt Gemini API key của bạn dưới dạng biến môi trường trước khi chạy ứng dụng.

**Trên Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="your_api_key_here"
mvn clean spring-boot:run
```

**Trên macOS/Linux (Bash/Zsh):**
```bash
export GEMINI_API_KEY="your_api_key_here"
./mvnw clean spring-boot:run
```

*(Lưu ý: Nếu bạn không thiết lập `GEMINI_API_KEY`, ứng dụng vẫn sẽ khởi động thành công, nhưng các tính năng Trợ lý AI sẽ không hoạt động).*

Ứng dụng sẽ tự động:
1. Tạo cấu trúc cơ sở dữ liệu (`ddl-auto: update`).
2. Khởi tạo dữ liệu mẫu (các tài khoản Admin, Beekeeper, Researcher, cùng với các mẫu thực vật và bầy ong) thông qua `DataInitializer.java`.

### Tài khoản mặc định
Bạn có thể đăng nhập bằng các tài khoản đã được khởi tạo sẵn sau:
*   **Admin**: `admin` / `admin123`
*   **Beekeeper**: `beekeeper` / `beekeeper123`
*   **Researcher**: `researcher` / `researcher123`

Truy cập giao diện web tại: **http://localhost:8080**

---

## 📂 Cấu trúc dự án

```text
src/main/java/com/beepollen/
├── ai/                 # Tích hợp Google Gemini AI client
├── config/             # Cấu hình Spring Security, JPA Auditing, và Data Seeding
├── controller/         # Spring MVC Web Controllers
├── dto/                # Data Transfer Objects
├── entity/             # JPA Domain Models (Plant, Colony, User, v.v.)
├── exception/          # Xử lý ngoại lệ toàn cầu (Global Exceptions)
├── iot/                # Bộ lập lịch mô phỏng thiết bị IoT
├── repository/         # Spring Data JPA Repositories
├── security/           # JWT Filters, UserDetails
└── service/            # Logic nghiệp vụ (Business Logic) & Nhật ký hoạt động
```

---

## 📄 Giấy phép (License)
Dự án này phục vụ cho mục đích giáo dục và minh hoạ, được xây dựng bởi một trợ lý AI!
