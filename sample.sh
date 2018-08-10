#!/bin/bash

echo Accounts before
curl -s http://localhost:8080/api/account/list
echo

echo Add account
curl -d '{"name":"Peter Phil", "amount":"12.45"}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/account/create
echo

echo Add account
curl -d '{"name":"Walt Disney", "amount":"42.24"}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/account/create
echo

echo Accounts after adding
curl -s http://localhost:8080/api/account/list
echo
