FROM maven:3.6.0-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:11
COPY --from=build /home/app/target/upper*.jar /usr/local/lib/app.jar
COPY --from=build /home/app/src/main/resources/ /usr/local/lib
EXPOSE 8080
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=deploy", "/usr/local/lib/app.jar"]