/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import static org.dspace.app.rest.security.DSpaceLoginFilterFactory.OIDC;

import java.io.IOException;

import org.springframework.security.core.Authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class will filter OpenID Connect (OIDC) requests and try and authenticate them.
 * In this case, the actual authentication is performed by OIDC. After authentication succeeds, OIDC will send
 * the authentication data to this filter in order for it to be processed by DSpace.
 *
 * @author Pasquale Cavallo (pasquale.cavallo at 4science dot it)
 */
public class OidcLoginFilter extends DSpaceLoginFilter {

    public OidcLoginFilter(DSpaceAuthenticationRequest authRequest) {
        super(authRequest);
    }

    @Override
    protected String getAuthMethodName() {
        return OIDC.getAuthMethodName();
    }

    @Override
    protected void successfulAuthentication(
        HttpServletRequest req,
        HttpServletResponse res,
        FilterChain chain,
        Authentication auth
    ) throws IOException, ServletException {
        super.successfulAuthentication(req, res, chain, auth);
        redirectAfterSuccess(req, res);
    }

}
