# Bank Service Demo

**Note: Java 8 or higher is required**

## Build
```bash
mvn clean package
 ```
 
## Run 
```bash
java -jar target/bank-service.jar
```

## Sample
```bash
#!/bin/bash

echo Accounts before
curl -s http://localhost:8080/api/account/list
echo

echo Add account
curl -d '{"name":"Peter Phil", "amount":"12.45"}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/account/create
echo

echo Add another account
curl -d '{"name":"Walt Disney", "amount":"42.24"}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/account/create
echo

echo Accounts after adding
curl -s http://localhost:8080/api/account/list
echo
```

## Endpoints
* List all accounts - http://localhost:8080/api/account/list (GET)
* Create an account - http://localhost:8080/api/account/create (POST)
* Transfer money between accounts - http://localhost:8080/api/money/transfer/:from/:to/:amount (POST)
