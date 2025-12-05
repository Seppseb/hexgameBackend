# hexgameBackend

TODOS:
make board modular, let user decide size


local setup:
mvn clean package
mvn clean compile
mvn clean spring-boot:run

push to server:
mvn clean package -DskipTests
scp -r ./ root@46.62.128.247:/opt/hexgameapp/backend/hexgameBackend

ssh root@46.62.128.247
# restart service
systemctl restart hexgameapp

# reload nginx
nginx -t && systemctl reload nginx
