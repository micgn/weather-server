FROM openjdk:8-jdk-alpine

LABEL maintainer="Michael Gnatz"

ENV LOG_LEVEL=info

ENV BASICAUTH_USER weather
ENV BASICAUTH_PW 12345
ENV MAIL_USER x@y.de
ENV MAIL_PW 12345
ENV MAIL_ALERT_MAIL x@y.de
ENV BROKER_URL 1.1.1.1
ENV BROKER_USER user
ENV BROKER_PW 12345

COPY ./build/libs/weather-server.jar app.jar

VOLUME /data

ADD docker/start.sh /start.sh
RUN chmod u+x /start.sh

EXPOSE 8080

CMD /start.sh
