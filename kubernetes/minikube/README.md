# Minikube

You can run Imixs-Office-Workflow for development with [Minikube](https://minikube.sigs.k8s.io/docs/). This is an easy way to full simulate a Kuberentes Cluster locally. 

After you have followed the [Install Guide for Minikube](https://kubernetes.io/de/docs/tasks/tools/install-minikube/) you can start minikube on your machine:

	$ minikube start
	
	
With the commandline tool kubectl you can now deploy the dev version of Imixs-Office-Workflow into minikube:

	$ kubectl deploy kubernetes/minikube/


You can check the deployment status with:

	$ kubectl get pods

We recommend the [k9s](https://github.com/derailed/k9s) tool to check your minikube cluster and your deployments.


## Network

To access the deployed instance of Imixs-Office-Workflow running within your minikube cluster see documentation [Accessing apps](https://minikube.sigs.k8s.io/docs/handbook/accessing/). 

We recommend to run a minikube tunnel. First check the service IP of your deployments:

```
$ kubectl get services
NAME                    TYPE           CLUSTER-IP     EXTERNAL-IP    PORT(S)                         AGE
db                      ClusterIP      None           <none>         5432/TCP                        82s
imixs-office-workflow   LoadBalancer   10.98.163.43   10.98.163.43   8080:30482/TCP,9990:32303/TCP   82s
kubernetes              ClusterIP      10.96.0.1      <none>         443/TCP                         13h
```

Now you can start a tunnel with will expose all ports of services of type `LoadBalancer` 

	$ minikube tunnel

Open in your browser:

	http://REPLACE_WITH_EXTERNAL_IP:8080




