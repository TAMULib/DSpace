<?xml version="1.0" encoding="UTF-8"?>

<!--  
    Author: Scott Phillips
    
    Rewrite all links in the HTML document so that 'ar/' is inserted after the contextpath. Thus a link for
    /whatever would be rewritten for /ar/whatever if the webapp was mounted at the root. Or if the webapp is
    mounted at /qatar, so the link would be /qatar/whatever the link would be rewritten as /qatar/ar/whatever.
-->    

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
     version="1.0">
    
    <xsl:output indent="yes"/>

	<!-- Determine the context-path where this webapp is being hosted at based upon the link to the theme's main stylesheet. All the paths are based upon this location.'-->
    <xsl:variable name="contextpath" select="substring-before(//*[local-name() = 'link'][@type='text/css'][@media='screen'][@rel='stylesheet'][contains(@href,'style.css')]/@href,'themes/')"/>

	<!-- Rewrite any links -->
  <xsl:template match="*[local-name() = 'a']">

    <xsl:choose>
      <!-- Case 0: Exclude these top level links to bitstreams or similar paths-->
      <xsl:when test="starts-with(./@href,concat($contextpath,'bitstream/')) or starts-with(./@href,concat($contextpath,'html/')) or starts-with(./@href,concat($contextpath,'retrieve/')) or starts-with(./@href,concat($contextpath,'exportdownload/')) or starts-with(./@href,concat($contextpath,'csv/')) or starts-with(./@href,concat($contextpath,'JSON/')) or starts-with(./@href,concat($contextpath,'metadata/')) or starts-with(./@href,concat($contextpath,'feed/')) or starts-with(./@href,concat($contextpath,'open-search/')) or starts-with(./@href,concat($contextpath,'htmlmap/')) or starts-with(./@href,concat($contextpath,'sitemap/')) or starts-with(./@href,concat($contextpath,'themes/')) or starts-with(./@href,concat($contextpath,'static/'))">
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:when>

      <!-- Case 1: Rewrite links to HTML documents-->
      <xsl:when test="starts-with(./@href,$contextpath)">
        <xsl:copy>
          <xsl:attribute name="href"><xsl:value-of select="$contextpath"/><xsl:text>ar</xsl:text><xsl:value-of select="substring(./@href,string-length($contextpath))"/></xsl:attribute>
          <xsl:apply-templates select="@*[local-name() != 'href']|node()"/>
        </xsl:copy>
      </xsl:when>

      <!-- Case 3: Relative links are left alone. -->
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


	<!-- Copy the document element for element, attribute for attribute -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

</xsl:stylesheet>