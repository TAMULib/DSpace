/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.RequestService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.util.UUIDUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Custom Spring authentication filter for Stateless authentication, intercepts requests to check for valid
 * authentication. This runs before *every* request in the DSpace backend to see if any authentication data
 * is passed in that request. If so, it authenticates the EPerson in the current Context.
 *
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 * @author Tom Desair (tom dot desair at atmire dot com)
 */
public class StatelessAuthenticationFilter extends BasicAuthenticationFilter {

    private static final Logger log = LogManager.getLogger();

    private static final String ON_BEHALF_OF_REQUEST_PARAM = "X-On-Behalf-Of";

    private final RestAuthenticationService restAuthenticationService;

    private final EPersonRestAuthenticationProvider authenticationProvider;

    private final RequestService requestService;

    private final AuthorizeService authorizeService
            = AuthorizeServiceFactory.getInstance().getAuthorizeService();

    private final EPersonService ePersonService
            = EPersonServiceFactory.getInstance().getEPersonService();

    private final ConfigurationService configurationService
            = DSpaceServicesFactory.getInstance().getConfigurationService();

    public StatelessAuthenticationFilter(AuthenticationManager authenticationManager,
                                         RestAuthenticationService restAuthenticationService,
                                         EPersonRestAuthenticationProvider authenticationProvider,
                                         RequestService requestService) {
        super(authenticationManager);
        this.requestService = requestService;
        this.restAuthenticationService = restAuthenticationService;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        Authentication authentication;
        try {
            authentication = getAuthentication(req, res);
        } catch (AuthorizeException e) {
            // just return an error, but do not log
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required");
            return;
        } catch (IllegalArgumentException | SQLException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Authentication request is invalid or incorrect");
            log.error("Authentication request is invalid or incorrect (status:{})",
                      HttpServletResponse.SC_BAD_REQUEST, e);
            return;
        } catch (AccessDeniedException e) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access is denied");
            log.error("Access is denied (status:{})", HttpServletResponse.SC_FORBIDDEN, e);
            return;
        }
        // If we have a valid Authentication, save it to Spring Security
        if (authentication != null) {
            Context context = ContextUtil.obtainContext(req);
            threadRequestSystemOut(context, req, "SAF: Authentication success. Adding authentication to the security context.");
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(req, res);
    }

