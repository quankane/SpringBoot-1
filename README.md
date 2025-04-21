```text

   ____         //\          _  __                
  / __ \       |/ \|        | |/ /                
 | |  | |_   _  __ _ _ __   | ' / __ _ _ __   ___ 
 | |  | | | | |/ _` | '_ \  |  < / _` | '_ \ / _ \
 | |__| | |_| | (_| | | | | | . \ (_| | | | |  __/
  \___\_\\__,_|\__,_|_| |_| |_|\_\__,_|_| |_|\___|
                                                                               
             ____/ Lập Trình Java Từ A-Z
```
## Prerequisite
- Cài đặt JDK 17+ nếu chưa thì [cài đặt JDK](https://tayjava.vn/cai-dat-jdk-tren-macos-window-linux-ubuntu/)
- Install Maven 3.5+ nếu chưa thì [cài đặt Maven](https://tayjava.vn/cai-dat-maven-tren-macos-window-linux-ubuntu/)
- Install IntelliJ nếu chưa thì [cài đặt IntelliJ](https://tayjava.vn/cai-dat-intellij-tren-macos-va-window/)

## Technical Stacks
- Java 17
- Maven 3.5+
- Spring Boot 3.2.3
- Spring Data Validation
- Spring Data JPA
- Postgres
- Lombok
- DevTools
- Docker, Docker compose

---
## Thiết lập Gmail
Để cho phép gửi email qua Gmail ta cần thực hiện 2 bước sau

- Step 1: Cho phép xác thực 2 nhân tố: https://myaccount.google.com/signinoptions/two-step-verification/enroll-welcome
- Step 2: Tạo app chỉ định password: https://myaccount.google.com/apppasswords
- Step 3: Gán thông tin vào mail sender: 

```Properties
spring.mail.username=quankane
spring.mail.password=cflw mnqi xhlr zeff
```

## th là gì?
- th: Là viết tắt của Thymeleaf, được sử dụng làm tiền tố (namespace) cho các thuộc tinh của Thymeleaf trong HTML. Nó cho phép Thymeleaf xử lý các phần tử HTML một cách động mà không làm phá vỡ cấu trúc HTML tĩnh
- Cách khai báo
``` Properties
<html xmlns:th="http://www.thymeleaf.com"
```
- Namespaces th được khai báo trong thẻ <html> để Thymeleaf nhận diện các thuộc tính như th:text, th:each, v.v
- Vai trò: Các thuộc tính: th:* được xử lý bởi Thymeleaf trên server để thay thế hoặc điều chỉnh nội dung HTML dựa trên dữ liệu động
## Các biểu thức (Expressions) trong Thymeleaf
Thymeleaf sử dụng các biểu thức để truy cập và xử lý dữ liệu từ server. Các biểu thức được viết trong dấu ${...}, *{...}, #{...}, v.v. Dưới đây là các loại biểu thức chính:
### Variable Expressions (${...})
- Mục đích: Truy cập các biến hoặc thuộc tính từ Model (được gửi từ Controller trong Spring)
- Cú pháp : ${variableName} hoặc ${object.property}

## JavaMailSener
### JavaMailSender là gì?
- JavaMailSender là một giao diện (Interface) trong SpringFramework, được thiết kế để gửi email thông qua giao thức SMTP (Simple Mail Transfer Protocol). Nó là một phần của module Spring Mail, giúp đơn hóa việc gửi email so với sử dụng trực tiếp thư viện Java Mail của Oracle
- Chức năng chính:
  - Gửi email đơn giản (Văn bản thuần)
  - Gửi email phức tạp (HTML, đi)
