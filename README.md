# Hướng dẫn chạy thử dự án

## 1. Clone dự án
Trước tiên, clone dự án từ GitHub:
```shell
git clone https://github.com/21522354/J2EEDemo
```

## 2. Tải và mở Docker Desktop
Tải Docker Desktop và mở ứng dụng.

## 3. Tạo Docker Image
Vào folder `postservicecommand` và chạy lệnh sau để tạo image:
```shell
docker build -t postservicecommand .
```
Sau đó image `postservicecommand` sẽ được tạo. Làm tương tự với `postservicequerry`.

## 4. Chỉnh sửa Docker Compose
Vào folder `YamlFile`, mở file `docker-compose.yml` bằng VSCode. Chỉnh lại đường dẫn cho đúng với thư mục project hiện tại ở các phần volumes cho `postservicecommand`, sau đó lưu lại.

## 5. Chạy Docker Compose
Mở thư mục `YamlFile` bằng terminal và chạy lệnh:
```shell
docker compose up -d
```
Hệ thống sẽ chạy bằng Docker, bao gồm các container cho Kafka, Redis, MySQL, Nginx và các service command và querry.

## 6. Gửi request bằng Postman
Sử dụng Postman để gửi request đến các endpoint được định nghĩa trong các controller ở các service.

- Khi gửi yêu cầu tạo post, hệ thống sẽ lưu dữ liệu ở phía command trước, sau đó gửi message đến phía querry để ghi dữ liệu.
- Theo dõi các container `postservicequerrry1` và `postservicequerry2` để xem việc nhận và xử lý các request, được quản lý bởi load balancer Nginx.
- Khi gửi yêu cầu đọc các bài post, service sẽ kiểm tra cache trước, nếu không có thì mới truy vấn từ database và lưu data vào cache với TTL là 10 giây.
