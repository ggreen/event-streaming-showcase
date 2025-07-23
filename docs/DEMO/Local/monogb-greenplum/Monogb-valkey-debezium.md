Setup

Start MongoDB server

```shell
export MONGODB_VERSION=6.0-ubi8
podman  network create tanzu
podman run --network=tanzu --name mongodb -it --rm  -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=mongo  -e MONGO_INITDB_ROOT_PASSWORD=mongo mongodb/mongodb-community-server:$MONGODB_VERSION
```


Mongos

```shell
podman run -it --rm --name mongodb -p 27017:27017 -e MONGODB_USER=debezium -e MONGODB_PASSWORD=dbz  debezium/example-mongodb:2.3.3.Final
```


Initialize collections

```shell
podman exec -it mongodb sh -c 'bash -c /usr/local/bin/init-inventory.sh'

```

```shell
show dbs
use admin
db.user.insert({"_id": "joedoe", name: "Joe Doe", age: 45, synced: false})
db.user.insert({"_id": "jilldoe", name: "Jill Doe", age: 50, synced: false})
db.user.insert({"_id": "jsmith", name: "John Smith", age: 33, synced: false})
db.user.insert({"_id": "jacme", name: "James Acme", age: 33, synced: false})
```

Delete

```shell
db.user.deleteMany({})
```

List of collections
```shell
db.getCollectionNames()
```

Query

{ age : { $lt : 0 }, accounts.balance : { $gt : 1000.00 }

```shell
db.user.find({ synced : { $eq :  false }})
```

```shell
db.user.find({})
```


```shell
db.user.updateMany({synced: true}, {$set:{synced: false}})  
```


```shell
db.user.updateMany({synced: true, "_id": "joedoe" }, {$set:{synced: false, age: 66}})  
```

```shell

gti 
```

query


------------------------

Postgres

```sql
DROP TABLE app_users;

CREATE TABLE app_users (
	id varchar NOT NULL,
	name varchar NOT NULL,
	age int NULL,
	CONSTRAINT app_users_id_pk PRIMARY KEY (id)
);
```

---------
# SCDF

Register ValKey sink

```properties
source.valkey-sql-cdc=file:///Users/Projects/solutions/cloudNativeData/showCase/dev/valkey-showcase/applications/integration/sources/valkey-sql-cdc-source/target/valkey-sql-cdc-source-0.0.1-SNAPSHOT.jar
source.valkey-sql-cdc.bootVersion=3

```
```shell
db.user.updateMany({synced: true, "_id": "jsmith" }, {$set:{synced: false, name: "Johnson Smith"}})  
```


```shell
mongo-valkey-stream=cdc-debezium | valkey-sink
```

debezium.properties.topic.prefix=my-topic

```properties
app.cdc-debezium.cdc.name=mongo-cdc
app.cdc-debezium.cdc.connector=MongoDB
app.cdc-debezium.debezium.properties.connector.class=io.debezium.connector.mongodb.MongodbSourceConnector
app.cdc-debezium.debezium.properties.name=my-connector
app.cdc-debezium.debezium.properties.database.server.id=85744
app.cdc-debezium.debezium.properties.schema.history.internal=io.debezium.relational.history.MemorySchemaHistory
app.cdc-debezium.debezium.properties.offset.storage=org.apache.kafka.connect.storage.MemoryOffsetBackingStore
app.cdc-debezium.debezium.properties.mongodb.hosts=rs0/localhost:27017
app.cdc-debezium.debezium.properties.topic.prefix=dbserver1
app.cdc-debezium.debezium.properties.mongodb.user=debezium
app.cdc-debezium.debezium.properties.mongodb.password=dbz
app.cdc-debezium.debezium.properties.database.whitelist=inventory
app.cdc-debezium.debezium.properties.tasks.max=1
app.cdc-debezium.debezium.properties.schema=true
app.cdc-debezium.debezium.properties.key.converter.schemas.enable=true
app.cdc-debezium.debezium.properties.value.converter.schemas.enable=true
app.cdc-debezium.debezium.properties.transforms=unwrap
app.cdc-debezium.debezium.properties.transforms.unwrap.type=io.debezium.transforms.ExtractNewRecordState
app.cdc-debezium.debezium.properties.transforms.unwrap.add.fields=name,db
app.cdc-debezium.debezium.properties.transforms.unwrap.delete.handling.mode=none
app.cdc-debezium.debezium.properties.transforms.unwrap.drop.tombstones=true
app.valkey-sink.valKey.consumer.key.prefix=mongo-
```