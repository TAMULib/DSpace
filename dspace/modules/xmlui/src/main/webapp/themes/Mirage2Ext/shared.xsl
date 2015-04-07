<?xml version="1.0" encoding="UTF-8"?>

<!--
  TAMU.xsl

  Version: $Revision: 2.0 $
 
  Date: $Date: 2008/11/1 22:54:52 $
 
  Copyright (c) 2002-2009, Texas A&M University. All rights reserved.
 
  
-->

<!--
    TODO: Describe this XSL file    
    Author: Alexey Maslov
    Author: James Creel
    Author: Adam Mikeal
    
-->    

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
    exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">
    
    <xsl:import href="../Mirage2/xsl/theme.xsl"/>
    <xsl:variable name="child-theme-path" select="concat($context-path,'/themes/Mirage2Ext/')"/>

    <!-- inject child theme content into Mirage2 generated document head -->
   <xsl:template name="appendHead">
                <!-- generate child theme css -->
                <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='stylesheet']">
                    <link rel="stylesheet" type="text/css">
                        <xsl:attribute name="media">
                            <xsl:value-of select="@qualifier"/>
                        </xsl:attribute>
                        <xsl:attribute name="href">
                            <xsl:value-of select="$child-theme-path"/>
                            <xsl:value-of select="."/>
                        </xsl:attribute>
                    </link>
                </xsl:for-each>
    </xsl:template>     
        
    <xsl:template match="mets:METS[mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']]" mode="metadataPopup">
        <xsl:choose>
            <xsl:when test="@LABEL='DSpace Item'">
                <xsl:call-template name="itemMetadataPopup-DIM"/>
            </xsl:when>
            <!-- The following calls are to templates not implemented yet (if ever)
            <xsl:when test="@LABEL='DSpace Collection'">
                <xsl:call-template name="collectionMetadataPopup-DIM"/>
            </xsl:when>
            <xsl:when test="@LABEL='DSpace Community'">
                <xsl:call-template name="communityMetadataPopup-DIM"/>
            </xsl:when>
            -->
            <xsl:otherwise>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.non-conformant</i18n:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- A metadata popup for an item rendered in the summaryList pattern.  -->
    <xsl:template name="itemMetadataPopup-DIM">
        <!-- Generate the info about the item from the metadata section -->
        <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
            mode="itemMetadataPopup-DIM"/>
        <!-- Generate the thunbnail, if present, from the file section -->
        <!-- <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']" mode="itemMetadataPopup-DIM"/> -->
    </xsl:template>

  <!-- Handle the Darwin Core metadata (used by capstones) -->
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
    
</xsl:stylesheet>
