# LibreOffice Online – How to Integrate

[LibreOffice Online](https://www.libreoffice.org/download/libreoffice-online/) is powerful online office suite that supports all major document, spreadsheet and presentation file formats, which can be integrated into Imixs-Documents. Key features are collaborative editing and excellent office file format support.

<img class="screenshot" src="wopi-01.png" width="900" />

LibreOffice Online is available as a Docker image and is developed by [Collabora](https://www.collaboraoffice.com/code/). Collabora is the community mainly developing the LibreOffice Online code. You can get the collabora docker image from [here](https://hub.docker.com/r/collabora/code).

General information about the Docker Image from Collabora and an integration guide can be found [here(https://sdk.collaboraonline.com/docs/installation/CODE_Docker_image.html).

Setup with Docker-Compose

For test environments Imixs-Documents and Collabora can be setup with Docker-Compose. The following example shows a .yaml file inclduing both services:

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
	      WOPI_PUBLIC_ENDPOINT: "http://localhost:9980/loleaflet/6a844e4/loleaflet.html?"
	      WOPI_HOST_ENDPOINT: "http://imixs-documents:8080/api/wopi/"
	    ports:
	      - "8080:8080"
	      - "9990:9990"
	    volumes:
	      - ./deployments:/opt/jboss/wildfly/standalone/deployments/
	
	# Collabora 
	  libreoffice-app:
	    image: collabora/code:6.4.8.4
	    container_name: libreoffice-app
	    expose:
	      - 9980
	    ports:
	      - "9980:9980"
	    environment:
	      - username=admin
	      - password=adminadmin
	    volumes:
	      - ./configuration/collabora/loolwsd.xml:/etc/loolwsd/loolwsd.xml

To integrate LibreOffice Online into Imixs-Documents the following Environment Variables need to be set:


| Variable              | Description  							| Example |
| --------------------- |---------------------------------------|---------|
| WOPI_PUBLIC_ENDPOINT  | Public access endpoint for the LibreOffice Online Suite. |http://libreoffice-app/loleaflet/6a844e4/loleaflet.html?
| WOPI_HOST_ENDPOINT    | Internal Wopi Host endpoint. This einpoint must point to the internal hostname of Imixs-Documetns | http://imixs-documents:8080/api/wopi/ |
| WOPI_FILE_EXTENSIONS | Optional comma separated list of file extensions to be supported. |.odt,.doc,.docx,.ods,.xls,.xlsx,.ppt,.pptx|     
     
You can find the technical details about the Imixs WOPI Protocoll Adapter [here](https://github.com/imixs/imixs-adapters/tree/master/imixs-adapters-wopi).
	
In this example the configuration file 'loolwsd.xml' is mapped locally. The loolwsd.xml gives you varios ways to configure Collabora. You can find a copy of this file [here](../docker/configuration/collabora/loolwsd.xml)
  
**Note:** The collabora office suite provides a lot more ways for a custom configuration and installation. Please find the the details in the [official online help](https://sdk.collaboraonline.com/). 

