# Definition of the base image
FROM eclipse-temurin:21.0.13.1_2-jre-jammy@sha256:cdbc48132dd37b9e65cfccf2417c5668ac75046c4168af5486526eb9788f0e8f

# Definition of the maintainer and author of the image
LABEL authors="Brody Gaudel MOUNANGA BOUKA"
LABEL maintainer="Brody Gaudel MOUNANGA BOUKA"

# Copying your jar application into the container
COPY target/discovery-service-0.0.3.jar discovery-service.jar

# Command to launch the application when running the container
ENTRYPOINT ["java","-jar","discovery-service.jar"]