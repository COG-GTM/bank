FROM openjdk:21-oracle

LABEL authors="Brody Gaudel MOUNANGA BOUKA"
LABEL maintainer="Brody Gaudel MOUNANGA BOUKA"

ENV DISCOVERY_SERVER=http://localhost:8761/eureka/
ENV MYSQL_HOST=172.20.0.2
ENV MYSQL_PORT=3307
ENV MYSQL_DATABASE=collections_db

COPY target/collections-service-0.0.1.jar collections-service.jar

ENTRYPOINT ["java","-jar","collections-service.jar"]
