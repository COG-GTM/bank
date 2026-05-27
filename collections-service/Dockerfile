FROM openjdk:21-oracle

LABEL authors="Brody Gaudel MOUNANGA BOUKA"
LABEL maintainer="Brody Gaudel MOUNANGA BOUKA"

ENV DISCOVERY_SERVICE=http://localhost:8761/eureka/
ENV MYSQL_USER=root
ENV MYSQL_PWD=root
ENV MYSQL_HOST=172.18.0.2
ENV MYSQL_PORT=3306
ENV MYSQL_DATABASE=collections_db
ENV AXON_HOST=172.18.0.3
ENV AXON_PORT=8124
ENV JWT_SECRET=AaZzBbCcYyDdXxEeWwFf
ENV JWT_EXPIRATION=604800000
ENV OVERDRAFT_THRESHOLD=0

COPY target/collections-service-0.0.1.jar collections-service.jar

ENTRYPOINT ["java","-jar","collections-service.jar"]