    /**
     * This method returns an Authentication object
     * This Authentication object will be attempted to be for the eperson with the uuid in the parameter. In case
     * this is able to be done properly, we'll be returning the EPerson Authentication.
     * If the Authentication object returned is not null, we'll be logged in as this EPerson given through from the
     * request.
     * If something goes wrong, we'll throw an IllegalArgumentException, AccessDeniedException or AuthorizeException
     * depending on what went wrong. This will be caught in the calling method and handled appropriately with the
     * corresponding response codes
     * @param request       The current request
     * @param res           The current response
     * @return              An Authentication object for the EPerson with the uuid in the parameter
     * @throws IOException  If something goes wrong
     */
    private Authentication getAuthentication(HttpServletRequest request, HttpServletResponse res)
        throws AuthorizeException, SQLException {

        if (restAuthenticationService.hasAuthenticationData(request)) {
            Context context = ContextUtil.obtainContext(request);
            final String servletPath = request.getServletPath();

            threadRequestSystemOut(context, request, "SAF: Request servlet path: " + servletPath);
            String authMethod = null;

            switch (servletPath) {
                case "/api/authn/login":
                    String user = request.getParameter("user");
                    String password = request.getParameter("password");

                    if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {
                        authMethod = "password"; // new PasswordAuthentication().getName()
                        threadRequestSystemOut(context, request, "SLF: Password Authentication");
                    } else {
                        authMethod = null;
                    }
                    break;
                case "/api/authn/shibboleth":
                    authMethod = "shib"; // new ShibAuthentication().getName()
                    threadRequestSystemOut(context, request, "SAF: Shibboleth Authentication");
                    break;
                case "/api/authn/orcid":
                    authMethod = "orcid"; // new OrcidAuthentication().getName()
                    threadRequestSystemOut(context, request, "SAF: Orcid Authentication");
                    break;
                case "/api/authn/oidc":
                    authMethod = "oidc"; // new OidcAuthentication().getName()
                    threadRequestSystemOut(context, request, "SAF: OIDC Authentication");
                    break;
                case "/api/authn/saml":
                    authMethod = "saml"; // new SamlAuthentication().getName()
                    threadRequestSystemOut(context, request, "SAF: SAML Authentication");
                    break;
                default:
                    break;
            }

            if (StringUtils.isNotEmpty(authMethod)) {
                threadRequestSystemOut(context, request, "SAF: Setting auth method " + authMethod + " on request attribute am");
                request.setAttribute("am", authMethod);
                threadRequestSystemOut(context, request, "SAF: Setting auth method " + authMethod + " on context");
                context.setAuthenticationMethod(authMethod);
            }

            threadRequestSystemOut(context, request, "SAF: Context: " + context);
            threadRequestSystemOut(context, request, "SAF: Context authentication method: " + context.getAuthenticationMethod());
            threadRequestSystemOut(context, request, "SAF: Context special groups: " + context.getSpecialGroupUuids());

            // parse the token.
            EPerson eperson = restAuthenticationService.getAuthenticatedEPerson(request, res, context);
            if (eperson != null) {
                threadRequestSystemOut(context, request, "SAF: Found authentication data in request for EPerson " + eperson.getEmail());
                log.debug("Found authentication data in request for EPerson {}", eperson::getEmail);
                //Pass the eperson ID to the request service
                requestService.setCurrentUserId(eperson.getID());

                //Get the Spring authorities for this eperson
                List<GrantedAuthority> authorities = authenticationProvider.getGrantedAuthorities(context);
                String onBehalfOfParameterValue = request.getHeader(ON_BEHALF_OF_REQUEST_PARAM);
                if (onBehalfOfParameterValue != null) {
                    if (configurationService.getBooleanProperty("webui.user.assumelogin")) {
                        return getOnBehalfOfAuthentication(context, onBehalfOfParameterValue, res);
                    } else {
                        throw new IllegalArgumentException("The 'login as' feature is not allowed" +
                                                     " due to the current configuration");
                    }
                }

                //Return the Spring authentication object
                return new DSpaceAuthentication(eperson, authorities);
            } else {
                return null;
            }
        } else {
            if (request.getHeader(ON_BEHALF_OF_REQUEST_PARAM) != null) {
                throw new AuthorizeException("Must be logged in (as an admin) to use the 'login as' feature");
            }
        }

        return null;
    }

    private void threadRequestSystemOut(Context context, HttpServletRequest request, String message) {
        System.out.println(
            String.format(
                "Context %10s - thread %10s - request %10s: %s",
                context.hashCode(),
                Thread.currentThread().getId(),
                request.getRequestId(), message
            )
        );
    }

    private Authentication getOnBehalfOfAuthentication(Context context, String onBehalfOfParameterValue,
                                                       HttpServletResponse res) throws SQLException {

        if (!authorizeService.isAdmin(context)) {
            throw new AccessDeniedException("Only admins are allowed to use the login as feature");
        }
        UUID epersonUuid = UUIDUtils.fromString(onBehalfOfParameterValue);
        if (epersonUuid == null) {
            throw new IllegalArgumentException("The given UUID in the X-On-Behalf-Of header " +
                                                   "was not a proper UUID");
        }
        EPerson onBehalfOfEPerson = ePersonService.find(context, epersonUuid);
        if (onBehalfOfEPerson == null) {
            throw new IllegalArgumentException("The given UUID in the X-On-Behalf-Of header " +
                                                   "was not a proper EPerson UUID");
        }
        if (!authorizeService.isAdmin(context, onBehalfOfEPerson)) {
            requestService.setCurrentUserId(epersonUuid);
            context.switchContextUser(onBehalfOfEPerson);
            log.debug("Found 'on-behalf-of' authentication data in request for EPerson {}",
                    onBehalfOfEPerson::getEmail);
            return new DSpaceAuthentication(onBehalfOfEPerson,
                                            authenticationProvider.getGrantedAuthorities(context));
        } else {
            throw new IllegalArgumentException("You're unable to use the login as feature to log " +
                                                   "in as another admin");
        }
    }

}
