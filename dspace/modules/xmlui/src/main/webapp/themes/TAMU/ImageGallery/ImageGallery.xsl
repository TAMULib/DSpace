<?xml version="1.0" encoding="UTF-8"?>

<!--
    NewImageGallery.xsl
    
    Version: $Revision: 1.0 $
    
    Date: $Date: 2006/07/27 22:54:52 $    
-->

<!--
    Author: Adam Mikeal <adam@mikeal.org>
    		Alexey Maslov <alexey@library.tamu.edu>
-->    

<xsl:stylesheet 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim" 
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:url="http://www.jclark.com/xt/java/java.net.URLEncoder"
    exclude-result-prefixes="url">
    
    <xsl:import href="../TAMU.xsl"/>
    <xsl:output indent="yes"/>
    
    <!-- Global variable to get the repository URL -->
    <xsl:variable name="repository-url">
        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request' and @qualifier='scheme'][1]/node()" />
        <xsl:text>://</xsl:text>
        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request' and @qualifier='serverName'][1]/node()" />
        <xsl:text>:</xsl:text>
        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request' and @qualifier='serverPort'][1]/node()" />
    </xsl:variable>
    
    <!-- Global variable to get the URL to the Djatoka image server from the metadata -->
    <xsl:variable name="image-server-url" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme' and @qualifier='image-server'][1]/node()"/>
    
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
	
	
	
	<!-- Override the template that decides what to do with the list of items
		  (we want to change the <ul> that the TAMU theme normally generates
		   into a collection of <div>s) -->
	<xsl:template match="dri:referenceSet[@type = 'summaryList']" priority="2">
        <xsl:apply-templates select="dri:head"/>
        <!-- Here we decide whether we have a hierarchical list or a flat one -->
        <xsl:choose>
        	<!--  This case is only for the hierarchical lists of collections on the item detail pages -->
            <xsl:when test="descendant-or-self::dri:referenceSet/@rend='hierarchy' or ancestor::dri:referenceSet/@rend='hierarchy'">
                <ul>
                    <xsl:apply-templates select="*[not(name()='head')]" mode="summaryList"/>
                </ul>
            </xsl:when>
            
            <!--  Our main case: lists of items that appear in browse pages -->
            <xsl:otherwise>
                <div class="image-gallery-tile-set">
                    <xsl:apply-templates select="*[not(name()='head')]" mode="summaryList"/>
                </div>
                <div class="clear">&#160;</div>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>

	
	
	<!-- Override the template that generates elements for each item reference in the list --> 
    <xsl:template match="dri:reference" mode="summaryList">
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
        </xsl:variable>
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
        <div class="image-gallery-tile">
            <xsl:apply-templates select="document($externalMetadataURL)" mode="summaryList"/>
            <xsl:apply-templates />
        </div>
    </xsl:template>
    
    
    
    
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
        
        <div class="image-gallery-tile-content">
        			
			<!-- Generate the thumbnail and direct file links -->
            <xsl:apply-templates select="." mode="metadataPopup"/>
			
            <!-- Generate the title from the metadata section -->
			<!-- <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim" mode="itemSummaryList-DIM"/> -->
				
			<!-- Generate the thunbnail, if present, from the file section -->
	        <!--  <xsl:apply-templates select="./mets:fileSec" mode="artifact-preview"/>  -->
			                                 
       </div>

    </xsl:template>
        
   
	<!-- Generate the metadata popup text about the item from the metadata section -->
    <xsl:template match="dim:dim" mode="itemMetadataPopup-DIM">
   
        <!-- display bitstreams -->		
		<xsl:variable name="context" select="ancestor::mets:METS"/>
        <xsl:variable name="data" select="./mets:METS/mets:dmdSec/mets:mdWrap/mets:xmlData/dim:dim"/>
        <xsl:variable name="image-width" select="dim:field[@element='resolution' and @qualifier='width'][1]/node()"/>
        <xsl:variable name="image-date" select="dim:field[@element='date' and @qualifier='created'][1]/node()"/>	
        <xsl:variable name="image-title">
        	<xsl:choose>
            	<xsl:when test="dim:field[@element='title']">
                	<xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                </xsl:when>
                <xsl:otherwise><i18n:text>Untitled</i18n:text></xsl:otherwise>
             </xsl:choose>
        </xsl:variable>       
        
        <xsl:apply-templates select="$data" mode="detailView"/>
            <!-- First, figure out if there is a primary bitstream -->
			<xsl:variable name="primary" select="$context/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:div[@TYPE='DSpace Content Bitstream']/mets:fptr/@FILEID" />
			<xsl:variable name="jp2-url" select="$context/mets:fileSec/mets:fileGrp[@USE = 'CONTENT']/mets:file[@MIMETYPE = 'image/jp2'][1]/mets:FLocat/@xlink:href" />
			<xsl:variable name="jp2-size" select="$context/mets:fileSec/mets:fileGrp[@USE = 'CONTENT']/mets:file[@MIMETYPE = 'image/jp2'][1]/@SIZE" />
			<xsl:variable name="thumb-url" select="$context/mets:fileSec/mets:fileGrp[@USE = 'THUMBNAIL']/mets:file[@MIMETYPE = 'image/jpeg'][1]/mets:FLocat/@xlink:href" />
			<xsl:variable name="bitstream-count" select="count($context/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:div[@TYPE='DSpace Content Bitstream'])" />
            
            <a class="image-gallery-anchor" alt="Click to view item">
            	<xsl:attribute name="title">
            		<xsl:value-of select="$image-title" /> 
            		<xsl:if test="$image-date">
            			<xsl:text> (</xsl:text>
            			<xsl:value-of select="$image-date" />
            			<xsl:text>) </xsl:text>
           			</xsl:if>
            	</xsl:attribute>	
                <xsl:attribute name="href">
                    <xsl:value-of select="ancestor::mets:METS/@OBJID"></xsl:value-of>
                </xsl:attribute>
                <!-- 
            	<xsl:attribute name="href">
            		<xsl:value-of select="$image-server-url"/>
            		<xsl:text>resolver?url_ver=Z39.88-2004&amp;rft_id=</xsl:text>
            		<xsl:value-of select="url:encode($repository-url)"/>
            		<xsl:value-of select="url:encode($jp2-url)"/>
            		<xsl:text>&amp;svc_id=info:lanl-repo/svc/getRegion&amp;svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&amp;svc.format=image/jpeg&amp;svc.scale=600</xsl:text>
            	</xsl:attribute>
                -->           	
            	<img class="image-gallery-thumbnail">
            		<xsl:attribute name="src">
            		    <!-- <xsl:value-of select="$thumb-url" /> -->
            			<xsl:choose>
            				<xsl:when test="$thumb-url">
            					<xsl:value-of select="$thumb-url" />
            				</xsl:when>
            				<xsl:otherwise>
		            			<xsl:value-of select="$image-server-url"/>
		            			<xsl:text>resolver?url_ver=Z39.88-2004&amp;rft_id=</xsl:text>
		            			<xsl:value-of select="url:encode($repository-url)"/>
		            			<xsl:value-of select="url:encode($jp2-url)"/>
		            			<xsl:text>&amp;svc_id=info:lanl-repo/svc/getRegion&amp;svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&amp;svc.format=image/jpeg&amp;svc.scale=192</xsl:text>
		            		</xsl:otherwise>
		            	</xsl:choose>
            		</xsl:attribute>
            		<xsl:attribute name="alt">
            			<xsl:value-of select="$image-title"/>
            			<xsl:text> &#160; (click for a larger preview)</xsl:text>
            		</xsl:attribute>
            	</img>
            </a>
            
            <div class="image-gallery-title">
            	<xsl:choose>
            		<xsl:when test="string-length($image-title) &gt; 28">
    					<xsl:value-of select="substring($image-title, 1, 26)" />&#8230;
            		</xsl:when>
            		<xsl:otherwise>
	            		<xsl:value-of select="$image-title" />
            		</xsl:otherwise>
            	</xsl:choose>
            </div>
            
            <div class="image-gallery-date">
            	<xsl:value-of select="$image-date" />
            </div>
    </xsl:template>
    
    
    
    
    <!-- Override the file handling end -->
    <xsl:template match="mets:file">
        <xsl:param name="context" select="."/>
        <tr>
            <xsl:attribute name="class">
                <xsl:text>ds-table-row </xsl:text>
                <xsl:if test="(position() mod 2 = 0)">even </xsl:if>
                <xsl:if test="(position() mod 2 = 1)">odd </xsl:if>
            </xsl:attribute>
            <td>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="string-length(mets:FLocat[@LOCTYPE='URL']/@xlink:title) > 50">
                            <xsl:variable name="title_length" select="string-length(mets:FLocat[@LOCTYPE='URL']/@xlink:title)"/>
                            <xsl:value-of select="substring(mets:FLocat[@LOCTYPE='URL']/@xlink:title,1,15)"/>
                            <xsl:text> ... </xsl:text>
                            <xsl:value-of select="substring(mets:FLocat[@LOCTYPE='URL']/@xlink:title,$title_length - 25,$title_length)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </td>
            <!-- File size always comes in bytes and thus needs conversion --> 
            <td>
                <xsl:choose>
                    <xsl:when test="@SIZE &lt; 1024">
                        <xsl:value-of select="@SIZE"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="@SIZE &lt; 1024 * 1024">
                        <xsl:value-of select="substring(string(@SIZE div 1024),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="@SIZE &lt; 1024 * 1024 * 1024">
                        <xsl:value-of select="substring(string(@SIZE div (1024 * 1024)),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="substring(string(@SIZE div (1024 * 1024 * 1024)),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <!-- Lookup File Type description in local messages.xml based on MIME Type.
                In the original DSpace, this would get resolved to an application via
                the Bitstream Registry, but we are constrained by the capabilities of METS
                and can't really pass that info through. -->
            <td>
                <xsl:call-template name="getFileTypeDesc">
                    <xsl:with-param name="mimetype">
                        <xsl:value-of select="substring-before(@MIMETYPE,'/')"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="substring-after(@MIMETYPE,'/')"/>
                    </xsl:with-param>
                </xsl:call-template>
            </td>
            <td>
            	<!-- Set the preview link to point to the IIP server if the bitstream is a JPEG 2000 image -->
            	<xsl:variable name="previewLink">
            		<xsl:choose>
            			<xsl:when test="@MIMETYPE='image/jp2'">
            				<xsl:value-of select="$image-server-url"/>
                            <xsl:text>viewer.html?rft_id=</xsl:text>
                            <xsl:value-of select="url:encode($repository-url)"/>
                            <xsl:value-of select="url:encode(mets:FLocat[@LOCTYPE='URL']/@xlink:href)"/>
            			</xsl:when>
            			<xsl:otherwise><xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/></xsl:otherwise>
            		</xsl:choose>
            	</xsl:variable>
                <xsl:choose>
                    <xsl:when test="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/mets:file[@GROUPID=current()/@GROUPID]">
                        <a class="image-link">
                            <xsl:attribute name="href">
                                <xsl:value-of select="$previewLink"/>
                            </xsl:attribute>
                            <img alt="Thumbnail">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/
                                        mets:file[@GROUPID=current()/@GROUPID]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                                </xsl:attribute>
                            </img>
                        </a>
                    </xsl:when>
                    <xsl:when test="@MIMETYPE='image/jp2'">
                    	<xsl:variable name="jp2-url" select="mets:FLocat[@LOCTYPE='URL']/@xlink:href" />
                    	<a class="image-link">
                            <xsl:attribute name="href">
                                <xsl:value-of select="$previewLink"/>
                            </xsl:attribute>
                            <img alt="Thumbnail">
                                <xsl:attribute name="src">
                                	<xsl:value-of select="$image-server-url"/>
		            				<xsl:text>resolver?url_ver=Z39.88-2004&amp;rft_id=</xsl:text>
		            				<xsl:value-of select="url:encode($repository-url)"/>
		            				<xsl:value-of select="url:encode($jp2-url)"/>
		            				<xsl:text>&amp;svc_id=info:lanl-repo/svc/getRegion&amp;svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&amp;svc.format=image/jpeg&amp;svc.scale=192</xsl:text>
                                </xsl:attribute>
                            </img>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$previewLink"/>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-viewOpen</i18n:text>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>                        
            </td>
            <!-- Display the contents of 'Description' as long as at least one bitstream contains a description -->
            <xsl:if test="$context/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file/mets:FLocat/@xlink:label != ''">
                <td>
                    <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/>
                </td>
            </xsl:if>
            
        </tr>
    </xsl:template>
	 
    

</xsl:stylesheet>