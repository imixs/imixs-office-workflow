<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:template match="/">
		<collection>
			<xsl:apply-templates select="/collection/entity" />			
		</collection>
	</xsl:template>


	<!-- Template -->
    	<xsl:template
		match="/collection/entity">
      			Status=<xsl:value-of select="item[name='txtworkflowstatus']/value" />
      			created=<xsl:value-of select="item[name='$created']/value" />
      			amount=<xsl:value-of select="item[name='_amount']/value" />
       </xsl:template>
 
</xsl:stylesheet>