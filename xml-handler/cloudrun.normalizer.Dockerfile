FROM adoptopenjdk/openjdk11:alpine

RUN mkdir ./app

COPY . ./app

WORKDIR ./app

RUN ./gradlew build --build-cache -p cloud-run/normalizer

EXPOSE 8080

CMD ["java", "-jar", "cloud-run/normalizer/build/libs/normalizer-0.0.1.jar"]
