FROM adoptopenjdk/openjdk11:alpine

RUN mkdir ./app

COPY ../csv-converter ./csv-converter
COPY . ./app

WORKDIR ./app

RUN ./gradlew build
