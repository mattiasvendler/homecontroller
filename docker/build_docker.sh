#!/bin/bash
cd ..
mvn clean install -TC2 -Dmaven.test.skip=true
cd docker
rm homeautomation-service-1.0-SNAPSHOT-jar-with-dependencies.jar
cp ../target/homeautomation-service-1.0-SNAPSHOT-jar-with-dependencies.jar .
export DOCKER_HOST=192.168.1.181
docker build -t homeautomation-service .
docker stop ha-service
docker rm ha-service
docker run -d --link test-mysql --name ha-service homeautomation-service:latest
docker logs -f ha-service | less
