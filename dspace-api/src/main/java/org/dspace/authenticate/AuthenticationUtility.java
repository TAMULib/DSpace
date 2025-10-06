package org.dspace.authenticate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dspace.eperson.Group;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Trace HttpServletRequest through authentication to type the web authentication details.
 */
public class AuthenticationUtility {

    public static final String ATTRIBUTES = "attributes";
    public static final String COOKIES = "cookies";
    public static final String HEADERS = "headers";
    public static final String PARAMETERS = "parameters";

    public static final String DOMAIN = "domain";
    public static final String PATH = "path";
    public static final String MAX_AGE = "maxAge";
    public static final String SECURE = "secure";
    public static final String HTTP_ONLY = "httpOnly";

    private static final String COMMA = ",";
    private static final String SEMICOLON = ":";
    private static final String SPACED_EQUAL = " = ";
    private static final String PARENTHESIS_OPEN = "(";
    private static final String PARENTHESIS_CLOSE = ")";

    private static final String[] COOKIE_DELIMETERS = new String[] {
        DOMAIN + SEMICOLON,    // 0
        PATH + SEMICOLON,      // 1
        DOMAIN + SEMICOLON,    // 2
        MAX_AGE + SEMICOLON,   // 3
        SECURE + SEMICOLON,    // 4
        HTTP_ONLY + SEMICOLON, // 5
    };

    private AuthenticationUtility() {
        throw new RuntimeException();
    }

    /**
     * Utility method for printing requests details to stdout.
     * 
     * @param location String request is found in code
     * @param request HttpServletRequest to print
     * @return function to call with a template to print about request details
     */
    public static Function<String, Integer> printRequest(String location, HttpServletRequest request) {
        // complete type Map
        Map<String, Object> details = new HashMap<>();

        int results = printRequestDetails(request, details);

        return (String template) -> {
            System.out.println(template);
            System.out.println(details);

            return results;
        };
    }

    public static Function<String, Integer> printGroups(String location, List<Group> groups) {
        final List<String> groupNames = Objects.nonNull(groups)
            ? groups.stream().map(Group::getName).collect(Collectors.toList())
            : new ArrayList<>();

        return (String template) -> {
            System.out.println(template);
            System.out.println(String.join(COMMA, groupNames));

            return 0;
        };
    }

    private static int printRequestDetails(HttpServletRequest request, Map<String, Object> details) {
        System.out.println("=== HTTP SERVLET REQUEST DETAILS ===");

        int results = 0;

        System.out.println("requestId: " + request.getRequestId());
        System.out.println("method: " + request.getMethod());
        System.out.println("pathInfo: " + request.getPathInfo());
        System.out.println("pathTranslated: " + request.getPathTranslated());
        System.out.println("contextPath: " + request.getContextPath());
        System.out.println("queryString: " + request.getQueryString());
        System.out.println("requestURI: " + request.getRequestURI());
        System.out.println("servletPath: " + request.getServletPath());
        
        if (Objects.nonNull(request.getRequestURL())) {
            System.out.println("requestURL: " + request.getRequestURL());
        }

        // @see HttpServletRequest#[BASIC_AUTH, FORM_AUTH, CLIENT_CERT_AUTH, DIGEST_AUTH]
        System.out.println("authType: " + request.getAuthType());
        System.out.println("remoteUser: " + request.getRemoteUser());
        System.out.println("sessionId: " + request.getRequestedSessionId());
        System.out.println("requestedSessionIdValid: " + request.isRequestedSessionIdValid());
        System.out.println("requestedSessionIdFromCookie: " + request.isRequestedSessionIdFromCookie());
        System.out.println("requestedSessionIdFromURL: " + request.isRequestedSessionIdFromURL());

        // check if response set something to read after the response body is read
        System.out.println("trailerFieldsReady: " + request.isTrailerFieldsReady());

        int i = 0;
        results = printRequestAttributes(request, details); i++;
        results = printRequestCookies(request, details); i++;
        results = printRequestHeaders(request, details); i++;
        results = printRequestParameters(request, details); i++;

        // results = printRequestSession(request, details); i++;
        // results = printServletMapping(request, details); i++;
        // results = printUserPrincipal(request, details); i++;
        // results = printSession(request, details); i++;
        // results = printParts(request, details); i++;
        // results = printTrailerFields(request, details); i++;

        // results 0 success, results -n number of request properties not printed
        if (results < i) {
            System.out.println("*** EMPTY REQUEST ***");
        }

        System.out.println("=== END REQUEST DETAILS ===");

        return results;
    }

