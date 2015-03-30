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
	xmlns:confman="org.dspace.core.ConfigurationManager"
	xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">
    
    <xsl:import href="../dri2xhtml.xsl"/>
	<xsl:import href="../TAMU/lib/xsl/date.month-name.template.xsl"/>
    <xsl:import href="../TAMU/lib/xsl/date.day-in-month.template.xsl"/>
    <xsl:import href="../TAMU/lib/xsl/date.year.template.xsl"/>
  <!--<xsl:import href="../dri2xhtml-alt/dri2xhtml.xsl"/>-->
    <xsl:import href="../Mirage2/xsl/theme.xsl"/>
    <xsl:variable name="theme-path" select="concat($context-path,'/themes/Mirage2/')"/>
    <xsl:variable name="child-theme-path" select="concat($context-path,'/themes/Mirage2Ext/')"/>
    <xsl:output indent="yes"/>
 
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
	
	<!-- following two templates are used to put the context path in place of
	 a "%contextPath% placeholder string that we use in some external xml (xhtml) that
	 gets imported -->	
	<xsl:template match="*" mode="import">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="import"/>
			<xsl:apply-templates mode="import"/>	
		</xsl:copy>
	</xsl:template>	
	<xsl:template match="@*" mode="import">
		<xsl:choose>
			<xsl:when test="contains(., '%contextPath%')">
				<xsl:attribute name="{name(.)}">
					<!-- <xsl:value-of select="name(.)"/> -->
					<xsl:value-of select="substring-before(., '%contextPath%')"/>
					<xsl:value-of select="$context-path"/>
					<xsl:value-of select="substring-after(., '%contextPath%')"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy/>
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>
	
    <!-- The HTML head element contains references to CSS as well as embedded JavaScript code. Most of this 
        information is either user-provided bits of post-processing (as in the case of the JavaScript), or 
        references to stylesheets pulled directly from the pageMeta element. -->
    
	<!-- 
        The template to handle the dri:body element. It simply creates the ds-body div and applies 
        templates of the body's child elements (which consists entirely of dri:div tags).
    -->    
    <xsl:template match="dri:body">
        <div id="ds-body">
				<xsl:variable name="site_home" select="*[@n='news']" />
        
				<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='alert'][@qualifier='message']">
					<div id="ds-system-wide-alert">
						<p>
							<xsl:copy-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='alert'][@qualifier='message']/node()"/>
						</p>
					</div>
				</xsl:if>
				
				<div id="content">
		            <xsl:apply-templates select="*[not(@n='front-page-search')]"/>
				</div>
			
        </div>
    </xsl:template>
	
	<!-- Utility function used by the item's summary view to list each bitstream -->
    <xsl:template name="buildBitstreamSingle">
        <xsl:param name="context" select="."/>
        <xsl:param name="file" select="."/>
        <div class="slider-bitstreams">
            <span>
                <a class="bitstream-file">
                    <xsl:attribute name="href">
                        <xsl:value-of select="$file/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$file/mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="string-length($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title) > 50">
                            <xsl:variable name="title_length" select="string-length($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title)"/>
                            <xsl:value-of select="substring($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title,1,15)"/>
                            <xsl:text> ... </xsl:text>
                            <xsl:value-of select="substring($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title,$title_length - 25,$title_length)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$file/mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </span>
			&#160;
            <!-- File size always comes in bytes and thus needs conversion --> 
            <span class="bitstream-filesize">(<xsl:choose>
                    <xsl:when test="$file/@SIZE &lt; 1000">
                        <xsl:value-of select="$file/@SIZE"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="$file/@SIZE &lt; 1000000">
                        <xsl:value-of select="substring(string($file/@SIZE div 1000),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="$file/@SIZE &lt; 1000000000">
                        <xsl:value-of select="substring(string($file/@SIZE div 1000000),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="substring(string($file/@SIZE div 1000000000),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
                    </xsl:otherwise>
                </xsl:choose>)
            </span>           
        </div>
    </xsl:template>
	
	
	<!-- Utility function used by the item's summary view the only and primary bitstream of an item -->
    <xsl:template name="buildBitstreamOnePrimary">
        <xsl:param name="context" select="."/>
        <xsl:param name="file" select="."/>
        <div class="slider-bitstreams">
            <span>
                <a class="bitstream-file">
                    <xsl:attribute name="href">
                        <xsl:value-of select="$file/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$file/mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="string-length($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title) > 50">
                            <xsl:variable name="title_length" select="string-length($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title)"/>
                            <xsl:value-of select="substring($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title,1,15)"/>
                            <xsl:text> ... </xsl:text>
                            <xsl:value-of select="substring($file/mets:FLocat[@LOCTYPE='URL']/@xlink:title,$title_length - 25,$title_length)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$file/mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </span>
			&#160;
            <!-- File size always comes in bytes and thus needs conversion --> 
            <span class="bitstream-filesize">(<xsl:choose>
                    <xsl:when test="$file/@SIZE &lt; 1000">
                        <xsl:value-of select="$file/@SIZE"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="$file/@SIZE &lt; 1000000">
                        <xsl:value-of select="substring(string($file/@SIZE div 1000),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="$file/@SIZE &lt; 1000000000">
                        <xsl:value-of select="substring(string($file/@SIZE div 1000000),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="substring(string($file/@SIZE div 1000000000),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
                    </xsl:otherwise>
                </xsl:choose>)
            </span>           
        </div>
    </xsl:template>
	
	
    <!-- TODO: doesn't match pattern of 1.4.  Are we ok? -->
    <!-- We resolve the reference tag to an external mets object --> 
    <xsl:template name="itemSummaryList-DIM">
		<xsl:param name="position"/>
	
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            <xsl:text>?sections=dmdSec,fileSec&amp;fileGrpTypes=THUMBNAIL</xsl:text>
        </xsl:variable>
		
        <xsl:variable name="item_list_position">
            <xsl:value-of select="$position"></xsl:value-of>
        </xsl:variable>
            <div>
				<xsl:choose>
					<xsl:when test="@LABEL='DSpace Item'">
						<xsl:attribute name="class">
							<xsl:text>ds-artifact-item-with-popup </xsl:text>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class">
							<xsl:text>ds-artifact-item </xsl:text>
						</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				
                <!-- Generate the info about the item from the metadata section -->
				<xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
					mode="itemSummaryList-DIM"/>
				<!-- Generate the thunbnail, if present, from the file section -->
		        <xsl:apply-templates select="./mets:fileSec" mode="artifact-preview"/>
		        
				
                <xsl:if test="@LABEL='DSpace Item'">
                    <!--
					<div class="item_metadata_more" id="item_details{$item_list_position}">
						<div class="item_more_text">[more]</div><div class="item_less_text" style="display: none">[less]</div>
					</div>
					
					<div class="item_metadata_slider hidden" id="item_slider{$item_list_position}">
						<xsl:apply-templates select="." mode="metadataPopup"/>
                    </div>
                    -->
                    <div class="item_metadata_more">
                        <div class="item_more_text">[more]</div><div class="item_less_text" style="display: none">[less]</div>
                    </div>
                    
                    <div class="item_metadata_slider hidden">
                        <xsl:apply-templates select="." mode="metadataPopup"/>
                    </div>
                    
                </xsl:if>
                                
            </div>
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
    
    <!-- Create a citation block by cobbling together pieces of metadata into an APA style reference -->
    <xsl:template name="makeCitation">
            <span class="author">
                <xsl:choose>
                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                <xsl:value-of select="node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='creator']">
                            <xsl:for-each select="dim:field[@element='creator']">
                                <xsl:value-of select="node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='contributor']">
                            <xsl:for-each select="dim:field[@element='contributor']">
                                <xsl:value-of select="node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                  </xsl:choose>
            </span>
            <xsl:choose>
                <xsl:when test="dim:field[@element='date'][@qualifier='created']">
                    <span class="date"> (<xsl:value-of select="substring(dim:field[@element='date'][@qualifier='created'][1]/child::node(),1,4)"/>). </span>
                </xsl:when>
                <xsl:when test="dim:field[@element='date'][@qualifier='issued']">
                    <span class="date"> (<xsl:value-of select="substring(dim:field[@element='date'][@qualifier='issued'][1]/child::node(),1,4)"/>). </span>
                </xsl:when>
            </xsl:choose>
            <span class="title">
                <xsl:choose>
                    <xsl:when test="dim:field[@element='title']">
                        <xsl:copy-of select="dim:field[@element='title'][1]/child::node()"/>
                        <xsl:text>. </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        <xsl:text>. </xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </span>
            <span class="degree">
                <xsl:choose>
                    <xsl:when test="dim:field[@element = 'degree'][@qualifier = 'level']='Doctoral'">
                        <xsl:text>Doctoral dissertation, </xsl:text>  
                    </xsl:when>
                    <xsl:when test="dim:field[@element = 'degree'][@qualifier = 'level']='Masters'">
                        <xsl:text>Master's thesis, </xsl:text>  
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </span>
            <xsl:if test="dim:field[@element = 'degree'][@qualifier = 'grantor']">
              <span class="grantor">
                <xsl:copy-of select="dim:field[@element = 'degree'][@qualifier = 'grantor']/child::node()"/>
                <xsl:text>. </xsl:text>
              </span>
            </xsl:if>
            <xsl:if test="dim:field[@element = 'publisher']">
                <span class="publisher">
                    <xsl:for-each select="dim:field[@element = 'publisher']">
                        <xsl:value-of select="node()"/>
                        <xsl:choose>
                            <xsl:when test="count(following-sibling::dim:field[@element='publisher']) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>.  </xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </span>
            </xsl:if>

            <xsl:if test="dim:field[@element='identifier'][@qualifier='uri'][1]/child::node()">
                <xsl:text>Available electronically from </xsl:text>
                <span class="citation-link">
                	<xsl:element name="a">
                        <xsl:attribute name="href">
                            <xsl:value-of select="dim:field[@element='identifier'][@qualifier='uri'][1]/child::node()"/>
                        </xsl:attribute>
                        <!--<xsl:copy-of select="dim:field[@element='identifier'][@qualifier='uri'][1]/child::node()"/>-->
                        <xsl:call-template name="wrapHack">
                            <xsl:with-param name="string"><xsl:value-of select="dim:field[@element='identifier'][@qualifier='uri'][1]/child::node()"/></xsl:with-param>
                        </xsl:call-template>
                    </xsl:element>.
                </span>
            </xsl:if>
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
    
    
    <!-- Generate the thunbnail (for the sliding item surrogate), if present, from the file section -->
    <xsl:template match="mets:fileGrp[@USE='THUMBNAIL']" mode="itemMetadataPopup-DIM">
        <div class="popup-artifact-preview">
            <!-- manakin-voss version: <a href="{ancestor::mets:METS/@OBJID}"> -->
			<a href="{ancestor::dri:object/@url}"> 
                <img alt="Thumbnail">
                    <xsl:attribute name="src">
                        <xsl:value-of select="mets:file/mets:FLocat[@LOCTYPE='URL']/@xlink:href" />
                    </xsl:attribute>
                </img>
            </a>
        </div>
    </xsl:template>
	
    <!-- Generate the thunbnail, if present, from the file section -->
    <xsl:template match="mets:fileGrp[@USE='THUMBNAIL']">
        <div class="artifact-preview">
            <!-- manakin-voss version: <a href="{ancestor::mets:METS/@OBJID}"> -->
            <a href="{ancestor::dri:object/@url}">
                <img alt="Thumbnail">
                    <xsl:attribute name="src">
                        <xsl:value-of select="mets:file/mets:FLocat[@LOCTYPE='URL']/@xlink:href" />
                    </xsl:attribute>
                </img>
            </a>
        </div>
    </xsl:template>
    

    <xsl:template match="dri:div[@n ='search-controls-gear']">
        <xsl:param name="position"/>
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class"><xsl:value-of select="$position"/></xsl:with-param>
            </xsl:call-template>

            <xsl:apply-templates/>
        </div>
    </xsl:template>

    
	
	<!-- The last thing in the structural elements section are the templates to cover the attribute calls. 
        Although, by default, XSL only parses elements and text, an explicit call to apply the attributes
        of children tags can still be made. This, in turn, requires templates that handle specific attributes,
        like the kind you see below. The chief amongst them is the pagination attribute contained by divs, 
        which creates a new div element to display pagination information. -->    
    <xsl:template match="@pagination">
        <xsl:param name="position"/>
        <!-- in case of only one page of results, we'll not give pagination -->
        <xsl:if test="not(parent::node()/@pagesTotal = 1)">
            <xsl:choose>
                <xsl:when test=". = 'simple'">
                    <div class="pagination {$position}">
                        <xsl:if test="parent::node()/@previousPage">
                            <a class="previous-page-link">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="parent::node()/@previousPage"/>
                                </xsl:attribute>
                                &#8592;&#160;<i18n:text>xmlui.dri2xhtml.structural.pagination-previous</i18n:text>
                            </a>
                        </xsl:if>
                        <p class="pagination-info">
                            <xsl:if test="not(parent::node()/@previousPage)">
                                <xsl:attribute name="style">left: 210px;</xsl:attribute>
                            </xsl:if>
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
                                </xsl:attribute>
                                <i18n:text>xmlui.dri2xhtml.structural.pagination-next</i18n:text>&#160;&#8594;
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
                                </xsl:attribute>
                                &#8592;&#160;<i18n:text>xmlui.dri2xhtml.structural.pagination-previous</i18n:text>
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
                            <xsl:if test="parent::node()/@firstItemIndex = 0 or parent::node()/@firstItemIndex = 1">
                                <xsl:attribute name="style">left: 265px;</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="(parent::node()/@currentPage - 4) &gt; 0">
                                <li class="first-page-link">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="substring-before(parent::node()/@pageURLMask,'{pageNum}')"/>
                                            <xsl:text>1</xsl:text>
                                            <xsl:value-of select="substring-after(parent::node()/@pageURLMask,'{pageNum}')"/>
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
                                </xsl:attribute>
                                <i18n:text>xmlui.dri2xhtml.structural.pagination-next</i18n:text>&#160;&#8594;
                            </a>
                        </xsl:if>


                    </div>
                </xsl:when>            
            </xsl:choose>
        </xsl:if>

        <xsl:if test="parent::node()/dri:div[@n = 'masked-page-control']">
            <xsl:apply-templates select="parent::node()/dri:div[@n='masked-page-control']/dri:div">
                    <xsl:with-param name="position" select="$position"/>
            </xsl:apply-templates>
        </xsl:if>

    </xsl:template>

    <xsl:template match="dri:div[@n ='search-controls-gear']">
    	<xsl:param name="position"/>
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class"><xsl:value-of select="$position"/></xsl:with-param>
            </xsl:call-template>

            <xsl:apply-templates/>
        </div>
    </xsl:template>
    
    
    <!--  *** Add the RSS feeds to collection and community home page -->
    <xsl:template match="dri:div[@n='collection-home'] | dri:div[@n='community-home']">
        <xsl:apply-imports/>
        <xsl:variable name="pageMeta" select="/dri:document/dri:meta/dri:pageMeta"/>
        <p class="feedbox">
            <xsl:for-each select="$pageMeta/dri:metadata[@element='feed']">
                <a href="{.}" class="feedboxlink">
                    <span>
                        <xsl:attribute name="class">
                            <xsl:if test="contains(@qualifier,'rss')"> rsslink</xsl:if>
                            <xsl:if test="contains(@qualifier,'atom')"> atomlink</xsl:if>                            
                        </xsl:attribute>
                        <xsl:value-of select="translate(substring-before(substring-after(., 'feed/'), '/'), 'rssa_', 'RSSA ')"/>
                    </span>
                </a>
            </xsl:for-each>
        </p>
    </xsl:template>    
    
    <!-- ***** The following templates are overridden to allow for the expansion/collapse of communities/collections on the community-list. ***** -->
    
    <!-- Non-interactive divs get turned into HTML div tags.  This template excludes the flat community list in 
        favor of the expanding/collapsing community list on the community view page -->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-view']" priority="2">
        <xsl:apply-templates select="dri:head"/>
        <xsl:apply-templates select="@pagination">
            <xsl:with-param name="position">top</xsl:with-param>
        </xsl:apply-templates>
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-static-div</xsl:with-param>
            </xsl:call-template>
            <xsl:choose>
                <!--  does this element have any children -->
                <xsl:when test="child::node()">
                    <xsl:apply-templates select="*[not(name()='head')][not(@id='aspect.artifactbrowser.CommunityViewer.referenceSet.community-view')]"/>
                    <!-- <xsl:apply-templates select="*[not(name()='head')]" mode='communityTreeExcludesOtherSummaryLists'/> -->
                        
                </xsl:when>
                <!-- if no children are found we add a space to eliminate self closing tags -->
                <xsl:otherwise>
                    &#160;
                </xsl:otherwise>
            </xsl:choose>
        </div>
        <xsl:apply-templates select="@pagination">
            <xsl:with-param name="position">bottom</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    
    <!-- New mode for the detailView used by the expanding/collapsing community hierarchy in community browse.
        Finally, we have the detailed view case that is applicable to items, communities and collections.
        In DRI it constitutes a standard view of collections/communities and a complete metadata listing
        view of items. -->
    <!--
    <xsl:template match="dri:referenceSet[@type = 'detailView']" priority="2" mode="communityTreeExcludesOtherSummaryLists">
        <xsl:apply-templates select="dri:head"/>        
        <xsl:apply-templates select="*[not(name()='head')]" mode="communityTreeExcludesOtherSummaryListsReferences"/>
    </xsl:template>
    -->
    
    <!-- New mode for the detailView used by the expanding/collapsing community hierarchy in community browse. -->
    <!-- 
    <xsl:template match="dri:reference" mode="communityTreeExcludesOtherSummaryListsReferences">
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            
        </xsl:variable>
        <xsl:text> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:text>
        <li>
            <xsl:apply-templates select="document($externalMetadataURL)" mode="detailList"/>
            <xsl:apply-templates/>
        </li>
        </xsl:template>
    -->
    
    
    
    
    

    <!-- Several templates are required to grab only the referenceSet[type='summaryList'] under the dri:reference under the referenceSet[type='detailView'][id='org.tdl.dspace.communityview.ExpandingCollapsingBrowser.referenceSet.community-browser'] found on a community or collection page
        start with the referenceSet[type='detailView'][id='org.tdl.dspace.communityview.ExpandingCollapsingBrowser.referenceSet.community-browser'] and apply a particular mode. -->
    <!-- Here, adapted from structural.xsl is the detailed view case that is applicable to items, communities and collections.
        In DRI it constitutes a standard view of collections/communities and a complete metadata listing
        view of items (see the next template to see what happens to the dri:reference)... -->
    <xsl:template match="dri:referenceSet[@type = 'detailView'][@id='org.tdl.dspace.communityview.ExpandingCollapsingBrowser.referenceSet.community-browser']" priority="3">
        <xsl:apply-templates select="dri:head"/>
        <xsl:apply-templates select="*[not(name()='head')]" mode="commCollDetailView"/>
    </xsl:template>    
    <!-- ... next comes the reference (see next template to see what happens to the dri:referenceSet underneath) ... -->
    <xsl:template match="dri:reference" mode='commCollDetailView'>
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            <!-- No options selected, render the full METS document -->
        </xsl:variable>
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
        <xsl:apply-templates select="document($externalMetadataURL)" mode="detailView"/>
        <xsl:apply-templates mode="commCollDetailView"/>
    </xsl:template>    
    <!-- ... and we conclude with the dri:referenceSet we were after all along. -->
    <xsl:template match="dri:referenceSet[@type = 'summaryList']" priority="2" mode="commCollDetailView">
        <xsl:apply-templates select="dri:head"/>
        <p id='expand_all_clicker' style="display: none">Expand All</p>
        <p id='collapse_all_clicker' style="display: none">Collapse All</p>
        <!-- Here we decide whether we have a hierarchical list or a flat one -->
        <xsl:choose>
            <xsl:when test="descendant-or-self::dri:referenceSet/@rend='hierarchy' or ancestor::dri:referenceSet/@rend='hierarchy'">
                <ul>
                    <xsl:apply-templates select="*[not(name()='head')]" mode="summaryList"/>
                </ul>
            </xsl:when>
            <xsl:otherwise>
                <ul class="ds-artifact-list">
                    <xsl:apply-templates select="*[not(name()='head')]" mode="summaryList"/>
                </ul>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>
    
    
    
    <!-- 
        We had to overwrite this template in order to prevent a float-left from occurring on the results readout of a curation task that has line-breaks.
        
        According to structural xsl: This does it for all the DRI elements. The only thing left to do is to handle Cocoon's i18n
        transformer tags that are used for text translation. The templates below simply push through
        the i18n elements so that they can translated after the XSL step. -->   
    <xsl:template match="i18n:text">
        <xsl:param name="text" select="."/>
        <xsl:choose>
            <xsl:when test="contains($text, '&#xa;')">
                <xsl:value-of select="substring-before($text, '&#xa;')"/>
                <ul>
                    <xsl:if test="not(ancestor::*/@id='aspect.general.NoticeTransformer.div.general-message')">
                        <xsl:attribute name="style">float:left; list-style-type:none; text-align:left;</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="linebreak">
                        <xsl:with-param name="text" select="substring-after($text,'&#xa;')"/>
                    </xsl:call-template>
                </ul>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>



    
    <!--- SUMMARY LISTS FOLLOW ************** -->
    <!--- ********************************** -->
    
    <!-- grabs only the summaryList found on the community/collection overview page -->
    <xsl:template match="dri:referenceSet[@type = 'summaryList'][@id='aspect.artifactbrowser.CommunityBrowser.referenceSet.community-browser']" priority="2">
        <xsl:apply-templates select="dri:head"/>
        <p id='expand_all_clicker' style="display: none">Expand All</p>
        <p id='collapse_all_clicker' style="display: none">Collapse All</p>
        <ul>
            <xsl:apply-templates select="*[not(name()='head')]" mode="summaryList"/>
        </ul>
    </xsl:template>


    


    
    <!-- A community rendered in the summaryList pattern. Encountered on the community-list and on 
        on the front page. -->
    <xsl:template name="communitySummaryList-DIM">
        <xsl:variable name="data" select="./mets:dmdSec/mets:mdWrap/mets:xmlData/dim:dim"/>
        <p class="ListPlus" style="display:none">[+]</p>
        <p class="ListMinus">[-]</p>
        <span class="bold">
            <a href="{@OBJID}" class="communitySummaryListAnchorDIM">
	            <xsl:choose>
		            <xsl:when test="string-length($data/dim:field[@element='title'][1]) &gt; 0">
		                <xsl:value-of select="$data/dim:field[@element='title'][1]"/>
		            </xsl:when>
		            <xsl:otherwise>
		                <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
		            </xsl:otherwise>
	            </xsl:choose>
	            <xsl:if test="$data/dim:field[@element='format'][@qualifier='extent']">
                	[<xsl:value-of select="$data/dim:field[@element='format'][@qualifier='extent'][1]"/>]
                </xsl:if>
            </a>
        </span>
    </xsl:template>
                                
                                
    <!-- The modified list on the user's profile -->
    <xsl:template match="dri:list[@id='aspect.eperson.EditProfile.list.memberships']">
        <xsl:apply-templates select="dri:item" mode="profile">
            <xsl:sort select="text()"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="dri:list[@id='aspect.administrative.eperson.EditEPersonForm.list.eperson-member-of']">
        <xsl:apply-templates select="dri:item" mode="eperson">
            <xsl:sort select="dri:xref/text()"/>
        </xsl:apply-templates>
    </xsl:template>
     
    <xsl:template match="dri:item" mode="profile">
        <li>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-simple-list-item</xsl:with-param>
            </xsl:call-template>
            <xsl:apply-templates />
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element=current()/text()]">
                <xsl:text> (</xsl:text>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                        <xsl:text>/handle/</xsl:text>
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element=current()/text()]"/>
                    </xsl:attribute>
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element=current()/text()]/@qualifier"/>
                </a>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </li>
    </xsl:template>
                                
    <xsl:template match="dri:item" mode="eperson">
        <li>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-simple-list-item</xsl:with-param>
            </xsl:call-template>
            <xsl:apply-templates />
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element=current()/dri:xref/text()]">
                <xsl:text> (</xsl:text>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                        <xsl:text>/handle/</xsl:text>
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element=current()/dri:xref/text()]"/>
                    </xsl:attribute>
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element=current()/dri:xref/text()]/@qualifier"/>
                </a>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </li>
    </xsl:template>
                                
                                
    
