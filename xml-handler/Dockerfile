FROM adoptopenjdk/openjdk11:alpine

RUN mkdir ./app

COPY . ./app

WORKDIR ./app

RUN ./gradlew build
