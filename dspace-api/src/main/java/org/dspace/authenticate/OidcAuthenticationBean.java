/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authenticate;


import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static org.apache.commons.lang.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authenticate.oidc.OidcClient;
import org.dspace.authenticate.oidc.model.OidcTokenResponseDTO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.services.ConfigurationService;
import org.dspace.web.ContextUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * OpenID Connect Authentication for DSpace.
 *
 * This implementation doesn't allow/needs to register user, which may be holder
 * by the openID authentication server.
 *
 * @link   https://openid.net/developers/specs/
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 */
public class OidcAuthenticationBean implements AuthenticationMethod {

    public static final String OIDC_AUTH_ATTRIBUTE = "oidc";

    protected GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();

    private static final String LOGIN_PAGE_URL_FORMAT = "%s?client_id=%s&response_type=code&scope=%s&redirect_uri=%s";

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String OIDC_AUTHENTICATED = "oidc.authenticated";

    private static ThreadLocal<Set<String>> threadLocalGroupNames = ThreadLocal.withInitial(() -> new HashSet<>());

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private OidcClient oidcClient;

    @Autowired
    private EPersonService ePersonService;

    @Override
    public boolean allowSetPassword(Context context, HttpServletRequest request, String username) throws SQLException {
        return false;
    }

    @Override
    public boolean isImplicit() {
        return false;
    }

    @Override
    public boolean canSelfRegister(Context context, HttpServletRequest request, String username) throws SQLException {
        return canSelfRegister();
    }

    @Override
    public void initEPerson(Context context, HttpServletRequest request, EPerson eperson) throws SQLException {
        // do nothing
    }

    @Override
    public List<Group> getSpecialGroups(Context context, HttpServletRequest request) throws SQLException {

        LOGGER.info("Getting special groups");

        if (context.getSpecialGroups().size() > 0 ) {
            LOGGER.info("Returning cached special groups.");
            return context.getSpecialGroups();
        }

        final List<Group> groups = new ArrayList<>();

        try {
            if (context.getCurrentUser() != null) {

                String code = (String) request.getParameter("code");
                if (StringUtils.isEmpty(code)) {
                    LOGGER.warn("The incoming request does not have a code parameter");
                }

                printRequestDetails(request);

                Set<String> groupNames = threadLocalGroupNames.get();
                LOGGER.info("Determining Special Groups (thread local) " + groupNames);

                if (groupNames.isEmpty()) {
                    groupNames = context.getSpecialGroupNames();
                    LOGGER.info("Determining Special Groups (context) " + groupNames);
                }

                if (groupNames.isEmpty()) {
                    groupNames = ContextUtil.obtainContext(request).getSpecialGroupNames();
                    LOGGER.info("Determining Special Groups (request context) " + groupNames);
                }

                for (String groupName : groupNames) {
                    if (groupName == null || groupName.isEmpty()) {
                        continue;
                    }
                    LOGGER.info("Looking Up Special Group " + groupName);
                    Group group = groupService.findByName(context, groupName);
                    if (group == null) {
                        LOGGER.warn("Group {} does not exist", groupName);
                    } else {
                        LOGGER.info("Found Special Group " + groupName);
                        groups.add(group);
                    }
                }
            }
        } catch (SQLException ex) {
            // ignoring database error
        }

        return groups;
    }

    @Override
    public String getName() {
        return OIDC_AUTH_ATTRIBUTE;
    }

    @Override
    public int authenticate(Context context, String username, String password, String realm, HttpServletRequest request)
        throws SQLException {

        LOGGER.info("Authenticating");

        if (request == null) {
            LOGGER.warn("Unable to authenticate using OIDC because the request object is null.");
            return BAD_ARGS;
        }

        if (request.getAttribute(OIDC_AUTH_ATTRIBUTE) == null) {
            return NO_SUCH_USER;
        }

        String code = (String) request.getParameter("code");
        if (StringUtils.isEmpty(code)) {
            LOGGER.warn("The incoming request does not have a code parameter");
            return NO_SUCH_USER;
        }

        return authenticateWithOidc(context, code, request);
    }

