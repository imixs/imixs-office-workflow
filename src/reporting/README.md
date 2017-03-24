#Reporting


Upload a report via curl:

    cd ~/git/imixs-office-workflow/src/reports
    curl --user admin:adminadmin --request POST -H "Content-Type: application/xml" -T01_rechnungseingang_count.imixs-report http://localhost:8080/office-rest/report
    