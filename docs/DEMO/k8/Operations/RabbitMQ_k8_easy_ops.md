

Easy to Provision (2 minutes)

```shell
kubectl apply -f deployment/cloud/k8/data-services/rabbitmq/rabbitmq-5-node.yml
```


Get Credentials

```shell
export mgmturl=`kubectl get services rabbitmq -o jsonpath='{.status.loadBalancer.ingress[0].ip}'`
kubectl get secret rabbitmq-default-user -o jsonpath="{.data.username}"
export ruser=`kubectl get secret rabbitmq-default-user -o jsonpath="{.data.username}"| base64 --decode`
export rpwd=`kubectl get secret rabbitmq-default-user -o jsonpath="{.data.password}"| base64 --decode`

echo ""
echo "USER:" $ruser
echo "PASSWORD:" $rpwd

echo "open : http://"$mgmturl":15672"

```


Upgrade


```shell
kubectl apply -f deployment/cloud/k8/data-services/rabbitmq/rabbitmq-5-node-upgrade.yml

```