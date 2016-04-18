<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="text" indent="no" encoding="ISO-8859-1" 
		omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<xsl:key name="groups" match="/collection/entity"
		use="item[name='datdate']/value" />

	<xsl:template match="/">
		{
			"labels" : [
						<!--Select the first element of each group -->
						<xsl:for-each select="/collection/entity[generate-id() =generate-id(key('groups', item[name='datdate']/value)[1])]" >
							<xsl:sort select="item[name='datdate']/value" data-type="text" order="ascending"/>
							<xsl:text><![CDATA["]]></xsl:text><xsl:value-of select="item[name='datdate']/value" /><xsl:text><![CDATA["]]></xsl:text>
							<!-- comma separator only if not last one -->
							<xsl:if test="position() != last()" ><xsl:text><![CDATA[,]]></xsl:text></xsl:if>
						</xsl:for-each>
					 ],
			"datasets" : [
			{
				"label": "Erbrachte Stunden",
				"fillColor" : "rgba(220,220,220,0.2)",
				"strokeColor" : "rgba(220,220,220,1)",
				"pointColor" : "rgba(220,220,220,1)",
				"pointStrokeColor" : "#fff",
				"pointHighlightFill" : "#fff",
				"pointHighlightStroke" : "rgba(220,220,220,1)",
				"data" : [
			<xsl:apply-templates
					select="/collection/entity[generate-id() = generate-id(key('groups', item[name='datdate']/value)[1])]" >
					<!-- sort -->
					<xsl:sort select="item[name='datdate']/value" data-type="text" order="ascending"/>
			</xsl:apply-templates>
				]
			}
			]
		}
	</xsl:template>


	<!-- This template builds a JSON array with sum of '_amount' over all groups -->
	<xsl:template match="/collection/entity">
	
		
		<!-- build sum variable -->
		<xsl:variable name="summe"
			select="sum(key('groups', item[name='datdate']/value)//item[name='_duration']/value)"/>
			
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