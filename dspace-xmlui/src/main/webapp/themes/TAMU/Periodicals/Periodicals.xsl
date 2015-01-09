<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
	    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
	    xmlns:mets="http://www.loc.gov/METS/"
	    xmlns:dc="http://purl.org/dc/elements/1.1/"
	    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	    xmlns:mods="http://www.loc.gov/mods/v3"
	    xmlns:xlink="http://www.w3.org/TR/xlink/"
	    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	 	xmlns:tdl="http://www.tdl.org/NS/tdl" version="1.0">

	    <xsl:import href="../TAMU.xsl"/>
	    <xsl:output indent="yes"/>
	    
    <!-- The header (NOT the HTML head element) contains the title, subtitle, login box and various 
        placeholders for header images -->
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

    

	<!-- Set up the key for the Muenchian grouping -->
	<xsl:key name="issues-by-vol" match="tdl:issue" use="@vol" />
	

	<!--
        The document variable is a reference to the top of the original DRI 
        document. This can be usefull in situations where the XSL has left
        the original document's context such as after a document() call and 
        would like to retrieve information back from the base DRI document.
    -->
    <xsl:variable name="document" select="/dri:document"/>
    
    <xsl:variable name="hidesearch" select="contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request' and @qualifier='queryString']/text(),'hidesearch')"/>

	<!-- A collection rendered in the detailView pattern; default way of viewing a collection. -->
    <xsl:template name="collectionDetailView-DIM">
        <div class="detail-view">&#160;
            <!-- Generate the logo, if present, from the file section -->
            <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='LOGO']"/>
            <!-- Generate the info about the collections from the metadata section -->
            <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
                mode="collectionDetailView-DIM"/>
        </div>
        
        <xsl:apply-templates select="//tdl:issue[generate-id(.) = generate-id(key('issues-by-vol', @vol)[1])]" />
		 <xsl:variable name="collection_handle" select="substring-after($document/dri:meta/dri:pageMeta/dri:metadata[@element='focus' and @qualifier='container'], ':')" />

		<p style="padding-top: 50px;"> </p>
		<p>
			<a href="{$context-path}/handle/{$collection_handle}/advanced-search">Search within this collection</a>
		</p>
    </xsl:template>


<!-- Iterate over the <tdl:issue> tags and group using the Muenchian method -->
<xsl:template match="tdl:issue">
	<xsl:variable name="search_path" select="$document/dri:meta/dri:pageMeta/dri:metadata[@element='search' and @qualifier='simpleURL']" />
	<xsl:variable name="query_string" select="$document/dri:meta/dri:pageMeta/dri:metadata[@element='search' and @qualifier='queryField']" />
	<xsl:variable name="context_path" select="$document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']" />
	<xsl:variable name="collection_handle" select="substring-after($document/dri:meta/dri:pageMeta/dri:metadata[@element='focus' and @qualifier='container'], ':')" />
	
	<div class="journal-volume-group">
	
		<h2>
			<xsl:text>Volume </xsl:text>
			<xsl:value-of select="@vol" />
		</h2>
		<xsl:for-each select="key('issues-by-vol', @vol)">
		<p>
			<strong>
				<xsl:text>Issues </xsl:text>
				<xsl:value-of select="@num" />
				<xsl:text> (</xsl:text>
				<xsl:value-of select="@year" />
				<xsl:text>)</xsl:text>
				<xsl:if test="@name != ''">
					<xsl:text> :: </xsl:text>
					<xsl:value-of select="@name" />
				</xsl:if>
			</strong> <br />
			<xsl:variable name="index"><xsl:if test="@index"><xsl:value-of select="@index"/></xsl:if><xsl:if test="not(@index)"><xsl:text>series</xsl:text></xsl:if></xsl:variable>
			<a href="{$context_path}/handle/{$collection_handle}/search?{$query_string}={$index}:%22{@browse}%22&amp;hidesearch">Browse Issue</a> |
			<a href="{$context_path}/handle/{@handle}">Download Complete Issue</a>
		</p>
		</xsl:for-each>
	
	</div>
		
</xsl:template>

<!-- Hide the search box -->
<xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-search-browse']" >
</xsl:template>

