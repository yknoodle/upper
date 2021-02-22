./mvnw clean package
docker build -t upper .
docker-compose up -d
