# Setup


Starting Tanzu RabbitMQ


```shell
PLUGINS="rabbitmq_jms,rabbitmq_jms_management,rabbitmq_amqp1_0,rabbitmq_shovel,rabbitmq_shovel_management,rabbitmq_stream,rabbitmq_stream_browser,rabbitmq_stream_management,rabbitmq_delayed_queue"

echo "Starting Tanzu RabbitMQ with initial plugins: $PLUGINS..."

podman run -it --rm \
  --name tanzu-rabbitmq \
  -p 5672:5672 \
  -p 5552:5552 \
  -p 15672:15672 \
  -p 1883:1883 \
  -e RABBITMQ_ENABLED_PLUGINS="$PLUGINS" \
  -e RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS="-rabbitmq_stream advertised_host localhost -rabbitmq_stream advertised_port 5552" \
  rabbitmq.packages.broadcom.com/vmware-tanzu-rabbitmq:4.3.0
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

curl -X 'POST' \
  'http://localhost:8077/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "04",
  "name": "Jill Smith",
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


```shell
curl -X 'POST' \
  'http://localhost:8077/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '[
  {
    "id": "01",
    "name": "John Doe",
    "accountType": "B",
    "status": "ACTIVE",
    "notes": "Premium client",
    "location": {
      "id": "loc1",
      "address": "100 Broadway",
      "cityTown": "New York",
      "stateProvince": "NY",
      "zipPostalCode": "10001",
      "countryCode": "US"
    }
  },
  {
    "id": "02",
    "name": "Jane Doe",
    "accountType": "S",
    "status": "ACTIVE",
    "notes": "Standard savings",
    "location": {
      "id": "loc2",
      "address": "200 Pine St",
      "cityTown": "San Francisco",
      "stateProvince": "CA",
      "zipPostalCode": "94104",
      "countryCode": "US"
    }
  },
  {
    "id": "03",
    "name": "Bob Johnson",
    "accountType": "B",
    "status": "INACTIVE",
    "notes": "Account suspended",
    "location": {
      "id": "loc3",
      "address": "300 Elm St",
      "cityTown": "Chicago",
      "stateProvince": "IL",
      "zipPostalCode": "60601",
      "countryCode": "US"
    }
  },
  {
    "id": "04",
    "name": "Jill Smith",
    "accountType": "B",
    "status": "ACTIVE",
    "notes": "Working hard",
    "location": {
      "id": "loc4",
      "address": "123 Strait Street",
      "cityTown": "NY",
      "stateProvince": "NY",
      "zipPostalCode": "90923",
      "countryCode": "US"
    }
  },
  {
    "id": "05",
    "name": "Michael Green",
    "accountType": "C",
    "status": "ACTIVE",
    "notes": "Corporate checking",
    "location": {
      "id": "loc5",
      "address": "555 Market St",
      "cityTown": "Philadelphia",
      "stateProvince": "PA",
      "zipPostalCode": "19103",
      "countryCode": "US"
    }
  },
  {
    "id": "06",
    "name": "Emily Brown",
    "accountType": "B",
    "status": "ACTIVE",
    "notes": "Daily spender",
    "location": {
      "id": "loc6",
      "address": "789 Maple Ave",
      "cityTown": "Austin",
      "stateProvince": "TX",
      "zipPostalCode": "78701",
      "countryCode": "US"
    }
  },
  {
    "id": "07",
    "name": "David Miller",
    "accountType": "S",
    "status": "PENDING",
    "notes": "Awaiting verification",
    "location": {
      "id": "loc7",
      "address": "101 Oak Rd",
      "cityTown": "Miami",
      "stateProvince": "FL",
      "zipPostalCode": "33101",
      "countryCode": "US"
    }
  },
  {
    "id": "08",
    "name": "Sarah Wilson",
    "accountType": "B",
    "status": "ACTIVE",
    "notes": "High net worth",
    "location": {
      "id": "loc8",
      "address": "202 Birch Blvd",
      "cityTown": "Seattle",
      "stateProvince": "WA",
      "zipPostalCode": "98101",
      "countryCode": "US"
    }
  },
  {
    "id": "09",
    "name": "James Taylor",
    "accountType": "C",
    "status": "ACTIVE",
    "notes": "Business payroll",
    "location": {
      "id": "loc9",
      "address": "303 Cedar Ln",
      "cityTown": "Boston",
      "stateProvince": "MA",
      "zipPostalCode": "02108",
      "countryCode": "US"
    }
  },
  {
    "id": "10",
    "name": "Linda Anderson",
    "accountType": "S",
    "status": "ACTIVE",
    "notes": "Retirement fund",
    "location": {
      "id": "loc10",
      "address": "404 Walnut St",
      "cityTown": "Denver",
      "stateProvince": "CO",
      "zipPostalCode": "80202",
      "countryCode": "US"
    }
  },
  {
    "id": "11",
    "name": "William Thomas",
    "accountType": "B",
    "status": "ACTIVE",
    "notes": "Frequent traveler",
    "location": {
      "id": "loc11",
      "address": "505 Spruce Way",
      "cityTown": "Las Vegas",
      "stateProvince": "NV",
      "zipPostalCode": "89101",
      "countryCode": "US"
    }
  },
  {
    "id": "12",
    "name": "Elizabeth Jackson",
    "accountType": "B",
    "status": "INACTIVE",
    "notes": "Closed by user",
    "location": {
      "id": "loc12",
      "address": "606 Ash Dr",
      "cityTown": "Phoenix",
      "stateProvince": "AZ",
      "zipPostalCode": "85001",
      "countryCode": "US"
    }
  },
  {
    "id": "13",
    "name": "Richard White",
    "accountType": "C",
    "status": "ACTIVE",
    "notes": "Merchant account",
    "location": {
      "id": "loc13",
      "address": "707 Willow Ct",
      "cityTown": "Atlanta",
      "stateProvince": "GA",
      "zipPostalCode": "30301",
      "countryCode": "US"
    }
  },
  {
    "id": "14",
    "name": "Barbara Harris",
    "accountType": "S",
    "status": "ACTIVE",
    "notes": "Student savings",
    "location": {
      "id": "loc14",
      "address": "808 Cypress Ave",
      "cityTown": "Columbus",
      "stateProvince": "OH",
      "zipPostalCode": "43201",
      "countryCode": "US"
    }
  },
  {
    "id": "15",
    "name": "Thomas Martin",
    "accountType": "B",
    "status": "ACTIVE",
    "notes": "Investment linked",
    "location": {
      "id": "loc15",
      "address": "909 Hickory Pl",
      "cityTown": "Detroit",
      "stateProvince": "MI",
      "zipPostalCode": "48201",
      "countryCode": "US"
    }
  },
  {
    "id": "16",
    "name": "Susan Thompson",
    "accountType": "B",
    "status": "PENDING",
    "notes": "New registration",
    "location": {
      "id": "loc16",
      "address": "111 Redwood Dr",
      "cityTown": "Portland",
      "stateProvince": "OR",
      "zipPostalCode": "97201",
      "countryCode": "US"
    }
  },
  {
    "id": "17",
    "name": "Joseph Garcia",
    "accountType": "C",
    "status": "ACTIVE",
    "notes": "Subsidiary firm",
    "location": {
      "id": "loc17",
      "address": "222 Magnolia St",
      "cityTown": "Houston",
      "stateProvince": "TX",
      "zipPostalCode": "77002",
      "countryCode": "US"
    }
  },
  {
    "id": "18",
    "name": "Jessica Martinez",
    "accountType": "S",
    "status": "ACTIVE",
    "notes": "Holiday savings",
    "location": {
      "id": "loc18",
      "address": "333 Poplar Rd",
      "cityTown": "San Diego",
      "stateProvince": "CA",
      "zipPostalCode": "92101",
      "countryCode": "US"
    }
  },
  {
    "id": "19",
    "name": "Christopher Robinson",
    "accountType": "B",
    "status": "ACTIVE",
    "notes": "Auto-pay enabled",
    "location": {
      "id": "loc19",
      "address": "444 Alder Ln",
      "cityTown": "NY",
      "stateProvince": "NY",
      "zipPostalCode": "90401",
      "countryCode": "US"
    }
  },
  {
    "id": "20",
    "name": "Karen Clark",
    "accountType": "B",
    "status": "INACTIVE",
    "notes": "Dormant account",
    "location": {
      "id": "loc20",
      "address": "555 Beech St",
      "cityTown": "St. Louis",
      "stateProvince": "MO",
      "zipPostalCode": "63101",
      "countryCode": "US"
    }
  }
]'
```
