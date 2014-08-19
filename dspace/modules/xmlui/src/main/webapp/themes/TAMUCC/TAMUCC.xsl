<?xml version="1.0" encoding="UTF-8"?>

<!--  
    Author: Alexey Maslov
-->    

<xsl:stylesheet 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:import href="../dri2xhtml.xsl"/>
    <xsl:output indent="yes"/>
     
       
    <xsl:template name="buildFooter">
        <div id="ds-footer">
            <a href="http://www.tamug.edu">                            
                <span id="ds-footer-logo"></span>
            </a>
            <p>
                <a href="http://www.state.tx.us/">Texas Web Site</a> - 
                <a href="comptex/index.htm">Compact with Texans</a> - 
                <a href="http://www.tsl.state.tx.us/trail/index.html">Texas Govt. Search</a> -  
                <a href="privacy.htm">Privacy Statement</a> - 
                <a href="openrec.html">Open Records</a> -  
                <a href="http://physicalplant.tamucc.edu/general/plans/energy">Energy Savings Program</a>
                <br/>
                Texas A&amp;M University-Corpus Christi &#8226; 6300 Ocean Drive, Corpus Christi, Texas 78412 &#8226; 361-825-5700
                <br/>
            </p>
            <div id="ds-footer-links">
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/contact</xsl:text>
                    </xsl:attribute>
                    <i18n:text>xmlui.dri2xhtml.structural.contact-link</i18n:text>
                </a>
                <xsl:text> | </xsl:text>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/feedback</xsl:text>
                    </xsl:attribute>
                    <i18n:text>xmlui.dri2xhtml.structural.feedback-link</i18n:text>
                </a>
            </div>
        </div>
        <div class="spacer"/>
        <!--                    
            <a href="http://digital.library.tamu.edu">                            
            <div id="ds-footer-logo"></div>
            </a>
            <p>
            This website is using Manakin, a new front end for DSpace created by Texas A&amp;M University 
            Libraries. The interface can be extensively modified through Manakin Aspects and XSL based Themes. 
            For more information visit 
            <a href="http://digital.library.tamu.edu">http://digital.library..tamu.edu</a> and
            <a href="http://dspace.org">http://dspace.org</a>                            
            </p>
        -->
    </xsl:template>

</xsl:stylesheet>
