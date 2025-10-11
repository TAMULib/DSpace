/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import static org.dspace.app.rest.security.DSpaceLoginFilterFactory.ORCID;
import static org.dspace.authenticate.OrcidAuthentication.ORCID_AUTH_ATTRIBUTE;
import static org.dspace.authenticate.OrcidAuthentication.ORCID_DEFAULT_REGISTRATION_URL;
import static org.dspace.authenticate.OrcidAuthentication.ORCID_REGISTRATION_TOKEN_ATTRUBUTE;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class will filter ORCID requests and try and authenticate them.
 * In this case, the actual authentication is performed by ORCID. After authentication succeeds, ORCID will send
 * the authentication data to this filter in order for it to be processed by DSpace.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 */

public class OrcidLoginFilter extends DSpaceLoginFilter {

    private static final Logger log = LogManager.getLogger(OrcidLoginFilter.class);

    public OrcidLoginFilter(DSpaceAuthenticationRequest authRequest) {
        super(authRequest);
    }

    @Override
    protected String getAuthMethodName() {
        return ORCID.getAuthMethodName();
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
        super.successfulAuthentication(req, res, chain, auth);
        redirectAfterSuccess(req, res);
    }

    @Override
    protected void unsuccessfulAuthentication(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException failed
    ) throws IOException, ServletException {

        String baseRediredirectUrl = configurationService.getProperty("dspace.ui.url");
        String redirectUrl = baseRediredirectUrl + "/error?status=401&code=orcid.generic-error";
        Object registrationToken = request.getAttribute(ORCID_REGISTRATION_TOKEN_ATTRUBUTE);
        if (registrationToken != null) {
            final String orcidRegistrationDataUrl =
                configurationService.getProperty("orcid.registration-data.url", ORCID_DEFAULT_REGISTRATION_URL);
            redirectUrl = baseRediredirectUrl + MessageFormat.format(orcidRegistrationDataUrl, registrationToken);
            if (log.isDebugEnabled()) {
                log.debug(
                    "Orcid authentication failed for user with ORCID {}.",
                    request.getAttribute(ORCID_AUTH_ATTRIBUTE)
                );
                log.debug("Redirecting to {} for registration completion.", redirectUrl);
            }
        }

        response.sendRedirect(redirectUrl); // lgtm [java/unvalidated-url-redirection]
    }

}
