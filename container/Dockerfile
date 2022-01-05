FROM openjdk:11

LABEL MAINTAINER="developer@aseno.de"

# in case we want to handle network conections
RUN apt-get update && \
    apt-get -y install net-tools vim curl wget apt-transport-https 

COPY launch.sh /app/
COPY *.jar /app/

RUN chmod +x /app/launch.sh

WORKDIR /app
EXPOSE 8080
CMD ["/app/launch.sh"]