# K3S

During development and also for production you can run Imixs-Office-Workflow in the lightweight Kubernetes Environment [K3S](https://docs.k3s.io/).

In it's most easiest setup you can install and start K3S following the [Quick-Start Guide](https://docs.k3s.io/quick-start)

	$ curl -sfL https://get.k3s.io | sh -
	$ sudo systemctl status k3s.service


A kubeconfig file will be written to `/etc/rancher/k3s/k3s.yaml` and the kubectl installed by K3s will automatically use it. Verify your cluster and node info.

	$ sudo kubectl cluster-info
	$ sudo kubectl get node

If you don't have kubectl installed you can use the k3s command instead

	$ sudo k3s kubectl cluster-info


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
	
	
Find more information about how to run Imixs-Office-Workflow with K3s on [doc.office-workflow.com](https://doc.office-workflow.com/)