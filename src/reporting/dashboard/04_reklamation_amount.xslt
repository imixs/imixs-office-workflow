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
					"label": "Reklamationen",
					"charttype": "Pie",
					"data" : [
						{
							"value": <xsl:value-of select="count(/collection/entity[normalize-space(item[name = 'txtworkflowstatus']/value) = 'Stellungnahme'])"></xsl:value-of>,							
							"color":"#f7464a",
							"highlight": "#f7464a",
							"label": "Stellungnahme"
						},
						
						{
							"value": <xsl:value-of select="count(/collection/entity[normalize-space(item[name = 'txtworkflowstatus']/value) = 'Umtausch'])"></xsl:value-of>,
							"color": "#aaaa39",
							"highlight": "#aaaa39",
							"label": "Umtausch"
						},
						
						{
							"value": <xsl:value-of select="count(/collection/entity[normalize-space(item[name = 'txtworkflowstatus']/value) = 'Reparatur'])"></xsl:value-of>,
							"color": "#46BFBD",
							"highlight": "#46BFBD",
							"label": "Reparatur"
						},

						{
							"value": <xsl:value-of select="count(/collection/entity[normalize-space(item[name = 'txtworkflowstatus']/value) = 'Versand'])"></xsl:value-of>,
							"color": "#4b9130",
							"highlight": "#4b9130",
							"label": "Versand"
						}
					]
				}
			]
		}
	</xsl:template>


	
</xsl:stylesheet>