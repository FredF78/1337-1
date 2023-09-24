FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean install

FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
EXPOSE 8080

COPY --from=builder /opt/app/target/tretton37-task-0.0.1-SNAPSHOT.jar /opt/app/tretton37-task-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/opt/app/tretton37-task-0.0.1-SNAPSHOT.jar"]