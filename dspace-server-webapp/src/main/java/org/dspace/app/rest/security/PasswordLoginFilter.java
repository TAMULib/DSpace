/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import static org.dspace.app.rest.security.StatelessAuthDetailsFactory.PASSWORD;

import java.util.Objects;

import org.dspace.app.rest.security.details.PasswordWebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This class will filter /api/authn/login requests to try and authenticate them.
 *
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 * @author Tom Desair (tom dot desair at atmire dot com)
 */
public class PasswordLoginFilter extends StatelessLoginFilter<PasswordWebAuthenticationDetails> {

    /**
     * Initialize a PasswordLoginFilter for the given URL and HTTP method. This login filter will ONLY attempt
     * authentication for requests that match this URL and method. The URL & method are defined in the configuration
     * in WebSecurityConfiguration.
     * @see org.dspace.app.rest.security.WebSecurityConfiguration
     * @param authRequest StatelessAuthRequest with URL, HTTP method name,
     *                    authentication method name, authentication manaher, and REST authentication service
     */
    public PasswordLoginFilter(StatelessAuthRequest authRequest) {
        super(authRequest);
    }

    @Override
    public boolean addCookie() {
        return false;
    }

    @Override
    public void addCredentials(HttpServletRequest request, DSpaceAuthentication authentication) {
        final String user = request.getParameter("user");
        final String password = request.getParameter("password");

        if (Objects.nonNull(user) && user.length() > 0) {
            authentication.withUsername(user);
        }

        if (Objects.nonNull(password) && password.length() > 0) {
            authentication.withCredentials(password);
        }
    }

    @Override
    protected String getAuthMethodName() {
        return PASSWORD.getAuthMethodName();
    }

    @Override
    protected String getProviderName() {
        return "Password";
    }

}
