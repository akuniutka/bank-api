FROM adoptopenjdk/openjdk8
COPY target/bank-api-1.2.0-SNAPSHOT.jar /app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]