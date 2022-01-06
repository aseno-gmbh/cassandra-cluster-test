FROM openjdk:11

LABEL MAINTAINER="developer@aseno.de"


# in case we want to handle network conections
RUN apt-get update && \
    apt-get -y install maven net-tools vim curl wget apt-transport-https 

RUN mvn -version
