package org.dspace.authenticate;

import java.util.Enumeration;
import java.util.function.Function;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class AuthenticationUtility {

    private AuthenticationUtility() {
        throw new RuntimeException();
    }

    /**
     * Utility method for printing to stdout.
     * 
     * @param anything
     * @return function to call with a template to print about anything
     */
    public static Function<String, Integer> print(Object anything) {

        if (anything instanceof HttpServletRequest request) {
            printRequestDetails(request); // as JSON
        }

        /**
         * Print the template.
         * 
         * @param template
         * @return 0
         */
        return (String template) -> {
            System.out.println(template); // template with details as context

            return 0;
        };
    }

    private static void printRequestDetails(HttpServletRequest request) {
        System.out.println("=== HTTP SERVLET REQUEST DETAILS ===");

        int results = 0;

        results = printRequestAttributes(request);
        results = printRequestCookies(request);
        results = printRequestHeaders(request);
        results = printRequestParameters(request);

        if (results < 0) {
            System.out.println("*** EMPTY REQUEST ***");
        }

        System.out.println("=== END REQUEST DETAILS ===");
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