<!-- ****************************************************************************** -->
<!--  Former candidates to put in to structural.xsl (they were passed over) follow: -->
<!-- ****************************************************************************** -->

		
    <!-- 
        
        The trail is built one link at a time. Each link is given the ds-trail-link class, with the first and
        the last links given an additional descriptor. 
        
        In case there are more than 6 nodes (which will show up as 4 here) show only 6.
        
        In that case,
        If node number 1
        then show it and an arrow and a dot dot dot
        Otherwise,
        If node number less than number_of_trail_items - 2 
        then do not show
        Otherwise,
        show it
    -->     
    <xsl:template match="dri:trail">
        <xsl:param name="number_of_trail_items"/>        
        
        <xsl:choose>
            <xsl:when test="$number_of_trail_items &gt; 4">
                <xsl:choose>                    
                    <xsl:when test="position()=1"> 
                        <xsl:choose>
                            <xsl:when test="./@target">
                                <a class="trail_anchor">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="./@target"/>
                                    </xsl:attribute>
                                    <xsl:apply-templates />
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates />
                            </xsl:otherwise>
                        </xsl:choose>
                        <span style="color: white; font-size: 110%;">&#8594;</span>
                        . . .
                    </xsl:when>                    
                    <xsl:otherwise>
                        <xsl:choose>                        
                            <xsl:when test="position() &lt; $number_of_trail_items - 2">
                            </xsl:when>
                            <xsl:otherwise>
                                <span style="color: white; font-size: 110%;">&#8594;</span>
                                <xsl:choose>
                                    <xsl:when test="./@target">
                                        <a class="trail_anchor">
                                            <xsl:attribute name="href">
                                                <xsl:value-of select="./@target"/>
                                            </xsl:attribute>
                                            <!-- <xsl:apply-templates /> -->
                                            <xsl:choose>
                                                <xsl:when test="string-length(.) &lt; 40">
                                                    <xsl:value-of select="." />    
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="substring(., 1, 37)" />...
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:apply-templates />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                            
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
                
            </xsl:when>
            <xsl:otherwise>
                <li>
                    <!-- put in a little arrow if this is not the first item in the trail -->
                    <xsl:if test="not(position()=1)">
                        <span style="color: white; font-size: 110%;">&#8594;</span>
                    </xsl:if>
                    <xsl:attribute name="class">
                        <xsl:text>ds-trail-link </xsl:text>
                        <xsl:if test="position()=1">
                            <xsl:text>first-link</xsl:text>
                        </xsl:if>
                        <xsl:if test="position()=last()">
                            <xsl:text>last-link</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <!-- Determine whether we are dealing with a link or plain text trail link -->
                    <xsl:choose>
                        <xsl:when test="./@target">
                            <a class="trail_anchor">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="./@target"/>
                                </xsl:attribute>
                                <xsl:apply-templates />
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates />
                        </xsl:otherwise>
                    </xsl:choose>
                    
                </li>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>  
    
	
	
    <!-- The first (and most complex) case of the header tag is the one used for divisions. Since divisions can 
        nest freely, their headers should reflect that. Thus, the type of HTML h tag produced depends on how
        many divisions the header tag is nested inside of. -->
    <!-- The font-sizing variable is the result of a linear function applied to the character count of the heading text -->
    <xsl:template match="dri:div/dri:head" priority="3">
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
	
	
	<!-- Generate the info about the item from the metadata section (taken from DIM-Handler in order to better format the date)
	    Used to generate the information in the item surrogate in browse lists -->
    <xsl:template match="dim:dim" mode="itemSummaryList-DIM"> 
        <xsl:variable name="date-issued" select="dim:field[@element='date' and @qualifier='created']/node()"/>
        <xsl:variable name="date-created" select="dim:field[@element='date' and @qualifier='created']/node()"/>

        
        <div class="artifact-description">
            <div class="artifact-title">
                <!-- manakin-voss version: <a href="{ancestor::mets:METS/@OBJID}"> -->
				<a href="{ancestor::mets:METS/@OBJID}">
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='title']">
                            <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </div>
            <div class="artifact-info">
                <span class="author">
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                <xsl:copy-of select="./node()"/>
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
                </span>
                <xsl:text> </xsl:text>
                <span class="publisher-date">
                    <xsl:text>(</xsl:text>
                    <xsl:if test="dim:field[@element='publisher']">
                        <span class="publisher">
                            <xsl:for-each select="dim:field[@element='publisher']/node()">
                                <xsl:value-of select="."/>
                                <xsl:if test="position() != last()">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>                            
                        </span>
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                    <span class="date">
                        <!-- Prefer dc.date.created, but fall back on dc.date.issued -->
                        <xsl:choose>
                            <xsl:when test="dim:field[@element='date' and @qualifier='created']/node()">
                                <xsl:call-template name="month-name">
                                    <xsl:with-param name="date-time" select = "dim:field[@element='date' and @qualifier='created']/node()"/>					
                                </xsl:call-template>
                                <xsl:text> </xsl:text>
                                
                                <!-- display the day of the month, but only if it is available -->			
                                <xsl:variable name="date-day">
                                    <xsl:call-template name="day-in-month">
                                        <xsl:with-param name="date-time" select = "dim:field[@element='date' and @qualifier='created']/node()"/>
                                    </xsl:call-template>
                                    <xsl:text>, </xsl:text>
                                </xsl:variable>
                                <xsl:if test="$date-day != 'NaN, '">
                                    <xsl:value-of select="$date-day"/>
                                </xsl:if>
                                
                                <xsl:call-template name="year">
                                    <xsl:with-param name="date-time" select = "dim:field[@element='date' and @qualifier='created']/node()"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="month-name">
                                    <xsl:with-param name="date-time" select = "dim:field[@element='date' and @qualifier='issued']/node()"/>					
                                </xsl:call-template>
                                <xsl:text> </xsl:text>
                                
                                <!-- display the day of the month, but only if it is available -->			
                                <xsl:variable name="date-day">
                                    <xsl:call-template name="day-in-month">
                                        <xsl:with-param name="date-time" select = "dim:field[@element='date' and @qualifier='issued']/node()"/>
                                    </xsl:call-template>
                                    <xsl:text>, </xsl:text>
                                </xsl:variable>
                                <xsl:if test="$date-day != 'NaN, '">
                                    <xsl:value-of select="$date-day"/>
                                </xsl:if>
                                
                                <xsl:call-template name="year">
                                    <xsl:with-param name="date-time" select = "dim:field[@element='date' and @qualifier='issued']/node()"/>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>	
                    </span>
                    <xsl:text>)</xsl:text>
                </span>
            </div>
        </div>
    </xsl:template>
	
	
    <!-- The handling of component fields, that is fields that are part of a composite field type --> 
    <xsl:template match="dri:field" mode="compositeComponent">
        <xsl:choose>
            <xsl:when test="@type = 'checkbox'  or @type='radio'">
                <xsl:apply-templates select="." mode="normalField"/>
                <br/>
                <xsl:apply-templates select="dri:label" mode="compositeComponent"/>
            </xsl:when>		
            <xsl:otherwise>
                <label class="ds-composite-component">
                    <xsl:if test="position()=last()">
                        <xsl:attribute name="class">ds-composite-component last</xsl:attribute>
                    </xsl:if>
                    <xsl:apply-templates select="." mode="normalField"/>
                    <br/>
                    <xsl:apply-templates select="dri:label" mode="compositeComponent"/>
                </label>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template match="dri:help" mode="help">
        <span class="field-help">
            <xsl:apply-templates />&#160;            
        </span>
    </xsl:template>

    <!-- wrapHack 2.0: harder, better, faster(slower) -->
    <xsl:template name="wrapHack">
        <xsl:param name="string"/>
        <xsl:param name="delims" select="'@#$%&amp;:;()-_.,/\~&gt;&lt;?'"/>
        
        <!-- Does our string contain any of the target delimiters? -->
        <xsl:choose>
            <!-- No, so no point in splitting it further -->
            <xsl:when test="translate($string,$delims,'')=$string">
                <xsl:value-of select="$string"/>
            </xsl:when>
            <!-- Yes, check the length -->
            <xsl:otherwise>
                <xsl:variable name="length" select="string-length($string)"/>
                <!-- Is it of length 1? -->
                <xsl:choose>
                    <!-- Yes, we're down to the last character and it's one of the targets: prepend the breaker -->
                    <xsl:when test="$length=1">
                        <span style="font-size: 0em;"><xsl:text> </xsl:text></span>
                        <xsl:value-of select="$string"/>
                    </xsl:when>
                    <!-- No, divide and conquer -->
                    <xsl:otherwise>
                        <!-- First half -->
                        <xsl:call-template name="wrapHack">
                            <xsl:with-param name="string" select="substring($string,1,floor($length div 2))"/>
                            <xsl:with-param name="delims" select="$delims"/>
                        </xsl:call-template>
                        <!-- Second half -->
                        <xsl:call-template name="wrapHack">
                            <xsl:with-param name="string" select="substring($string,1+floor($length div 2))"/>
                            <xsl:with-param name="delims" select="$delims"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
     
     
     
     <!-- Display the loggen chooser page which asks the user to login via shibboleth or via an email/password pair -->
	 <xsl:template match="dri:div[@id='aspect.eperson.LoginChooser.div.login-chooser']">
	    <xsl:apply-templates select="./dri:head"/>
		<div id="aspect_eperson_LoginChooser_div_login-chooser" class="ds-static-div">

	 		<xsl:for-each select="dri:list/dri:item">
		 		<xsl:choose>
		 		
		 			<!-- Shibboleth login -->
		 			<xsl:when test="contains(dri:xref/@target,'shibboleth-login')">
		 				<div id="shibboleth-authentication-method" class="authentication-method">
		 					<h2>For A&amp;M Students, Faculty, and Staff:</h2>
 							<p class="authentication-item"><a>
 								<xsl:attribute name="href"><xsl:value-of select="dri:xref/@target"/></xsl:attribute>
 								Login with university NetID
 							</a></p>
 						</div>
		 			</xsl:when>

					<!-- Password login -->
		 			<xsl:when test="contains(dri:xref/@target,'password-login')">
		 				<div id="password-authentication-method" class="authentication-method">
		 					<h2>For those not affiliated with A&amp;M:</h2>
		 					<p class="authentication-item"><a>
		 						<xsl:attribute name="href"><xsl:value-of select="dri:xref/@target"/></xsl:attribute>
		 						Login with email address
		 					</a></p>
		 					<p class="authentication-item"><a href="#">
		 						<xsl:attribute name="href"><xsl:value-of select="//dri:xref/@target[contains(.,'register')]"/></xsl:attribute>
		 						Register new account
		 					</a></p>
		 				</div>
		 			</xsl:when>

					<!-- Some other login that is not special cased -->
		 			<xsl:otherwise>
		 				<p class="authentication-item"><xsl:apply-templates/></p>
		 			</xsl:otherwise>

		 		</xsl:choose>
		 	</xsl:for-each>

	 	</div>
     </xsl:template>
     
     
     
     <!-- Front page recent submission handling  -->
     
     <xsl:template match="dri:referenceSet" mode="frontPageRecent">
        <xsl:apply-templates mode="frontPageRecent"/>
    </xsl:template>
    
    
    <xsl:template match="dri:reference" mode='frontPageRecent'>
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            <!-- No options selected, render the full METS document -->
        </xsl:variable>
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
        <xsl:apply-templates select="document($externalMetadataURL)" mode="frontPageRecent"/>
    </xsl:template>
    
    
    
    <xsl:template match="dim:dim" mode="frontPageRecent">
        <xsl:variable name="context" select="ancestor::mets:METS"/>
        <xsl:variable name="data" select="./mets:METS/mets:dmdSec/mets:mdWrap/mets:xmlData/dim:dim"/>
        <div class="frontPageRecent">
            <div class="dateTitle">
                <span class="dateAccessioned">
                    [<xsl:value-of select="substring(dim:field[@element='date' and @qualifier='accessioned'], 0, 11)"/>]
                </span>
                <span class="artifact-title">
                    <!-- manakin-voss version: <a href="{ancestor::mets:METS/@OBJID}"> -->
                    <a href="{ancestor::mets:METS/@OBJID}">
                        <xsl:choose>
                            <xsl:when test="dim:field[@element='title']">
                                <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </a>
                </span>
            </div>
            <div class="authorDate">
                <span class="authors">
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                <xsl:copy-of select="./node()"/>
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
                </span>
                <span class="dateIssued">
                    (<xsl:value-of select="substring(dim:field[@element='date' and @qualifier='issued'], 0, 11)"/>)
                </span>
            </div>
            <xsl:variable name="primary" select="$context/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:div[@TYPE='DSpace Content Bitstream']/mets:fptr/@FILEID" />
            <xsl:call-template name="buildBitstreamOnePrimary">
                <xsl:with-param name="context" select="$context"/>
                <xsl:with-param name="file" select="$context/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@ID=$primary]"/>
            </xsl:call-template>                
        </div>
    </xsl:template>
     
     
     
    <!-- Metadata Tree Browser  -->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionRecentSubmissions.div.collection-recent-submission' and //dri:div[@id='edu.tamu.metadatatreebrowser.BrowseOverview.div.metadata-tree-browser-overview']]" priority="100">
     	<!-- don't display the recent submission when the metadata tree browser is being used for collections. -->
    </xsl:template>
     
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityRecentSubmissions.div.community-recent-submission' and //dri:div[@id='edu.tamu.metadatatreebrowser.BrowseOverview.div.metadata-tree-browser-overview']]" priority="100">
     	<!-- don't display the recent submission when the metadata tree browser is being used for communities. -->
    </xsl:template>
     
    <!-- Generate the license information from the file section -->
    <xsl:template match="mets:fileGrp[@USE='CC-LICENSE' or @USE='LICENSE']">
        <div class="license-info">
            <p><i18n:text>xmlui.dri2xhtml.METS-1.0.license-text</i18n:text></p>
            <ul>
                <xsl:if test="@USE='CC-LICENSE'">
                    <li><a href="{mets:file/mets:FLocat[@xlink:title='license_rdf']/@xlink:href}"><i18n:text>xmlui.dri2xhtml.structural.link_cc</i18n:text></a></li>
                </xsl:if>
                <xsl:if test="@USE='LICENSE'">
                    <li><a href="{mets:file/mets:FLocat[@xlink:title='license.txt']/@xlink:href}"><i18n:text>xmlui.dri2xhtml.structural.link_original_license</i18n:text></a></li>
                </xsl:if>
            </ul>
        </div>
    </xsl:template> 

   <xsl:template name="buildChildThemeCSS">
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

	<xsl:template name="addJavascript">
        <xsl:variable name="jqueryVersion">
            <xsl:text>1.11.1</xsl:text>
        </xsl:variable>
        <xsl:variable name="jqueryUIVersion">
            <xsl:text>1.10.4</xsl:text>
        </xsl:variable>

        <xsl:variable name="protocol">
            <xsl:choose>
                <xsl:when test="starts-with(confman:getProperty('dspace.baseUrl'), 'https://')">
                    <xsl:text>https://</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>http://</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <script type="text/javascript" src="{concat($protocol, 'ajax.googleapis.com/ajax/libs/jquery/', $jqueryVersion ,'/jquery.min.js')}">&#160;</script>
        <script type="text/javascript" src="{concat($protocol, 'ajax.googleapis.com/ajax/libs/jqueryui/', $jqueryUIVersion ,'/jquery-ui.min.js')}">&#160;</script>
		

		
        <!-- Add theme javascipt  -->
        <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][@qualifier='url']">
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of select="."/>
                </xsl:attribute>&#160;</script>
        </xsl:for-each>

        <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][not(@qualifier)]">
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="."/>
                </xsl:attribute>&#160;</script>
        </xsl:for-each>
        

        <!-- add "shared" javascript from static, path is relative to webapp root -->
        <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][@qualifier='static']">
            <!--This is a dirty way of keeping the scriptaculous stuff from choice-support
            out of our theme without modifying the administrative and submission sitemaps.
            This is obviously not ideal, but adding those scripts in those sitemaps is far
            from ideal as well-->
            <xsl:choose>
                <xsl:when test="text() = 'static/js/choice-support.js'">
                    <script type="text/javascript">
                        <xsl:attribute name="src">
                            <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/themes/</xsl:text>
                            <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                            <xsl:text>/lib/js/choice-support.js</xsl:text>
                        </xsl:attribute>&#160;</script>
                </xsl:when>
                <xsl:when test="not(starts-with(text(), 'static/js/scriptaculous'))">
                    <script type="text/javascript">
                        <xsl:attribute name="src">
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/</xsl:text>
                            <xsl:value-of select="."/>
                        </xsl:attribute>&#160;</script>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>

        <!-- add setup JS code if this is a choices lookup page -->
        <xsl:if test="dri:body/dri:div[@n='lookup']">
          <xsl:call-template name="choiceLookupPopUpSetup"/>
        </xsl:if>

    
	    <!-- Add a google analytics script if the key is present -->
        <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']">
            <script type="text/javascript"><xsl:text>
                   var _gaq = _gaq || [];
                   _gaq.push(['_setAccount', '</xsl:text><xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']"/><xsl:text>']);
                   _gaq.push(['_trackPageview']);

                   (function() {
                       var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                       ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                       var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
                   })();
           </xsl:text></script>
        </xsl:if>

        
        
        
        
        
        <!-- The following javascript removes the default text of empty text areas when they are focused on or submitted -->
            <!-- There is also javascript to disable submitting a form when the 'enter' key is pressed. -->
            <script type="text/javascript">
                //Clear default text of emty text areas on focus
                function tFocus(element)
                {
                if (element.value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){element.value='';}
                }
                //Clear default text of emty text areas on submit
                function tSubmit(form)
                {
                var defaultedElements = document.getElementsByTagName("textarea");
                for (var i=0; i != defaultedElements.length; i++){
                if (defaultedElements[i].value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){
                defaultedElements[i].value='';}}
                }
                //Disable pressing 'enter' key to submit a form (otherwise pressing 'enter' causes a submission to start over)
                function disableEnterKey(e)
                {
                var key;
                
                if(window.event)
                key = window.event.keyCode;     //Internet Explorer
                else
                key = e.which;     //Firefox and Netscape
                
                if(key == 13)  //if "Enter" pressed, then disable!
                return false;
                else
                return true;
                }
            </script>
            
 
            
            <!-- BREADCRUMB FIX UNTIL OAKTRUST IS BEHIND SHIBBOLETH -->
            <script type="text/javascript">          	
            		$(document).ready(function() {
            		console.log(document.location.hostname);
            		if(document.location.hostname.toLowerCase().indexOf("repository.tamu.edu") != -1) {
	            		$("#breadcrumbs .first-link a").attr("href", "https://repository.library.tamu.edu/community-list");
					}    
            	});
           	</script>


            
            <!-- BREADCRUMB FIX UNTIL OAKTRUST IS BEHIND SHIBBOLETH -->
            <script type="text/javascript">          	
            		$(document).ready(function() {
            		console.log(document.location.hostname);
            		if(document.location.hostname.toLowerCase().indexOf("repository.tamu.edu") != -1) {
	            		$("#breadcrumbs .first-link a").attr("href", "https://repository.tamu.edu/community-list");
					}    
            	});
           	</script>
        
        
        
        
    </xsl:template>




     
        
</xsl:stylesheet>