    private int authenticateWithOidc(Context context, String code, HttpServletRequest request) throws SQLException {

        OidcTokenResponseDTO accessToken = getOidcAccessToken(code);
        if (accessToken == null) {
            LOGGER.warn("No access token retrieved by code");
            return NO_SUCH_USER;
        }

        LOGGER.info("Access Token " + accessToken.getAccessToken());

        LOGGER.info("Access Claims " + decodeJwt(accessToken.getAccessToken()));

        LOGGER.info("ID Token " + accessToken.getIdToken());

        Map<String, Object> claims = decodeJwt(accessToken.getIdToken()).get("payload");

        LOGGER.info("ID Claims " + claims);

        Map<String, Object> userInfo = getOidcUserInfo(accessToken.getAccessToken());

        LOGGER.info("User Info " + userInfo);

        Map<String, Map<String, String[]>> groupMappings = getGroupMappings();

        LOGGER.info("Group Mappings " + groupMappings);

        Set<String> groups = determineGroups(groupMappings, claims);

        LOGGER.info("Groups " + groups);

        threadLocalGroupNames.set(groups);

        request.setAttribute("code", code);
        request.setAttribute("specialgroups", groups);

        context.setSpecialGroupNames(groups);


        String email = getAttributeAsString(userInfo, getEmailAttribute());
        if (StringUtils.isBlank(email)) {
            LOGGER.warn("No email found in the user info attributes");
            return NO_SUCH_USER;
        }

        EPerson ePerson = ePersonService.findByEmail(context, email);
        if (ePerson != null) {
            request.setAttribute(OIDC_AUTHENTICATED, true);
            return ePerson.canLogIn() ? logInEPerson(context, ePerson) : BAD_ARGS;
        }

        // if self registration is disabled, warn about this failure to find a matching eperson
        if (!canSelfRegister()) {
            LOGGER.warn("Self registration is currently disabled for OIDC, and no ePerson could be found for email: {}",
                email);
            return NO_SUCH_USER;
        } else {
            int result = registerNewEPerson(context, userInfo, email);
            if (result == SUCCESS) {
                // It is important to set this attribute so the new user
                // can be granted permissions of the special group in the first login
                request.setAttribute(OIDC_AUTHENTICATED, true);
            }
            return result;
        }
    }

    @Override
    public String loginPageURL(Context context, HttpServletRequest request, HttpServletResponse response) {

        String authorizeUrl = configurationService.getProperty("authentication-oidc.authorize-endpoint");
        String clientId = configurationService.getProperty("authentication-oidc.client-id");
        String clientSecret = configurationService.getProperty("authentication-oidc.client-secret");
        String redirectUri = configurationService.getProperty("authentication-oidc.redirect-url");
        String tokenUrl = configurationService.getProperty("authentication-oidc.token-endpoint");
        String userInfoUrl = configurationService.getProperty("authentication-oidc.user-info-endpoint");
        String[] defaultScopes =
            new String[] {
                "openid", "email", "profile"
            };
        String scopes = String.join(" ", configurationService.getArrayProperty("authentication-oidc.scopes",
            defaultScopes));

        if (isAnyBlank(authorizeUrl, clientId, redirectUri, clientSecret, tokenUrl, userInfoUrl)) {
            LOGGER.error("Missing mandatory configuration properties for OidcAuthenticationBean");

            // prepare a Map of the properties which can not have sane defaults, but are still required
            final Map<String, String> map = Map.of("authorizeUrl", authorizeUrl, "clientId", clientId, "redirectUri",
                redirectUri, "clientSecret", clientSecret, "tokenUrl", tokenUrl, "userInfoUrl", userInfoUrl);
            final Iterator<Entry<String, String>> iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                final Entry<String, String> entry = iterator.next();

                if (isBlank(entry.getValue())) {
                    LOGGER.error(" * {} is missing", entry::getKey);
                }
            }
            return "";
        }

