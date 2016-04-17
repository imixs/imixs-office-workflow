<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes" />


	<xsl:key name="groups" match="/collection/entity"
		use="item[name='txtworkflowstatus']/value" />



	<xsl:template match="/">
		<collection>
			<xsl:apply-templates
				select="/collection/entity[generate-id() = generate-id(key('groups', item[name='txtworkflowstatus']/value)[1])]" />
		</collection>
	</xsl:template>

	<xsl:template match="/collection/entity">
		<status>
			<xsl:value-of select="item[name='txtworkflowstatus']/value" />
		</status>

		<total>
			<xsl:value-of select="sum(key('groups', item[name='txtworkflowstatus']/value)//item[name='_amount']/value)" />
		</total>

	</xsl:template>




</xsl:stylesheet>