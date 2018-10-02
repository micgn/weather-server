#!/bin/sh

export BROKER_URL=tcp://${MQTT_SERVICE_HOST}:${MQTT_SERVICE_PORT}
echo BROKER_URL=${BROKER_URL}

echo deleting possible database lock:
rm /data/hsqldb/weather/weather.lck

java -Dspring.profiles.active=cloud -Dspring.datasource.url=jdbc:hsqldb:file:/data/hsqldb/weather/weather -Dmqtt.broker.url=${BROKER_URL} -Dmqtt.broker.user=${BROKER_USER} -Dmqtt.broker.password=${BROKER_PW} -DbasicAuth.user=${BASICAUTH_USER} -DbasicAuth.password=${BASICAUTH_PW} -Dspring.mail.username=${MAIL_USER} -Dspring.mail.password=${MAIL_PW} -DalertEmail=${MAIL_ALERT_MAIL} -Dlogging.level.de.mg.weather.server=${LOG_LEVEL} -jar app.jar