        try {
            return format(LOGIN_PAGE_URL_FORMAT, authorizeUrl, clientId, scopes, encode(redirectUri, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e::getMessage, e);
            return "";
        }

    }

    private int logInEPerson(Context context, EPerson ePerson) {
        context.setCurrentUser(ePerson);
        return SUCCESS;
    }

    private int registerNewEPerson(Context context, Map<String, Object> userInfo, String email) throws SQLException {
        try {

            context.turnOffAuthorisationSystem();

            EPerson eperson = ePersonService.create(context);

            eperson.setNetid(email);
            eperson.setEmail(email);

            String firstName = getAttributeAsString(userInfo, getFirstNameAttribute());
            if (firstName != null) {
                eperson.setFirstName(context, firstName);
            }

            String lastName = getAttributeAsString(userInfo, getLastNameAttribute());
            if (lastName != null) {
                eperson.setLastName(context, lastName);
            }

            eperson.setCanLogIn(true);
            eperson.setSelfRegistered(true);

            ePersonService.update(context, eperson);
            context.setCurrentUser(eperson);
            context.dispatchEvents();

            return SUCCESS;

        } catch (Exception ex) {
            LOGGER.error("An error occurs registering a new EPerson from OIDC", ex);
            return NO_SUCH_USER;
        } finally {
            context.restoreAuthSystemState();
        }
    }

    private OidcTokenResponseDTO getOidcAccessToken(String code) {
        try {
            return oidcClient.getAccessToken(code);
        } catch (Exception ex) {
            LOGGER.error("An error occurs retrieving the OIDC access_token", ex);
            return null;
        }
    }

    private Map<String, Object> getOidcUserInfo(String accessToken) {
        try {
            return oidcClient.getUserInfo(accessToken);
        } catch (Exception ex) {
            LOGGER.error("An error occurs retrieving the OIDC user info", ex);
            return Map.of();
        }
    }

    private String getAttributeAsString(Map<String, Object> userInfo, String attribute) {
        if (isBlank(attribute)) {
            return null;
        }
        return userInfo.containsKey(attribute) ? String.valueOf(userInfo.get(attribute)) : null;
    }

    private String getEmailAttribute() {
        return configurationService.getProperty("authentication-oidc.user-info.email", "email");
    }

    private String getFirstNameAttribute() {
        return configurationService.getProperty("authentication-oidc.user-info.first-name", "given_name");
    }

    private String getLastNameAttribute() {
        return configurationService.getProperty("authentication-oidc.user-info.last-name", "family_name");
    }

    private Map<String, Map<String, String[]>> getGroupMappings() {
        final Map<String, Map<String, String[]>> groupMappings = new HashMap<>();

        final String groupClaims = configurationService.getProperty("authentication-oidc.group.claims", "groups");

        LOGGER.info("Group Claims " + groupClaims);

        final String[] groupKeys = groupClaims.split(",");

        for (String groupKey : groupKeys) {
            final String claimKey = groupKey.trim();
            LOGGER.info("Claim Key " + claimKey);
            if (claimKey.length() > 0) {
                groupKey = claimKey;

                LOGGER.info("Group Key " + groupKey);
                final Map<String, String[]> groupMapping = getGroupMapping(claimKey);
                LOGGER.info("Group Mapping " + groupMapping);
                groupMappings.put(groupKey, groupMapping);
            }
        }

        // {claim key, {claim value, [groups]}}
        return groupMappings;
    }

