#Building the website
FROM node:lts as js
RUN mkdir /resources
WORKDIR /resources/
COPY src/main/resources/ .
RUN npm i && npm run build

#Building the api
FROM gradle:8 as kt
WORKDIR /
RUN mkdir -p /kt/src/main/resources /kt/src/main/kotlin
WORKDIR /kt/
COPY src/main/kotlin src/main/kotlin
COPY *.gradle.kts .
COPY gradle.properties .
COPY --from=js /resources src/main/resources/
RUN gradle build -Pdocker

#Running the api and the website
FROM eclipse-temurin:17-alpine
RUN mkdir /app
WORKDIR /app
COPY --from=kt /kt/build/libs/citra-cloudsaves-api-all.jar /app/

LABEL authors="minemobs"
LABEL version="1.0"
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/citra-cloudsaves-api-all.jar"]