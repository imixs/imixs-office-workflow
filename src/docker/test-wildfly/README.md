#imixs/wildfly


The imixs/wildfly image extends the wildfly default image and adds OR-Mapper eclipselink and the JDBC drivers for MySQL and PostgreSQL.




##How to build form dockerfile

The docker image 'imixs/wildfly' extends the wildfly:10.0.0.Final image and adds the EclipseLink library as also the postgres JDBC driver.
See also jboss/wildfly

Build:

	docker build --tag=imixs/wildfly .


##How to run the imixs/wildfly container
Run wildfly with life console: 

	docker run -it imixs/wildfly

Run wildfly in background

	docker run -it -d --name office-wildfly imixs/wildfly 


Stop container:

	docker stop office-wildfly
 
###Run with volumes form imixs/config

