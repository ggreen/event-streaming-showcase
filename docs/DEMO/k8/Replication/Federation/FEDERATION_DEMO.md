kubectl apply -f deployment/cloud/k8/data-services/rabbitmq/Federation



export bunny_user=`kubectl get secret federation-bunny-default-user -o jsonpath="{.data.username}"| base64 --decode`
export bunny_pwd=`kubectl get secret federation-bunny-default-user -o jsonpath="{.data.password}"| base64 --decode`

export hare_user=`kubectl get secret federation-hare-default-user -o jsonpath="{.data.username}"| base64 --decode`
export hare_pwd=`kubectl get secret federation-hare-default-user -o jsonpath="{.data.password}"| base64 --decode`


kubectl exec federation-bunny-server-0  -- rabbitmqctl add_user admin admin
kubectl exec federation-bunny-server-0 -- rabbitmqctl set_permissions  -p / admin ".*" ".*" ".*"
kubectl exec federation-bunny-server-0 -- rabbitmqctl set_user_tags admin administrator


kubectl exec federation-hare-server-0  -- rabbitmqctl add_user admin admin
kubectl exec federation-hare-server-0 -- rabbitmqctl set_permissions  -p / admin ".*" ".*" ".*"
kubectl exec federation-hare-server-0 -- rabbitmqctl set_user_tags admin administrator

kubectl exec -it federation-bunny-server-0 -- rabbitmqctl set_parameter federation-upstream hare-upstream '{"uri":"amqp://admin:admin@federation-hare","expires":3600000}'

kubectl exec -it federation-hare-server-0 -- rabbitmqctl set_parameter federation-upstream bunny-upstream '{"uri":"amqp://admin:admin@federation-bunny","expires":3600000}'



##  all exchanges whose names begin with "amq." 

kubectl exec -it federation-bunny-server-0 -- rabbitmqctl set_policy --apply-to exchanges federate-hare "^amq\." '{"federation-upstream-set":"all"}'

##  all exchanges whose names begin with "bi."
kubectl exec -it federation-hare-server-0 -- rabbitmqctl set_policy --apply-to exchanges federate-bunny "^bi\." '{"federation-upstream-set":"all"}'


## Published message in hare will replicate to bunny

Binding RULE
stream

```json
{"id":"1", "stock","VMW"}
```


## Published message in bunny will replicate to har

Create exchange bi.....

