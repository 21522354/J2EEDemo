# Bước 1: Sử dụng image Java cho JDK 17
FROM openjdk:17-jdk-alpine

# Bước 2: Đặt biến môi trường cho file JAR
ARG JAR_FILE=target/*.jar

# Bước 3: Sao chép file JAR của ứng dụng vào trong container
COPY ${JAR_FILE} app.jar

# Bước 4: Cấu hình port mà ứng dụng sẽ chạy
EXPOSE 8080

# Thêm cổng debug
EXPOSE 6006

# Bước 5: Cấu hình để chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:6006", "-jar", "/app.jar"]