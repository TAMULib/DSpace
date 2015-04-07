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

        <xsl:import href="../shared.xsl"/>
        <xsl:output indent="yes"/>
        
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
    <xsl:variable name="discoveryUrl" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search' and @qualifier='simpleURL']"/>

    <!-- inject child theme content into Mirage2 generated document head -->
    <xsl:template name="appendHead">
        <link rel="stylesheet" href="{concat($child-theme-path, 'Periodicals/lib/css/style.css')}"/>
    </xsl:template>

    <!-- Handle the Darwin Core metadata -->
    <xsl:template name="handleDWC">
        <xsl:if test="dim:field[@element='basisOfRecord'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Basis of Record:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='basisOfRecord'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='basisOfRecord'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='catalogNumber'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Catalog Number:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='catalogNumber'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='catalogNumber'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='collectionCode'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Collection Code:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='collectionCode'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='collectionCode'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='collectionID'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Collection ID:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='collectionID'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='collectionID'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='institutionCode'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Institution Code:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='institutionCode'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='institutionCode'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='institutionID'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Institution ID:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='institutionID'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='institutionID'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='recordedBy'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Recorded By:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='recordedBy'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='recordedBy'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='scientificName'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Scientific Name:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='scientificName'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='scientificName'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='scientificNameID'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Scientific Name ID:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='scientificNameID'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='scientificNameID'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='dataGeneralizations'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Data Generalizations:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='dataGeneralizations'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='dataGeneralizations'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
        <xsl:if test="dim:field[@element='family'][@mdschema='dwc']">
            <tr class="ds-table-row odd">
                <td><span class="bold">Family:</span></td>
                <td>
                    <xsl:for-each select="dim:field[@element='family'][@mdschema='dwc']">
                        <xsl:copy-of select="node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='family'][@mdschema='dwc']) != 0"> <br /> </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template> 

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
            <xsl:variable name="index"><xsl:if test="@index"><xsl:value-of select="@index"/></xsl:if>
            <xsl:if test="not(@index)"><xsl:text>series</xsl:text></xsl:if></xsl:variable>
            <a href="{$context_path}/handle/{$collection_handle}{$discoveryUrl}?{$query_string}={$index}:vol. {@vol} no. {@num}">Browse Issue</a> |
            <a href="{$context_path}/handle/{@handle}">Download Complete Issue</a>
        </p>
        </xsl:for-each>
    
    </div>
        
</xsl:template>

<!-- Hide the search box -->
<xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-search-browse']" >
</xsl:template>

<!-- Hide the recent submissions list -->
                                 
<xsl:template match="dri:div[@id='aspect.discovery.CollectionRecentSubmissions.div.collection-recent-submission']" >
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