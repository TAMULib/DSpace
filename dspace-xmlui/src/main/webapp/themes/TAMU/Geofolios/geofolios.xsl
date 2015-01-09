<?xml version="1.0" encoding="UTF-8"?>

<!--
    geofolios.xsl
    
    Version: $Revision: 1.2 $
    
    Date: $Date: 2006/07/27 22:54:52 $    
-->

<!--
    TODO: Describe this XSL file    
    Author: Alexey Maslov
    
-->    

<xsl:stylesheet 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim" 
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:import href="../TAMU.xsl"/>
    <xsl:output indent="yes"/>
    
	
	
	
	
	<!-- Overriden to place the Yahoo Maps scripts past all the Jquery calls. For some reason, ymap
	stuff breaks JQuery in IE6 and IE7. -->
	<xsl:template match="dri:document">
        <!-- <html debug="true"> -->
		<html>
            <!-- First of all, build the HTML head element -->
            <xsl:call-template name="buildHead"/>
            <!-- Then proceed to the body -->
            <body>
				
                <div id="ds-main">
                    <!-- 
                        The header div, complete with title, subtitle, trail and other junk. The trail is 
                        built by applying a template over pageMeta's trail children. -->
                    <xsl:call-template name="buildHeader"/>
                    
                    <!-- 
                        Goes over the document tag's children elements: body, options, meta. The body template
                        generates the ds-body div that contains all the content. The options template generates
                        the ds-options div that contains the navigation and action options available to the 
                        user. The meta element is ignored since its contents are not processed directly, but 
                        instead referenced from the different points in the document. -->
					<div id="wrapper">
						<xsl:apply-templates select="dri:body" />
						<xsl:apply-templates select="dri:options" />
						<div class="spacer">&#160;</div>
					</div>

                    <!-- 
                        The footer div, dropping whatever extra information is needed on the page. It will
                        most likely be something similar in structure to the currently given example. -->
                    <xsl:call-template name="buildFooter"/>
                    
					<xsl:choose>
						<xsl:when test="/dri:document/dri:body//dri:referenceSet[@type='detailView' and @n='collection-view']">
							<script type="text/javascript">
								<![CDATA[
								
								// Set up the map and variables 
								var map = new YMap(document.getElementById('mapContainer'));
								map.setMapType(YAHOO_MAP_SAT);
								map.drawZoomAndCenter(new YGeoPoint(40.0 , -96.0), 14);
								map.addTypeControl();
								
								// Add a slider zoom control 
								map.addZoomLong(); 
											
								// Overlay data from XML file type GeoRSS (no worky quite right)
								// map.addOverlay(new YGeoRSS(']]><xsl:value-of select="concat($context-path,'/themes/TAMU/Geofolios/georss.xml')"/><![CDATA['));
								
								var iconImg = new YImage();
								iconImg.src = ']]><xsl:value-of select="concat($context-path,'/themes/TAMU/Geofolios/images/icon_lg.png')"/><![CDATA[';
								iconImg.size = new YSize(28,27);
								iconImg.offsetSmartWindow = new YCoordPoint(0,-22);
								
								var point;
								var marker;
								var _autoExpand;
						   
								var SW;
								var NW;
								var NE;
								var SE;
							
								
								]]>
							
								
								<xsl:apply-templates select="document('feeds/formattedList.xml')/folios/*"/>

							</script> 
						</xsl:when>
					    <xsl:when test="/dri:document/dri:body//dri:div[@id='aspect.artifactbrowser.SimpleSearch.div.search' or @id='aspect.artifactbrowser.AdvancedSearch.div.advanced-search']">
							<script type="text/javascript">
								<![CDATA[
								
								// Set up the map and variables 
								var map = new YMap(document.getElementById('mapContainer'));
								map.setMapType(YAHOO_MAP_SAT);
								map.drawZoomAndCenter(new YGeoPoint(40.0 , -96.0), 14);
								map.addTypeControl();
								
								// Add a slider zoom control 
								map.addZoomLong(); 
											
								// Overlay data from XML file type GeoRSS (no worky quite right)
								// map.addOverlay(new YGeoRSS(']]><xsl:value-of select="concat($context-path,'/themes/TAMU/Geofolios/georss.xml')"/><![CDATA['));
								
								var iconImg = new YImage();
								iconImg.src = ']]><xsl:value-of select="concat($context-path,'/themes/TAMU/Geofolios/images/icon_lg.png')"/><![CDATA[';
								iconImg.size = new YSize(27,27);
								iconImg.offsetSmartWindow = new YCoordPoint(0,-22);
								
								var point;
								var marker;
								var _autoExpand;
							
								]]>
								
								<!-- Add the folios iteratively -->
								<xsl:for-each select="/dri:document/dri:body//dri:div[@id='aspect.artifactbrowser.SimpleSearch.div.search' or @id='aspect.artifactbrowser.AdvancedSearch.div.advanced-search']
										/dri:div[@n='search-results']/dri:referenceSet/dri:reference">
								    
								    <xsl:variable name="externalMetadataURL">
								        <xsl:text>cocoon:/</xsl:text>
								        <xsl:value-of select="@url"/>
								        <!-- No options selected, render the full METS document -->
								    </xsl:variable>
							    
								    <xsl:variable name="data" select="document($externalMetadataURL)/mets:METS/mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"/>
									<xsl:variable name="number" select="substring-after($data/dim:field[@element='identifier' and @qualifier='govdoc'],':')"/>
									
									<xsl:variable name="mk" select="concat('marker_', $number)"/>
									<xsl:variable name="pop" select="concat('popup_', $number)"/>
								    <xsl:variable name="coords_y" select="(number(substring-before(substring-after($data/dim:field[@element='coverage' and @qualifier='box'],'northlimit='),';')) + 
								        number(substring-before(substring-after($data/dim:field[@element='coverage' and @qualifier='box'],'southlimit='),';'))) div 2.0"/>
								    <xsl:variable name="coords_x" select="(number(substring-before(substring-after($data/dim:field[@element='coverage' and @qualifier='box'],'westlimit='),';')) + 
								        number(substring-after($data/dim:field[@element='coverage' and @qualifier='box'],'eastlimit='))) div 2.0"/>
									
									<xsl:value-of select="concat('point = new YGeoPoint(', $coords_y, ', ', $coords_x, ');')"/>
									
									<xsl:value-of select="concat($mk,' = new YMarker(point, iconImg);')"/>
									<xsl:value-of select="$mk"/><xsl:text><![CDATA[.setSmartWindowColor('maroon');
										]]></xsl:text>
									<xsl:value-of select="$mk"/><xsl:text><![CDATA[.addLabel("<span class='folio-marker'>]]></xsl:text><xsl:value-of select="$number"/><xsl:text><![CDATA[</span>");
										]]></xsl:text>
									
									<xsl:text><![CDATA[_autoExpand = '<div class="folio-popup">]]></xsl:text>
									<xsl:value-of select="concat($number, ' ', $data/dim:field[@element='title'])"/>
									<xsl:text><![CDATA[</div>'; 
										]]></xsl:text>
									<xsl:value-of select="$mk"/><xsl:text><![CDATA[.addAutoExpand(_autoExpand);
										]]></xsl:text>
									
									<xsl:value-of select="concat('var ', $pop, ' = ')"/>
									<xsl:text><![CDATA['<div class="folio-desc"><span class="title">]]></xsl:text>
									<xsl:value-of select="concat($number, ' ', $data/dim:field[@element='title'])"/>
									<xsl:text><![CDATA[</span><br/>]]></xsl:text>
									<xsl:value-of select="concat('Folio ', $number, ', published ', $data/dim:field[@element='date' and @qualifier='issued'])"/>
									<xsl:text><![CDATA[<br/>]]></xsl:text>
									<xsl:value-of select="concat('Lat: ', substring-before($data/dim:field[@element='coverage' and @qualifier='point'],'N'), '; Lon: ', substring-before(substring-after($data/dim:field[@element='coverage' and @qualifier='point'],'N'),'W'))"/>
									<xsl:text><![CDATA[<br/><a href="]]></xsl:text>
									<xsl:value-of select="concat($context-path, '/handle/',substring-after($data/dim:field[@element='identifier' and @qualifier='uri'],'http://hdl.handle.net/'))"/>
									<xsl:text><![CDATA[">View complete folio</a></div>';]]></xsl:text>
									
									<xsl:value-of select="concat('YEvent.Capture(', $mk, ', EventsList.MouseClick, function() {', $mk, '.openSmartWindow(', $pop, ');});')"/>
									
									<xsl:value-of select="concat('map.addOverlay(', $mk, ');')"/>
									
								</xsl:for-each>
							</script>
						</xsl:when>
					</xsl:choose>
                </div>
            </body>
        </html>
    </xsl:template>
	

	
	
    
	<!-- The HTML head element contains references to CSS as well as embedded JavaScript code. Most of this 
		information is either user-provided bits of post-processing (as in the case of the JavaScript), or 
		references to stylesheets pulled directly from the pageMeta element. -->
	<xsl:template name="buildHead">
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
			<!-- Add stylesheets -->
			<xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='stylesheet']">
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="media">
						<xsl:value-of select="@qualifier"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
						<xsl:text>/themes/</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
						<xsl:text>/</xsl:text>
						<xsl:value-of select="."/>
					</xsl:attribute>
				</link>
			</xsl:for-each>
			
			<!-- Add syndication feeds -->
			<xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']">
				<link rel="alternate" type="application">
					<xsl:attribute name="type">
						<xsl:text>application/</xsl:text>
						<xsl:value-of select="@qualifier"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="."/>
					</xsl:attribute>
				</link>
			</xsl:for-each>
			
			
			
			
			<!-- the following javascript removes the default text of empty text areas when they are focused on or submitted -->
			<script type="text/javascript">
				function tFocus(element){if (element.value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){element.value='';}}
				function tSubmit(form){var defaultedElements = document.getElementsByTagName("textarea");
				for (var i=0; i != defaultedElements.length; i++){
				if (defaultedElements[i].value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){
				defaultedElements[i].value='';}}}
			</script>
			
			
			<!-- Add javascipt  -->
			<xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript']">
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
						<xsl:text>/themes/</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
						<xsl:text>/</xsl:text>
						<xsl:value-of select="."/>
					</xsl:attribute>
					&#160;   
				</script>
			</xsl:for-each>
			
			<!-- Javascript for geofolio map -->
			<script type="text/javascript" src="http://api.maps.yahoo.com/ajaxymap?v=5.0&amp;appid=adammikeal_appid">
				<xsl:text>&#160;</xsl:text>
			</script>
		    
		    <!-- Add a google analytics script if the key is present -->
		    <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']">
		        <script src="https://www.google-analytics.com/urchin.js" type="text/javascript"><xsl:text>&#160;</xsl:text></script>
		        <script type="text/javascript">
		            <xsl:text>_uacct = "</xsl:text><xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']"/><xsl:text>";</xsl:text>
		            <xsl:text>urchinTracker();</xsl:text>
		        </script>
		    </xsl:if>
			
			<!-- Add the title in -->
			<xsl:variable name="page_title" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title']" />
			<title>
				<xsl:choose>
					<xsl:when test="not($page_title)">
						<xsl:text>  </xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="$page_title/node()" />
					</xsl:otherwise>
				</xsl:choose>
			</title>
		</head>
	</xsl:template>
	
	
	<!-- Overriden to point the link to the new location -->
	<xsl:template name="importHead">
		<!--<xsl:apply-templates select="document('../static/header.xml')" mode="import"/>-->
	    <!--Quick fix for the strange Xalan bug that throws an Null Pointer error for the template call above. This is probably because the
	        the header.xml cannot be located or something like that; fix this later. -->
	    <div id="header">		
	        <div id="site_logo"><a href="/"><img src="{$context-path}/themes/TAMU/images/tamudl_logo.jpg" alt="Library Logo" /></a></div>
	        <div id="page_header"><img src="{$context-path}/themes/TAMU/images/wheelan_banner.jpg" alt="Header Image" /></div>
	    </div>
	</xsl:template>
    
    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    <xsl:template match="dri:p[@rend='item-view-toggle item-view-toggle-bottom']">
    </xsl:template>

    <xsl:template match="dri:div[@n='collection-recent-submission']"></xsl:template>
    
        
	
	
        
    <!-- Viewing an individual folio --> 
    <xsl:template name="itemSummaryView-DIM">
        <!-- Generate the info about the item from the metadata section -->
        <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
            mode="itemSummaryView-DIM"/>
                
        
        <div id="bitstreams">
            <xsl:for-each select="mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@MIMETYPE='image/tiff']">
                <!-- push the back page to the back of the list -->
                <xsl:sort data-type="number" order="ascending" select="number(contains(./mets:FLocat/@xlink:title,'backcover')) * 4"/>
                <!-- sort the rest... luckily of us, "front" comes before "insidefront" comes before "pg" -->
                <xsl:sort data-type="text" order="ascending" select="./mets:FLocat/@xlink:title"/>
                <div class="thumbnail">
                    <a rel="lightbox" alt="Click for a larger preview">
                        <xsl:attribute name="href">
                            <xsl:value-of select="../../mets:fileGrp[@USE='THUMBNAIL']/mets:file[substring-before(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-')
                                = substring-before(current()/mets:FLocat[@LOCTYPE='URL']/@xlink:title,'.') and contains(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-lg')]
                                /mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                        </xsl:attribute>
                        <img alt="Click for larger version" >
                            <xsl:attribute name="src">
                                <xsl:value-of select="../../mets:fileGrp[@USE='THUMBNAIL']/mets:file[substring-before(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-')
                                    = substring-before(current()/mets:FLocat[@LOCTYPE='URL']/@xlink:title,'.') and contains(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-sm')]
                                    /mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                            </xsl:attribute>
                        </img>
                    </a>                    
                    <p>
                        <strong>
                            <xsl:value-of select="substring-before(./mets:FLocat[@LOCTYPE='URL']/@xlink:title,'.')"/>
                            <xsl:text>, 300 ppi</xsl:text>
                        </strong>
                        <br />
                        <xsl:for-each select="../mets:file[@MIMETYPE='image/jpeg'][mets:FLocat[substring-before(@xlink:title,'.') = substring-before(current()/mets:FLocat/@xlink:title,'.')]]">
                            <a>
                                <xsl:attribute name="href"><xsl:value-of select="./mets:FLocat[@LOCTYPE='URL']/@xlink:href"/></xsl:attribute>
                                <xsl:text>Download </xsl:text>
                                <xsl:choose>
                                    <!--
                                    <xsl:when test="./@SIZE &lt; 1000000">
                                        <xsl:value-of select="substring(string(./@SIZE div 1000),1,5)"/>
                                        <xsl:text>KB </xsl:text>
                                    </xsl:when>-->
                                    <xsl:when test="./@SIZE &lt; 1000000000">
                                        <xsl:value-of select="substring(string(./@SIZE div 1000000),1,3)"/>
                                        <xsl:text>MB </xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="substring(string(./@SIZE div 1000000000),1,5)"/>
                                        <xsl:text>GB </xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>JPEG</xsl:text>
                            </a>
                        </xsl:for-each>
                        <br />
                        <a>
                            <xsl:attribute name="href"><xsl:value-of select="./mets:FLocat[@LOCTYPE='URL']/@xlink:href"/></xsl:attribute>
                            <xsl:text>Download </xsl:text>
                            <xsl:choose>
                                <xsl:when test="./@SIZE &lt; 1000">
                                    <xsl:value-of select="./@SIZE"/>
                                    <xsl:text>B </xsl:text>
                                </xsl:when>
                                <xsl:when test="./@SIZE &lt; 1000000">
                                    <xsl:value-of select="substring(string(./@SIZE div 1000),1,5)"/>
                                    <xsl:text>KB </xsl:text>
                                </xsl:when>
                                <xsl:when test="./@SIZE &lt; 1000000000">
                                    <xsl:value-of select="substring(string(floor(./@SIZE div 1000000)),1,5)"/>
                                    <xsl:text>MB </xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="substring(string(./@SIZE div 1000000000),1,5)"/>
                                    <xsl:text>GB </xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:text>TIFF</xsl:text>
                        </a>
                    </p>
                </div>
            </xsl:for-each>
        </div>
        
        <p class="linkbox">
            <span>View this folio as:</span>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="./mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@MIMETYPE='application/pdf']/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                </xsl:attribute>
                <span class="linkboxlink" id="pdflink">
                    <xsl:text>Screen-optimized PDF</xsl:text>
                </span>
            </a>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="./mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file/mets:FLocat[@LOCTYPE='URL' and substring(@xlink:title,1,6)='GFolio' and substring(@xlink:title,10,4)='.zip']/@xlink:href"/>
                </xsl:attribute>
                <span class="linkboxlink" id="gislink">
                    <xsl:text>GIS map data</xsl:text>
                </span>
            </a>
        </p>

        <p class="linkbox">
            <span>View all folios as:</span>
            <a href="{$context-path}/themes/TAMU/Geofolios/feeds/Geologic_Atlas_of_the_United_States.kmz">
                <span class="linkboxlink" id="kmllink">
                    <xsl:text>Google Earth overlays</xsl:text>
                </span>
            </a>
            <!-- 
            <a href="{$context-path}/themes/TAMU/Geofolios/feeds/georss.xml">
                <span class="linkboxlink" id="georsslink">
                    <xsl:text>GeoRSS feed</xsl:text>
                </span>
            </a>
            -->
        </p>
    </xsl:template>
    
    
    
    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <div id="metadata">
            <p class="byline">
                <xsl:text>Folio </xsl:text>
                <xsl:value-of select="substring-after(dim:field[@element='identifier' and @qualifier='govdoc'],':')"/>
                <xsl:text>, published </xsl:text>
                <xsl:value-of select="dim:field[@element='date' and @qualifier='issued']"/>
            </p>
            
            <p id="smallset">
                <strong>Latitude: </strong>
                <xsl:value-of select="substring-before(dim:field[@element='coverage' and @qualifier='point'],'N')"/>
                <xsl:text>N</xsl:text>
                <br/>
                <strong>Longitude: </strong>
                <xsl:value-of select="substring-before(substring-after(dim:field[@element='coverage' and @qualifier='point'],'N'),'W')"/>
                <xsl:text>W </xsl:text>
                <br/>
                
                <strong>Author: </strong>
                <xsl:value-of select="dim:field[@element='creator']"/>
                <br />
                <strong>Gov't Doc number: </strong>
                <xsl:value-of select="dim:field[@element='identifier' and @qualifier='govdoc']"/>
                <br />
                <strong>Published by: </strong>
                <xsl:value-of select="dim:field[@element='publisher']"/>
                <br />
                <!--<strong>In collection: </strong> <a href="http://txspace.tamu.edu/handle/1969.1/2490">Geologic Atlas of the United States</a> <br />-->
                <strong>In collection: </strong> <a href="http://hdl.handle.net/1969.1/2490">Geologic Atlas of the United States</a> 
                <br />
                <strong>Permanent URI: </strong> 
                <a class="showlink">
                    <xsl:attribute name="href"><xsl:value-of select="dim:field[@element='identifier' and @qualifier='uri']"/></xsl:attribute>
                    <xsl:value-of select="dim:field[@element='identifier' and @qualifier='uri']"/>
                </a>
                <br />
            </p>
        </div>
    </xsl:template>
    
    
    
    
    
    <!-- The templates that handle the respective cases: item, collection, and community. In the case of items
        current Manakin build does really have a special use for detailList so the logic of summaryList is 
        basically used in its place. --> 
    <xsl:template name="itemDetailView_DS-METS-1.0-MODS">
        <xsl:variable name="data" select="./mets:METS/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods"/>
        <xsl:variable name="context" select="."/>
        
        <div id="metadata">
            <p class="byline">
                <xsl:text>Folio </xsl:text>
                <xsl:value-of select="substring-after($data/mods:identifier[@type='govdoc'],':')"/>
                <xsl:text>, published </xsl:text>
                <xsl:value-of select="$data/mods:originInfo/mods:dateIssued[@encoding='iso8601']"/>
            </p>
            <xsl:apply-templates select="$data" mode="detailView"/>
        </div>
        
        <div id="bitstreams">
            <xsl:for-each select="mets:METS/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@MIMETYPE='image/tiff']">
                <!-- push the back page to the back of the list -->
                <xsl:sort data-type="number" order="ascending" select="number(contains(./mets:FLocat/@xlink:title,'backcover')) * 4"/>
                <!-- sort the rest... luckily of us, "front" comes before "insidefront" comes before "pg" -->
                <xsl:sort data-type="text" order="ascending" select="./mets:FLocat/@xlink:title"/>
                <div class="thumbnail">
                    <a rel="lightbox" alt="Click for a larger preview">
                        <xsl:attribute name="href">
                            <xsl:value-of select="../../mets:fileGrp[@USE='THUMBNAIL']/mets:file[substring-before(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-')
                                = substring-before(current()/mets:FLocat[@LOCTYPE='URL']/@xlink:title,'.') and contains(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-lg')]
                                /mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                        </xsl:attribute>
                        <img alt="Click for larger version" >
                            <xsl:attribute name="src">
                                <xsl:value-of select="../../mets:fileGrp[@USE='THUMBNAIL']/mets:file[substring-before(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-')
                                    = substring-before(current()/mets:FLocat[@LOCTYPE='URL']/@xlink:title,'.') and contains(mets:FLocat[@LOCTYPE='URL']/@xlink:title,'-sm')]
                                    /mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                            </xsl:attribute>
                        </img>
                    </a>                    
                    <p>
                        <strong>
                            <xsl:value-of select="substring-before(./mets:FLocat[@LOCTYPE='URL']/@xlink:title,'.')"/>
                            <xsl:text>, 300 ppi</xsl:text>
                        </strong>
                        <br />
                        <xsl:for-each select="../mets:file[@MIMETYPE='image/jpeg'][mets:FLocat[substring-before(@xlink:title,'.') = substring-before(current()/mets:FLocat/@xlink:title,'.')]]">
                            <a>
                                <xsl:attribute name="href"><xsl:value-of select="./mets:FLocat[@LOCTYPE='URL']/@xlink:href"/></xsl:attribute>
                                <xsl:text>Download </xsl:text>
                                <xsl:choose>
                                    <xsl:when test="./@SIZE &lt; 1000000">
                                        <xsl:value-of select="substring(string(./@SIZE div 1000000),2,3)"/>
                                        <xsl:text>MB </xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="substring(string(./@SIZE div 1000000000),1,5)"/>
                                        <xsl:text>GB </xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>JPEG</xsl:text>
                            </a>
                        </xsl:for-each>
                        <br />
                        <a>
                            <xsl:attribute name="href"><xsl:value-of select="./mets:FLocat[@LOCTYPE='URL']/@xlink:href"/></xsl:attribute>
                            <xsl:text>Download </xsl:text>
                            <xsl:choose>
                                <xsl:when test="./@SIZE &lt; 1000">
                                    <xsl:value-of select="./@SIZE"/>
                                    <xsl:text>B </xsl:text>
                                </xsl:when>
                                <xsl:when test="./@SIZE &lt; 1000000">
                                    <xsl:value-of select="substring(string(./@SIZE div 1000),1,5)"/>
                                    <xsl:text>KB </xsl:text>
                                </xsl:when>
                                <xsl:when test="./@SIZE &lt; 1000000000">
                                    <xsl:value-of select="substring(string(./@SIZE div 1000000),1,5)"/>
                                    <xsl:text>MB </xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="substring(string(./@SIZE div 1000000000),1,5)"/>
                                    <xsl:text>GB </xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:text>TIFF</xsl:text>
                        </a>
                    </p>
                </div>
            </xsl:for-each>
        </div>
        
        <p class="linkbox">
            <span>View this folio as:</span>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="./mets:METS/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@MIMETYPE='application/pdf']/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                </xsl:attribute>
                <span class="linkboxlink" id="pdflink">
                    <xsl:text>Screen-optimized PDF</xsl:text>
                </span>
            </a>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="./mets:METS/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file/mets:FLocat[@LOCTYPE='URL' and substring(@xlink:title,1,6)='GFolio' and substring(@xlink:title,10,4)='.zip']/@xlink:href"/>
                </xsl:attribute>
                <span class="linkboxlink" id="gislink">
                    <xsl:text>GIS map data</xsl:text>
                </span>
            </a>
        </p>
        
        <p class="linkbox">
            <span>View all folios as:</span>
            <a href="{$context-path}/themes/TAMU/Geofolios/feeds/Geologic_Atlas_of_the_United_States.kmz">
                <span class="linkboxlink" id="kmllink">
                    <xsl:text>Google Earth overlays</xsl:text>
                </span>
            </a>
            <!--  
            <a href="{$context-path}/themes/TAMU/Geofolios/feeds/georss.xml">
                <span class="linkboxlink" id="georsslink">
                    <xsl:text>GeoRSS feed</xsl:text>
                </span>
            </a>
            -->
        </p>
        
    </xsl:template>
    
    <!-- The block of templates used to render the mods contents of a DRI object -->
    <!-- The first template creates the top level table and sets the order in which the mods elements are
        to be processed. -->
    <xsl:template match="mods:mods" mode="detailView" priority="2">
        <table id="fullset">
            <xsl:apply-templates select="*[not(@type='provenance') and not(name()='mods:physicalDescription')]">
                <xsl:sort data-type="number" order="ascending" select="
                    number(name()='mods:titleInfo') * 1
                    + number(name()='mods:abstract') * 2
                    + number(name()='mods:name') *3
                    + number(name()='mods:accessCondition') * 4
                    + number(name()='mods:classification') * 5
                    + number(name()='mods:genre') * 6
                    + number(name()='mods:identifier') * 7 
                    + number(name()='mods:language') * 8
                    + number(name()='mods:location') * 9
                    + number(name()='mods:note') * 10
                    + number(name()='mods:originInfo') * 11 
                    + number(name()='mods:part') * 12
                    + number(name()='mods:physicalDescription') * 13 
                    + number(name()='mods:recordInfo') * 14
                    + number(name()='mods:relatedItem') * 15
                    + number(name()='mods:subject') * 16
                    + number(name()='mods:tableOfContents') * 17 
                    + number(name()='mods:targetAudience') * 18
                    + number(name()='mods:typeOfResource') * 19
                    + number(name()='mods:extension') * 20
                    "/>
            </xsl:apply-templates>
            <tr>
                <td>Appears in Collections:</td>
                <td></td>
                <td>
                    <!-- Somebody shoot me for doing this... or better yet, shoot Adam first and then shoot me. -->
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="key('DSMets1.0', /dri:document/dri:body//dri:objectInclude[@objectSource = current()/ancestor::dri:object/@objectIdentifier]
                                /dri:includeSet/dri:objectInclude/@objectSource)/@url"/>
                        </xsl:attribute>
                        <xsl:value-of select="key('DSMets1.0', /dri:document/dri:body//dri:objectInclude[@objectSource = current()/ancestor::dri:object/@objectIdentifier]
                            /dri:includeSet/dri:objectInclude/@objectSource)/mets:METS/mets:dmdSec/mets:mdWrap/mets:xmlData
                            /mods:mods/mods:titleInfo/mods:title"/>
                    </a>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    
    
    
	<xsl:template match="folio">
		<xsl:variable name="mk" select="concat('marker_',@number)"/>
		<xsl:variable name="pop" select="concat('popup_',@number)"/>
	    <xsl:variable name="poly" select="concat('poly_',@number)"/>
		
		
	    <!-- Add the flags -->
	    <xsl:variable name="coords_y" select="(number(substring-before(substring-after(coverage/box,'northlimit='),';')) + 
	        number(substring-before(substring-after(coverage/box,'southlimit='),';'))) div 2.0"/>
	    <xsl:variable name="coords_x" select="(number(substring-before(substring-after(coverage/box,'westlimit='),';')) + 
	        number(substring-after(coverage/box,'eastlimit='))) div 2.0"/>
		
		
		<xsl:value-of select="concat('point = new YGeoPoint(', $coords_y, ', -', $coords_x, '); ')"/>
		
		<!-- test lines -->
		<!-- <xsl:value-of select="concat($mk,' = iconImg + point; ')"/> -->
		<!-- <xsl:value-of select="concat('var ', (concat($mk,' = new YMarker(point, iconImg); ')))"/>-->
		<!-- <xsl:text>  x = new YMarker(point, iconImg); </xsl:text> -->

		<xsl:value-of select="concat($mk,' = new YMarker(point, iconImg); ')"/>


		<xsl:value-of select="$mk"/><xsl:text><![CDATA[.setSmartWindowColor('maroon');
			]]></xsl:text>
		<xsl:value-of select="$mk"/><xsl:text><![CDATA[.addLabel("<span class='folio-marker'>]]></xsl:text><xsl:value-of select="@number"/><xsl:text><![CDATA[</span>");
			]]></xsl:text>
		
		<xsl:text><![CDATA[_autoExpand = '<div class="folio-popup">]]></xsl:text>
		<xsl:value-of select="concat(@number, ' ', title, ' folio, ', coverage/political)"/>
		<xsl:text><![CDATA[</div>'; 
			]]></xsl:text>
		<xsl:value-of select="$mk"/><xsl:text><![CDATA[.addAutoExpand(_autoExpand);
			]]></xsl:text>
		
		
		<xsl:value-of select="concat('var ', $pop, ' = ')"/>
		<xsl:text><![CDATA['<div class="folio-desc"><span class="title">]]></xsl:text>
		<xsl:value-of select="concat(@number, ' ', title, ' folio, ', coverage/political)"/>
		<xsl:text><![CDATA[</span><br/>]]></xsl:text>
		<xsl:value-of select="concat('Folio ', @number, ', published ', date)"/>
		<xsl:text><![CDATA[<br/>]]></xsl:text>
		<xsl:value-of select="concat('Lat: ', substring-before(coverage/point,'N'), '; Lon: ', substring-before(substring-after(coverage/point,'N'),'W'))"/>
		<xsl:text><![CDATA[<br/><a href="]]></xsl:text>
		<xsl:value-of select="concat($context-path, '/', url)"/>
		<xsl:text><![CDATA[">View complete folio</a></div>';]]></xsl:text>
		
		<xsl:value-of select="concat('YEvent.Capture(', $mk, ', EventsList.MouseClick, function() {', $mk, '.openSmartWindow(', $pop, ');});')"/>
		
		<xsl:value-of select="concat('map.addOverlay(', $mk, ');')"/>
		
	    <![CDATA[
	    	    
	    ]]>
	</xsl:template>
    
    
    <xsl:template match="dri:div[@n='collection-home']/dri:head">
        <!-- Do nothing -->
    </xsl:template>
    
    <xsl:template match="dri:div[@n='collection-home']/dri:head" mode="geo">
        <xsl:variable name="head_count" select="count(ancestor::dri:div)"/>
        <!-- with the help of the font-sizing variable, the font-size of our header text is made continuously variable based on the character count -->
        <!-- first constant used to be 375, but I changed it to 325 - JSC -->
        <!-- in case the chosen size is less than 120%, don't let it go below. Shrinking stops at 120% -->
        <xsl:variable name="font-sizing" select="325 - $head_count * 80 - string-length(current())"></xsl:variable>
        <xsl:element name="h{$head_count}">
            <xsl:choose>
                <xsl:when test="$font-sizing &lt; 120">
                    <xsl:attribute name="style">font-size: 120%;</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="style">font-size: <xsl:value-of select="$font-sizing"/>%;</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-div-head</xsl:with-param>
            </xsl:call-template>            
            <xsl:apply-templates />
        </xsl:element>	
    </xsl:template>
    
    
    
    <!-- Rendering the main collection view (map and all) --> 
    <xsl:template name="collectionDetailView-DIM">
        <div class="detail-view">&#160;
            
            <div id="mapContainer"></div>
            
            <xsl:apply-templates select="//dri:div[@n='collection-home']/dri:head" mode="geo"/>
            
            <!-- Generate the info about the collections from the metadata section -->
            <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
                mode="collectionDetailView-DIM"/>
            
            <!-- JS script moved from here to the dri:document template -->
        </div>
    </xsl:template>
    
    
    
    <!-- Generate the info about the collection from the metadata section -->
    <xsl:template match="dim:dim" mode="collectionDetailView-DIM"> 
        <xsl:if test="string-length(dim:field[@element='description'][not(@qualifier)])&gt;0">
            <p class="intro-text">
                <xsl:copy-of select="dim:field[@element='description'][not(@qualifier)]/node()"/>
            </p>
        </xsl:if>
        
        <!-- The links to all the top-level stuff -->
        <p class="linkbox front">
            <a href="{$context-path}/themes/TAMU/Geofolios/feeds/Geologic_Atlas_of_the_United_States.kmz">
                <span class="linkboxlink" id="kmllink">
                    <xsl:text>Google Earth overlays</xsl:text>
                </span>
            </a>
            <!-- 
            <a href="{$context-path}/themes/TAMU/Geofolios/feeds/georss.xml">
                <span class="linkboxlink" id="georsslink">
                    <xsl:text>GeoRSS feed</xsl:text>
                </span>
            </a>
            <a href="{$context-path}/themes/TAMU/Geofolios/feeds/GIS-data.zip">
                <span class="linkboxlink" id="gislink">
                    <xsl:text>GIS map data</xsl:text>
                </span>
            </a>
            -->
        </p>
        
        <xsl:if test="string-length(dim:field[@element='rights'][not(@qualifier)])&gt;0 or string-length(dim:field[@element='rights'][@qualifier='license'])&gt;0">
            <div class="detail-view-rights-and-license">
                <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.copyright</i18n:text></h3>
                <xsl:if test="string-length(dim:field[@element='rights'][not(@qualifier)])&gt;0">
                    <div class="copyright-text">
                        <xsl:copy-of select="dim:field[@element='rights'][not(@qualifier)]/node()"/>
                    </div>
                </xsl:if>
                <xsl:if test="string-length(dim:field[@element='rights'][@qualifier='license'])&gt;0">
                    <div class="license-text">
                        <xsl:copy-of select="dim:field[@element='rights'][@qualifier='license']/node()"/>
                    </div>
                </xsl:if>
            </div>
        </xsl:if>
    </xsl:template>
    
    
    
    
    
    <xsl:template match="dri:div[@n='collection-search-browse']">
    </xsl:template>
    

    
    
    
    
    
    
    
    
    
    
    <xsl:template match="dri:div[@n='search-results']/dri:head">
        <h3 style="margin-bottom: 5px;"><xsl:apply-templates/></h3>
    </xsl:template>
    
    <xsl:template match="dri:referenceSet[@n='search-results-repository']/dri:head">
    </xsl:template>
       
    
	
    
	
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.SimpleSearch.div.search'] | dri:div[@id='aspect.artifactbrowser.AdvancedSearch.div.advanced-search']">   
        <div id="mapContainer"></div>
		
		<!-- JS script moved from here to the dri:document template -->
		
        <xsl:apply-imports/>
    </xsl:template>
	
	
	
	
	
    <!-- Included here to override the default behaviour of including the collection parent -->
    <xsl:template match="dri:reference" mode="summaryView">
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            <!-- No options selected, render the full METS document -->
        </xsl:variable>
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
        <xsl:apply-templates select="document($externalMetadataURL)" mode="summaryView"/>
    </xsl:template>  
    
</xsl:stylesheet>
