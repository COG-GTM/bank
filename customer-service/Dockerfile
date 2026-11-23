FROM eclipse-temurin:21.0.13.1_2-jre-jammy@sha256:cdbc48132dd37b9e65cfccf2417c5668ac75046c4168af5486526eb9788f0e8f

# Definition of the maintainer and author of the image
LABEL authors="Brody Gaudel MOUNANGA BOUKA"
LABEL maintainer="Brody Gaudel MOUNANGA BOUKA"

# Setting environment variables
ENV DISCOVERY_SERVER=http://localhost:8761/eureka/
ENV MYSQL_USER=root
ENV MYSQL_PWD=root
ENV MYSQL_HOST=172.20.0.2
ENV MYSQL_PORT=3307
ENV MYSQL_DATABASE=customer_db

# Copying your jar application into the container
COPY target/customer-service-0.0.1.jar customer-service.jar

# Command to launch the application when running the container
ENTRYPOINT ["java","-jar","customer-service.jar"]