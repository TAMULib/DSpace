package org.dspace.authenticate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dspace.core.Context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * TAMU Customization - #382 Shibboleth Special Groups
 * Utility for authentication.
 */
public class AuthenticationUtility {

    public static final String PASSWORD_PATH = "/api/authn/login";
    public static final String SHIBBOLETH_PATH = "/api/authn/shibboleth";
    public static final String ORCID_PATH = "/api/authn/orcid";
    public static final String OIDC_PATH = "/api/authn/oidc";
    public static final String SAML_PATH = "/api/authn/saml";

    public static final String AUTHENTICATION_METHOD = "authenticationMethod";

    private AuthenticationUtility() {
        // private empty constructor
    }

    public enum Mapping {
        // update if constants used in AuthenticationMethod#getName
        PASSWORD(new PasswordAuthentication().getName(), PASSWORD_PATH),
        SHIBBOLETH(new ShibAuthentication().getName(), SHIBBOLETH_PATH),
        ORCID(new OrcidAuthentication().getName(), ORCID_PATH),
        OIDC(new OidcAuthentication().getName(), OIDC_PATH),
        SAML(new SamlAuthentication().getName(), SAML_PATH);

        private static final Map<String, String> urlToName = new HashMap<>();

        static {
            for (Mapping mapping : Mapping.values()) {
                urlToName.put(mapping.getMethodUrl(), mapping.getMethodName());
            }
        }

        /* must match AuthenticationMethod#getName */
        private final String methodName;

        /* must match AbstractAuthenticationProcessingFilter URL arguments in WebSecurityConfiguration */
        private final String methodUrl;

        Mapping(String methodName, String methodUrl) {
            this.methodName = methodName;
            this.methodUrl = methodUrl;
        }

        public String getMethodName() {
            return methodName;
        }

        // update WebSecurityConfiguration to prevent unsynchronized changes of authentication method URLs
        public String getMethodUrl() {
            return methodUrl;
        }

        public static String getMethodName(String url) {
            return urlToName.get(url);
        }
    }

    /**
     * Update context authentication method from request servlet path or `authenticationMethod`
     * request attribute set during authentication process.
     * 
     * Sets context authentication method.
     * Sets request `authenticationMethod` attribute.
     *
     * @param context Context current DSpace context
     * @param request HttpServletRequest current request
     */
    public static void updateAuthenticationMethod(Context context, HttpServletRequest request) {
        String authMethod = context.getAuthenticationMethod();

        if (request != null && StringUtils.isBlank(authMethod)) {
            authMethod = Mapping.getMethodName(request.getServletPath());
        }

        // if (StringUtils.isBlank(authMethod)) {
        //     authMethod = (String) request.getAttribute(AUTHENTICATION_METHOD);
        // }

        if (StringUtils.isNotBlank(authMethod)) {
            context.setAuthenticationMethod(authMethod);
            // request.setAttribute(AUTHENTICATION_METHOD, authMethod);
        }
    }
}
