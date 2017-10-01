#Reporting


Upload a report via curl:

    cd ~/git/imixs-office-workflow/src/reporting/dashboard
    curl --user admin:adminadmin --request POST -H "Content-Type: application/xml" -T01_rechnungseingang_amount.imixs-report http://localhost:8080/office-rest/report
    
    
    curl --user admin:adminadmin --request POST -H "Content-Type: application/xml" -T02_rechnungseingang_count.imixs-report http://localhost:8080/office-rest/report

    curl --user admin:adminadmin --request POST -H "Content-Type: application/xml" -T04_projektstunden_count.imixs-report http://localhost:8080/office-rest/report    