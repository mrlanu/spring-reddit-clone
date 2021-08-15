# Starting a MySQL instance

docker run --name reddit-clone -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=reddit_clone -e MYSQL_USER=mrlanu -e MYSQL_PASSWORD=12345 -d mysql:latest
