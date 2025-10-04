/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.eperson.EPerson;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Custom Authentication for use with DSpace
 *
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 * @author Tom Desair (tom dot desair at atmire dot com)
 */
public class DSpaceAuthentication implements Authentication {

    private static final Logger log = LogManager.getLogger();

    private String username;

    private String password;

    private List<GrantedAuthority> authorities;

    private boolean authenticated;

    private Object details;

    private Instant previousLoginDate;

    // Only allow use of static factory method create
    private DSpaceAuthentication() {
        this.authenticated = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getName() {
        return username;
    }

    public Instant getPreviousLoginDate() {
        return previousLoginDate;
    }

    /**
     * Apply EPerson to authentication.
     *
     * Sets the username and previous login date.
     *
     * @param ePerson the current EPerson requesting authentication
     * @return this DSpaceAuthentication
     */
    DSpaceAuthentication forEPerson(EPerson ePerson) {
        this.username = ePerson.getEmail();
        this.previousLoginDate = ePerson.getPreviousActive();

        log.debug("Adding EPerson {} to authentication with previous active date {}", username, previousLoginDate);

        return this;
    }

    /**
     * Add user to authentication for password authentication.
     *
     * Sets the username.
     *
     * @param username a String username credential
     * @return this DSpaceAuthentication
     */
    DSpaceAuthentication withUsername(String username) {
        this.username = username;

        log.debug("Adding user {} to authentication", username);

        return this;
    }

    /**
     * Add password to authentication for password authentication.
     *
     * Sets the password.
     *
     * @param username a String password credential
     * @return this DSpaceAuthentication
     */
    DSpaceAuthentication withCredentials(String password) {
        this.password = password;

        // dont log password!
        log.debug("Credentials added to authentication for {}", username);

        return this;
    }

    /**
     * Add details to the authentication.
     *
     * Sets the details.
     *
     * Currently only a Set<String> for all WebAuthenticationDetails as list of special group names.
     *
     * @param details an Object details
     * @return this DSpaceAuthentication
     */
    DSpaceAuthentication withDetails(Object details) {
        this.details = details;

        log.debug("Adding details {} to authentication for {}", details, username);

        return this;
    }

    /**
     * Add granted authorities to the authentication.
     *
     * Sets the authorities.
     *
     * @param authorities a List of GrantedAuthority
     * @return this DSpaceAuthentication
     */
    DSpaceAuthentication withGrantedAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;

        log.debug("Adding authorities {} to authentication for {}", authorities, username);

        return this;
    }

    /**
     * Authenticates the authentication request.
     *
     * Sets the authenticated true.
     *
     * @return this DSpaceAuthentication
     */
    DSpaceAuthentication withAuthenticatedTrue() {
        this.authenticated = true;

        log.debug("Authentication for {} authenticated", username);

        return this;
    }

    /**
     * Create a DSpace authentication object for Spring Security Context.
     *
     * Sets the authenticated true.
     *
     * @return this DSpaceAuthentication
     */
    public static DSpaceAuthentication create() {
        log.debug("Authentication created for thread {}", Thread.currentThread().getId());

        return new DSpaceAuthentication();
    }
}
