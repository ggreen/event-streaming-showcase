#!/bin/bash

# Set GemFire Pre-Requisite

kubectl create namespace cert-manager
kubectl create namespace sql-system
kubectl create namespace tanzu-data


 kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.4/cert-manager.yaml

sleep 5
kubectl wait pod -l=app=cert-manager --for=condition=Ready --timeout=360s --namespace=cert-manager
kubectl wait pod -l=app=cainjector --for=condition=Ready --timeout=360s --namespace=cert-manager
kubectl wait pod -l=app=webhook --for=condition=Ready --timeout=360s --namespace=cert-manager



# Install Postgres

#docker images "postgres-*"
helm registry login tanzu-sql-postgres.packages.broadcom.com \
       --username=$BROADCOM_USERNAME \
       --password=$TANZU_POSTGRES_FOR_K8_PASSWORD


rm -rf /tmp/vmware-sql-postgres-operator
helm pull oci://tanzu-sql-postgres.packages.broadcom.com/vmware-sql-postgres-operator --version v4.0.0 --untar --untardir /tmp


kubectl create secret docker-registry regsecret \
        --docker-server=https://tanzu-sql-postgres.packages.broadcom.com/ \
        --docker-username=$BROADCOM_USERNAME \
        --docker-password=$TANZU_POSTGRES_FOR_K8_PASSWORD --namespace=sql-system


kubectl create secret docker-registry regsecret \
        --docker-server=https://tanzu-sql-postgres.packages.broadcom.com/ \
        --docker-username=$BROADCOM_USERNAME \
        --docker-password=$TANZU_POSTGRES_FOR_K8_PASSWORD --namespace=tanzu-data


helm install tanzu-postgres-operator /tmp/vmware-sql-postgres-operator/  --wait  --namespace=sql-system

kubectl get serviceaccount --namespace=sql-system
kubectl get all --selector app=postgres-operator --namespace=sql-system
kubectl logs -l app=postgres-operator  --namespace=sql-system
kubectl api-resources --api-group=sql.tanzu.vmware.com --namespace=sql-system


#kubectl apply -f deployment/cloud/k8/data-services/postgres/tanzu-data-services-storage-class.yaml
#kubectl patch storageclass tanzu-data-services -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"false"}}}'

kubectl config set-context --current --namespace=tanzu-data
kubectl get storageclasses --namespace=tanzu-data


#sleep 30
#kubectl apply -f deployments/cloud/k8/dataServices/postgres/postgres.yml
#sleep 40
#kubectl wait pod -l=app=postgres --for=condition=Ready --timeout=360s
#kubectl wait pod -l=statefulset.kubernetes.io/pod-name=postgres-0 --for=condition=Ready --timeout=360s
