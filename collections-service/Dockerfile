FROM openjdk:21-oracle

# Definition of the maintainer and author of the image
LABEL authors="Devin"
LABEL maintainer="Devin"

# Setting environment variables
ENV DISCOVERY_SERVER=http://localhost:8761/eureka/
ENV MYSQL_HOST=172.20.0.2
ENV MYSQL_PORT=3307
ENV MYSQL_DATABASE=collections_db

# Copying your jar application into the container
COPY target/collections-service-0.0.1.jar collections-service.jar

# Command to launch the application when running the container
ENTRYPOINT ["java","-jar","collections-service.jar"]
