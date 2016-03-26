FROM java:alpine

MAINTAINER Mariusz Jozala "http://aetas.pl"

COPY artifacts/pushq-integration-all-*.jar /srv/slack-foos-integration.jar

VOLUME /srv/config
EXPOSE 4567

WORKDIR /srv
ENTRYPOINT ["java", "-jar", "slack-foos-integration.jar"]