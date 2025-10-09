/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authenticate;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authenticate.service.AuthenticationService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.core.factory.CoreServiceFactory;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Access point for the stackable authentication methods.
 * <p>
 * This class initializes the "stack" from the DSpace configuration,
 * and then invokes methods in the appropriate order on behalf of clients.
 * <p>
 * See the AuthenticationMethod interface for details about what each
 * function does.
 * <p>
 * <b>Configuration</b><br>
 * The stack of authentication methods is defined by one property in the DSpace configuration:
 * <pre>
 *   plugin.sequence.org.dspace.eperson.AuthenticationMethod = <em>a list of method class names</em>
 *     <em>e.g.</em>
 *   plugin.sequence.org.dspace.eperson.AuthenticationMethod = \
 *       org.dspace.eperson.X509Authentication, \
 *       org.dspace.eperson.PasswordAuthentication
 * </pre>
 * <p>
 * The "stack" is always traversed in order, with the methods
 * specified first (in the configuration) thus getting highest priority.
 *
 * @author Larry Stone
 * @see AuthenticationMethod
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    /**
     * Logging category
     */
    private final Logger log = LogManager.getLogger();

    @Autowired(required = true)
    protected EPersonService ePersonService;

    protected AuthenticationServiceImpl() {

    }

    public List<AuthenticationMethod> getAuthenticationMethodStack() {
        return Arrays.asList((AuthenticationMethod[]) CoreServiceFactory.getInstance().getPluginService()
                                                                        .getPluginSequence(AuthenticationMethod.class));
    }

    @Override
    public int authenticate(Context context,
                            String username,
                            String password,
                            String realm,
                            HttpServletRequest request) {
        return authenticateInternal(context, username, password, realm,
                                    request, false);
    }

    @Override
    public int authenticateImplicit(Context context,
                                    String username,
                                    String password,
                                    String realm,
                                    HttpServletRequest request) {
        return authenticateInternal(context, username, password, realm,
                                    request, true);
    }

    protected int authenticateInternal(Context context,
                                       String username,
                                       String password,
                                       String realm,
                                       HttpServletRequest request,
                                       boolean implicitOnly) {
        int results = AuthenticationMethod.BAD_ARGS;

        for (AuthenticationMethod method : getAuthenticationMethodStack()) {
            if (!implicitOnly || method.isImplicit()) {
                int result = 0;
                try {
                    result = method.authenticate(context, username, password, realm, request);
                } catch (SQLException e) {
                    result = AuthenticationMethod.NO_SUCH_USER;
                }
                if (result == AuthenticationMethod.SUCCESS) {
                    updateLastActiveDate(context);

                    return result;
                }
                if (result < results) {
                    results = result;
                }
            }
        }

        return results;
    }

    @Override
    public void updateLastActiveDate(Context context) {
        EPerson me = context.getCurrentUser();
        if (me != null) {
            me.setLastActive(Instant.now());
            try {
                ePersonService.update(context, me);
            } catch (SQLException ex) {
                log.error("Could not update last-active stamp", ex);
            } catch (AuthorizeException ex) {
                log.error("Could not update last-active stamp", ex);
            }
        }
    }

    @Override
    public boolean canSelfRegister(Context context,
                                   HttpServletRequest request,
                                   String username)
        throws SQLException {
        for (AuthenticationMethod method : getAuthenticationMethodStack()) {
            if (method.canSelfRegister(context, request, username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean allowSetPassword(Context context,
                                    HttpServletRequest request,
                                    String username)
        throws SQLException {
        for (AuthenticationMethod method : getAuthenticationMethodStack()) {
            if (method.allowSetPassword(context, request, username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initEPerson(Context context,
                            HttpServletRequest request,
                            EPerson eperson)
        throws SQLException {
        for (AuthenticationMethod method : getAuthenticationMethodStack()) {
            method.initEPerson(context, request, eperson);
        }
    }

    @Override
    public List<Group> getSpecialGroups(Context context,
                                        HttpServletRequest request) throws SQLException {
        final List<Group> groups = new ArrayList<>();

        for (AuthenticationMethod method : getAuthenticationMethodStack()) {
            if (method.areSpecialGroupsApplicable(context, request)) {
                List<Group> gl = method.getSpecialGroups(context, request);
                if (gl.size() > 0) {
                    groups.addAll(gl);
                }
            }
        }

        return groups;
    }

    @Override
    public Iterator<AuthenticationMethod> authenticationMethodIterator() {
        return getAuthenticationMethodStack().iterator();
    }

    @Override
    public String getAuthenticationMethod(final Context context, final HttpServletRequest request) {
        final Iterator<AuthenticationMethod> authenticationMethodIterator = authenticationMethodIterator();

        while (authenticationMethodIterator.hasNext()) {
            final AuthenticationMethod authenticationMethod = authenticationMethodIterator.next();

            if (authenticationMethod.isUsed(context, request)) {
                return authenticationMethod.getName();
            }
        }

        return null;
    }

    @Override
    public boolean canChangePassword(Context context, EPerson ePerson, String currentPassword) {

        for (AuthenticationMethod method : getAuthenticationMethodStack()) {
            if (method.getName().equals(context.getAuthenticationMethod())) {
                return method.canChangePassword(context, ePerson, currentPassword);
            }
        }

        return false;
    }
}
