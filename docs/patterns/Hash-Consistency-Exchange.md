
Start RabbitMQ

```shell
deployment/local/containers/rabbit.sh
```

# Hash Exchange

RabbitMQ Consistent Hash Exchange Type supports 
 distribution across a number of queues based on the routing key of the message.


```shell
rabbitmq-plugins enable rabbitmq_consistent_hash_exchange
```

```shell
rabbitmqadmin declare exchange name=ordering.scaling.hash.exchange type=x-consistent-hash durable=true
```


```shell
rabbitmqadmin declare queue name=ordering.scaling.hash.q1 durable=true queue_type=quorum
rabbitmqadmin declare queue name=ordering.scaling.hash.q2 durable=true queue_type=quorum
rabbitmqadmin declare queue name=ordering.scaling.hash.q3 durable=true queue_type=quorum
```


Binding


```shell
rabbitmqadmin declare binding source=ordering.scaling.hash.exchange destination=ordering.scaling.hash.q1 destination_type=queue routing_key="1"
rabbitmqadmin declare binding source=ordering.scaling.hash.exchange destination=ordering.scaling.hash.q2 destination_type=queue routing_key="1"
rabbitmqadmin declare binding source=ordering.scaling.hash.exchange destination=ordering.scaling.hash.q3 destination_type=queue routing_key="1"
```

If the account number is used as a routing key, then orders with the same account will be routed to the same.
This allows you scale by partitioning the message across a desired number of queues. 
It also allows you to maintain ordering as you scale.


When a queue is bound to a Consistent Hash exchange, the binding key is a 
number-as-a-string which indicates the binding weight: 
the number of buckets (sections of the range) that will be associated with the target queue.

In most environments, using one bucket per binding (and thus queue) is highly recommended as it is the simplest way to achieve reasonably even balancing.


In the following example, there are 

```shell
for i in {1..3000}
do
  accountNumber=$((i % 5))
  echo accountNumber: $accountNumber
  rabbitmqadmin publish exchange=ordering.scaling.hash.exchange routing_key="$accountNumber" payload="{\"orderId\" :\"$i\", \"accountNumber\" : \"VMware$i\"}"
  rabbitmqadmin publish exchange=ordering.scaling.hash.exchange routing_key="$accountNumber" payload="{\"orderId\" :\"$i\", \"accountNumber\" : \"Tanzu$i\"}"
done
```