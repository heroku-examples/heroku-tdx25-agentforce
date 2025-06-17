web: APP_PORT=3000 heroku-applink-service-mesh-latest-amd64
web-classic: java $JAVA_OPTS -Dserver.port=$PORT -jar target/agentforce-actions-0.0.1-SNAPSHOT.jar
web-mesh-full: APP_PORT=3000 heroku-applink-service-mesh-latest-amd64 "java $JAVA_OPTS -jar target/agentforce-actions-0.0.1-SNAPSHOT.jar"
