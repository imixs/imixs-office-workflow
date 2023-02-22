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



Find more information about how to run Imixs-Office-Workflow with K3s on [doc.office-workflow.com](https://doc.office-workflow.com/)