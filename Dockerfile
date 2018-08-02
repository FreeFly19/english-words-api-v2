FROM hseeberger/scala-sbt as builder
WORKDIR /tmp
COPY . /tmp
RUN ["sbt", "assembly"]

FROM openjdk:8-jre as runner
COPY --from=builder /tmp/target/scala-2.12/http4s-doobie-circle-example-assembly-0.1.jar /tmp/app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/tmp/app.jar"]