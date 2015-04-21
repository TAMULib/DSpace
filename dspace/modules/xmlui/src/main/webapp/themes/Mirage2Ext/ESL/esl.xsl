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