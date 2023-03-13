# K3S

During development and also for production you can run Imixs-Office-Workflwo in the lithweight Kubernetes Environment [K3S](https://docs.k3s.io/).

In it's most easiest setup you can install and start K3S following the [Quick-Start Guide](https://docs.k3s.io/quick-start)

    $ curl -sfL https://get.k3s.io | sh -
    $ sudo systemctl status k3s.service

A kubeconfig file will be written to `/etc/rancher/k3s/k3s.yaml` and the kubectl installed by K3s will automatically use it. Verify your cluster and node info.

    $ sudo k3s kubectl cluster-info
    $ sudo k3s kubectl get node

If you have  kubectl installed you can configure your cluster to use the k3s configuration. 

    # grant access to all users
    $ sudo chmod a+r /etc/rancher/k3s/k3s.yaml
    # setting a environment variable or
    $ export KUBECONFIG=/etc/rancher/k3s/k3s.yaml


Now you can access your k3s cluster with kubectl directly:

    $ kubectl cluster-info

## K9S

We recommend the [k9s](https://github.com/derailed/k9s) tool to check your K3S cluster and your local deployments.

<img src="../install/k9s.png" />

To run k9s connecting to your local k3s Cluster run:

    $ sudo ./k9s --kubeconfig /etc/rancher/k3s/k3s.yaml

## Start, Stop, Autostart Function

You can stop the k3s service if it is currently running by running the following command:

    $ sudo systemctl stop k3s

This will stop the k3s service immediately. To start k3s again run:

    $ sudo systemctl start k3s

K3s is typically managed as a service in Linux systems, so it will typically start on boot. You can disable it from starting automatically by using the systemctl command:

    $ sudo systemctl disable k3s

This will prevent k3s from starting automatically when the system boots up.

Another option to stop k3s and all related tasks is to run:

    $ /usr/local/bin/k3s-killall.sh


## Network

In difference to the Minikube System K3S does not require additional network configuration for local testing. The network traffic is controlled by the [Traefik ingress](https://traefik.io/) controller which is part of K3S.


## Persistence Volumes

When deploying Imixs-Office-Workflow or other services you may need to retain data storage for the SQL Database or the Search Index. Within Kubernetes this is managed by providing a persistent storage. A storage solution is in general not part of Kubernetes but you can use the so called 'Local Storage' to use local disk space.

### Kuberentes Local Storage

A local volume represents a mounted local storage device such as a disk, partition or directory. 
To use local storage within your local Kubernetes cluster first deploy the dev-storage-class:

    $ kubectl apply -f git/imixs-office-workflow/kubernetes/k3s/local-storage-class.yaml

next you can create your local storage directory where the data should be stored on your disk:

	$ mkdir -p /opt/kubernetes-local-pv/office-dbdata

Here is an example how to setup a Persistence Volume Claim and a Persistence Volume using the local storage:

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

Find more details about the usage of Kubernetes Local Storage [here](https://kubernetes.io/docs/concepts/storage/volumes/#local).
## Deployment Imixs-Office-Workflow

To deploy Imixs-Office-Workflow on K3S you can use the deployment configuration located in /kubernetes/k3s.

First ensure that you have created your local storage directories to store the SQL and index data:

```
	$ mkdir -p /opt/kubernetes-local-pv/office-dbdata
	$ mkdir -p /opt/kubernetes-local-pv/office-index
```

next you can apply the deployment:

    $ kubectl apply -f git/imixs-office-workflow/kubernetes/k3s/office-workflow


## Container Registry

You can also easily install a private registry in K3s to push custom container images into your dev environment. These images are than accessible by your cluster:

Frist create a local storage directory for your registry data:

```
$ mkdir -p /opt/kubernetes-local-pv/registry-storage
```

next you can start the deployment:

```
$ kubectl apply -f git/imixs-office-workflow/kubernetes/k3s/registry
```

**Note:** You can define a HTTP Access Password by setting the environment variable `REGISTRY_HTTP_SECRET`.

To test your registry open the following URL in a Web Browser:

    http://localhost:5000/v2/_catalog

### Secure Your Registry

You can secure the access to your registry by defining a Basic Authentication middleware via  a Traefik IngressRoute 

First use the commandline tool `htpasswd` to generate a password file:

    $ htpasswd -c registry-auth-file admin

Next you can create a corresponding Kubernetes Secret:

    $ kubectl create secret generic registry-basic-auth --from-file=registry-auth

The following is an example how to define a IngressRoute with Traefik:

```
---
kind: Middleware
apiVersion: traefik.containo.us/v1alpha1
metadata:
  name: registry-basic-auth
spec:
  basicAuth:
    secret: registry-basic-auth

---
apiVersion: traefik.containo.us/v1alpha1
kind: IngressRoute
metadata:
  name: docker-registry
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`example.foo.com`)
      kind: Rule
      middlewares:
        - name: registry-basic-auth
      services:
      - name: docker-registry
        port: 5000
```