    private static int printRequestAttributes(HttpServletRequest request, Map<String, Object> details) {
        final Map<String, Object> attributes = new HashMap<>();
        details.put(ATTRIBUTES, attributes);

        int results = 0;
        Enumeration<String> attributeNames = request.getAttributeNames();
        if (attributeNames.hasMoreElements()) {
            System.out.println("--- ATTRIBUTES ---");
            while (attributeNames.hasMoreElements()) {
                final String attributeName = attributeNames.nextElement();
                final Object attributeValue = request.getAttribute(attributeName);

                attributes.put(attributeName, attributeValue);

                System.out.println(attributeName + SPACED_EQUAL + attributeValue);
            }
        } else {
            System.out.println("No attributes found");
            results = -1;
        }

        return results;
    }

    private static int printRequestCookies(HttpServletRequest request, Map<String, Object> details) {
        final Map<String, Object> cookies = new HashMap<>();
        details.put(COOKIES, cookies);

        int results = 0;
        // if request has a cookie
        if (Objects.nonNull(request.getCookies()) && request.getCookies().length > 0) {
            System.out.println("--- COOKIES ---");
            for (Cookie cookie : request.getCookies()) {
                final String cookieKey = String.join(SPACED_EQUAL, cookie.getName(), cookie.getValue());

                cookies.put(cookieKey, cookie);

                StringBuilder cookieDetails = new StringBuilder();
                cookieDetails.append(PARENTHESIS_OPEN);
                cookieDetails.append(String.join(COOKIE_DELIMETERS[0], cookie.getDomain()));
                cookieDetails.append(String.join(COOKIE_DELIMETERS[1], cookie.getPath()));
                cookieDetails.append(String.join(COOKIE_DELIMETERS[2], String.valueOf(cookie.getMaxAge())));
                cookieDetails.append(String.join(COOKIE_DELIMETERS[3], String.valueOf(cookie.getSecure())));
                cookieDetails.append(String.join(COOKIE_DELIMETERS[4], String.valueOf(cookie.isHttpOnly())));
                cookieDetails.append(PARENTHESIS_CLOSE);

                System.out.println(cookieKey + cookieDetails.toString());
            }
        } else {
            System.out.println("No cookies found");
            results = -1;
        }

        return results;
    }

    private static int printRequestHeaders(HttpServletRequest request, Map<String, Object> details) {
        final Map<String, Object> headers = new HashMap<>();
        details.put(HEADERS, headers);

        int results = 0;
        final Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames.hasMoreElements()) {
            System.out.println("--- HEADERS ---");
            while (headerNames.hasMoreElements()) {
                final String headerName = headerNames.nextElement();
                final Enumeration<String> headerValues = request.getHeaders(headerName);
                final StringBuilder headerValue = new StringBuilder();
                boolean first = true;
                while (headerValues.hasMoreElements()) {
                    if (!first) {
                        headerValue.append(COMMA);
                    }
                    headerValue.append(headerValues.nextElement());
                    first = false;
                }

                headers.put(headerName, headerValue);

                System.out.println(headerName + SPACED_EQUAL + headerValue);
            }
        } else {
            System.out.println("No headers found");
            results = -1;
        }

        return results;
    }

    private static int printRequestParameters(HttpServletRequest request, Map<String, Object> details) {
        final Map<String, Object> parameters = new HashMap<>();
        details.put(PARAMETERS, parameters);

        int results = 0;
        Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames.hasMoreElements()) {
            System.out.println("--- PARAMETERS ---");
            while (paramNames.hasMoreElements()) {
                final String paramName = paramNames.nextElement();
                final String paramValue = String.join(COMMA, request.getParameterValues(paramName));

                parameters.put(paramName, paramValue);

                System.out.println(String.join(SPACED_EQUAL, paramName, paramValue));
            }
        } else {
            System.out.println("No parameters found");
            results = -1;
        }

        return results;
    }

}
