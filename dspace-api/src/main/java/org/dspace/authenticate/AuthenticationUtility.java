package org.dspace.authenticate;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Trace HttpServletRequest through authentication to type the web authentication details.
 */
public class AuthenticationUtility {

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
    public static Function<String, Integer> print(String location, HttpServletRequest request) {
        // completely type Map
        Map<String, Object> details = new HashMap<>();

        int results = printRequestDetails(request, details);

        return (String template) -> {
            System.out.println(template); // Java 21 interpolation with details
            System.out.println(details);

            return results;
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

        System.out.println("trailerFieldsReady: " + request.isTrailerFieldsReady());

        results = printRequestAttributes(request);
        results = printRequestCookies(request);
        results = printRequestHeaders(request);
        results = printRequestParameters(request);

        // results = printRequestSession(request);
        // results = printServletMapping(request);
        // results = printUserPrincipal(request);
        // results = printSession(request);
        // results = printParts(request);\
        // results = printTrailerFields(request);

        if (results < 0) {
            System.out.println("*** EMPTY REQUEST ***");
        }

        System.out.println("=== END REQUEST DETAILS ===");

        return results;
    }

    private static int printRequestAttributes(HttpServletRequest request) {
        int results = 0;
        Enumeration<String> attributeNames = request.getAttributeNames();
        if (attributeNames.hasMoreElements()) {
            System.out.println("--- ATTRIBUTES ---");
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = request.getAttribute(attributeName);
                System.out.println(attributeName + " = " + attributeValue);
            }
        } else {
            System.out.println("No attributes found");
            results = -1;
        }

        return results;
    }

    private static int printRequestCookies(HttpServletRequest request) {
        int results = 0;
        Cookie[] cookies = request.getCookies();
        boolean hasCookies = !(cookies == null || cookies.length == 0);
        if (hasCookies) {
            System.out.println("--- COOKIES ---");
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName() + " = " + cookie.getValue() +
                    " (domain: " + cookie.getDomain() +
                    ", path: " + cookie.getPath() +
                    ", maxAge: " + cookie.getMaxAge() +
                    ", secure: " + cookie.getSecure() +
                    ", httpOnly: " + cookie.isHttpOnly() + ")");
            }
        } else {
            System.out.println("No cookies found");
            results = -1;
        }

        return results;
    }

    private static int printRequestHeaders(HttpServletRequest request) {
        int results = 0;
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames.hasMoreElements()) {
            System.out.println("--- HEADERS ---");
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
                System.out.println(message);
            }
        } else {
            System.out.println("No headers found");
            results = -1;
        }

        return results;
    }

    private static int printRequestParameters(HttpServletRequest request) {
        int results = 0;
        Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames.hasMoreElements()) {
            System.out.println("--- PARAMETERS ---");
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                if (paramValues.length == 1) {
                    System.out.println(paramName + " = " + paramValues[0]);
                } else {
                    String message = paramName + " = [";
                    for (int i = 0; i < paramValues.length; i++) {
                        message += paramValues[i];
                        if (i < paramValues.length - 1) {
                            message += ", ";
                        }
                    }
                    message += "]";
                    System.out.println(message);
                }
            }
        } else {
            System.out.println("No parameters found");
            results = -1;
        }

        return results;
    }

}
