FROM openjdk:14
ARG JAR=target/*.jar
COPY ${JAR} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
