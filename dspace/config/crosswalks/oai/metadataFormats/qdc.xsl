<?xml version="1.0" encoding="UTF-8" ?>
<!-- 


    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/
	Developed by DSpace @ Lyncode <dspace@lyncode.com>
	
	> http://www.openarchives.org/OAI/2.0/oai_dc.xsd

 -->
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:doc="http://www.lyncode.com/xoai"
	version="1.0">
	<xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />
	
	<xsl:template match="/">

				
		<!-- dc.creator -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']">
			<dc:creator xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dc:creator>
		</xsl:for-each>

		<!-- dc.contributor -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name!='author']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.author -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:author">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.advisor -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='advisor']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:advisor">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.editor -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='editor']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:editor">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.illustrator -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='illustrator']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:illustrator">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.committeechair -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='committeechair']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:committeechair">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.committeeMember -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='committeeMember']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:committeeMember">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.photographer -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='photographer']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:photographer">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.sponsor -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='sponsor']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:sponsor">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.contributor.other -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='other']/doc:element/doc:field[@name='value']">
			<dc:contributor xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:other">
				<xsl:value-of select="." />
			</dc:contributor>
		</xsl:for-each>
		
		<!-- dc.date -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>
		
		<!-- dc.date.accessioned -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='accessioned']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:dateAccepted">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>
		
		<!-- dc.date.available -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:available">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>
		
		<!-- local.embargo.lift -->
		<xsl:for-each select="doc:metadata/doc:element[@name='local']/doc:element[@name='embargo']/doc:element[@name='lift']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:available">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>
		
		<!-- dc.date.copyright -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='copyright']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:dateCopyrighted">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>
		
		<!-- dc.date.created -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='created']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:created">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>
		
		<!-- dc.date.issued -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/"  type="dcterms:issued">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>
		
		<!-- dc.date.submitted -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='submitted']/doc:element/doc:field[@name='value']">
			<dc:date xmlns:dc="http://purl.org/dc/elements/1.1/"  type="dcterms:dateSubmitted">
				<xsl:value-of select="." />
			</dc:date>
		</xsl:for-each>

		<!-- dc.identifier -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element/doc:field[@name='value']">
			<dc:identifier xmlns:dc="http://purl.org/dc/elements/1.1/"  >
				<xsl:value-of select="." />
			</dc:identifier>
		</xsl:for-each>

		<!-- dc.identifier.citation -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='citation']/doc:element/doc:field[@name='value']">
			<dc:identifier xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:bibliographicCitation" >
				<xsl:value-of select="." />
			</dc:identifier>
		</xsl:for-each>
		
		<!-- dc.identifier.govdoc -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='govdoc']/doc:element/doc:field[@name='value']">
			<dc:identifier xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:govdoc" >
				<xsl:value-of select="." />
			</dc:identifier>
		</xsl:for-each>

		<!-- dc.identifier.isbn -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='isbn']/doc:element/doc:field[@name='value']">
			<dc:identifier xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:isbn" >
				<xsl:value-of select="." />
			</dc:identifier>
		</xsl:for-each>

		<!-- dc.identifier.issn -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='issn']/doc:element/doc:field[@name='value']">
			<dc:identifier xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:issn" >
				<xsl:value-of select="." />
			</dc:identifier>
		</xsl:for-each>

		<!-- dc.identifier.sici -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='sici']/doc:element/doc:field[@name='value']">
			<dc:identifier xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:sici" >
				<xsl:value-of select="." />
			</dc:identifier>
		</xsl:for-each>
				
		<!-- dc.identifier.uri -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
            <dc:identifier xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:URI">
                <xsl:value-of select="." />
            </dc:identifier>
        </xsl:for-each>

		<!-- dc.description -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" >
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>

		<!-- dc.description.abstract -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" type="abstract">
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>
		
		<!-- dc.description.college -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='college']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" type="college">
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>
		
		<!-- dc.description.department -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='department']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" type="department">
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>
		
		<!-- dc.description.school -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='school']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" type="school">
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>
		
		<!-- dc.description.sponsorship -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='sponsorship']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" type="sponsorship">
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>
		
		<!-- dc.description.tableofcontents -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='tableofcontents']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" type="tableOfContents">
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>
		
		<!-- dc.description.uri -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
			<dc:description xmlns:dc="http://purl.org/dc/elements/1.1/" type="URI">
				<xsl:value-of select="." />
			</dc:description>
		</xsl:for-each>
		
		<!-- dc.format -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='format']/doc:element/doc:field[@name='value']">
			<dc:format xmlns:dc="http://purl.org/dc/elements/1.1/" >
				<xsl:value-of select="." />
			</dc:format>
		</xsl:for-each>
		
		<!-- dc.format (via bitstreams) -->
		<xsl:for-each select="doc:metadata/doc:element[@name='bitstreams']/doc:element[@name='bitstream']/doc:field[@name='format']">
			<dc:format xmlns:dc="http://purl.org/dc/elements/1.1/" >
				<xsl:value-of select="." />
			</dc:format>
		</xsl:for-each>
		
		<!-- dc.format.extent -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='format']/doc:element[@name='extent']/doc:element/doc:field[@name='value']">
			<dc:format xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:extent">
				<xsl:value-of select="." />
			</dc:format>
		</xsl:for-each>
		
		<!-- dc.format.medium -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='format']/doc:element[@name='medium']/doc:element/doc:field[@name='value']">
			<dc:format xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:medium">
				<xsl:value-of select="." />
			</dc:format>
		</xsl:for-each>
		
		<!-- dc.format.mimetype -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='format']/doc:element[@name='mimetype']/doc:element/doc:field[@name='value']">
			<dc:format xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:mimetype">
				<xsl:value-of select="." />
			</dc:format>
		</xsl:for-each>

		<!-- dc.title -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
			<dc:title xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dc:title>
		</xsl:for-each>
		
		<!-- dc.title.alternative -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element[@name='alternative']/doc:element/doc:field[@name='value']">
			<dc:title xmlns:dc="http://purl.org/dc/elements/1.1/" type="alternative">
				<xsl:value-of select="." />
			</dc:title>
		</xsl:for-each>
		
		<!-- dc.subject -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
			<dc:subject xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dc:subject>
		</xsl:for-each>

		<!-- dc.subject.lcsh -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='lcsh']/doc:element/doc:field[@name='value']">
			<dc:subject xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:LCSH">
				<xsl:value-of select="." />
			</dc:subject>
		</xsl:for-each>
		
		<!-- dc.subject.mesh -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='mesh']/doc:element/doc:field[@name='value']">
			<dc:subject xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:MESH">
				<xsl:value-of select="." />
			</dc:subject>
		</xsl:for-each>
		
		<!-- dc.subject.other -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='other']/doc:element/doc:field[@name='value']">
			<dc:subject xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:other">
				<xsl:value-of select="." />
			</dc:subject>
		</xsl:for-each>		

		<!-- dc.subject.classification -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='classification']/doc:element/doc:field[@name='value']">
			<dc:subject xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:classification">
				<xsl:value-of select="." />
			</dc:subject>
		</xsl:for-each>

		<!-- dc.subject.ddc -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='ddc']/doc:element/doc:field[@name='value']">
			<dc:subject xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:DDC">
				<xsl:value-of select="." />
			</dc:subject>
		</xsl:for-each>
		
		<!-- dc.subject.lcc -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='lcc']/doc:element/doc:field[@name='value']">
			<dc:subject xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:LCC">
				<xsl:value-of select="." />
			</dc:subject>
		</xsl:for-each>
		
		<!-- dc.terms -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
			<dcterms:abstract xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
				</dcterms:abstract>
		</xsl:for-each>
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']">
			<dcterms:dateAccepted xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dcterms:dateAccepted>
		</xsl:for-each>
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']">
			<dcterms:available xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dcterms:available>
		</xsl:for-each>
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']">
			<dcterms:created xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dcterms:created>
		</xsl:for-each>
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
			<dcterms:issued xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dcterms.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2006/01/06/dc.xsd">
				<xsl:value-of select="." />
			</dcterms:issued>
		</xsl:for-each>

		<!-- dc.type -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']">
			<dc:type xmlns:dc="http://purl.org/dc/elements/1.1/" >
				<xsl:value-of select="." />
			</dc:type>
		</xsl:for-each>
		
		<!-- dc.type.genre -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='genre']/doc:element/doc:field[@name='value']">
			<dc:type xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:genre" >
				<xsl:value-of select="." />
			</dc:type>
		</xsl:for-each>
		
		<!-- dc.type.material -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='material']/doc:element/doc:field[@name='value']">
			<dc:type xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:material" >
				<xsl:value-of select="." />
			</dc:type>
		</xsl:for-each>
		
		
		<!-- dc.language -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:field[@name='value']">
			<dc:language xmlns:dc="http://purl.org/dc/elements/1.1/">
				<xsl:value-of select="." />
			</dc:language>
		</xsl:for-each>
		
		<!-- dc.language.iso -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element[@name='iso']/doc:element/doc:field[@name='value']">
			<dc:language xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:ISO639-2">
				<xsl:value-of select="." />
			</dc:language>
		</xsl:for-each>

		<!-- dc.publisher -->
        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:field[@name='value']">
            <dc:publisher xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:main">
                <xsl:value-of select="." />
            </dc:publisher>
        </xsl:for-each>

		<!-- dc.publisher.uri -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
			<dc:publisher xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:URI">
				<xsl:value-of select="." />
			</dc:publisher>
		</xsl:for-each>

		<!-- dc.relation -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.isformatof -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isformatof']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:isFormatOf" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.ispartof -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartof']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:isPartOf" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.haspart -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='haspart']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:hasPart" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.isversionof -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isversionof']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:isVersionOf" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.hasversion -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='hasversion']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:hasVersion" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.ispartofseries -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:isPartOfSeries" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.isreferencedby -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isreferencedby']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:isReferencedBy" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.requires -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='requires']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:requires" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>

		<!-- dc.relation.replaces -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='replaces']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:replaces" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>
		
		<!-- dc.relation.isreplacedby -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isreplacedby']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:isReplacedBy" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>		
		
		<!-- dc.relation.uri -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
			<dc:relation xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:URI" >
				<xsl:value-of select="." />
			</dc:relation>
		</xsl:for-each>	
		
		<!-- dc.rights -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:field[@name='value']">
			<dc:rights xmlns:dc="http://purl.org/dc/elements/1.1/" >
				<xsl:value-of select="." />
			</dc:rights>
		</xsl:for-each>
		
		<!-- dc.rights.uri -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
			<dc:rights xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:URI">
				<xsl:value-of select="." />
			</dc:rights>
		</xsl:for-each>
		
		<!-- dc.source -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='source']/doc:element/doc:field[@name='value']">
			<dc:source xmlns:dc="http://purl.org/dc/elements/1.1/" >
				<xsl:value-of select="." />
			</dc:source>
		</xsl:for-each>
		
		<!-- dc.source.uri -->
		<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='source']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
			<dc:source xmlns:dc="http://purl.org/dc/elements/1.1/" type="dcterms:URI">
				<xsl:value-of select="." />
			</dc:source>
		</xsl:for-each>
		
		<!-- TAMU specific: thesis.degree.discipline -->
		<xsl:for-each select="doc:metadata/doc:element[@name='thesis']/doc:element[@name='degree']/doc:element[@name='discipline']/doc:element/doc:field[@name='value']">
			<thesis:degree xmlns:thesis="http://digital.library.tamu.edu/schemas/thesis" type="discipline">
				<xsl:value-of select="." />
			</thesis:degree>
		</xsl:for-each>
		
		<!-- TAMU specific: thesis.degree.department -->
		<xsl:for-each select="doc:metadata/doc:element[@name='thesis']/doc:element[@name='degree']/doc:element[@name='department']/doc:element/doc:field[@name='value']">
			<thesis:degree xmlns:thesis="http://digital.library.tamu.edu/schemas/thesis" type="department">
				<xsl:value-of select="." />
			</thesis:degree>
		</xsl:for-each>
		
		<!-- TAMU specific: thesis.degree.level -->
		<xsl:for-each select="doc:metadata/doc:element[@name='thesis']/doc:element[@name='degree']/doc:element[@name='level']/doc:element/doc:field[@name='value']">
			<thesis:degree xmlns:thesis="http://digital.library.tamu.edu/schemas/thesis" type="level">
				<xsl:value-of select="." />
			</thesis:degree>
		</xsl:for-each>
		
		<!-- TAMU specific: thesis.degree.name -->
		<xsl:for-each select="doc:metadata/doc:element[@name='thesis']/doc:element[@name='degree']/doc:element[@name='name']/doc:element/doc:field[@name='value']">
			<thesis:degree xmlns:thesis="http://digital.library.tamu.edu/schemas/thesis" type="name">
				<xsl:value-of select="." />
			</thesis:degree>
		</xsl:for-each>

	</xsl:template>
</xsl:stylesheet>