    private Map<String, String[]> getGroupMapping(String claimKey) {
        final Map<String, String[]> groupMapping = new HashMap<>();

        final String configPrefix = String.join(".", "authentication-oidc", claimKey);

        LOGGER.info("Config Prefix " + configPrefix);

        final List<String> claimGroupPropertyKeys = configurationService.getPropertyKeys(configPrefix);

        LOGGER.info("Claim Group Property Keys " + claimGroupPropertyKeys);

        for (String claimGroupPropertyKey : claimGroupPropertyKeys) {

            LOGGER.info("Claim Group Property Key " + claimGroupPropertyKey);

            final String[] claimGroupPropertyKeySections = claimGroupPropertyKey.split("\\.");

            LOGGER.info("Claim Group Property Key Sections " + Arrays.toString(claimGroupPropertyKeySections));

            if (claimGroupPropertyKeySections.length == 3) {

                final String groupClaimValue = claimGroupPropertyKeySections[2];

                LOGGER.info("Group Claim Value " + groupClaimValue);

                final String groupClaim = configurationService.getProperty(claimGroupPropertyKey, "").trim();

                LOGGER.info("Group Claim " + groupClaim);

                if (groupClaim.length() > 0) {
                    final String[] groups = groupClaim.split(",");

                    LOGGER.info("Groups " + Arrays.toString(groups));

                    for (String group : groups) {
                        group = group.trim();
                    }
                    groupMapping.put(groupClaimValue, groups);
                }
            }
        }

        // {claim value, [groups]}
        return groupMapping;
    }

    private Set<String> determineGroups(Map<String, Map<String, String[]>> groupMappings, Map<String, Object> claims) {
        final Set<String> groups = new HashSet<>();

        for (Entry<String, Object> claimEntry : claims.entrySet()) {
            if (claimEntry.getKey() == null || claimEntry.getValue() == null) {
                continue;
            }

            final String claimKey = claimEntry.getKey().trim();
            final Object claimValue = claimEntry.getValue();

            if (claimKey.length() > 0 && groupMappings.containsKey(claimKey)) {
                final Map<String, String[]> groupMapping = groupMappings.get(claimKey);

                for (Entry<String, String[]> groupMappingEntry : groupMapping.entrySet()) {
                    if (groupMappingEntry.getKey() == null || groupMappingEntry.getValue() == null) {
                        continue;
                    }

                    final String groupClaimValue = groupMappingEntry.getKey().trim();
                    final List<String> groupsForClaim = Arrays.asList(groupMappingEntry.getValue())
                        .stream()
                        .map(groupForClaim -> groupForClaim.trim())
                        .filter(groupForClaim -> groupForClaim.length() > 0)
                        .collect(Collectors.toList());

                    if (claimValue instanceof String claim) {
                        final String[] groupClaims = claim.split(",");
                        for (String groupClaim : groupClaims) {
                            groupClaim = groupClaim.trim();
                            if (groupClaim.length() > 0 && groupClaim.startsWith(groupClaimValue)) {
                                groups.addAll(groupsForClaim);
                            }
                        }
                    } else if (claimValue instanceof Collection claim) {
                        for (Object groupsClaim : claim) {
                            if (!(groupsClaim instanceof String)) {
                                continue;
                            }
                            final String groupClaim = ((String) groupsClaim).trim();
                            if (groupClaim.length() > 0 && groupClaim.startsWith(groupClaimValue)) {
                                groups.addAll(groupsForClaim);
                            }
                        }
                    }
                }
            }
        }

        // [groups]
        return groups;
    }

    private boolean canSelfRegister() {
        String canSelfRegister = configurationService.getProperty("authentication-oidc.can-self-register", "true");
        if (isBlank(canSelfRegister)) {
            return true;
        }
        return toBoolean(canSelfRegister);
    }

    public OidcClient getOidcClient() {
        return this.oidcClient;
    }

    public void setOidcClient(OidcClient oidcClient) {
        this.oidcClient = oidcClient;
    }

