FROM openjdk:11

LABEL MAINTAINER="valentin@aseno.de"

COPY launch.sh /app/
COPY *.jar /app/

# copy contrast artifacts

WORKDIR /app
EXPOSE 8080
CMD ["/app/launch.sh"]