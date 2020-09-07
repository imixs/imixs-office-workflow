# Kubernetes Deployment

*Imixs-Office-Workflow* already provides a base deployment configuration for Kubernetes. The setup is based on [Kustomize](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/) providing a declarative object management.


## Deploy 

To create the deployment objects form the base-deployment run:

	$ kubectl apply --kustomize https://github.com/imixs/imixs-office-workflow/kubernetes/

The service endpoint of imixs-office-workflow will be published on port 8080.
This basic deployment configuration assumes that  a default storage class is defined within your kubernetes cluster. This storage class will be used for the database storage and the search index. You can customize the service and persistence volume configuration to your needs by using a custom setup.  
	

## Custom Setups

To create a custom deployment create an overlay with custom settings based on the base deployment. First create a new folder with the file *kustomization.yaml*:

	namespace: my-application
	bases:
	- https://github.com/imixs/imixs-office-workflow/kubernetes/
	
	resources:
	- 031-network.yaml
	
	patchesStrategicMerge:
	- 010-deployment.yaml


The *kustomization.yaml* file simply points into the base directory hosted on github. It defines the new namespace 'my-application' where the resource objects will be created. Within this directory you can define new resources or resources to be merged in a existing resource. 

So will have the following directory structure:

	.
	├── my-deployment
	│   ├── 010-deployment.yaml
	│   ├── 031-network.yaml
	│   └── kustomization.yaml
	
You can now build the overlay with:

	$ kubectl apply --kustomize  ./my-deployment


In this example the file *010-deployment.yaml* adds a new additional environment variable with the name "ARCHIVE_SERVICE_ENDPOINT" to the imixs-office-workflow deployment


	apiVersion: apps/v1
	kind: Deployment
	metadata:
	  name: imixs-office-workflow
	  labels: 
	    app: imixs-office-workflow
	spec:
	  template:
	    spec:
	      containers:
	        - name: imixs-office-workflow
	          env:
	            - name: ARCHIVE_SERVICE_ENDPOINT
	              value: "http://imixs-archive:8080/api"
          
This shows how you can overwrite or extend existing deployment settings.          

The file *031-network.yaml* defines a new ingress configuration to publish the service endpoint of *Imixs-Office-Workflow* to a public or private Internet address:


	---
	###################################################
	# Ingress
	###################################################
	kind: Ingress
	apiVersion: networking.k8s.io/v1beta1
	metadata:
	  name: documents-imixs-tls
	spec:
	  rules:
	  - host: documents.foo.com
	    http:
	      paths:
	      - path: /
	        backend:
	          serviceName: imixs-office-workflow
	          servicePort: 8080

This resource will be added to the base deployment.

          
You can find further details about Kustomize [here](https://github.com/imixs/imixs-cloud/blob/master/doc/KUSTOMIZE.md). 
          