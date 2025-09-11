# Insurance Demo


Example Claims JSON data

```json

    {
      "id": "1",
      "policyId": "11",
      "claimType": "auto",
      "description" : "",
      "notes" : "",
      "claimAmount": 2323.22,
      "dateOfLoss": "3/3/20243",
      "insured": {
        "name": "Josiah Imani",
        "homeAddress" : {
          "street" : "1 Straight",
          "city" : "JC",
          "state" : "JC",
          "zip" : "02323"
        }
      },
      "lossType": "Collision"
    }

```

# Setup

Setup Services

Valkey
```shell
cf create-service p.redis on-demand-cache valkey
```

Postgres

```shell
cf create-service postgres on-demand-postgres-db postgres
```


Download Applications

```shell
mkdir -p runtime/apps
wget -P runtime/apps https://github.com/ggreen/spring-modern-data-architecture/releases/download/expore-2025/valkey-console-app-0.0.2.jar
wget -P runtime/apps  https://github.com/ggreen/spring-modern-data-architecture/releases/download/expore-2025/jdbc-sql-console-app-0.0.4.jar
```

Cf push

```shell
cf push insurance-jdbc-sql-console -f deployment/cloud/cloudFoundry/apps/insurance/jdbc-console.yaml -b java_buildpack_offline -p runtime/apps/jdbc-sql-console-app-0.0.4.jar
```

Valkey Console

```shell
cf push insurance-valkey-console-app -f deployment/cloud/cloudFoundry/apps/insurance/valkey-console.yaml -b java_buildpack_offline -p runtime/apps/valkey-console-app-0.0.2.jar
```

**Register Custom Apps In SCDF**

Add Applications using properties

```properties
sink.jdbc-upsert=maven://com.github.ggreen:jdbc-upsert:0.2.1
sink.jdbc-upsert.bootVersion=3
processor.jdbc-sql-processor=maven://com.github.ggreen:jdbc-sql-processor:0.0.1
processor.jdbc-sql-processor.bootVersion=3
sink.valkey-sink=maven://com.github.ggreen:valkey-sink:0.0.1
sink.valkey-sink.bootVersion=3
```

-------------------------------

# Start Demo


## Create Table


```shell
export JDBC_CONSOLE_APP=`cf apps | grep insurance-jdbc-sql-console  | awk  '{print $5}'`
echo $JDBC_CONSOLE_APP

open http://$JDBC_CONSOLE_APP
```


Create SQL in the JDBC console

```sql
create schema if not exists insurance;

CREATE TABLE if not exists  insurance.claims(
   id varchar(255) PRIMARY KEY,
   payload JSONB NOT NULL
);
```
--------------------------------------

## Create HTTP JSON JDBC API

Open SCDF Pipeline

```shell
claims-http=http| tanzu-sql: jdbc-upsert --insert-sql="UPDATE insurance.claims SET payload=to_json(:payload::json) WHERE id= :id" --update-sql="INSERT INTO insurance.claims (id,payload) VALUES(:id, to_json(:payload::json))"
```

```properties
deployer.tanzu-sql.cloudfoundry.services=postgres
app.tanzu-sql.jdbc.upsert.insert-sql=UPDATE insurance.claims SET payload=to_json(:payload::json) WHERE id= :id
app.tanzu-sql.jdbc.upsert.update-sql=INSERT INTO insurance.claims (id,payload) VALUES(:id, to_json(:payload::json))
deployer.tanzu-sql.bootVersion=3
deployer.http.bootVersion=2
deployer.jdbc.bootVersion=3
app.http.spring.cloud.stream.rabbit.binder.connection-name-prefix=http
```

Save URIs for API
```shell
export CLAIMS_HOST=`cf apps | grep claims-http-http  | awk  '{print $4}'`
export CLAIMS_URI="https://$CLAIMS_HOST"
echo $CLAIMS_URI
```

Submit 1 Claim
```shell
curl $CLAIMS_URI -L -H "Accept: application/json" --header "Content-Type: application/json"  -X POST -d "{ \"id\": \"1\", \"policyId\": \"11\", \"claimType\": \"auto\", \"description\" : \"\", \"notes\" : \"\", \"claimAmount\": 2323.22, \"dateOfLoss\": \"3/3/20243\", \"insured\": { \"name\": \"Josiah Imani\", \"homeAddress\" : { \"street\" : \"1 Straight\", \"city\" : \"JC\", \"state\" : \"JC\", \"zip\" : \"02323\" } }, \"lossType\": \"Collision\" }"
```


Submit Multiple Claims

