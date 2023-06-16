# LibreOffice Online – How to Integrate

[LibreOffice Online](https://www.libreoffice.org/download/libreoffice-online/) is powerful online office suite that supports all major document, spreadsheet and presentation file formats, which can be integrated into Imixs-Documents. Key features are collaborative editing and excellent office file format support.

<img class="screenshot" src="wopi-01.png" width="900" />

LibreOffice Online is available as a Docker image and is developed by [Collabora](https://www.collaboraoffice.com/code/). Collabora is the community mainly developing the LibreOffice Online code. You can get the collabora docker image from [here](https://hub.docker.com/r/collabora/code).

General information about the Docker Image from Collabora can be found in the [Official Integration Guide](https://sdk.collaboraonline.com/docs/installation/CODE_Docker_image.html).

## Setup with Docker-Compose

For test environments Imixs-Office-Workflow and Collabora can be setup with Docker-Compose. The following example shows a .yaml file including both services:

	version: "3.6"
	services:
	
	# PostgreSQL
	  db:
	    image: postgres:9.6.1
	    environment:
	      POSTGRES_PASSWORD: adminadmin
	      POSTGRES_DB: office
	    volumes: 
	      - dbdata:/var/lib/postgresql/data
	
	# Imixs-Documents
	  imixs-documents:
	    image: imixs/imixs-documents:latest
	    depends_on:
	      - db
	    environment:
	      JAVA_OPTS: "-Dnashorn.args=--no-deprecation-warning"
	      POSTGRES_USER: "postgres"
	      POSTGRES_PASSWORD: "adminadmin"
	      POSTGRES_CONNECTION: "jdbc:postgresql://db/office"      
	      TZ: "Europe/Berlin"
	      LANG: "en_US.UTF-8"
	      MAILGATEWAY: "localhost"
	      # Collabora integration
	      WOPI_PUBLIC_ENDPOINT: "http://localhost:9980"
		  WOPI_DISCOVERY_ENDPOINT: "http://collabora:9980/hosting/discovery"
	      WOPI_HOST_ENDPOINT: "http://imixs-documents:8080/api/wopi/"                
	    ports:
	      - "8080:8080"
	      - "9990:9990"
	    volumes:
	      - ./deployments:/opt/jboss/wildfly/standalone/deployments/
	
	# Collabora 
	  collabora:
		image: collabora/code:23.05.0.5.1
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


**Note:** In this example the SSL configuration is disabled. You can also setup a [Reverse Proxy configuration](https://sdk.collaboraonline.com/docs/installation/Proxy_settings.html) if you run in container environments like Kubernetes. You can find  more details about the configuration in the [official Integration Guide](https://sdk.collaboraonline.com/docs/installation/CODE_Docker_image.html).

## WOPI Parameters 

To integrate LibreOffice Online into Imixs-Documents the following Environment Variables need to be set to configure the WOPI integration:


| Variable              | Description  							| Example |
| --------------------- |---------------------------------------|---------|
| WOPI_PUBLIC_ENDPOINT  | Public access endpoint for the LibreOffice Online Suite. |http://libreoffice-app:9980
| WOPI_DISCOVERY_ENDPOINT  | Public WOPI discovery endpoint  |http://libreoffice-app:9980/hosting/discovery
| WOPI_HOST_ENDPOINT    | Internal Wopi Host endpoint. This einpoint must point to the internal hostname of Imixs-Documetns | http://imixs-documents:8080/api/wopi/ |
| WOPI_FILE_EXTENSIONS | Optional comma separated list of file extensions to be supported. |.odt,.doc,.docx,.ods,.xls,.xlsx,.ppt,.pptx|     
     
You can find the technical details about the Imixs WOPI Protocol Adapter [here](https://github.com/imixs/imixs-adapters/tree/master/imixs-adapters-wopi).


## Multiple WOPI Hosts

In case you have multiple instnaces of Imixs-Office-Workflow you can define also multiple WOPI Host Aliases in Collabora.For this you can either separate your hots addresses with ',' or with additional aliasgroups. See the following example defining 3 different WOPI Hosts:

	  collabora:
		image: collabora/code:23.05.0.5.1
		container_name: collabora
		expose:
		- 9980
		ports:
		- "9980:9980"
		environment:
		- username=admin
		- password=adminadmin
		- extra_params=--o:ssl.enable=false
		- aliasgroup1=http://app1.foo.com:8080:443,http://app2.foo.com:8080:443, 
		- aliasgroup2=http://app1.foo.net:8080:443
 


