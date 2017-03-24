<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="xml" indent="yes"/>
        <xsl:template match="/">
        <collection>
           <xsl:apply-templates select="/collection/document[normalize-space(item[@name='txtworkflowgroup']/value) = 'Rechnungseingang']" />       
      </collection>
  </xsl:template>
  <!-- Template Definition -->
  <xsl:template
    match="/collection/document[normalize-space(item[@name='txtworkflowgroup']/value) = 'Rechnungseingang']">
     Status=<xsl:value-of select="item[@name='txtworkflowstatus']/value" />
   </xsl:template>
</xsl:stylesheet>