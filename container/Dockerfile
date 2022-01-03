FROM openjdk:11

LABEL MAINTAINER="valentin@aseno.de"

RUN apt-get update && \
    apt-get -y install net-tools vim curl wget apt-transport-https 

#RUN sh -c 'echo "deb http://www.apache.org/dist/cassandra/debian 40x main" > /etc/apt/sources.list.d/cassandra.list'
#RUN wget -q -O - https://www.apache.org/dist/cassandra/KEYS |  apt-key add -

#RUN apt-get update && \
#    apt-get -y install cassandra

COPY launch.sh /app/
COPY *.jar /app/
# copy contrast artifacts
RUN chmod +x /app/launch.sh


WORKDIR /app
EXPOSE 8080
CMD ["/app/launch.sh"]