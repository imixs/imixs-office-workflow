<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="text" indent="no" encoding="ISO-8859-1" 
		omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<xsl:key name="groups" match="/collection/document"
		use="item[@name='_invoicedate']/value" />

	<xsl:template match="/">
		{
		    "type" : "bar",
		    "title" : "Investitionen",
		    "options" : { 
		    				"responsive" : true,
		    				"legend" : {
		    				           "position": "top"
                             }
		    			},
			"labels" : [
						<!--Select the first element of each group -->
						<xsl:for-each select="/collection/document[generate-id() =generate-id(key('groups', item[@name='_invoicedate']/value)[1])]" >
							<xsl:sort select="item[@name='_invoicedate']/value" data-type="text" order="ascending"/>
							<xsl:text><![CDATA["]]></xsl:text><xsl:value-of select="item[@name='_invoicedate']/value" /><xsl:text><![CDATA["]]></xsl:text>
							<!-- comma separator only if not last one -->
							<xsl:if test="position() != last()" ><xsl:text><![CDATA[,]]></xsl:text></xsl:if>
						</xsl:for-each>
					 ],
			"datasets" : [
			{
				"label": "EUR",
				"fillColor" : "rgba(151,187,205,0.5)",
				"strokeColor" : "rgba(151,187,205,0.8)",
				"highlightFill" : "rgba(151,187,205,0.75)",
				"highlightStroke" : "rgba(151,187,205,1)",
				"type": "bar",
				"data" : [
			<xsl:apply-templates
					select="/collection/document[generate-id() = generate-id(key('groups', item[@name='_invoicedate']/value)[1])]" >
					<!-- sort -->
					<xsl:sort select="item[@name='_invoicedate']/value" data-type="text" order="ascending"/>
			</xsl:apply-templates>
				]
			}
			]
		}
	</xsl:template>


	<!-- This template builds a JSON array with sum of '_amount' over all groups -->
	<xsl:template match="/collection/document">
	
		
		<!-- build sum variable -->
		<xsl:variable name="summe"
			select="sum(key('groups', item[@name='_invoicedate']/value)//item[@name='_amount']/value)"/>
			
			<!-- output sum  -->
			<xsl:choose>
 			  <!-- test if NaN -->
			  <xsl:when test="not(number($summe))">
			      <!-- myNode is a not a number or empty(NaN) or zero -->  
			      <xsl:text><![CDATA[0]]></xsl:text>   
			  </xsl:when>
			  <xsl:otherwise>
			      <!-- myNode is a number (!= zero) -->     
			      <xsl:value-of select="$summe" />   
			  </xsl:otherwise>
			</xsl:choose>
			<!-- comma separator only if not last one -->
			<xsl:if test="position() != last()" ><xsl:text><![CDATA[,]]></xsl:text></xsl:if>
	</xsl:template>

</xsl:stylesheet>