/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.security.details.SpecialGroupsWebAuthenticationDetails;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.core.Context;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * This abstract class provides the stateless base for authentication. Keep in mind, this filter runs *after*
 * {@link StatelessAuthenticationFilter} (which looks for authentication data in the request itself). So, in some scenarios
 * (e.g. after a Shibboleth login) the StatelessAuthenticationFilter does the actual authentication, and this Filter
 * just ensures the auth token (JWT) is sent back in an Authorization header.
 *
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 * @author Tom Desair (tom dot desair at atmire dot com)
 */
public abstract class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger log = LogManager.getLogger();

    protected final AuthenticationManager authenticationManager;

    protected final RestAuthenticationService restAuthenticationService;

    /**
     * Initialize a StatelessLoginFilter for the given URL and HTTP method. This login filter will ONLY attempt
     * authentication for requests that match this URL and method. The URL & method are defined in the configuration
     * in WebSecurityConfiguration.
     * @see org.dspace.app.rest.security.WebSecurityConfiguration
     * @param authRequest StatelessAuthRequest with URL, HTTP method name,
     *                    authentication method name, authentication manaher, and REST authentication service
     */
    public StatelessLoginFilter(StatelessAuthenticationRequest authRequest) {
        super(new AntPathRequestMatcher(authRequest.getUrl(), authRequest.getHttpMethodName()));
        this.authenticationManager = authRequest.getAuthenticationManager();
        this.restAuthenticationService = authRequest.getRestAuthenticationService();
    }

    @Override
    public void afterPropertiesSet() {

    }

    /**
     * Attempt to authenticate the user by using Spring Security's AuthenticationManager.
     * The AuthenticationManager will delegate this task to one or more AuthenticationProvider classes.
     * <P>
     * For DSpace, our custom AuthenticationProvider is {@link EPersonRestAuthenticationProvider}.
     *
     * @param req current request
     * @param res current response
     * @return a Spring Security Authentication object if authentication succeeds
     * @throws AuthenticationException if authentication fails
     * @see EPersonRestAuthenticationProvider
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        Context context = ContextUtil.obtainContext(req);

        if (isEnabled(context, req)) {
            context.setAuthenticationMethod(getAuthMethodName());
        }

        DSpaceAuthentication authentication = DSpaceAuthentication.create();
            // .withDetails(getWebAuthenticationDetails(req));

        addCredentials(req, authentication);

        log.info(String.format("%s authentication attempt (new context): %s", getClass().getSimpleName(), authentication));
        System.out.println(String.format("%s authentication attempt (new context): %s", getClass().getSimpleName(), authentication));

        return ((DSpaceAuthentication) authenticationManager.authenticate(authentication));
            // .withDetails(getWebAuthenticationDetails(req));
    }

    /**
     * If the above attemptAuthentication() call was successful (no authentication error was thrown),
     * then this method will take the returned {@link DSpaceAuthentication} class (which includes all
     * the data from the authenticated user) and add the authentication data to the response.
     * <P>
     * For DSpace, this is calling our {@link org.dspace.app.rest.security.jwt.JWTTokenRestAuthenticationServiceImpl}
     * in order to create a JWT based on the authentication data & send that JWT back in the response.
     *
     * @param req current request
     * @param res response
     * @param chain FilterChain
     * @param auth Authentication object containing info about user who had a successful authentication
     * @throws IOException
     * @throws ServletException
     * @see org.dspace.app.rest.security.jwt.JWTTokenRestAuthenticationServiceImpl
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
        DSpaceAuthentication dSpaceAuthentication = ((DSpaceAuthentication) auth);
            // .withDetails(getWebAuthenticationDetails(req));

        log.debug(String.format("%s authentication successful for EPerson %s", getClass().getSimpleName(), dSpaceAuthentication.getName()));
        System.out.println(String.format("%s authentication successful for EPerson %s", getClass().getSimpleName(), dSpaceAuthentication.getName()));

        restAuthenticationService.addAuthenticationDataForUser(req, res, dSpaceAuthentication, addCookie());
    }

    /**
     * If the above attemptAuthentication() call was unsuccessful, then ensure that the response is a 401 Unauthorized
     * AND it includes a WWW-Authentication header. We use this header in DSpace to return all the enabled
     * authentication options available to the UI (along with the path to the login URL for each option)
     * @param request current request
     * @param response current response
     * @param failed exception that was thrown by attemptAuthentication()
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {

        String authenticateHeaderValue = restAuthenticationService.getWwwAuthenticateHeaderValue(request, response);

        response.setHeader("WWW-Authenticate", authenticateHeaderValue);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed!");

        log.error("Authentication failed (status:{})",
                  HttpServletResponse.SC_UNAUTHORIZED, failed);
    }

    /**
     * Return only details built from the source and empty immutable map otherwise.
     * 
     * @param req HttpServletRequest incoming request to check for details
     * @return Map<String, Object> mutable details otherwise immutable map
     * @see 
     */
    protected Map<String, Object> getWebAuthenticationDetails(HttpServletRequest req) {
        return authenticationDetailsSource != null
            ? ((SpecialGroupsWebAuthenticationDetails) authenticationDetailsSource.buildDetails(req)).getDetails()
            : Collections.emptyMap();
    }

    protected boolean addCookie() {
        return true;
    }

    protected void addCredentials(HttpServletRequest request, DSpaceAuthentication authentication) {
        // nothing todo here
    }

    protected abstract String getAuthMethodName();

    private boolean isEnabled(Context context, HttpServletRequest request) {
        final String authMethodName = getAuthMethodName();
        final String servletPath = request.getServletPath();
        final String factoryAuthMethodName = StatelessLoginFilterFactory.getAuthMethodNameByServletPath(servletPath);

        // only enable if not already authenticated
        return Objects.isNull(context.getCurrentUser())
            && Objects.nonNull(authMethodName)
            && Objects.nonNull(factoryAuthMethodName)
            && authMethodName.equals(factoryAuthMethodName);
    }

}
