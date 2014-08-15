<?xml version="1.0" encoding="UTF-8"?>

<!--  
    Author: Scott Phillips
    
    Stylesheets that are common to both the right and left themes.
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
 
</xsl:stylesheet>
