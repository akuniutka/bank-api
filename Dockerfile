FROM eclipse-temurin:8-jdk as build
RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install -y git
RUN git clone https://github.com/akuniutka/bank-api.git bank-api
RUN apt-get install -y maven
WORKDIR bank-api
RUN mvn clean package
FROM eclipse-temurin:8-jre as target
COPY --from=build /bank-api/target/bank-api-1.2.1-SNAPSHOT.jar /app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]