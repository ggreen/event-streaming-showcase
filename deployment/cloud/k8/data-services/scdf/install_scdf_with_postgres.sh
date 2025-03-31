export ACCT_USER_NM=`kubectl get secrets/rabbitmq-default-user --template={{.data.username}} | base64 -d`
export ACCT_USER_PWD=`kubectl get secrets/rabbitmq-default-user --template={{.data.password}} | base64 -d`
export DB_USER_NM=`kubectl get secrets/postgres-db-app-user-db-secret --template={{.data.username}} | base64 -d`
export DB_USER_PASSWORD=`kubectl get secrets/postgres-db-app-user-db-secret --template={{.data.password}} | base64 -d`
helm repo add bitnami https://charts.bitnami.com/bitnami




helm install scdf oci://registry-1.docker.io/bitnamicharts/spring-cloud-dataflow --set externalRabbitmq.enabled=true --set rabbitmq.enabled=false --set externalRabbitmq.host=rabbitmq --set externalRabbitmq.username=$ACCT_USER_NM --set externalRabbitmq.password=$ACCT_USER_PWD --set server.service.type=LoadBalancer --set server.service.ports.http=9393 --set mariadb.enabled=false --set externalDatabase.dataflow.url=jdbc:postgresql://postgres-db/postgres-db --set externalDatabase.dataflow.username=$DB_USER_NM --set externalDatabase.dataflow.password=$DB_USER_PASSWORD --set externalDatabase.dataflow.database=postgres-db --set externalDatabase.skipper.username=$DB_USER_NM --set externalDatabase.skipper.password=$DB_USER_PASSWORD --set externalDatabase.skipper.url=jdbc:postgresql://postgres-db/postgres-db


sleep 20

kubectl wait pod -l=app.kubernetes.io/instance=scdf --for=condition=Ready --timeout=360s

export SERVICE_PORT=$(kubectl get -o jsonpath="{.spec.ports[0].port}" services scdf-spring-cloud-dataflow-server)

export SERVICE_IP=$(kubectl get svc scdf-spring-cloud-dataflow-server -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

echo "http://${SERVICE_IP}:${SERVICE_PORT}/dashboard"
