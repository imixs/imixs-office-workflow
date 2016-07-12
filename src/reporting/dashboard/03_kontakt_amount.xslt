<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="text" indent="no" encoding="ISO-8859-1" 
		omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<xsl:template match="/">
		{
			"datasets" :[ 
				{
					"label": "Kontaktanfragen",
					"charttype": "Pie",
					"data" : [
						{
							"value": <xsl:value-of select="count(/collection/entity[normalize-space(item[name = 'txtworkflowstatus']/value) = 'Neue Anfrage'])"></xsl:value-of>,							
							"color":"#F7464A",
							"highlight": "#FF5A5E",
							"label": "Neue Anfrage"
						},
						
						{
							"value": <xsl:value-of select="count(/collection/entity[normalize-space(item[name = 'txtworkflowstatus']/value) = 'On Hold'])"></xsl:value-of>,
							"color": "#46BFBD",
							"highlight": "#5AD3D1",
							"label": "On Hold"
						},
						
						{
							"value": <xsl:value-of select="count(/collection/entity[normalize-space(item[name = 'txtworkflowstatus']/value) = 'Wiedervorlage'])"></xsl:value-of>,
							"color": "#FDB45C",
							"highlight": "#FFC870",
							"label": "Wiedervorlage"
						}
					]
				}
			]
		}
	</xsl:template>


	
</xsl:stylesheet>