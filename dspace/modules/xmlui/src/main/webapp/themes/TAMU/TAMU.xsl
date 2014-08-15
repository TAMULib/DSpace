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
	<xsl:import href="./lib/xsl/date.month-name.template.xsl"/>
    <xsl:import href="./lib/xsl/date.day-in-month.template.xsl"/>
    <xsl:import href="./lib/xsl/date.year.template.xsl"/>
    <xsl:output indent="yes"/>
    
    
	
	
	
	
	
	
	
	
	
	
	
	
	<!-- This template moves external reference logic out here to allow it to be overriden if necessary. -->
	<xsl:template name="importHead">
	    <xsl:apply-templates select="document('static/header.xml')" mode="import"/>
	</xsl:template>
    
    
    <xsl:template name="importFrontPageLeftContentColumn">
        <xsl:apply-templates select="document('static/col1.xml')" mode="import"/>
    </xsl:template>
    
    <xsl:template name="importFrontPageCenterContentColumn">
        <xsl:apply-templates select="document('static/col2.xml')" mode="import"/>
    </xsl:template>
    
    <!--
    <xsl:template name="importFrontPageRightContentColumn">
        <xsl:apply-templates select="document('static/col3.xml')" mode="import"/>
    </xsl:template>
	-->
	
	
	
	
		<!--
        The starting point of any XSL processing is matching the root element. In DRI the root element is document, 
        which contains a version attribute and three top level elements: body, options, meta (in that order). 
        
        This template creates the html document, giving it a head and body. A title and the CSS style reference
        are placed in the html head, while the body is further split into several divs. The top-level div
        directly under html body is called "ds-main". It is further subdivided into:
            "ds-header"  - the header div containing title, subtitle, trail and other front matter
            "ds-body"    - the div containing all the content of the page; built from the contents of dri:body
            "ds-options" - the div with all the navigation and actions; built from the contents of dri:options
            "ds-footer"  - optional footer div, containing misc information
        
        The order in which the top level divisions appear may have some impact on the design of CSS and the 
        final appearance of the DSpace page. While the layout of the DRI schema does favor the above div 
        arrangement, nothing is preventing the designer from changing them around or adding new ones by 
        overriding the dri:document template.   
    -->
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
                    
                </div>












<!-- all the javascript business goes at the bottom of the body -->

