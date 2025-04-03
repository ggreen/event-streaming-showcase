# Setup SCDF with Postgres

This page explains how to use Postgres with SCDF.


Components

- [RabbitMQ](https://www.rabbitmq.com/)
- [Spring Cloud Data Flow](https://spring.io/projects/spring-cloud-dataflow)
- [Bitnami](https://github.com/bitnami/charts/tree/main/bitnami/spring-cloud-dataflow/) 
- [Tanzu Postgres](https://www.vmware.com/products/app-platform/tanzu-for-postgres)


#  Getting Started



Note, for Postgres you will need to get USERNAME/Password
from [Broadcom Support](https://support.broadcom.com/) to 
access the Portal Registry.


## Postgres Install

Install Tanzu Postgres Operator 

```shell
deployment/cloud/k8/data-services/postgres/tanzu-postgres-operator-setup.sh
```

Deploy Postgres Database

```shell
kubectl apply -f deployment/cloud/k8/data-services/postgres/postgres-db.yaml
```

## RabbitMQ Install

Install RabbitMQ Operator

```shell
kubectl apply -f deployment/cloud/k8/data-services/rabbitmq/rabbit-k8-setup.sh
```

Deploy RabbitMQ Cluster

```shell
kubectl apply -g deployment/cloud/k8/data-services/rabbitmq/rabbitmq-3-node.yml
```

## SCDF Install

See install_scdf_with_postgres.sh

```shell
./deployment/cloud/k8/data-services/scdf/install_scdf_with_postgres.sh
```


Access SCDF dashboard 

Execute the following to the port

```shell
export SERVICE_PORT=$(kubectl get -o jsonpath="{.spec.ports[0].port}" services scdf-spring-cloud-dataflow-server)
export SERVICE_IP=$(kubectl get svc scdf-spring-cloud-dataflow-server -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

echo "http://${SERVICE_IP}:${SERVICE_PORT}/dashboard"
```