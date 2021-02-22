# upper
## 🏗️ setup
### with docker
```
cd /{path-to-repo}/upper
mvn package
docker build -t upper
docker -dp 8080:8080 upper
```
### alternate
```
cd /{path-to-repo}/upper
mvn package install
java -jar target/upper-0.0.1-SNAPSHOT.jar
```
### postman tests
due to a multipart form required to be uploaded, i was not able to find a way to automate it
some documentation supporting this can be found in the [postman quick reference guide](https://postman-quick-reference-guide.readthedocs.io/en/latest/cheatsheet.html?highlight=multipart#pm-sendrequest)

instead, in order to test the collection, run post-invoice-submission first, then run the rest of the collection, ignoring the post
i also suggest using the same data.csv found from kaggle, as the upload is polled every 20s during the test

## 💡 swagger-ui
available at http://localhost:8080/swagger-ui.html
