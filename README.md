# 📘 Hotel Booking Microservices

Микросервисная система бронирования отелей с поддержкой двухшаговой согласованности (**confirm / compensate**) между сервисами бронирования и отелей.  
Сервисы обмениваются событиями по HTTP (REST) с идемпотентностью на уровне `requestId`.

---

## 🧩 Архитектура

Проект состоит из 4 отдельных Spring Boot микросервисов:

| Сервис | Назначение | Порт | Примечание |
|:--|:--|:--|:--|
| **Eureka Server** | Service discovery | `8761` | Содержит реестр всех сервисов |
| **API Gateway** | Маршрутизация + JWT фильтр | `8080` | Проксирует запросы к Booking и Hotel |
| **Booking Service** | Работа с пользователями и бронированиями | `8081` | Реализует двухфазное бронирование |
| **Hotel Service** | Управление отелями и номерами | `8090` | Обработка блокировок / подтверждений |

Все сервисы используют встроенную базу **H2** и общую JWT-подпись (HMAC).

---

## ⚙️ Как пользоваться системой

### 1️⃣ Установите зависимости
- JDK 17+  
- Maven (если хотите собирать JAR)  
- (опционально) `curl` / `Postman` / `httpie` для тестирования API  

---

### 2️⃣ Соберите проект

Запуск из IDE — просто откройте каждый модуль (`eureka-server`, `api-gateway`, `booking-service`, `hotel-service`) и выполните **Run → Spring Boot Application**.

Либо соберите JAR:
```bash
mvn -f eureka-server/pom.xml clean package -DskipTests
mvn -f hotel-service/pom.xml clean package -DskipTests
mvn -f booking-service/pom.xml clean package -DskipTests
mvn -f api-gateway/pom.xml clean package -DskipTests
```

---

### 3️⃣ Запустите сервисы (по порядку)

```bash
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
java -jar hotel-service/target/hotel-service-0.0.1-SNAPSHOT.jar
java -jar booking-service/target/booking-service-0.0.1-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

После запуска откройте [http://localhost:8761](http://localhost:8761) — там появятся все сервисы.

---

### 4️⃣ Основной сценарий работы (пример)

1. **Создать отель и комнату**
   ```bash
   curl -X POST http://localhost:8090/api/hotels      -H "Content-Type: application/json"      -d '{"name":"Hotel One","address":"Street 1"}'

   curl -X POST http://localhost:8090/api/rooms      -H "Content-Type: application/json"      -d '{"hotelId":1,"number":"101"}'
   ```

2. **Зарегистрировать пользователя**
   ```bash
   curl -X POST http://localhost:8081/api/user/register      -H "Content-Type: application/json"      -d '{"username":"alice","password":"123"}'
   ```
   Ответ: `{"token":"<JWT_TOKEN>"}`

3. **Создать бронирование**
   ```bash
   curl -X POST http://localhost:8081/api/booking      -H "Content-Type: application/json"      -H "Authorization: Bearer <JWT_TOKEN>"      -d '{"userId":1,"roomId":1,"startDate":"2025-10-30","endDate":"2025-11-02","requestId":"req-1"}'
   ```

4. **Проверить бронирования**
   ```bash
   curl -X GET "http://localhost:8081/api/bookings?userId=1"      -H "Authorization: Bearer <JWT_TOKEN>"
   ```

---

## 🌐 REST API

### Booking Service (порт 8081)

| Метод | Endpoint | Описание |
|:--|:--|:--|
| `POST` | `/api/user/register` | Регистрация пользователя (возвращает JWT) |
| `POST` | `/api/user/auth` | Аутентификация пользователя |
| `POST` | `/api/booking` | Создать бронирование (2-фазная логика) |
| `GET` | `/api/bookings?userId={id}` | Список бронирований пользователя |
| `GET` | `/api/booking/{id}` | Получить бронирование |
| `DELETE` | `/api/booking/{id}` | Отмена бронирования (выполняет компенсацию) |

---

### Hotel Service (порт 8090)

| Метод | Endpoint | Описание |
|:--|:--|:--|
| `POST` | `/api/hotels` | Добавить отель |
| `GET` | `/api/hotels` | Получить список отелей |
| `POST` | `/api/rooms` | Добавить комнату |
| `GET` | `/api/rooms` | Список всех комнат |
| `GET` | `/api/rooms/recommend` | Рекомендации (по количеству бронирований) |
| `POST` | `/api/rooms/{id}/confirm-availability` | INTERNAL: забронировать (lock) |
| `POST` | `/api/rooms/{id}/release` | INTERNAL: освободить (compensate) |
| `POST` | `/api/rooms/{id}/finalize` | INTERNAL: финализировать (commit) |

---

## 🔑 JWT

- Генерируется при регистрации / логине (Booking Service)
- Подпись: HMAC SHA-256  
- Секрет: `supersecretjwtkeysupersecretjwtkey`
- Проверяется в Gateway и Hotel Service
- Добавляйте токен в заголовок:
  ```
  Authorization: Bearer <JWT_TOKEN>
  ```

---

## 🧪 Как протестировать код

В проекте есть smoke-тесты (`BookingControllerTest`) с использованием **JUnit 5 + MockMvc**.

### Запуск тестов

```bash
mvn -f booking-service/pom.xml test
```

или из IDE:  
открой `booking-service/src/test/java/.../BookingControllerTest.java` → **Run Test**.

**Проверяется:**
- регистрация пользователя возвращает токен,
- аутентификация работает,
- REST контроллер отвечает корректно.

---

## 🛠️ Troubleshooting

| Проблема | Решение |
|:--|:--|
| Порт занят | Измени `server.port` в `application.yml` |
| JWT invalid | Проверь заголовок `Authorization` |
| Сервисы не видны в Eureka | Запусти Eureka перед остальными |
| `CANCELLED` при бронировании | Комната занята или HotelService вернул ошибку |

---

## 💡 Что можно улучшить

- Перенести секрет JWT в переменные окружения  
- Добавить OpenAPI (Swagger UI)  
- Расширить тесты на двухфазное подтверждение  
- Реализовать retry через **Resilience4j**  
- Подключить централизованный логгер (MDC, Sleuth)

---
