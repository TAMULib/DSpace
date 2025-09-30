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

    private String username;

    private String password;

    private List<GrantedAuthority> authorities;

    private boolean authenticated;

    private Object details;

    private Instant previousLoginDate;

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

    DSpaceAuthentication forEPerson(EPerson ePerson) {
         this.previousLoginDate = ePerson.getPreviousActive();
         this.username = ePerson.getEmail();

         return this;
    }

    DSpaceAuthentication withUsername(String username) {
        this.username = username;

        return this;
    }

    DSpaceAuthentication withCredentials(String password) {
        this.password = password;

        return this;
    }

    DSpaceAuthentication withDetails(Object details) {
        this.details = details;

        return this;
    }

    DSpaceAuthentication withGrantedAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;

        return this;
    }

    DSpaceAuthentication withAuthenticatedTrue() {
        this.authenticated = true;

        return this;
    }

    public static DSpaceAuthentication create() {
        return new DSpaceAuthentication();
    }
}
