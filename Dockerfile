# Используем официальный образ Maven для сборки
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем pom.xml и зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем весь проект и собираем jar
COPY . .
RUN mvn clean package -DskipTests

# Берём минимальный JRE образ для запуска
FROM eclipse-temurin:17-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем jar из стадии сборки
COPY --from=builder /app/target/*.jar app.jar

# Открываем порт (если ваше приложение использует его, например через Spring Boot)
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]
