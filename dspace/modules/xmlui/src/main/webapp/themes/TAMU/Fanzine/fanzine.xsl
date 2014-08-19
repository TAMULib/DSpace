<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns="http://www.w3.org/1999/xhtml"

    xmlns:encoder="xalan://java.net.URLEncoder"
    
    exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">
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
    
    
    <!-- Item Detailed View: The block of templates used to render the complete DIM contents of a DRI object -->
    <xsl:template match="dim:dim" mode="itemDetailView-DIM">
        <span class="Z3988">
            <xsl:attribute name="title">
                <xsl:call-template name="renderCOinS"/>
            </xsl:attribute>
        </span>                
        
        <table class="ds-includeSet-table">
            <xsl:apply-templates mode="itemDetailView-DIM"/>
        </table>
    </xsl:template>
    
    
    
    <!-- Item Brief View: Generate the info about the item from the metadata section -->
    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
                
        <table class="ds-includeSet-table">
            <tr class="ds-table-row even">
                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>: </span></td>
                <td>
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='title']">
                            <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
            
            <xsl:if test="dim:field[@element='relation'][@qualifier='haspart']">
                <tr class="ds-table-row odd">
                    <td><span class="bold">Contains:</span></td>
                    <td>
                        <xsl:for-each select="dim:field[@element='relation'][@qualifier='haspart']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='relation'][@qualifier='haspart']) != 0"> <br /> </xsl:if>
                        </xsl:for-each>
                    </td>
                </tr>
            </xsl:if>
            
            <xsl:if test="dim:field[@element='contributor' and @qualifier='editor']">
            	<tr class="ds-table-row even">
            		<td><span class="bold">Editor:</span></td>
            		<td>
            			<xsl:for-each select="dim:field[@element='contributor'][@qualifier='editor']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='editor']) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
            		</td>
            	</tr>            
            </xsl:if>
            
            <xsl:if test="dim:field[@element='contributor' and @qualifier='illustrator']">
            	<tr class="ds-table-row even">
            		<td><span class="bold">Illustrator:</span></td>
            		<td>
            			<xsl:for-each select="dim:field[@element='contributor'][@qualifier='illustrator']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='illustrator']) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
            		</td>
            	</tr>            
            </xsl:if>
            
            <xsl:if test="dim:field[@element='contributor' and @qualifier='author'] or dim:field[@element='creator']">
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
	                    </xsl:choose>
	                </td>
	            </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='description' and @qualifier='abstract']">
                <tr class="ds-table-row even">
                    <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:</span></td>
                    <td>
                        <xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='abstract']) != 0"> <br class="simpleItemViewValueBreak"/> </xsl:if>
                        </xsl:for-each>	                  
                    </td>
                </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='description' and not(@qualifier)]">
                <tr class="ds-table-row odd">
                    <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:</span></td>
                    <td>
	                    <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">
	                        <xsl:copy-of select="node()"/>
	                        <xsl:if test="count(following-sibling::dim:field[@element='description']) != 0"> <br class="simpleItemViewValueBreak"/> </xsl:if>
	                    </xsl:for-each>
	                </td>
                </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='publisher']">
                <tr class="ds-table-row odd">
                    <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-publisher</i18n:text>:</span></td>
                    <td>
	                    <xsl:for-each select="dim:field[@element='publisher']">
	                        <xsl:copy-of select="node()"/>
	                        <xsl:if test="count(following-sibling::dim:field[@element='publisher']) != 0"> <br class="simpleItemViewValueBreak"/> </xsl:if>
	                    </xsl:for-each>
	                </td>
                </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='subject']">
                <tr class="ds-table-row odd">
                    <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-subject</i18n:text>:</span></td>
                    <td>
                        <xsl:for-each select="dim:field[@element='subject']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='subject']) != 0"> <br /> </xsl:if>
                        </xsl:for-each>
                    </td>
                </tr>
            </xsl:if>
            <tr class="ds-table-row even">
                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:</span></td>
                <td>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:copy-of select="dim:field[@element='identifier' and @qualifier='uri'][1]/node()"/>
                        </xsl:attribute>
                        <xsl:copy-of select="dim:field[@element='identifier' and @qualifier='uri'][1]/node()"/>
                    </a>
                </td>
            </tr>
            <tr class="ds-table-row odd">
                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:</span></td>
                <td><xsl:copy-of select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/></td>
            </tr>
            
            <xsl:call-template name="handleDWC"/>
            
        </table>
        <!-- display the containing collections -->
    </xsl:template>
    
    
    
    
    
    
    
    
    
    
    
    
    <!--borrowed from DIM-Handler.xsl
        *********************************************
        OpenURL COinS Rendering Template
        *********************************************
        
        COinS Example:
        
        <span class="Z3988" 
        title="ctx_ver=Z39.88-2004&amp;
        rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Amtx%3Adc&amp;
        rfr_id=info%3Asid%2Focoins.info%3Agenerator&amp;
        rft.title=Making+WordPress+Content+Available+to+Zotero&amp;
        rft.aulast=Kraus&amp;
        rft.aufirst=Kari&amp;
        rft.subject=News&amp;
        rft.source=Zotero%3A+The+Next-Generation+Research+Tool&amp;
        rft.date=2007-02-08&amp;
        rft.type=blogPost&amp;
        rft.format=text&amp;
        rft.identifier=http://www.zotero.org/blog/making-wordpress-content-available-to-zotero/&amp;
        rft.language=English"></span>
        
        This Code does not parse authors names, instead relying on dc.contributor to populate the
        coins
    -->
    
    <xsl:template name="renderCOinS">
        <xsl:text>ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Amtx%3Adc&amp;</xsl:text>
        <xsl:for-each select=".//dim:field[@element = 'identifier']">
            <xsl:text>rft_id=</xsl:text>
            <xsl:value-of select="encoder:encode(string(.))"/>
            <xsl:text>&amp;</xsl:text>
        </xsl:for-each>
        <xsl:text>rfr_id=info%3Asid%2Fdatadryad.org%3Arepo&amp;</xsl:text>
        <xsl:for-each select=".//dim:field[@element != 'description' and @mdschema !='dc' and @qualifier != 'provenance']">
            <xsl:value-of select="concat('rft.', @element,'=',encoder:encode(string(.))) "/>
            <xsl:if test="position()!=last()">
                <xsl:text>&amp;</xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    
    
</xsl:stylesheet>