<!-- add jQuery -->
            <!-- Javascript at the bottom for fast page loading -->
            <xsl:call-template name="addJavascript"/>
            
            
            







              
            </body>
        </html>
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
    <xsl:template name="buildHead">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <!-- Add stylsheets -->
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
    
	
	
    <!-- The header (NOT the HTML head element) contains the title, subtitle, login box and various 
        placeholders for header images -->
    <xsl:template name="buildHeader">	    
	    	
	    <div id="page_top">
			
			<!-- TODO:  insert the real date here -->
			<!-- <span id="date">Monday, July 24, 2007</span> -->
			
			<ul class="page_top_buttons">
				<li>
					<a class="top-icon">
						<xsl:attribute name="href">
							http://library.tamu.edu/portal/site/Library/
						</xsl:attribute>
						<img src="{$theme-path}/images/home_icon.png" alt="Home Page">
						</img>
						
						<span class="top-icon-hover-text" id="top-text-home">Library Home</span>
					</a>
				</li>
				
							
				<li>
					<a class="top-icon">
						<xsl:attribute name="href">
							http://tamucb.cb.docutek.com/
						</xsl:attribute>
						<img src="{$theme-path}/images/live_chat_icon.png" alt="Live Chat with A Librarian">
						</img>
						
						<span class="top-icon-hover-text" id="top-text-chat">Chat with A Librarian</span>
					</a>	
				</li>

								
				<li>
					<a class="top-icon">
						<xsl:attribute name="href">
							<xsl:value-of select="$context-path"/>/contact
						</xsl:attribute>
						<img src="{$theme-path}/images/reportprobs_icon.png" alt="Report Problems">
						</img>
						
						<span class="top-icon-hover-text" id="top-text-report">Report Problems</span>
					</a>					
				</li>
				
				<li>
					<a class="top-icon">
						<xsl:attribute name="href">
							http://www.tamu.edu
						</xsl:attribute>
						<img src="{$theme-path}/images/tamu_icon.png" alt="Texas A&amp;M University Homepage">
						</img>
						
						<span class="top-icon-hover-text" id="top-text-tamu">Texas A&amp;M University</span>			
					</a>					
				</li>				
			</ul>

            <!--
	        <ul id="portal-globalnav">
	            <li id="portaltab-about" class="plain">
	                <a title="" href="http://dl.tamu.edu/about">About</a>
	            </li>
	            <li id="portaltab-services" class="plain">
	                <a title="" href="http://dl.tamu.edu/services">Services</a>
	            </li>
	            <li id="portaltab-help" class="plain">
	                <a title="" href="http://dl.tamu.edu/help">Help</a>
	            </li>
	        </ul>
	        -->
			
		</div><!--end of <div id="page_top">-->
		
		
		<div id="ds-header">
           			
		    
		    <xsl:call-template name="importHead"/>
			
		    <map name="logomap">
		        <area shape="rect" coords="0,0,162,64" href="http://www.tamu.edu" alt="TAMU Homepage" />
		        <area shape="rect" coords="163,0,288,64" href="http://library.tamu.edu" alt="TAMU Libraries Homepage" />
		        <area shape="rect" coords="289,0,393,64" href="http://digital.library.tamu.edu" alt="TAMU Digital Library Homepage" />
		    </map>

                  
        
		
			<div id="top_navigation">
				<ul id="breadcrumbs">
				    <xsl:apply-templates select="/dri:document/dri:meta/dri:pageMeta/dri:trail">
				        <xsl:with-param name="number_of_trail_items" select="count(/dri:document/dri:meta/dri:pageMeta/dri:trail)"/>
				    </xsl:apply-templates>
				
				</ul>
	
				
				<xsl:choose>
						<xsl:when test="/dri:document/dri:meta/dri:userMeta/@authenticated = 'yes'">
							<div id="ds-user-box">
								<p>
									<a>
										<xsl:attribute name="href">
											<xsl:value-of select="/dri:document/dri:meta/dri:userMeta/
												dri:metadata[@element='identifier' and @qualifier='url']"/>
										</xsl:attribute>
										<i18n:text>xmlui.dri2xhtml.structural.profile</i18n:text>
										<xsl:value-of select="/dri:document/dri:meta/dri:userMeta/
											dri:metadata[@element='identifier' and @qualifier='firstName']"/>
										<xsl:text> </xsl:text>
										<xsl:value-of select="/dri:document/dri:meta/dri:userMeta/
											dri:metadata[@element='identifier' and @qualifier='lastName']"/>
									</a>
									<xsl:text> | </xsl:text>
									<a>
										<xsl:attribute name="href">
											<xsl:value-of select="/dri:document/dri:meta/dri:userMeta/
												dri:metadata[@element='identifier' and @qualifier='logoutURL']"/>
										</xsl:attribute>
										<i18n:text>xmlui.dri2xhtml.structural.logout</i18n:text>
									</a>
								</p>
							</div>
						</xsl:when>
						<xsl:otherwise>
							<div id="ds-user-box">
								<p>
									<a>
										<xsl:attribute name="href">
											<xsl:value-of select="/dri:document/dri:meta/dri:userMeta/
												dri:metadata[@element='identifier' and @qualifier='loginURL']"/>
										</xsl:attribute>
										<i18n:text>xmlui.dri2xhtml.structural.login</i18n:text>
									</a>
								</p>
							</div>
						</xsl:otherwise>
				</xsl:choose>
				
				
				<!--<span id="quicklinks"><a href="">Quick Links</a></span>-->
			
			</div><!--end of <div id="top_navigation">-->    
			
		
		
	
		
			<!-- The form, complete with a text box and a button, all built from attributes referenced
                    from under pageMeta. -->
	
			<div id="searchbar">
					
				<!-- <h3 id="ds-search-option-head" class="ds-option-set-head"><i18n:text>xmlui.dri2xhtml.structural.search</i18n:text></h3> -->
			    <h3 id="ds-search-option-head" class="ds-option-set-head">Search Repository</h3>
					<form id="ds-search-form" method="post">
						<xsl:attribute name="action">
							<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"/>
						</xsl:attribute>
						<fieldset>
							<input class="ds-text-field " type="text">
								<xsl:attribute name="name">
									<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"/>
								</xsl:attribute>                        
							</input>
							
							
							<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container']">
								<select id="ds-search-form-scope-container" style="width:168px;">
									<option selected ="selected" value="/"><i18n:text>xmlui.dri2xhtml.structural.search</i18n:text>
									</option>
																		
									<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container']">
										<xsl:variable name="focus">
											<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus' and @qualifier='container']"/>
										</xsl:variable>
										<option>
											<xsl:attribute name="value">
												<xsl:value-of select="substring-after($focus,':')"/>
											</xsl:attribute>       
											<xsl:choose>
												<xsl:when test="/dri:document/dri:body//dri:reference[contains(@url, substring-after(//dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container'], ':')) and @type='DSpace Community']">This Community</xsl:when>
										        <xsl:when test="/dri:document/dri:body//dri:reference[contains(@url, substring-after(//dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container'], ':')) and @type='DSpace Collection']">This Collection</xsl:when>
												<!--
											    <xsl:when test="/dri:document/dri:body//dri:reference[contains(@url, substring-after(//dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container'], ':'))][@type='DSpace Community']">This Community</xsl:when>
										        <xsl:when test="/dri:document/dri:body//dri:reference[contains(@url, substring-after(//dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container'], ':'))][@type='DSpace Collection']">This Collection</xsl:when>
												-->
												<!-- TODO:  how to detect community/collection distinction without the reference in the body? -->
												<xsl:otherwise>Current Collection</xsl:otherwise>
											</xsl:choose>
	
										</option>
									</xsl:if>
								</select>
							</xsl:if>
							
							<input class="ds-button-field " name="submit" type="submit" i18n:attr="value" value="xmlui.general.search" >
								<xsl:attribute name="onclick">
								   <xsl:text>
								   		var dropdown = document.getElementById(&quot;ds-search-form-scope-container&quot;);
																				
										if (dropdown != undefined &amp;&amp; dropdown.value != "/")
										{
											var form = document.getElementById(&quot;ds-search-form&quot;);
											form.action=
									</xsl:text>
									<xsl:text>&quot;</xsl:text>
									<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
									<xsl:text>/handle/&quot; + dropdown.value + &quot;/search&quot; ; </xsl:text>
									<xsl:text>
										}										
									</xsl:text>
								</xsl:attribute>
							</input>
					
						</fieldset>
					</form>
				
				
			    <!-- The "Advanced search" link, to be perched underneath the search box -->
					<a>
						<xsl:attribute name="href">
							<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='advancedURL']"/>
						</xsl:attribute>
						<i18n:text>xmlui.dri2xhtml.structural.search-advanced</i18n:text>
					</a> 
      
			</div><!-- end #searchbar -->		
		
		</div>
		
		
    </xsl:template>
	
    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

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
				    <xsl:choose>                    
				        <xsl:when test="count($site_home) >= 1">
				            <!-- <xsl:copy-of select="document('about_the_dl.xml')"/> -->
				            
				            <!-- Also do the nifty rotating header trick -->
				            <div id="rotating_header_wrapper">
				                <div id="rotating_header">
				                    <a id="rotating_header_anchor" href="">
				                        <img id="rotating_header_img" src="" alt=""/>
				                    </a>
				                </div>
				                <div id="rotating_header_about_this_image_wrapper">
				                    <p id="rotating_header_para" style="display: none"></p>
				                    <p id="rotating_header_more" class="rotating_header_clicker" style="">[+] About this image</p>
				                    <p id="rotating_header_less" class="rotating_header_clicker" style="display: none;">[-] About this image</p>
				                </div>
				                
				                <!-- Read the little config file off disk -->
				                <xsl:variable name="header_imgs" select="document('images/headers/list.xml')/images"/>
				                <script type="text/javascript">				                    
				                    var randomnumber=Math.floor(1+Math.random()*<xsl:value-of select="count($header_imgs/image)"/>)
				                    
				                    var anchorTag= document.getElementById("rotating_header_anchor");
				                    var imgTag = document.getElementById("rotating_header_img");
				                    var paraTag = document.getElementById("rotating_header_para");
				                    
				                    switch(randomnumber) {				                    
				                    <xsl:for-each select="$header_imgs/image">
				                        case <xsl:value-of select="position()"/>: 
				                        <xsl:text>anchorTag.setAttribute("href","</xsl:text>
				                        <xsl:value-of select="link"/>
				                        <xsl:text>"); </xsl:text>
				                        <xsl:text>imgTag.setAttribute("src","</xsl:text>
				                        <xsl:choose>
				                            <xsl:when test="contains(src,'http://')">
				                                <xsl:value-of select="src"/>
				                            </xsl:when>
				                            <xsl:otherwise>
				                                <xsl:value-of select="$theme-path"/>/images/headers/<xsl:value-of select="src"/>
				                            </xsl:otherwise>
				                        </xsl:choose>
				                        <xsl:text>"); </xsl:text>
				                        <xsl:text>imgTag.setAttribute("alt","</xsl:text>
				                        <xsl:value-of select="short_desc"/>
				                        <xsl:text>"); </xsl:text>
				                        <xsl:text>$("#rotating_header_para").html('</xsl:text>
				                        <!-- <h2 style="font-weight: normal; padding: 10px 0px;">About this image</h2> -->
				                        <xsl:copy-of select="long_desc/child::node()"/>
				                        <xsl:text>'); </xsl:text>
				                        <xsl:text>break;</xsl:text>
				                    </xsl:for-each>
				                    }				                    		                    
				                </script>
				                
				            </div>
				            
				            
				            <div id="front_page_column_container">
    				            <div id="left_content_column">
    				                <xsl:call-template name="importFrontPageLeftContentColumn"/>
    				            </div>
    				            
    				            <div id="center_content_column">
    				                <xsl:call-template name="importFrontPageCenterContentColumn"/>
    				            </div>
    				            
    				            <div id="right_content_column">
    				                <p class="front-page-col-heading">Recent Submissions</p>
    				                <xsl:apply-templates select="//dri:referenceSet[@id='aspect.discovery.SiteRecentSubmissions.referenceSet.site-last-submitted']" mode="frontPageRecent"/>
    				            </div>
				            </div>
				            <!-- <div class="spacer">&#160;</div> -->
				        </xsl:when>
				        <xsl:otherwise>
				            <xsl:choose>
				                <xsl:when test="//dri:meta/dri:pageMeta/dri:metadata[@element='restricted'] and //dri:meta/dri:pageMeta/dri:metadata[@element='focus' and @qualifier='container']='hdl:1969.1/2'">
				                    <div id="bitstreamRestrictionNotice">The full text of this item is not available at this time because the student has placed this item under an embargo for a period of time. The Libraries are not authorized to provide a copy of this work during the embargo period, even for Texas A&amp;M users with NetID.</div>
				                </xsl:when>
				                <xsl:when test="//dri:meta/dri:userMeta[@authenticated='no'] and //dri:meta/dri:pageMeta/dri:metadata[@element='restricted' and @qualifier='campus']">
				                    <div id="bitstreamRestrictionNotice">NOTE: This item is not available outside the Texas A&amp;M University network.  Texas A&amp;M affiliated users who are off campus can access the item through NetID and password authentication or by using <a href="http://it.tamu.edu/Connecting/Internet_Wireless/Virtual_Private_Network__VPN_.php" target="_blank">TAMU VPN</a>.  Non-affiliated individuals should request a copy through their local library's interlibrary loan service.</div>
				                </xsl:when>
				                <xsl:when test="//dri:meta/dri:userMeta[@authenticated='no'] and //dri:meta/dri:pageMeta/dri:metadata[@element='restricted']">
				                    <div id="bitstreamRestrictionNotice">NOTE: Restrictions are in place to limit access to one or more of the files associated with this item. Authorized users must log in to gain access. Non-authorized users do not have access to these files.</div>
				                </xsl:when>
				                <xsl:when test="//dri:meta/dri:userMeta[@authenticated='yes'] and //dri:meta/dri:pageMeta/dri:metadata[@element='restricted']">
				                    <div id="bitstreamRestrictionNotice">NOTE: You are not authorized to access some or all of the files below.</div>
				                </xsl:when>
				            </xsl:choose>
				            
				            <xsl:apply-templates select="*[not(@n='front-page-search')]"/>
				        </xsl:otherwise>                    
				    </xsl:choose>           
				    
				    <!-- </div> -->
				</div>
			
        </div>
    </xsl:template>
	
	
	
	<!-- Omit the Request Access div if the item has no restricted components --> 
    <xsl:template match="dri:div[@id='org.tamu.dspace.requestcopy.RequestCopy.div.request-item']">
        <xsl:if test="//dri:meta/dri:pageMeta/dri:metadata[@element='restricted']">
            <xsl:apply-imports/>
        </xsl:if>
    </xsl:template>
	
    
    <!-- -->  
    <xsl:template name="buildFooter">
		
		<div id="ds-footer">
		
			<!-- TODO:  some changing content here -->
			<!-- <xsl:apply-templates select="document('featured_news.xml')" mode="import"/> -->

			<div id="giving_to_libraries">
				<span>
				<img id="gift_icon" src="{$theme-path}/images/gift_icon.gif" alt="Gifts:" />
				</span>
				<h3><a href="http://library.tamu.edu/giving/">Giving to the Libraries</a></h3>
			</div>
	
			<div class="footer_links">
			    <p>
			        <a title="Texas A&amp;M University" href="http://www.tamu.edu">Texas A&amp;M University</a>
			        <a title="Employment opportunities at the Texas A&amp;M University Libraries" href="http://library.tamu.edu/about/employment/">Employment</a> 
			        <a title="Contact the Library Webmaster" href="http://library.tamu.edu/services/forms/contact-info.html">Webmaster</a>			        
			        <a title="Legal Notices" href="http://library.tamu.edu/about/general-information/legal-notices.html">Legal</a>
			        <a title="Comments" href="/feedback">Comments</a>
			        <a title="Phone" href="http://library.tamu.edu/about/phone/">979-845-5741</a>
			        <a title="Accessibility" href="http://digital.library.tamu.edu/accessibility/">Accessibility</a>
			    </p>
			</div>
		</div><!--end of <div id="footer">-->
		<!-- Link for search engines to get a list of all items in the repository -->
		<a href="/htmlmap" style="display:none;">-</a>
    </xsl:template>
	
	
	<!-- 
        The template to handle dri:options. Since it contains only dri:list tags (which carry the actual
        information), the only things than need to be done is creating the ds-options div and applying 
        the templates inside it. 
        
        In fact, the only bit of real work this template does is add the search box, which has to be 
        handled specially in that it is not actually included in the options div, and is instead built 
        from metadata available under pageMeta.
    -->
    <!-- TODO: figure out why i18n tags break the go button -->
    <xsl:template match="dri:options">
        <!-- changed the id of the following div from ds-options to navigation -->
        <div id="navigation">
            <div id="navigation_inner">
                
                
                <!-- Once the search box is built, the other parts of the options are added -->			
                <xsl:apply-templates />
                
                <!-- the featured news  -->
                <!-- Eliminated featured news in the navigation, and instead put it in the footer as a bar for 2nd version of the theme, August 8 2007 -->
                <!--
                    <xsl:copy-of select="document('featured_news.xml')"/>
                -->
                
            </div>
            <div class="navigation-static-link">
                <a href="http://scholarlycommunication.library.tamu.edu/repository-getting-started/help.html">Help and Documentation</a>
                <xsl:call-template name="social-stuff"/>
            </div>
        </div>
    </xsl:template>


	<xsl:template name="social-stuff">
		<div id="social-links">
			<a>
				<xsl:attribute name="href">
                    <xsl:text>http://www.addthis.com/bookmark.php?v=250&amp;pub=ra-4dca6193440878ca</xsl:text>
                </xsl:attribute>
				<xsl:attribute name="class">
                    <xsl:text>addthis_button</xsl:text>
                </xsl:attribute>

				<img>
					<xsl:attribute name="src">
						<xsl:value-of select="$context-path"/>
                        <xsl:text>/themes/TAMU/images/share_lt.png</xsl:text>
                    </xsl:attribute>
					<xsl:attribute name="width">
                        <xsl:text>140</xsl:text>
                    </xsl:attribute>
					<xsl:attribute name="height">
                        <xsl:text>24</xsl:text>
                    </xsl:attribute>
					<xsl:attribute name="style">
                        <xsl:text>border:0</xsl:text>
                    </xsl:attribute>
				</img>
			</a>


			<script type="text/javascript">
				<xsl:attribute name="src">
                    <xsl:text>https://s7.addthis.com/js/250/addthis_widget.js?pub=ra-4dca6193440878ca</xsl:text>
                </xsl:attribute>
				<xsl:text>// Empty comment</xsl:text>
			</script>
		</div>
	</xsl:template>
	
	
	
	
	
	
	
	
	<!-- Next, special handling is performed for lists under the options tag, making them into option sets to
        reflect groups of similar options (like browsing, for example). -->
    
    <!-- The template that applies to lists directly under the options tag that have other lists underneath 
        them. Each list underneath the matched one becomes an option-set and is handled by the appropriate 
        list templates. -->
    <xsl:template match="dri:options/dri:list[dri:list]" priority="4">
		<xsl:variable name="menu_item_id">
				<xsl:value-of select="count(preceding-sibling::dri:list)" />
		</xsl:variable>
	
        <xsl:if test="dri:item | dri:list">
    	    <div class="menuslider">
                <xsl:apply-templates select="dri:head"/>
    	        <div style="display: block;">
                    <xsl:call-template name="standardAttributes">
                        <xsl:with-param name="class">ds-option-set</xsl:with-param>
                    </xsl:call-template>
                    
                    <!-- <ul class="ds-options-list" id="menu_{$menu_item_id}" style="display: none"> -->
                    <ul class="ds-options-list" id="menu_{$menu_item_id}">
                        <xsl:apply-templates select="*[not(name()='head')]" mode="nested"/>
                    </ul>
                </div>
    	    </div>
        </xsl:if>
        
    </xsl:template>
	
    
    <xsl:template match="dri:options/dri:list" priority="3">
		<xsl:variable name="menu_item_id">
				<xsl:value-of select="count(preceding-sibling::dri:list)" />
		</xsl:variable>
        
        <xsl:if test="dri:item | dri:list">
            <div class="menuslider">
                <xsl:apply-templates select="dri:head"/>
                <div>
                    <xsl:call-template name="standardAttributes">
                        <xsl:with-param name="class">ds-option-set</xsl:with-param>
                    </xsl:call-template>
        		
        			<!-- options or simple? -->
                    <!-- <ul class="ds-options-list" id="menu_{$menu_item_id}" style="display: none"> -->
        			<ul class="ds-options-list" id="menu_{$menu_item_id}"> 
                        <xsl:apply-templates select="dri:item" mode="nested"/>
                    </ul>
                </div>
            </div>
            </xsl:if>
    </xsl:template>
    
	
	
	<!-- template for the headings within the lists directly under the dri:options navigation -->
	<!-- here are two ways to change the navigation list heading icon from closed to open:  
	1. Make a span with a class that has a background image, then change the class of the span.3
	2. Make an img with a particular src, then change that src.
	We have chosen route 2 and commented out route 1 which has some complications with displaying the icon 
	Update:  we have chosen to forget all this and use jQuery library instead -->
	<xsl:template match="dri:options/dri:list/dri:head" priority="3">
		<xsl:variable name="menu_item_id">
				<xsl:value-of select="count(../preceding-sibling::dri:list)" />
		</xsl:variable>
		
	    <h3 class="ds-option-set-head-clickable open" id="menu_heading_{$menu_item_id}">
			<!--
			<xsl:attribute name="onclick">
				var my_heading = document.getElementById("menu_heading_<xsl:value-of select='$menu_item_id' />");
				var my_menu    = document.getElementById("menu_<xsl:value-of select='$menu_item_id' />");
				var my_icon    = document.getElementById("menu_nav_icon_<xsl:value-of select='$menu_item_id' />");
				var curr_style = my_menu.getAttribute('style');
				if(curr_style.search(/block/g) > -1)
				{
					my_menu.setAttribute("style", "display: none;");
					my_heading.setAttribute("style", "background-image: none;");
					//my_icon.setAttribute("class", "nav_closed");
					my_icon.setAttribute("src", "<xsl:value-of select='$theme-path' />/images/nav_arrow_closed.gif");
				}
				else if(curr_style.search(/none/g) > -1)
				{
					my_menu.setAttribute("style", "display: block;");
					my_heading.setAttribute("style", "background: url('/manakin/themes/Library/images/nav_selected_bg.png') repeat-x #798b98;");
					//my_icon.setAttribute("class", "nav_open");
					my_icon.setAttribute("src", "<xsl:value-of select='$theme-path' />/images/nav_arrow_open.gif");
				}
				else
				{
					alert("Comparison failure");
				}
				
			</xsl:attribute>
			-->
			
			<!-- <span class="nav_closed" id="menu_nav_icon_{$menu_item_id}"> &#160; </span> -->
			<img class="menu_nav_icon_closed" id="menu_nav_icon_closed_{$menu_item_id}" src="{$theme-path}/images/nav_arrow_closed.gif" alt="(menu closed)" style="display:none"/>
			<img class="menu_nav_icon_open" id="menu_nav_icon_open_{$menu_item_id}" src="{$theme-path}/images/nav_arrow_open.gif" alt="(menu opened)"/>
			
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-option-set-head</xsl:with-param>
            </xsl:call-template>
				
            <xsl:apply-templates />
			
        </h3>
		
    </xsl:template>
	
	
	
    <!-- Recent Submissions: append a link to the end of the recent submissions list -->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionRecentSubmissions.div.collection-recent-submission'] | dri:div[@id='aspect.artifactbrowser.CommunityRecentSubmissions.div.community-recent-submission']" >
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-static-div</xsl:with-param>
            </xsl:call-template>
            <xsl:apply-templates/>        
            <div id="recent_submissions_view_more_div">
                <a id="recent_submissions_view_more_link">
                    <xsl:attribute name="href">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI']"/>
                        <xsl:text>/browse?type=title</xsl:text>
                    </xsl:attribute>                
                    <xsl:text>View More...</xsl:text>
                </a>
            </div>
        </div>
    </xsl:template>
	
	
	<!-- Overrides the community/collection search to omit the search form when a filter-search form is already present -->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-search-browse'] | dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-search-browse']" priority="1">
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
                    <xsl:choose>
                        <!-- in case there's a filter search from TDL aspect, leave out the community or collection search -->
                        <xsl:when test="descendant::dri:div[@id='org.tdl.dspace.filtersearch.FilterSearch.div.community-filter-search'] | descendant::dri:div[@id='org.tdl.dspace.filtersearch.FilterSearch.div.collection-filter-search']">
                            <xsl:apply-templates select="*[not(name()='head')][not(@id='aspect.artifactbrowser.CommunitySearch.div.community-search')][not(@id='aspect.artifactbrowser.CollectionSearch.div.collection-search')]"/>                         
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="*[not(name()='head')]"/>
                            <xsl:variable name="contextNav" select="/dri:document/dri:options/dri:list[@n='browse']/dri:list[@n='context']"/>
                            <div class="collectionBrowse">
                                <span>Browse this collection: </span>
                                <select id="browseSelector" onchange="window.location.href=document.getElementById('browseSelector').value">
                                    <xsl:for-each select="$contextNav/dri:item">
                                        <option value="{./dri:xref/@target}"><xsl:apply-templates select="./dri:xref"/></option>
                                    </xsl:for-each>
                                </select>
                            </div>
                        </xsl:otherwise>                    
                        
                    </xsl:choose>
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
    
    
    <!-- matches the filter-search in order to restructure the divs for some JQuery manipulation 
         picks out the title field and puts the rest of them in their own div
    -->
    <xsl:template match="dri:list[not(@type)][@n='filter-search']" priority="2">
        <xsl:variable name="etdfulltext_type">
            <xsl:value-of select="dri:item[position()=2]/attribute::type"/>            
        </xsl:variable>
        <xsl:variable name="etdfulltext_name">
            <xsl:value-of select="dri:item[position()=2]/attribute::n"/>      
        </xsl:variable>
        
        <xsl:apply-templates select="dri:head"/>
        <div id="filter-search-fulltext-field">
            <!-- <span class="ds-gloss-list-label"><xsl:value-of select="dri:label[position()=1]"/></span>: -->
            <xsl:apply-templates select="dri:item[position()=1]" mode="labeled"/>
            <!-- <input id="org_tdl_dspace_filtersearch_FilterSearch_field_etdauthor" class="ds-text-field" type="$etdfulltext_type" value="" name="$etdfulltext_name"/> -->
        </div>
        
        <xsl:if test="//dri:div[@rend='primary filter-search-results']">
            <div id="collapsible-filter-search-fields">
                <div class="more_text">[more]</div>
                <div class="less_text" style="display: none">[less]</div>
                <div id="collapsible-filter-search-fields-table-container" style="display: none">
                    <table>
                        
                        <xsl:call-template name="standardAttributes">
                            <xsl:with-param name="class">ds-gloss-list</xsl:with-param>
                        </xsl:call-template>
                        <xsl:apply-templates select="dri:item[position()&gt;1]" mode="labeled"/>
                    </table>
                </div>
            </div>       
        </xsl:if>
        
        <xsl:if test="not(//dri:div[@rend='primary filter-search-results'])">
            <div id="collapsible-filter-search-fields">
                <div class="more_text" style="display:none">[more]</div>
                <div class="less_text">[less]</div>
                <div id="collapsible-filter-search-fields-table-container">
                    <table>
                        
                        <xsl:call-template name="standardAttributes">
                            <xsl:with-param name="class">ds-gloss-list</xsl:with-param>
                        </xsl:call-template>
                        <xsl:apply-templates select="dri:item[position()&gt;1]" mode="labeled"/>
                    </table>
                </div>
            </div>       
        </xsl:if>
    </xsl:template>
	
	
	
	
	

    
    <!-- Then we resolve the reference tag to an external mets object --> 
    <xsl:template match="dri:reference" mode="summaryList">
        
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
        </xsl:variable>
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
        <li>
            <xsl:attribute name="class">
                <xsl:text>ds-artifact-item </xsl:text>
                <xsl:choose>
                    <xsl:when test="position() mod 2 = 0">even</xsl:when>
                    <xsl:otherwise>odd</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates select="document($externalMetadataURL)" mode="summaryList"/>
            <xsl:apply-templates />
        </li>
    </xsl:template>
	
	<!-- Generate the metadata popup text about the item from the metadata section -->
    <xsl:template match="dim:dim" mode="itemMetadataPopup-DIM">
        <!-- TODO : we would like to attach a list of containing collections to this metadata list -->
        <!--
        <xsl:variable name="objxml">
            <xsl:text>cocoon://DRI</xsl:text><xsl:value-of select="ancestor::mets:METS/@OBJID"/>
        </xsl:variable>
        -->
        
        <table class="ds-includeSet-metadata-table">
			<!-- abstract -->
			<xsl:choose>
				<xsl:when test="dim:field[@element='description' and @qualifier='abstract']">
					<tr class="ds-table-row even">
						<td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:</span></td>
						<!-- we chose to truncate the abstract and add a "..." in case it is too long. -->
						<!-- naah, we changed or mind. -->
						<!--
						<xsl:choose>
							<xsl:when test="string-length(dim:field[@element='description' and @qualifier='abstract']/child::node()) > 201">
								<td><xsl:copy-of select="substring(dim:field[@element='description' and @qualifier='abstract']/child::node(), 0, 200)"/>...</td>
							</xsl:when>
							<xsl:otherwise>
								<td><xsl:copy-of select="dim:field[@element='description' and @qualifier='abstract']/child::node()"/></td>
							</xsl:otherwise>
						</xsl:choose>
						-->
					    <td>
					        <xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']/child::node()">
					            <xsl:value-of select="."/><br/>
					        </xsl:for-each>						    
					    </td>
							
					</tr>
				</xsl:when>
				<xsl:otherwise>
				</xsl:otherwise>
			</xsl:choose>
			
            <!-- description -->
            <xsl:choose>
                <xsl:when test="dim:field[@element='description' and not(@qualifier)]">
                    <tr class="ds-table-row odd">
                        <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:</span></td>
                        <td>
                            <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]/child::node()">
                                <xsl:value-of select="."/><br/>     
                            </xsl:for-each>
                        </td>
                    </tr>
                </xsl:when>
            </xsl:choose>
				
			<!-- URI -->
			<xsl:choose>
				<xsl:when test="dim:field[@element='identifier' and @qualifier='uri']">
					<tr class="ds-table-row even">
						<td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:</span></td>
						<td>
							<a>
								<xsl:attribute name="href">
									<xsl:copy-of select="dim:field[@element='identifier' and @qualifier='uri'][1]/child::node()"/>
								</xsl:attribute>
								<xsl:copy-of select="dim:field[@element='identifier' and @qualifier='uri'][1]/child::node()"/>
							</a>
						</td>
					</tr>
				</xsl:when>
			</xsl:choose>
		</table>
        
        
        <!-- display bitstreams -->		
		<xsl:variable name="context" select="ancestor::mets:METS"/>
        <xsl:variable name="data" select="./mets:METS/mets:dmdSec/mets:mdWrap/mets:xmlData/dim:dim"/>
        
        <xsl:apply-templates select="$data" mode="detailView"/>
            <!-- First, figure out if there is a primary bitstream -->
			<xsl:variable name="primary" select="$context/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:div[@TYPE='DSpace Content Bitstream']/mets:fptr/@FILEID" />
            <xsl:variable name="bitstream-count" select="count($context/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:div[@TYPE='DSpace Content Bitstream'])" />
                       
			<h2 class="slider-files-header"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text>:&#160;<xsl:value-of select="$bitstream-count"/></h2>
        	<xsl:choose>
                <!-- If one exists only display the primary bitstream-->
                <xsl:when test="$context/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@ID=$primary]">
                    <xsl:if test="$bitstream-count&lt;2">
						<xsl:call-template name="buildBitstreamOnePrimary">
							<xsl:with-param name="context" select="$context"/>
							<xsl:with-param name="file" select="$context/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@ID=$primary]"/>
						</xsl:call-template>					
					</xsl:if>					
					<xsl:if test="$bitstream-count&gt;1">
						<xsl:call-template name="buildBitstreamSingle">
							<xsl:with-param name="context" select="$context"/>
							<xsl:with-param name="file" select="$context/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file[@ID=$primary]"/>
						</xsl:call-template>					
					
						<a class="slider-bitstream-count" href="{ancestor::mets:METS/@OBJID}">
							(more files)
						</a>
					</xsl:if>
				</xsl:when>
                <!-- Otherwise, iterate over and display some (4) of them -->
                <xsl:otherwise>
            		<xsl:for-each select="$context/mets:fileSec/mets:fileGrp[@USE='CONTENT']/mets:file">
                        <xsl:sort select="./mets:FLocat[@LOCTYPE='URL']/@xlink:title"/> 
                        <xsl:if test="position()&lt;5">
							<xsl:call-template name="buildBitstreamSingle">
								<xsl:with-param name="context" select="$context"/>
							</xsl:call-template>
						</xsl:if>
                    </xsl:for-each>
					<xsl:if test="$bitstream-count&gt;4">
						<a class="slider-bitstream-count" href="{ancestor::mets:METS/@OBJID}">
							(more files)
						</a>
					</xsl:if>
					
                </xsl:otherwise>
        	</xsl:choose>
        
        <!--
        <xsl:value-of select="$objxml"/>:
       
        <xsl:apply-templates select="document($objxml)/dri:document/dri:body/dri:div/dri:referenceSet" mode="test"/> -->
    </xsl:template>
	
    <!--
    <xsl:template match="dri:referenceSet" mode="test">
        <p>BLAH!</p>        
    </xsl:template>
	-->
	
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
    
    
    <!-- Generate the info about the item from the metadata section
        Used to display item information on the simple item record -->
    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        
        <table class="ds-includeSet-table">
            <!--
            <tr class="ds-table-row odd">
                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-preview</i18n:text>:</span></td>
                <td>
                    <xsl:choose>
                        <xsl:when test="mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']">
                            <a class="image-link">
                                <xsl:attribute name="href"><xsl:value-of select="@OBJID"/></xsl:attribute>
                                <img alt="Thumbnail">
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/
                                            mets:file/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-preview</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>-->
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
                        <xsl:when test="dim:field[@element='contributor']">
                            <xsl:for-each select="dim:field[@element='contributor']">
                                <a href="/browse?type=author&amp;value={node()}"><xsl:value-of select="node()"/></a>
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
	                <!-- <td><xsl:copy-of select="dim:field[@element='description' and not(@qualifier)]/node()"/></td> -->
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
	                <!-- <td><xsl:copy-of select="dim:field[@element='publisher'][1]/node()"/></td> -->
	                <td>
	                    <xsl:for-each select="dim:field[@element='publisher']">
	                        <xsl:copy-of select="node()"/>
	                        <xsl:if test="count(following-sibling::dim:field[@element='publisher']) != 0"> <br class="simpleItemViewValueBreak"/> </xsl:if>
	                    </xsl:for-each>
	                </td>
	            </tr>
            </xsl:if>
	        <xsl:if test="dim:field[@element='subject']">
	            <tr class="ds-table-row even">
	                <td><span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-subject</i18n:text>:</span></td>
	                <td>
	                	<xsl:for-each select="dim:field[@element='subject']">
                            <a href="/browse?type=subject&amp;value={node()}"><xsl:value-of select="node()"/></a>
                            <xsl:if test="count(following-sibling::dim:field[@element='subject']) != 0"> <br /> </xsl:if>
                        </xsl:for-each>
					</td>
	            </tr>
            </xsl:if>
            <xsl:if test="dim:field[@element='department']">
              <tr class="ds-table-row odd">
                <td><span class="bold"><i18n:text>Department</i18n:text>:</span></td>
                <td> 
                <xsl:for-each select="dim:field[@element='department']">
                  <a href="/browse?type=department&amp;value={node()}"><xsl:value-of select="node()"/></a>
                  <xsl:if test="count(following-sibling::dim:field[@element='department']) != 0"> <br /> </xsl:if>
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


       <!-- Citation block -->
       <h2>Citation</h2>
       <div class="citation">
         <xsl:choose>
           <xsl:when test="dim:field[@element='identifier'][@qualifier='citation']">
             <xsl:copy-of select="dim:field[@element='identifier'][@qualifier='citation']"/>
           </xsl:when>
           <xsl:otherwise>
             <xsl:call-template name="makeCitation"/>
           </xsl:otherwise>
          </xsl:choose>
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
