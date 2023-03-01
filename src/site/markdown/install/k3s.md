# K3S

During development and also for production you can run Imixs-Office-Workflwo in the lithweight Kubernetes Environment [K3S](https://docs.k3s.io/).

In it's most easiest setup you can install and start K3S following the [Quick-Start Guide](https://docs.k3s.io/quick-start)

	$ curl -sfL https://get.k3s.io | sh -
	$ sudo systemctl status k3s.service


A kubeconfig file will be written to `/etc/rancher/k3s/k3s.yaml` and the kubectl installed by K3s will automatically use it. Verify your cluster and node info.

	$ sudo kubectl cluster-info
	$ sudo kubectl get node

If you don't have kubectl installed you can use the k3s command instead

	$ sudo k3s kubectl cluster-info


We recommend the [k9s](https://github.com/derailed/k9s) tool to check your K3S cluster and your local deployments.

## Network

In difference to the Minikube System K3S does not require additional network configuration for local testing. The network traffic is controlled by the [Traefik ingress](https://traefik.io/) controller which is part of K3S.


## Persistence Volumes

When deploying Imixs-Office-Workflow you need to retain data storage for the SQL Database and the Search Index. Within Kubernetes this is managed by providing a persistent storage. 

### Kuberentes Local Storage

A local volume represents a mounted local storage device such as a disk, partition or directory. Find details about Kubernetes Local Storage [here](https://kubernetes.io/docs/concepts/storage/volumes/#local).

To use local storage first deploy the dev-storage-class:

	$ kubectl apply -f 010-local-storage.yaml

next create your storage directory localy. For example:

	$ mkdir -p /opt/kubernetes-local-pv/office-dbdata
	$ mkdir -p /opt/kubernetes-local-pv/office-index

Here is an example how to setup a Persistence Volume Claim and a Persistence Volume using local storage:

```
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: dbdata
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: dev-local-storage
  resources:
    requests:
      storage: 128Mi

---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: dbdata
spec:
  capacity:
    storage: 128Mi
  accessModes:
  - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: dev-local-storage
  local:
    path: /opt/kubernetes-local-pv/office-dbdata
  nodeAffinity:
    required:
      nodeSelectorTerms:
      - matchExpressions:
        - key: node.kubernetes.io/instance-type
          operator: In
          values:
          - k3s
```

          
**Note:** For deploying local persistence volumes we also create a storage class for dev:

```
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: dev-local-storage
provisioner: kubernetes.io/no-provisioner
volumeBindingMode: WaitForFirstConsumer
```

# Deployment

To deploy Imixs-Office-Workflow on K3S you can use the deployment configuration located in /kubernetes/k3s

	$ kubectl apply -f imixs-office-workflow/kubernetes/k3s/office-workflow






# Registry

You can install a private registry in K3s to push custom container images into your dev environment:

	# Create the registry storage directory
	$ mkdir -p /opt/kubernetes-local-pv/registry-storage
	# Deploy a Docker Registry
	$ kubectl apply -f 010-local-storage.yaml





