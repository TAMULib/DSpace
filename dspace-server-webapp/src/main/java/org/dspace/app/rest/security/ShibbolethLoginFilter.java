/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import static org.dspace.app.rest.security.DSpaceLoginFilterFactory.SHIBBOLETH;

import java.io.IOException;

import org.springframework.security.core.Authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class will filter Shibboleth requests to see if the user has been authenticated via Shibboleth.
 * <P>
 * The overall Shibboleth login process is as follows:
 *   1. When Shibboleth plugin is enabled, client/UI receives Shibboleth's absolute URL in WWW-Authenticate header.
 *      See {@link org.dspace.authenticate.ShibAuthentication} loginPageURL() method.
 *   2. Client sends the user to that URL when they select Shibboleth authentication.
 *   3. User logs in using Shibboleth
 *   4. If successful, they are redirected by Shibboleth to the path where this Filter is "listening" (that path
 *      is passed to Shibboleth as a URL param in step 1)
 *   5. This filter then intercepts the request in order to check for a valid Shibboleth login (see
 *      ShibAuthentication.authenticate()) and stores that user info in a JWT. It also saves that JWT in a *temporary*
 *      authentication cookie.
 *   6. This filter then looks for a "redirectUrl" param (also a part of the original URL from step 1), and redirects
 *      the user to that location (after verifying it's a trusted URL). Usually this is a redirect back to the
 *      Client/UI page where the User started.
 *   7. At that point, the client reads the JWT from the Cookie, and sends it back in a request to /api/authn/login,
 *      which triggers the server-side to destroy the Cookie and move the JWT into a Header
 * <P>
 * This Shibboleth Authentication process is tested in AuthenticationRestControllerIT.
 *
 * @author Giuseppe Digilio (giuseppe dot digilio at 4science dot it)
 * @author Tim Donohue
 * @see org.dspace.authenticate.ShibAuthentication
 */
public class ShibbolethLoginFilter extends DSpaceLoginFilter {

    public ShibbolethLoginFilter(DSpaceAuthenticationRequest authRequest) {
        super(authRequest);
    }

    @Override
    protected String getAuthMethodName() {
        return SHIBBOLETH.getAuthMethodName();
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
