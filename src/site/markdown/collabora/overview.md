#  Collabora

[Collabora Online](https://www.collaboraoffice.com/)  is a powerful online office suite to create and edit all types of office documents from your Web Browser.  


 - **Writer** - visualize and edit text documents like MS Word with an easy to use WYSIWYG editor. You can edit and style your pages with various options, add comments and track changes from anywhere Format.
 - **Calc** - enables you to work with spreadsheets and calculations. You can edit MS Excel Sheets with  advanced formulas, add charts, sparklines and hyperlinks.  With powerful multi-column sort and filter options, you can make your data work for you.
 - **Impress** - allows you to create and view presentations and PowerPoint slides. You can design you own slides, add text, images, tables and SmartArt, insert speaker notes, custom timings and transitions to engage your audience. 
 - **Draw** - enables you to draw shapes and diagrams, add text, charts and tables, insert fields, links and comments. All documents can be exported in PDF documents and shared with your customers. 
 
Collabora Online can be integrated with **Imixs-Office-Workflow** to access office documents out from your business process. Creating and editing documents becomes automatically part of your workflow. All changes are tracked by Imixs-Office-Workflow and documents can be routed to colleagues and exprted to business partners. 

<img class="screenshot" src="../collabora/screen-001.png" />

The following section provides additional information about how to integrate [Collabora](https://www.collaboraoffice.com/) with Imixs-Office-Workflow. General installation guide can be found [here](https://sdk.collaboraonline.com/docs/installation/index.html).

## Integration

Collabora and Imixs-Office-Workflow can both run as Containers in a container environment like Docker-Compose or Kubernetes. The communication is configured through the internal network by defining Endpoints. You will find detailed technical background in the section [WOPI](./wopi.md)


The following example shows a Docker-Compose file including Imixs-Documents and Collabora:

```yaml
version: "3.6"
services:
  db:
    image: postgres:9.6.1
    environment:
      POSTGRES_PASSWORD: adminadmin
      POSTGRES_DB: office-ib-vassen
    volumes:
      - dbdata:/var/lib/postgresql/data

  app:
    image: imixs/office-ib-vassen
    environment:
      POSTGRES_USER: "postgres"
      POSTGRES_PASSWORD: "adminadmin"
      POSTGRES_CONNECTION: "jdbc:postgresql://db/office-ib-vassen"
      TZ: "CET"
      LANG: "en_US.UTF-8"
      # Collabora integration
      WOPI_PUBLIC_ENDPOINT: "http://localhost:9980"
      WOPI_DISCOVERY_ENDPOINT: "http://collabora:9980/hosting/discovery"
      WOPI_HOST_ENDPOINT: "http://app:8080/api/wopi/"
    ports:
      - "8080:8080"

  collabora:
    image: collabora/code:23.05.0.5.1
    ports:
      - "9980:9980"
    environment:
      - extra_params=--o:ssl.enable=false
      - aliasgroup1=http://app:8080:443

volumes:
  dbdata:
```


### Kubernetes

You can also run Collabora in Kubernetes. With an Ingress configuration you control the access and secure your environment. By defining alias groups it is also possible to access Collabora from multiple instances of Imixs-Office-Workflow. See the following configuration example defining two different groups of workflow instances:

```yaml
....
apiVersion: apps/v1
kind: Deployment
metadata:
  name: collabora
  labels: 
    app: collabora
spec:
  replicas: 1
  selector: 
    matchLabels:
      app: collabora
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: collabora
    spec:
      containers:
      - image: collabora/code:23.05.0.5.1
        name: collabora
        imagePullPolicy: IfNotPresent
        env:
        - name: TZ
          value: Europe/Berlin
        - name: extra_params
          value: --o:ssl.termination=true --o:ssl.enable=false
        - name: aliasgroup1
          value: https://office-group1.foo.com
        - name: aliasgroup2
          value: https://office-group2.foo.net,https://test.office-group2.foot.net
        ports: 
          - name: web
            containerPort: 9980
        resources:
          requests:
            memory: "512M"
          limits:
            memory: "1G"
      restartPolicy: Always
....
```



### Fonts

The post-install script from Collabora will look for additional fonts in the docker container, and install them for Collabora Code (in the `systemplate`). If you have a host directory with additional fonts - e.g. `/opt/local/my-fonts` - you mount a directory by mapping the volume like this:

```yaml
  collabora:
    image: collabora/code:23.05.0.5.1
    #image: imixs/collabora
    container_name: collabora
    expose:
      - 9980
    ports:
      - "9980:9980"
    environment:
      - username=admin
      - password=adminadmin
      - extra_params=--o:ssl.enable=false
      - aliasgroup1=http://app:8080:443
    volumes:
      - /opt/local/my-fonts:/usr/share/fonts/truetype/more/
      - /opt/local/my-fonts:/opt/cool/systemplate/usr/share/fonts/truetype/more/
```
