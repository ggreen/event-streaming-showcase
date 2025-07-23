registry=<host name of your registry>
namespace=<the namespace to use>



cd /Users/devtools/integration/scdf/tanzu/k8/spring-cloud-data-flow

./bin/import-all-images.sh --repository-path-prefix cloudnativedata


#registry.packages.broadcom.com/p-scdf-for-kubernetes

#docker pull registry.packages.broadcom.com/p-scdf-for-kubernetes/scdf-pro-server:1.6.5


#BROADCOM_USERNAME
#--docker-server=${registry} \

kubectl create secret \
docker-registry scdf-image-regcred \
--namespace=default \
--docker-username=${username} \
--docker-password=${password}