<!-- Hide the recent submissions list -->
                                 
<xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionRecentSubmissions.div.collection-recent-submission']" >
</xsl:template>

<!-- Group of templates to hide the search forms when appropriate (if the "hidesearch" parameter is in the contextualized-search URL) -->
<xsl:template match="dri:div[@n='general-query'][$hidesearch]" >
</xsl:template>
<xsl:template match="dri:p[@n='result-query'][$hidesearch]" >
</xsl:template>
<xsl:template match="dri:div[@id='aspect.artifactbrowser.SimpleSearch.div.search'][$hidesearch]/dri:head" >
	<h1>
	    <span class="header-insert">Browse Issue</span>
	</h1>
</xsl:template>
<xsl:template match="dri:div[@id='aspect.artifactbrowser.SimpleSearch.div.search-results'][$hidesearch]/dri:head">
</xsl:template>



    <!-- Override the pagination handling to ensure propagation of the ?hidesearch parameter -->
    <xsl:template match="@pagination[$hidesearch]">
        <xsl:param name="position"/>
        <xsl:choose>
            <xsl:when test=". = 'simple'">
                <div class="pagination {$position}">
                    <xsl:if test="parent::node()/@previousPage">
                        <a class="previous-page-link">
                            <xsl:attribute name="href">
                                <xsl:value-of select="parent::node()/@previousPage"/>
                                <xsl:text>&amp;hidesearch</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-previous</i18n:text>
                        </a>
                    </xsl:if>
                    <p class="pagination-info">
                        <i18n:translate>
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-info</i18n:text>
                            <i18n:param><xsl:value-of select="parent::node()/@firstItemIndex"/></i18n:param>
                            <i18n:param><xsl:value-of select="parent::node()/@lastItemIndex"/></i18n:param>
                            <i18n:param><xsl:value-of select="parent::node()/@itemsTotal"/></i18n:param>
                        </i18n:translate>
                        <!--
                            <xsl:text>Now showing items </xsl:text>
                            <xsl:value-of select="parent::node()/@firstItemIndex"/>
                            <xsl:text>-</xsl:text>
                            <xsl:value-of select="parent::node()/@lastItemIndex"/>
                            <xsl:text> of </xsl:text>
                            <xsl:value-of select="parent::node()/@itemsTotal"/>
                        -->
                    </p>
                    <xsl:if test="parent::node()/@nextPage">
                        <a class="next-page-link">
                            <xsl:attribute name="href">
                                <xsl:value-of select="parent::node()/@nextPage"/>
                                <xsl:text>&amp;hidesearch</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-next</i18n:text>
                        </a>
                    </xsl:if>
                </div>
            </xsl:when>
            <xsl:when test=". = 'masked'">
                <div class="pagination-masked {$position}">
                    <xsl:if test="not(parent::node()/@firstItemIndex = 0 or parent::node()/@firstItemIndex = 1)">
                        <a class="previous-page-link">
                            <xsl:attribute name="href">
                                <xsl:value-of select="substring-before(parent::node()/@pageURLMask,'{pageNum}')"/>
                                <xsl:value-of select="parent::node()/@currentPage - 1"/>
                                <xsl:value-of select="substring-after(parent::node()/@pageURLMask,'{pageNum}')"/>
                                <xsl:text>&amp;hidesearch</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-previous</i18n:text>
                        </a>
                    </xsl:if>
                    <p class="pagination-info">
                        <i18n:translate>
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-info</i18n:text>
                            <i18n:param><xsl:value-of select="parent::node()/@firstItemIndex"/></i18n:param>
                            <i18n:param><xsl:value-of select="parent::node()/@lastItemIndex"/></i18n:param>
                            <i18n:param><xsl:value-of select="parent::node()/@itemsTotal"/></i18n:param>
                        </i18n:translate>
                    </p>
                    <ul class="pagination-links">
                        <xsl:if test="(parent::node()/@currentPage - 4) &gt; 0">
                            <li class="first-page-link">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="substring-before(parent::node()/@pageURLMask,'{pageNum}')"/>
                                        <xsl:text>1</xsl:text>
                                        <xsl:value-of select="substring-after(parent::node()/@pageURLMask,'{pageNum}')"/>
                                        <xsl:text>&amp;hidesearch</xsl:text>
                                    </xsl:attribute>
                                    <xsl:text>1</xsl:text>
                                </a>
                                <xsl:text> . . . </xsl:text>
                            </li>
                        </xsl:if>
                        <xsl:call-template name="offset-link">
                            <xsl:with-param name="pageOffset">-3</xsl:with-param>
                        </xsl:call-template>
                        <xsl:call-template name="offset-link">
                            <xsl:with-param name="pageOffset">-2</xsl:with-param>
                        </xsl:call-template>
                        <xsl:call-template name="offset-link">
                            <xsl:with-param name="pageOffset">-1</xsl:with-param>
                        </xsl:call-template>
                        <xsl:call-template name="offset-link">
                            <xsl:with-param name="pageOffset">0</xsl:with-param>
                        </xsl:call-template>
                        <xsl:call-template name="offset-link">
                            <xsl:with-param name="pageOffset">1</xsl:with-param>
                        </xsl:call-template>
                        <xsl:call-template name="offset-link">
                            <xsl:with-param name="pageOffset">2</xsl:with-param>
                        </xsl:call-template>
                        <xsl:call-template name="offset-link">
                            <xsl:with-param name="pageOffset">3</xsl:with-param>
                        </xsl:call-template>
                        <xsl:if test="(parent::node()/@currentPage + 4) &lt;= (parent::node()/@pagesTotal)">
                            <li class="last-page-link">
                                <xsl:text> . . . </xsl:text>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="substring-before(parent::node()/@pageURLMask,'{pageNum}')"/>
                                        <xsl:value-of select="parent::node()/@pagesTotal"/>
                                        <xsl:value-of select="substring-after(parent::node()/@pageURLMask,'{pageNum}')"/>
                                        <xsl:text>&amp;hidesearch</xsl:text>
                                    </xsl:attribute>
                                    <xsl:value-of select="parent::node()/@pagesTotal"/>
                                </a>
                            </li>
                        </xsl:if>
                    </ul>
                    <xsl:if test="not(parent::node()/@lastItemIndex = parent::node()/@itemsTotal)">
                        <a class="next-page-link">
                            <xsl:attribute name="href">
                                <xsl:value-of select="substring-before(parent::node()/@pageURLMask,'{pageNum}')"/>
                                <xsl:value-of select="parent::node()/@currentPage + 1"/>
                                <xsl:value-of select="substring-after(parent::node()/@pageURLMask,'{pageNum}')"/>
                                <xsl:text>&amp;hidesearch</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-next</i18n:text>
                        </a>
                    </xsl:if>
                </div>
            </xsl:when>            
        </xsl:choose>
    </xsl:template>
    
    <!-- A quick helper function used by the @pagination template for repetitive tasks -->
    <xsl:template name="offset-link">
        <xsl:param name="pageOffset"/>
        <xsl:if test="((parent::node()/@currentPage + $pageOffset) &gt; 0) and 
            ((parent::node()/@currentPage + $pageOffset) &lt;= (parent::node()/@pagesTotal))">
            <li class="page-link">
                <xsl:if test="$pageOffset = 0">
                    <xsl:attribute name="class">current-page-link</xsl:attribute>
                </xsl:if>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="substring-before(parent::node()/@pageURLMask,'{pageNum}')"/>
                        <xsl:value-of select="parent::node()/@currentPage + $pageOffset"/>
                        <xsl:value-of select="substring-after(parent::node()/@pageURLMask,'{pageNum}')"/>
                        <xsl:text>&amp;hidesearch</xsl:text>
                    </xsl:attribute>
                    <xsl:value-of select="parent::node()/@currentPage + $pageOffset"/>
                </a>
            </li>
        </xsl:if>
    </xsl:template>