```shell
for i in {2..20}
do
  claimJson='{ "id": "';
  claimJson+=$i;
  claimJson+='", "policyId": "';
  claimJson+=$i;
  claimJson+='",  "claimType": "auto", "description" : "", "notes" : "", "claimAmount": ';
  claimJson+=$i; 
  claimJson+=', "dateOfLoss": "3/3/20243", "insured": { "name": "User ';
  claimJson+=$i;
  claimJson+='", "homeAddress" : { ';
  claimJson+=' "street" : "';
  claimJson+=$i;
  claimJson+=' Straight", "city" : "JC", "state" : "NJ", "zip" : "02323" } }, "lossType": "Collision" }';

  echo '========' POSTING $claimJson;
  
  curl $CLAIMS_URI -H "Accept: application/json" --header "Content-Type: application/json"  -X POST -d $claimJson
  echo
done
```

In the JDBC Console

Execute SQL

```sql
select * from insurance.claims
```

-------------------------------------


WARNING WORK IN PROGRESS

#  HTTP ValKey JDBC Caching Enrichment API

Create SCDF stream

```shell
claims-caching-http=http | tanzu-sql-select: jdbc-sql-processor --query="select id, payload->> 'lossType' as lossType, payload-> 'insured' ->> 'name' as name, concat( payload->'insured'->'homeAddress' ->> 'street', ', ', payload->'insured'->'homeAddress' ->> 'city', ', ', payload ->'insured'->'homeAddress' ->> 'state', ' ', payload -> 'insured'->'homeAddress' ->> 'zip') as homeAddress from insurance.claims WHERE id= :id" | valkey-sink --valKey.consumer.key.prefix="insurance-claims-"
```

Deploy using Proeprties

```properties
deployer.tanzu-sql-select.cloudfoundry.services=postgres
app.tanzu-sql-select.jdbc.sql.query="select id, payload->> 'lossType' as lossType, payload-> 'insured' ->> 'name' as name, concat( payload->'insured'->'homeAddress' ->> 'street', ', ', payload->'insured'->'homeAddress' ->> 'city', ', ', payload ->'insured'->'homeAddress' ->> 'state', ' ', payload -> 'insured'->'homeAddress' ->> 'zip') as homeAddress from insurance.claims WHERE id= :id"
app.valkey-sink.valKey.consumer.key.prefix=insurance-claims-
deployer.tanzu-sql-select.bootVersion=3
deployer.http.bootVersion=2
deployer.jdbc.bootVersion=3
app.http.spring.cloud.stream.rabbit.binder.connection-name-prefix=http
app.valkey-sink.spring.cloud.stream.rabbit.binder.connection-name-prefix=valkey
app.valkey.redis.consumer. key-expression=payload.id
deployer.valkey-sink.cloudfoundry.services=valkey
deployer.http.memory=1400
deployer.valkey-sink.memory=1400
deployer.tanzu-sql-select.memory=1400
```


Get Data Pipeline HTTP URL

```shell
export HTTP_CACHE_CLAIMS_HOST=`cf apps | grep claims-caching-http-http  | awk  '{print $4}'`
export HTTP_CACHE_CLAIMS_HOST_URI="https://$HTTP_CACHE_CLAIMS_HOST"
echo $HTTP_CACHE_CLAIMS_HOST_URI
```

VaKey Access App

```shell
export VALKEY_HOST=`cf apps | grep insurance-valkey-console-app  | awk  '{print $5}'`
export VALKEY_HOST_URI="https://$VALKEY_HOST"
open $VALKEY_HOST_URI
```



Command in Console

Execute Valkey command
```shell
 keys *
```

Or Get using Curl No Data

Execute Valkey command
```valkey
GET ValKeyConsumer-1
```

Cache First Claim


```shell
curl $HTTP_CACHE_CLAIMS_HOST_URI -H "Accept: application/json" --header "Content-Type: application/json"  -X POST -d "{ \"id\": \"1\" }"
```

Get claim

Execute Valkey command
```valkey
GET ValKeyConsumer-1
```


Second Claim - No Data

Execute Valkey command
```valkey
GET ValKeyConsumer-1
```

Cache Second Claim in ValKey

```shell
curl $HTTP_CACHE_CLAIMS_HOST_URI -H "Accept: application/json" --header "Content-Type: application/json"  -X POST -d "{ \"id\": \"2\" }"
```


Get Cache 2nd claim

```shell
GET ValKeyConsumer-2
```

Cache Second Claim in ValKey

```shell
curl $HTTP_CACHE_CLAIMS_HOST_URI -H "Accept: application/json" --header "Content-Type: application/json"  -X POST -d "{ \"id\": \"2\" }"
```


-------------

# Clean Up

ValKey Clean up

```shell
 DEL 1 2
```

```shell
curl -X 'DELETE' \
  "$VALKEY_HOST_URI/valKey/del?keys=1&keys=2" \
  -H 'accept: */*'
```



```shell
open http://$JDBC_CONSOLE_APP
```


```sql
delete from insurance.claims
```
