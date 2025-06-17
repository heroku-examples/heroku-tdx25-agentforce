web: APP_PORT=3000 heroku-applink-service-mesh-latest-amd64 /app/startup.sh
web-mesh-1: APP_PORT=3000 heroku-applink-service-mesh-latest-amd64 java $JAVA_OPTS -jar target/agentforce-actions-0.0.1-SNAPSHOT.jar
web-mesh-2: APP_PORT=3000 heroku-applink-service-mesh-latest-amd64
web-mesh-3: APP_PORT=3000 heroku-applink-service-mesh-latest-amd64 "java $JAVA_OPTS -jar target/agentforce-actions-0.0.1-SNAPSHOT.jar"
web-classic: java $JAVA_OPTS -Dserver.port=$PORT -jar target/agentforce-actions-0.0.1-SNAPSHOT.jar
