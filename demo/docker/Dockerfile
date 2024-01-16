FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . ./
RUN ./gradlew build
CMD ["java", "-cp", "./build/libs/demo-1.0-SNAPSHOT.jar:.", "org.example.Main"]
