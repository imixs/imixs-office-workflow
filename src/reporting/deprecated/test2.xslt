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

		<xsl:for-each select="key('groups', item[name='txtworkflowstatus']/value)">
			<created>
				<xsl:value-of select="item[name='$created']/value" />
			</created>
			<amount>
				<xsl:value-of select="item[name='_amount']/value" />
			</amount>
		</xsl:for-each>
	</xsl:template>




</xsl:stylesheet>