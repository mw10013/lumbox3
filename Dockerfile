FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/lumbox3.jar /lumbox3/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/lumbox3/app.jar"]
