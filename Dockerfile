FROM gradle:8.12 as builder

WORKDIR /home/gradle/stausee

COPY buildSrc buildSrc
COPY src src
COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY gradlew gradlew
COPY .git .git

RUN ./gradlew --no-daemon installDist

FROM openjdk:21-jdk as runner
RUN microdnf install findutils
WORKDIR /home/stausee

EXPOSE 8022
EXPOSE 8080

ENV STORAGE_PATH=/data/stausee

COPY --from=builder /home/gradle/stausee/build/install/stausee .

RUN chmod +x ./bin/stausee

ENTRYPOINT ./bin/stausee