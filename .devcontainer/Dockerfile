FROM openjdk:11

LABEL MAINTAINER="developer@aseno.de"


# in case we want to handle network conections
RUN apt-get update && \
    apt-get -y install maven net-tools vim curl wget apt-transport-https 

EXPOSE 8080
RUN mvn -version


