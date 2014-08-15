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
                                A&amp;M University Galveston · PO Box 1675 · Galveston, 
                                Texas 77553 -1675 · Toll Free 1-87-Sea-Aggie<br/>
                                    
                            <a href="http://www.tamug.edu/computing/accessibility.htm">Accessibility</a> | 
                            <a href="http://www.tamug.edu/priv.htm">Privacy Statement</a> | 
                                   <a href="http://www.tamug.edu/legal.htm">Legal Notice</a> | 
                                    <a href="http://finance.tamu.edu/vp/electronic%2Dreports/">State Req. Reports</a> |
                                    <a href="http://www.tamug.edu/forms/compact.htm">Compact with Texans</a> | 
                                    <a href="http://www.tamug.edu/physical/utilities/index.htm">Energy Strategic Plan</a> | 
                                    <a href="http://www.tamug.edu/emailWebmaster.html">Webmaster</a> | 
                                <a href="http://www.tamug.edu/quickhelp.html">Contact Us</a> |
                <a href="http://homelandsecurity.tamu.edu/">TAMU Homeland Security</a> | 
                                    <a href="http://tamusystem.tamu.edu/">Texas A&amp;M University System</a> | 
                                    <a href="http://www.tamu.edu/">Texas A&amp;M College Station</a> | 
                                    <a href="http://www.qatar.tamu.edu/">Texas A&amp;M Qatar</a> | 
                                    <a href="http://www.tamug.edu/">Texas A&amp;M Galveston</a> |
                               <a href="http://www2.tsl.state.tx.us/trail/">Statewide Search</a> |
                               <a href="http://www.texashomelandsecurity.com/">Texas Homeland Security</a> |
                               <a href="http://www.state.tx.us/">State of Texas</a><br/>
                                    
                                    <a href="http://www.tamug.edu/police/Security_Brochure.mht">Annual Security Report</a>
                        
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
        <!--                    
            <a href="http://digital.library.tamu.edu">                            
            <div id="ds-footer-logo"></div>
            </a>
            <p>
            This website is using Manakin, a new front end for DSpace created by Texas A&amp;M University 
            Libraries. The interface can be extensively modified through Manakin Aspects and XSL based Themes. 
            For more information visit 
            <a href="http://digital.library.tamu.edu">http://digital.library.tamu.edu</a> and
            <a href="http://dspace.org">http://dspace.org</a>                            
            </p>
        -->
    </xsl:template>


</xsl:stylesheet>
