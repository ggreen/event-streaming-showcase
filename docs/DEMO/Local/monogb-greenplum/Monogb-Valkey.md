Setup

Mongo Server

```shell
podman run -it --rm --name mongodb -p 27017:27017 -e MONGODB_USER=debezium -e MONGODB_PASSWORD=dbz  debezium/example-mongodb:2.3.3.Final
podman run --name mongodb -e ALLOW_EMPTY_PASSWORD=yes -p 27017:27017  bitnami/mongodb:latest
```


Initialize collections

```shell
podman exec -it mongodb sh -c 'bash -c /usr/local/bin/init-inventory.sh'
```

Connect Mongo Shell

```properties
podman exec -it mongodb mongo -u user -p password 
```


```shell
db.getUsers();
```

---------
# SCDF

Register ValKey sink

```properties
source.valkey-sql-cdc=file:///Users/Projects/solutions/cloudNativeData/showCase/dev/valkey-showcase/applications/integration/sources/valkey-sql-cdc-source/target/valkey-sql-cdc-source-0.0.1-SNAPSHOT.jar
source.valkey-sql-cdc.bootVersion=3

```


```shell
mongo-valkey-stream=mongodb --username=mongo --database=admin --password=mongo --host=localhost --collection=user --query="{ synced : { $eq :  'false' }" --update-expression="{synced: true}" | valkey-sink
```
