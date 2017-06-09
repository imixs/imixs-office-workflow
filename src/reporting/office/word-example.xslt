<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="/">
		<xsl:processing-instruction name="mso-application">
			<xsl:text>progid="Word.Document"</xsl:text>
		</xsl:processing-instruction>
		<w:wordDocument xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
			xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas"
			xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
			xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
			xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:v="urn:schemas-microsoft-com:vml"
			xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:w="http://schemas.microsoft.com/office/word/2003/wordml"
			xmlns:wx="http://schemas.microsoft.com/office/word/2003/auxHint"
			xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
			xmlns:wsp="http://schemas.microsoft.com/office/word/2003/wordml/sp2"
			xmlns:sl="http://schemas.microsoft.com/schemaLibrary/2003/core"
			w:macrosPresent="no" w:embeddedObjPresent="no" w:ocxPresent="no"
			xml:space="preserve"><w:ignoreSubtree
			w:val="http://schemas.microsoft.com/office/word/2003/wordml/sp2" /><o:DocumentProperties><o:Author>Ralph</o:Author><o:LastAuthor>Ralph</o:LastAuthor><o:Revision>2</o:Revision><o:TotalTime>0</o:TotalTime><o:Created>2017-06-08T15:55:00Z</o:Created><o:LastSaved>2017-06-08T15:55:00Z</o:LastSaved><o:Pages>1</o:Pages><o:Words>94</o:Words><o:Characters>541</o:Characters><o:Lines>4</o:Lines><o:Paragraphs>1</o:Paragraphs><o:CharactersWithSpaces>634</o:CharactersWithSpaces><o:Version>14</o:Version></o:DocumentProperties><w:fonts><w:defaultFonts
			w:ascii="Calibri" w:fareast="Calibri" w:h-ansi="Calibri" w:cs="Times New Roman" /><w:font
			w:name="Times New Roman"><w:panose-1 w:val="02020603050405020304" /><w:charset
			w:val="00" /><w:family w:val="Roman" /><w:pitch
			w:val="variable" /><w:sig w:usb-0="E0002AFF" w:usb-1="C0007841"
			w:usb-2="00000009" w:usb-3="00000000" w:csb-0="000001FF" w:csb-1="00000000" /></w:font><w:font
			w:name="Cambria Math"><w:panose-1 w:val="02040503050406030204" /><w:charset
			w:val="01" /><w:family w:val="Roman" /><w:notTrueType /><w:pitch
			w:val="variable" /></w:font><w:font w:name="Calibri"><w:panose-1
			w:val="020F0502020204030204" /><w:charset w:val="00" /><w:family
			w:val="Swiss" /><w:pitch w:val="variable" /><w:sig
			w:usb-0="E00002FF" w:usb-1="4000ACFF" w:usb-2="00000001" w:usb-3="00000000"
			w:csb-0="0000019F" w:csb-1="00000000" /></w:font></w:fonts><w:styles><w:versionOfBuiltInStylenames
			w:val="7" /><w:latentStyles w:defLockedState="off"
			w:latentStyleCount="267"><w:lsdException w:name="Normal" /><w:lsdException
			w:name="heading 1" /><w:lsdException w:name="heading 2" /><w:lsdException
			w:name="heading 3" /><w:lsdException w:name="heading 4" /><w:lsdException
			w:name="heading 5" /><w:lsdException w:name="heading 6" /><w:lsdException
			w:name="heading 7" /><w:lsdException w:name="heading 8" /><w:lsdException
			w:name="heading 9" /><w:lsdException w:name="toc 1" /><w:lsdException
			w:name="toc 2" /><w:lsdException w:name="toc 3" /><w:lsdException
			w:name="toc 4" /><w:lsdException w:name="toc 5" /><w:lsdException
			w:name="toc 6" /><w:lsdException w:name="toc 7" /><w:lsdException
			w:name="toc 8" /><w:lsdException w:name="toc 9" /><w:lsdException
			w:name="caption" /><w:lsdException w:name="Title" /><w:lsdException
			w:name="Default Paragraph Font" /><w:lsdException
			w:name="Subtitle" /><w:lsdException w:name="Strong" /><w:lsdException
			w:name="Emphasis" /><w:lsdException w:name="Table Grid" /><w:lsdException
			w:name="Placeholder Text" /><w:lsdException w:name="No Spacing" /><w:lsdException
			w:name="Light Shading" /><w:lsdException w:name="Light List" /><w:lsdException
			w:name="Light Grid" /><w:lsdException w:name="Medium Shading 1" /><w:lsdException
			w:name="Medium Shading 2" /><w:lsdException w:name="Medium List 1" /><w:lsdException
			w:name="Medium List 2" /><w:lsdException w:name="Medium Grid 1" /><w:lsdException
			w:name="Medium Grid 2" /><w:lsdException w:name="Medium Grid 3" /><w:lsdException
			w:name="Dark List" /><w:lsdException w:name="Colorful Shading" /><w:lsdException
			w:name="Colorful List" /><w:lsdException w:name="Colorful Grid" /><w:lsdException
			w:name="Light Shading Accent 1" /><w:lsdException
			w:name="Light List Accent 1" /><w:lsdException w:name="Light Grid Accent 1" /><w:lsdException
			w:name="Medium Shading 1 Accent 1" /><w:lsdException
			w:name="Medium Shading 2 Accent 1" /><w:lsdException
			w:name="Medium List 1 Accent 1" /><w:lsdException
			w:name="Revision" /><w:lsdException w:name="List Paragraph" /><w:lsdException
			w:name="Quote" /><w:lsdException w:name="Intense Quote" /><w:lsdException
			w:name="Medium List 2 Accent 1" /><w:lsdException
			w:name="Medium Grid 1 Accent 1" /><w:lsdException
			w:name="Medium Grid 2 Accent 1" /><w:lsdException
			w:name="Medium Grid 3 Accent 1" /><w:lsdException
			w:name="Dark List Accent 1" /><w:lsdException
			w:name="Colorful Shading Accent 1" /><w:lsdException
			w:name="Colorful List Accent 1" /><w:lsdException
			w:name="Colorful Grid Accent 1" /><w:lsdException
			w:name="Light Shading Accent 2" /><w:lsdException
			w:name="Light List Accent 2" /><w:lsdException w:name="Light Grid Accent 2" /><w:lsdException
			w:name="Medium Shading 1 Accent 2" /><w:lsdException
			w:name="Medium Shading 2 Accent 2" /><w:lsdException
			w:name="Medium List 1 Accent 2" /><w:lsdException
			w:name="Medium List 2 Accent 2" /><w:lsdException
			w:name="Medium Grid 1 Accent 2" /><w:lsdException
			w:name="Medium Grid 2 Accent 2" /><w:lsdException
			w:name="Medium Grid 3 Accent 2" /><w:lsdException
			w:name="Dark List Accent 2" /><w:lsdException
			w:name="Colorful Shading Accent 2" /><w:lsdException
			w:name="Colorful List Accent 2" /><w:lsdException
			w:name="Colorful Grid Accent 2" /><w:lsdException
			w:name="Light Shading Accent 3" /><w:lsdException
			w:name="Light List Accent 3" /><w:lsdException w:name="Light Grid Accent 3" /><w:lsdException
			w:name="Medium Shading 1 Accent 3" /><w:lsdException
			w:name="Medium Shading 2 Accent 3" /><w:lsdException
			w:name="Medium List 1 Accent 3" /><w:lsdException
			w:name="Medium List 2 Accent 3" /><w:lsdException
			w:name="Medium Grid 1 Accent 3" /><w:lsdException
			w:name="Medium Grid 2 Accent 3" /><w:lsdException
			w:name="Medium Grid 3 Accent 3" /><w:lsdException
			w:name="Dark List Accent 3" /><w:lsdException
			w:name="Colorful Shading Accent 3" /><w:lsdException
			w:name="Colorful List Accent 3" /><w:lsdException
			w:name="Colorful Grid Accent 3" /><w:lsdException
			w:name="Light Shading Accent 4" /><w:lsdException
			w:name="Light List Accent 4" /><w:lsdException w:name="Light Grid Accent 4" /><w:lsdException
			w:name="Medium Shading 1 Accent 4" /><w:lsdException
			w:name="Medium Shading 2 Accent 4" /><w:lsdException
			w:name="Medium List 1 Accent 4" /><w:lsdException
			w:name="Medium List 2 Accent 4" /><w:lsdException
			w:name="Medium Grid 1 Accent 4" /><w:lsdException
			w:name="Medium Grid 2 Accent 4" /><w:lsdException
			w:name="Medium Grid 3 Accent 4" /><w:lsdException
			w:name="Dark List Accent 4" /><w:lsdException
			w:name="Colorful Shading Accent 4" /><w:lsdException
			w:name="Colorful List Accent 4" /><w:lsdException
			w:name="Colorful Grid Accent 4" /><w:lsdException
			w:name="Light Shading Accent 5" /><w:lsdException
			w:name="Light List Accent 5" /><w:lsdException w:name="Light Grid Accent 5" /><w:lsdException
			w:name="Medium Shading 1 Accent 5" /><w:lsdException
			w:name="Medium Shading 2 Accent 5" /><w:lsdException
			w:name="Medium List 1 Accent 5" /><w:lsdException
			w:name="Medium List 2 Accent 5" /><w:lsdException
			w:name="Medium Grid 1 Accent 5" /><w:lsdException
			w:name="Medium Grid 2 Accent 5" /><w:lsdException
			w:name="Medium Grid 3 Accent 5" /><w:lsdException
			w:name="Dark List Accent 5" /><w:lsdException
			w:name="Colorful Shading Accent 5" /><w:lsdException
			w:name="Colorful List Accent 5" /><w:lsdException
			w:name="Colorful Grid Accent 5" /><w:lsdException
			w:name="Light Shading Accent 6" /><w:lsdException
			w:name="Light List Accent 6" /><w:lsdException w:name="Light Grid Accent 6" /><w:lsdException
			w:name="Medium Shading 1 Accent 6" /><w:lsdException
			w:name="Medium Shading 2 Accent 6" /><w:lsdException
			w:name="Medium List 1 Accent 6" /><w:lsdException
			w:name="Medium List 2 Accent 6" /><w:lsdException
			w:name="Medium Grid 1 Accent 6" /><w:lsdException
			w:name="Medium Grid 2 Accent 6" /><w:lsdException
			w:name="Medium Grid 3 Accent 6" /><w:lsdException
			w:name="Dark List Accent 6" /><w:lsdException
			w:name="Colorful Shading Accent 6" /><w:lsdException
			w:name="Colorful List Accent 6" /><w:lsdException
			w:name="Colorful Grid Accent 6" /><w:lsdException
			w:name="Subtle Emphasis" /><w:lsdException w:name="Intense Emphasis" /><w:lsdException
			w:name="Subtle Reference" /><w:lsdException w:name="Intense Reference" /><w:lsdException
			w:name="Book Title" /><w:lsdException w:name="Bibliography" /><w:lsdException
			w:name="TOC Heading" /></w:latentStyles><w:style w:type="paragraph"
			w:default="on" w:styleId="Standard"><w:name w:val="Normal" /><wx:uiName
			wx:val="Standard" /><w:pPr><w:spacing w:after="200" w:line="276"
			w:line-rule="auto" /></w:pPr><w:rPr><wx:font wx:val="Calibri" /><w:sz
			w:val="22" /><w:sz-cs w:val="22" /><w:lang w:val="EN-GB"
			w:fareast="EN-US" w:bidi="AR-SA" /></w:rPr></w:style><w:style
			w:type="character" w:default="on" w:styleId="Absatz-Standardschriftart"><w:name
			w:val="Default Paragraph Font" /><wx:uiName
			wx:val="Absatz-Standardschriftart" /></w:style><w:style
			w:type="table" w:default="on" w:styleId="NormaleTabelle"><w:name w:val="Normal Table" /><wx:uiName
			wx:val="Normale Tabelle" /><w:rPr><wx:font wx:val="Calibri" /><w:lang
			w:val="EN-US" w:fareast="EN-US" w:bidi="AR-SA" /></w:rPr><w:tblPr><w:tblInd
			w:w="0" w:type="dxa" /><w:tblCellMar><w:top w:w="0" w:type="dxa" /><w:left
			w:w="108" w:type="dxa" /><w:bottom w:w="0" w:type="dxa" /><w:right
			w:w="108" w:type="dxa" /></w:tblCellMar></w:tblPr></w:style><w:style
			w:type="list" w:default="on" w:styleId="KeineListe"><w:name w:val="No List" /><wx:uiName
			wx:val="Keine Liste" /></w:style><w:style w:type="table"
			w:styleId="Tabellenraster"><w:name w:val="Table Grid" /><wx:uiName
			wx:val="Tabellenraster" /><w:basedOn w:val="NormaleTabelle" /><w:rsid
			w:val="00B0579B" /><w:rPr><wx:font wx:val="Calibri" /></w:rPr><w:tblPr><w:tblBorders><w:top
			w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto" /><w:left
			w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto" /><w:bottom
			w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto" /><w:right
			w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto" /><w:insideH
			w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto" /><w:insideV
			w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto" /></w:tblBorders></w:tblPr></w:style></w:styles><w:shapeDefaults><o:shapedefaults
			v:ext="edit" spidmax="1026" /><o:shapelayout v:ext="edit"><o:idmap
			v:ext="edit" data="1" /></o:shapelayout></w:shapeDefaults><w:docPr><w:view
			w:val="print" /><w:zoom w:percent="140" /><w:doNotEmbedSystemFonts /><w:defaultTabStop
			w:val="720" /><w:punctuationKerning /><w:characterSpacingControl
			w:val="DontCompress" /><w:optimizeForBrowser /><w:allowPNG /><w:validateAgainstSchema /><w:saveInvalidXML
			w:val="off" /><w:ignoreMixedContent w:val="off" /><w:alwaysShowPlaceholderText
			w:val="off" /><w:compat><w:breakWrappedTables /><w:snapToGridInCell /><w:wrapTextWithPunct /><w:useAsianBreakRules /><w:dontGrowAutofit /></w:compat><wsp:rsids><wsp:rsidRoot
			wsp:val="00B0579B" /><wsp:rsid wsp:val="005B4668" /><wsp:rsid
			wsp:val="006E3D89" /><wsp:rsid wsp:val="00A71BE5" /><wsp:rsid
			wsp:val="00B0579B" /><wsp:rsid wsp:val="00E97A69" /></wsp:rsids></w:docPr><w:body><wx:sect><wx:pBdrGroup><wx:borders><wx:bottom
			wx:val="solid" wx:bdrwidth="15" wx:space="1" wx:color="auto" /></wx:borders><w:p
			wsp:rsidR="00B0579B" wsp:rsidRPr="00B0579B" wsp:rsidRDefault="00B0579B"
			wsp:rsidP="00B0579B"><w:pPr><w:pBdr><w:bottom w:val="single" w:sz="6"
			wx:bdrwidth="15" w:space="1" w:color="auto" /></w:pBdr><w:jc
			w:val="right" /><w:rPr><w:b /><w:sz w:val="52" /><w:sz-cs
			w:val="52" /><w:lang w:val="DE" /></w:rPr></w:pPr><w:r
			wsp:rsidRPr="00B0579B"><w:rPr><w:b /><w:sz w:val="52" /><w:sz-cs
			w:val="52" /><w:lang w:val="DE" /></w:rPr><w:t>Imixs-Office-Workflow</w:t></w:r></w:p></wx:pBdrGroup><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B" wsp:rsidP="00B0579B"><w:pPr><w:jc
			w:val="right" /><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr><w:r><w:rPr><w:lang
			w:val="DE" /></w:rPr><w:t>Imixs Software Solutions GmbH</w:t></w:r></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:u w:val="single" /><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><w:p wsp:rsidR="00B0579B"
			wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr><w:r><w:rPr><w:lang w:val="DE" /></w:rPr><w:t>Firma: <w:br/><xsl:value-of select="document/item[@name='_subject']/value" /></w:t></w:r></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><w:p wsp:rsidR="00B0579B"
			wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><wx:pBdrGroup><wx:borders><wx:bottom
			wx:val="solid" wx:bdrwidth="15" wx:space="1" wx:color="auto" /></wx:borders><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:pBdr><w:bottom
			w:val="single" w:sz="6" wx:bdrwidth="15" w:space="1" w:color="auto" /></w:pBdr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr><w:r><w:rPr><w:lang w:val="DE" /></w:rPr><w:t>Angebots-Nr.: <xsl:value-of select="document/item[@name='numsequencenumber']/value" /></w:t></w:r></w:p></wx:pBdrGroup><w:p
			wsp:rsidR="00B0579B" wsp:rsidRPr="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><w:p wsp:rsidR="00B0579B"
			wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr><w:r
			wsp:rsidRPr="00B0579B"><w:rPr><w:lang w:val="DE" /></w:rPr><w:t>Sehr </w:t></w:r><w:r><w:rPr><w:lang
			w:val="DE" /></w:rPr><w:t>geehrte Damen und Herren,</w:t></w:r></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><w:p wsp:rsidR="00B0579B"
			wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr><w:r><w:rPr><w:lang
			w:val="DE" /></w:rPr><w:t>nachstehend finden Sie unser Angebot. </w:t></w:r><w:r
			wsp:rsidRPr="00B0579B"><w:rPr><w:lang w:val="DE" /></w:rPr><w:t>Mit einer Service Subscription nutzen Sie die Open Source Plattform Imixs-Office-Workflow und erhalten gleichzeitig einen professionellen Support. Neben der Beratung durch unsere Workflow-Experten übernehmen wir auch den Betrieb der Web Lösung. Dies beinhaltet natürlich auch regelmäßige Updates. So erhalten Sie die Sicherheit, Imixs-Office-Workflow in Ihrem Unternehmen optimal einzusetzen und nutzen gleichzeitig die Vorteile von Open Source.</w:t></w:r></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><w:p wsp:rsidR="00B0579B"
			wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><w:p wsp:rsidR="00B0579B"
			wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr><w:r><w:rPr><w:lang
			w:val="DE" /></w:rPr><w:t>Mit freundlichen Grüßen</w:t></w:r><w:r><w:rPr><w:lang
			w:val="DE" /></w:rPr><w:br /></w:r></w:p><w:p wsp:rsidR="00B0579B"
			wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang w:val="DE" /></w:rPr></w:pPr><w:r><w:rPr><w:lang
			w:val="DE" /></w:rPr><w:t>Imixs GmbH</w:t></w:r></w:p><w:p
			wsp:rsidR="00B0579B" wsp:rsidRPr="00B0579B" wsp:rsidRDefault="00B0579B"><w:pPr><w:rPr><w:lang
			w:val="DE" /></w:rPr></w:pPr></w:p><w:sectPr wsp:rsidR="00B0579B"
			wsp:rsidRPr="00B0579B"><w:pgSz w:w="12240" w:h="15840" /><w:pgMar
			w:top="1417" w:right="1417" w:bottom="1134" w:left="1417" w:header="708"
			w:footer="708" w:gutter="0" /><w:cols w:space="708" /><w:docGrid
			w:line-pitch="360" /></w:sectPr></wx:sect></w:body></w:wordDocument>
	</xsl:template>
</xsl:stylesheet>