# upper
## setup
### setup (with docker)
```
cd /{path-to-repo}/upper
docker build -t upper
docker -dp 8080:8080 upper
```
### setup
```
cd /{path-to-repo}/upper
mvn clean install
java -jar target/upper-0.0.1-SNAPSHOT.jar
```
### swagger-ui available at http://localhost:8080/swagger-ui.html