    @Override
    public boolean isUsed(final Context context, final HttpServletRequest request) {
        if (request != null &&
                context.getCurrentUser() != null &&
                request.getAttribute(OIDC_AUTHENTICATED) != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canChangePassword(Context context, EPerson ePerson, String currentPassword) {
        return false;
    }

    /**
     * Decodes a JWT string and returns its header and payload as structured maps.
     *
     * <p>
     * ⚠️ This decoder is designed for JWTs (RFC 7519), not JWEs (RFC 7516).
     * JWEs are encrypted and require decryption before decoding.
     * </p>
     * @param jwt the JWT string in the format {@code header.payload.signature}
     * @return a map containing two entries:
     *         <ul>
     *             <li>{@code "header"} → decoded JWT header as a map</li>
     *             <li>{@code "payload"} → decoded JWT payload as a map</li>
     *         </ul>
     * 
     * @throws IllegalArgumentException if the JWT format is invalid or decoding fails
     */
    private static Map<String, Map<String, Object>> decodeJwt(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }
       
        Map<String, Map<String, Object>> result = new HashMap<>();
        result.put("header", parseJson(parts[0]));
        result.put("payload", parseJson(parts[1]));

        return result;
    }

    /**
     * Decodes a Base64URL-encoded JWT section and parses it into a map.
     *
     * @param base64Url the Base64URL-encoded string
     * @return a map representing the decoded JSON object
     * @throws IllegalArgumentException if decoding or parsing fails
     */
    private static Map<String, Object> parseJson(String base64Url) {
        String json = new String(Base64.getUrlDecoder().decode(padBase64(base64Url)));
        JSONObject jsonObject = new JSONObject(json);

        return jsonObject.toMap();
    }

    /**
     * Pads a Base64URL string to ensure it has correct length for decoding.
     *
     * @param base64 the unpadded Base64URL string
     * @return a padded Base64URL string suitable for decoding
     */
    private static String padBase64(String base64) {
        int padding = 4 - (base64.length() % 4);

        return base64 + "=".repeat(padding % 4);
    }

    private static void printRequestDetails(HttpServletRequest request) {
        LOGGER.info("=== HTTP SERVLET REQUEST DETAILS ===");

        LOGGER.info("--- PARAMETERS ---");
        Enumeration<String> paramNames = request.getParameterNames();
        if (!paramNames.hasMoreElements()) {
            LOGGER.info("No parameters found");
        } else {
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                if (paramValues.length == 1) {
                    LOGGER.info(paramName + " = " + paramValues[0]);
                } else {
                    String message = paramName + " = [";
                    for (int i = 0; i < paramValues.length; i++) {
                        message += paramValues[i];
                        if (i < paramValues.length - 1) {
                            message += ", ";
                        }
                    }
                    message += "]";
                    LOGGER.info(message);
                }
            }
        }

        LOGGER.info("--- HEADERS ---");
        Enumeration<String> headerNames = request.getHeaderNames();
        if (!headerNames.hasMoreElements()) {
            LOGGER.info("No headers found");
        } else {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValues = request.getHeaders(headerName);

                String message  = headerName + " = ";

                boolean first = true;
                while (headerValues.hasMoreElements()) {
                    if (!first) {
                        message += ", ";
                    }
                    message += headerValues.nextElement();
                    first = false;
                }
                LOGGER.info(message);
            }
        }

        LOGGER.info("--- COOKIES ---");
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            LOGGER.info("No cookies found");
        } else {
            for (Cookie cookie : cookies) {
                LOGGER.info(cookie.getName() + " = " + cookie.getValue() +
                    " (domain: " + cookie.getDomain() +
                    ", path: " + cookie.getPath() +
                    ", maxAge: " + cookie.getMaxAge() +
                    ", secure: " + cookie.getSecure() +
                    ", httpOnly: " + cookie.isHttpOnly() + ")");
            }
        }

        LOGGER.info("=== END REQUEST DETAILS ===");
    }

}
