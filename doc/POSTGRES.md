# How to dump & restore 

The following example illustrates how to dump/restore the postgresql db 'office' in development mode (docker-compose-dev.yaml)

## Create a dump

1. Connect to postgres container:

	$ $ docker exec -it docker_db_1 bash

2. Create dump

	# pg_dump -Upostgres -v -Fc office > office.sql
	# exit


3. Copy file to host


	$ docker cp docker_db_1:/office.sql docker/backup/office.sql

Now the file is in your code repository under docker/backup/

## Create a restore

1. Copy the dump from your host into your container


	$ docker cp docker/backup/office.sql docker_db_1:/office.sql

2. Connect to postgres container:

	$ docker exec -it docker_db_1 bash
	
3. Restore the database


	# pg_restore -v -c -Upostgres -doffice  /office.sql
	# exit



	