<!-- Generate the info about the item from the metadata section -->
    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <table class="ds-includeSet-table">
            <tr class="ds-table-row even">
                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>: </span></td>
                <td>
                    <xsl:choose>
                        <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) &gt; 1">
                            <xsl:for-each select="dim:field[@element='title'][not(@qualifier)]">
                            	<xsl:value-of select="./node()"/>
                            	<xsl:if test="count(following-sibling::dim:field[@element='title'][not(@qualifier)]) != 0">
	                                    <xsl:text>; </xsl:text><br/>
	                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                         <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) = 1">
                            <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
            <xsl:if test="dim:field[@element='contributor'][@qualifier='author'] or dim:field[@element='creator'] or dim:field[@element='contributor']">
	            <tr class="ds-table-row odd">
	                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-author</i18n:text>:</span></td>
	                <td>
	                    <xsl:choose>
	                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
	                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
	                                <xsl:copy-of select="node()"/>
	                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:when test="dim:field[@element='creator']">
	                            <xsl:for-each select="dim:field[@element='creator']">
	                                <xsl:copy-of select="node()"/>
	                                <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:when test="dim:field[@element='contributor']">
	                            <xsl:for-each select="dim:field[@element='contributor']">
	                                <xsl:copy-of select="node()"/>
	                                <xsl:if test="count(following-sibling::dim:field[@element='contributor']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:otherwise>
	                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
	                        </xsl:otherwise>
	                    </xsl:choose>
	                </td>
	            </tr>
            </xsl:if>
            
            <xsl:if test="dim:field[@element='relation' and @qualifier='ispartofseries']">
	            <tr class="ds-table-row odd">
	                <td><span class="bold"><i18n:text>Issue</i18n:text>:</span></td>
	                <td>
		                <xsl:for-each select="dim:field[@element='relation' and @qualifier='ispartofseries']">
		                	 <xsl:copy-of select="./node()"/>
		                	 <xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">
	                    	<br/>
	                    </xsl:if>
		                </xsl:for-each>
	                </td>
	            </tr>
            </xsl:if>
            
            
            <xsl:if test="dim:field[@element='date' and @qualifier='issued']">
	            <tr class="ds-table-row odd">
	                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:</span></td>
	                <td>
		                <xsl:for-each select="dim:field[@element='date' and @qualifier='issued']">
		                	 <xsl:copy-of select="./node()"/>
		                	 <xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">
	                    	<br/>
	                    </xsl:if>
		                </xsl:for-each>
	                </td>
	            </tr>
            </xsl:if>
            
            <xsl:if test="dim:field[@element='description' and @qualifier='abstract']">
	            <tr class="ds-table-row even">
	                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:</span></td>
	                <td>
	                <xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
	                	<hr class="metadata-seperator"/>
	                </xsl:if>
	                <xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']">
		                <xsl:copy-of select="./node()"/>
		                <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='abstract']) != 0">
	                    	<hr class="metadata-seperator"/>
	                    </xsl:if>
	              	</xsl:for-each>
	              	<xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
	                	<hr class="metadata-seperator"/>
	                </xsl:if>
	                </td>
	            </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='description' and not(@qualifier)]">
	            <tr class="ds-table-row odd">
	                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:</span></td>
	                <td>
	                <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1 and not(count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1)">
	                	<hr class="metadata-seperator"/>
	                </xsl:if>
	                <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">
		                <xsl:copy-of select="./node()"/>
		                <xsl:if test="count(following-sibling::dim:field[@element='description' and not(@qualifier)]) != 0">
	                    	<hr class="metadata-seperator"/>
	                    </xsl:if>
	               	</xsl:for-each>
	               	<xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1">
	                	<hr class="metadata-seperator"/>
	                </xsl:if>
	                </td>
	            </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='identifier' and @qualifier='uri']">
	            <tr class="ds-table-row even">
	                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:</span></td>
	                <td>
	                	<xsl:for-each select="dim:field[@element='identifier' and @qualifier='uri']">
		                    <a>
		                        <xsl:attribute name="href">
		                            <xsl:copy-of select="./node()"/>
		                        </xsl:attribute>
		                        <xsl:copy-of select="./node()"/>
		                    </a>
		                    <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='uri']) != 0">
		                    	<br/>
		                    </xsl:if>
	                    </xsl:for-each>
	                </td>
	            </tr>
            </xsl:if>
            
            <xsl:call-template name="handleDWC"/>
            
        </table>
    </xsl:template>

</xsl:stylesheet>