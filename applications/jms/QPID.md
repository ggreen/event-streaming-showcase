




```shell
podman exec -it rabbitmq bash

```

```shell
rabbitmq-plugins enable rabbitmq_amqp1_0 rabbitmq_jms_topic_exchange
```
```shell
rabbitmq-plugins enable rabbitmq_jms
```

rabbitmq-plugins enable rabbitmq_jms_management

```shell
rabbitmq-plugins list
```


store.orders.queue
x-queue-type = jms
x-selector-fields = ["JMSPriority", "category", "price", "in_stock"]



accounts


app.message.selector=status = 'ACTIVE' AND (state = 'NY' OR state = 'CA') AND city IN ('NY', 'LA', 'IM','ANI') AND zip LIKE '90%'

status = 'ACTIVE' 


```shell
rabbitmqadmin -u $TANZU_RABBIT_USERNAME -p $TANZU_RABBIT_PASSWORD declare queue --name accounts --arguments='{"x-queue-type": "jms", "x-selector-fields": ["name", "status","city", "state", "zip"]}'

  --vhost="/" \
  --arguments='{"x-queue-type": "jms", }'
```


podman exec -it rabbitmq bash
rabbitmq-plugins enable rabbitmq_shovel rabbitmq_shovel_management


```shell
curl -X 'POST' \
  'http://localhost:8077/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "01",
  "name": "Joe Smith",
  "accountType": "B",
  "status": "ACTIVE",
  "notes": "Working hard",
  "location": {
    "id": "loc1",
    "address": "123 Strait Street",
    "cityTown": "NY",
    "stateProvince": "NY",
    "zipPostalCode": "90923",
    "countryCode": "US"
  }
}'
```
