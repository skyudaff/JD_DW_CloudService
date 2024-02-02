FROM openjdk:20-jdk-oracle

EXPOSE 8081

ADD target/CloudService-0.0.1-SNAPSHOT.jar csapp.jar

CMD ["java", "-jar", "csapp.jar"]