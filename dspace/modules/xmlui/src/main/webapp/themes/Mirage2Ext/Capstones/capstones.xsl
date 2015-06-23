<!-- todo: update template to use Mirage 2 -->

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
    <xsl:import href="../shared.xsl"/>
    <xsl:output indent="yes"/>
    
    <!-- Generate the info about the item from the metadata section
        Used to display item information on the simple item record -->
    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        
        <table class="ds-includeSet-table">
            <tr class="ds-table-row odd">
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
            <xsl:if test="dim:field[@element='contributor' and @qualifier='advisor']">
                <tr class="ds-table-row even">
                    <td><span class="bold">Project Advisor:</span></td>
                    <td>
                        <xsl:for-each select="dim:field[@element='contributor' and @qualifier='advisor']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='contributor' and @qualifier='advisor']) != 0"> <br class="simpleItemViewValueBreak"/> </xsl:if>
                        </xsl:for-each>	                  
                    </td>
                </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='contributor' and @qualifier='sponsor']">
                <tr class="ds-table-row even">
                    <td><span class="bold">Client:</span></td>
                    <td>
                        <xsl:for-each select="dim:field[@element='contributor' and @qualifier='sponsor']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='contributor' and @qualifier='sponsor']) != 0"> <br class="simpleItemViewValueBreak"/> </xsl:if>
                        </xsl:for-each>	                  
                    </td>
                </tr>
            </xsl:if>
            <tr class="ds-table-row odd">
                <xsl:choose> <!-- label the creator dynamically depending on the variety of the work -->
                    <xsl:when test="dim:field[@element='format'][@qualifier='medium'] = 'Photograph'">
                        <td><span class="bold">Photographer:</span></td>
                    </xsl:when>
                    <xsl:when test="dim:field[@element='type'] = 'Image'">
                        <td><span class="bold">Creator:</span></td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-author</i18n:text>:</span></td>
                    </xsl:otherwise>
                </xsl:choose>
                <td>
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                <a href="/browse?type=author&amp;value={node()}"><xsl:value-of select="node()"/></a>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='creator']">
                            <xsl:for-each select="dim:field[@element='creator']">
                                <a href="/browse?type=author&amp;value={node()}"><xsl:value-of select="node()"/></a>
                                <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
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
             
            <tr class="ds-table-row odd">
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
            <tr class="ds-table-row even">
                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:</span></td>
                
                <!-- Prefer dc.date.created but fall back on dc.date.issued -->
                <td>
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='date' and @qualifier='created']/node()">
                            <xsl:copy-of select="substring(dim:field[@element='date' and @qualifier='created']/node(),1,10)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
            
            <xsl:call-template name="handleDWC"/>
            
        </table>
        
    </xsl:template>
        
    
    
    
    
    
    
</xsl:stylesheet>