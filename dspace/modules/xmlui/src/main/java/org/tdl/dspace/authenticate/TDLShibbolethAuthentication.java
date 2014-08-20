package org.tdl.dspace.authenticate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authenticate.AuthenticationManager;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.storage.rdbms.DatabaseManager;


/**
 * @author Scott Phillips
 */
public class TDLShibbolethAuthentication implements AuthenticationMethod {

	/** Has authentication been initalized **/
	private static boolean initialized = false;

	/** log4j category */
	private static Logger log = Logger.getLogger(TDLShibbolethAuthentication.class);


	private static final Map<String, Boolean> VALID_AFFILIATIONS;
	static {
		VALID_AFFILIATIONS = new HashMap<String, Boolean>();
		VALID_AFFILIATIONS.put("faculty", true);
		VALID_AFFILIATIONS.put("student", true);
		VALID_AFFILIATIONS.put("staff", true);
		VALID_AFFILIATIONS.put("alum", true);
		VALID_AFFILIATIONS.put("member", true);
		VALID_AFFILIATIONS.put("affiliate", true);
		VALID_AFFILIATIONS.put("employee", true);
	}


	/**
	 * Predicate, whether to allow new EPerson to be created. The answer
	 * determines whether a new user is created when the credentials describe a
	 * valid entity but there is no corresponding EPerson in DSpace yet. The
	 * EPerson is only created if authentication succeeds.
	 * 
	 * @param context
	 *            DSpace context
	 * @param request
	 *            HTTP request, in case it's needed. May be null.
	 * @param username
	 *            Username, if available. May be null.
	 * @return true if new ePerson should be created.
	 */
	public boolean canSelfRegister(Context context, HttpServletRequest request,
			String username) throws SQLException {
		return false;
	}

	/**
	 * Initialize a new EPerson record for a self-registered new user. Set any
	 * data in the EPerson that is specific to this authentication method.
	 * 
	 * @param context
	 *            DSpace context
	 * @param request
	 *            HTTP request, in case it's needed. May be null.
	 * @param eperson
	 *            newly created EPerson record - email + information from the
	 *            registration form will have been filled out.
	 */
	public void initEPerson(Context context, HttpServletRequest request,
			EPerson eperson) throws SQLException {
		// nothing to do here all attributes are added at authenticate()

	}

	/**
	 * Should (or can) we allow the user to change their password. Note that
	 * this means the password stored in the EPerson record, so if <em>any</em>
	 * method in the stack returns true, the user is allowed to change it.
	 * 
	 * @param context
	 *            DSpace context
	 * @param request
	 *            HTTP request, in case it's needed. May be null.
	 * @param username
	 *            Username, if available. May be null.
	 * @return true if this method allows user to change ePerson password.
	 */
	public boolean allowSetPassword(Context context,
			HttpServletRequest request, String username) throws SQLException {
		// we do not maintain their password since they logged on via shibboleth
		return false;
	}

	/**
	 * Typically shibboleth-based authentication is an "implicit" authentication
	 * method, mean it dosn't interact with the user. However this
	 * TDL-specialized plugin will fall back to password-based authentication
	 * when needed. So we return false even though we can handle implicit, but
	 * we can also handle explicit.
	 * 
	 * @return true if this method uses implicit authentication.
	 */
	public boolean isImplicit() {
		// since we don't even see the credentials this is an implicit method

		// return true;
		return false;
	}

	/**
	 * Get list of extra groups that user implicitly belongs to. Returns IDs of
	 * any EPerson-groups that the user authenticated by this request is
	 * <em>implicitly</em> a member of -- e.g. a group that depends on the
	 * client network-address.
	 * <p>
	 * It might make sense to implement this method by itself in a separate
	 * authentication method that just adds special groups, if the code doesn't
	 * belong with any existing auth method. The stackable authentication system
	 * was designed expressly to separate functions into "stacked" methods to
	 * keep your site-specific code modular and tidy.
	 * 
	 * @param context
	 *            A valid DSpace context.
	 * 
	 * @param request
	 *            The request that started this operation, or null if not
	 *            applicable.
	 * 
	 * @return array of EPerson-group IDs, possibly 0-length, but never
	 *         <code>null</code>.
	 */
	public int[] getSpecialGroups(Context context, HttpServletRequest request) {
        if (request == null)
        	// SWORD will send a rull request object.
            return new int[0];
		
		// groups will be added based on the affiliation(s) returned from
		// shibboleth
		// which were set to the session in the authenticate method below
		ArrayList<Integer> groups = null;
		if (request.getSession().getAttribute("TDL-SPECIAL-GROUPS") != null) {
			groups = (ArrayList<Integer>) request.getSession().getAttribute("TDL-SPECIAL-GROUPS");
		}

		if ((groups == null) || (groups.size() == 0)) {
			return new int[0];
		}

		log.debug("SHIB-GROUP: Creating special group arrays");
		if (groups.size() > 0) {
			int[] groupIds = new int[groups.size()];
			int j = 0;
			for (Integer id : groups) {
				groupIds[j] = id;
				j++;
			}

			return groupIds;
		}

		return new int[0];
	}

	/**
	 * Authenticate the given or implicit credentials. This is the heart of the
	 * authentication method: test the credentials for authenticity, and if
	 * accepted, attempt to match (or optionally, create) an
	 * <code>EPerson</code>. If an <code>EPerson</code> is found it is set in
	 * the <code>Context</code> that was passed.
	 * 
	 * @param context
	 *            DSpace context, will be modified (ePerson set) upon success.
	 * 
	 * @param username
	 *            Username (or email address) when method is explicit. Use null
	 *            for implicit method.
	 * 
	 * @param password
	 *            Password for explicit auth, or null for implicit method.
	 * 
	 * @param realm
	 *            Realm is an extra parameter used by some authentication
	 *            methods, leave null if not applicable.
	 * 
	 * @param request
	 *            The HTTP request that started this operation, or null if not
	 *            applicable.
	 * 
	 * @return One of: SUCCESS, BAD_CREDENTIALS, CERT_REQUIRED, NO_SUCH_USER,
	 *         BAD_ARGS
	 *         <p>
	 *         Meaning: <br>
	 *         SUCCESS - authenticated OK. <br>
	 *         BAD_CREDENTIALS - user exists, but credentials (e.g. passwd)
	 *         don't match <br>
	 *         CERT_REQUIRED - not allowed to login this way without X.509 cert.
	 *         <br>
	 *         NO_SUCH_USER - user not found using this method. <br>
	 *         BAD_ARGS - user/pw not appropriate for this method
	 */

	public int authenticate(Context context, String username, String password,
			String realm, HttpServletRequest request) throws SQLException {

		log.debug("SHIB-AUTH: Starting Shibboleth authentication.");
		initalizeShibbolethAuthentication(context);
		
		if (log.isTraceEnabled()) {
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				Enumeration<String> headerValues = request.getHeaders(headerName);
				while (headerValues.hasMoreElements()) {
					String headerValue = headerValues.nextElement();
					log.trace("SHIB_AUTH: Recieved header: "+headerName+"='"+headerValue+"'");
				}
				
			}
		}
		

		// Fall back to password-based authentication if presented with it. This will
		// allow for the SWORD app to work with TDL's shibboleth plugin.
		if (username != null && username.length() > 0 && password != null
				&& password.length() > 0) {
			log.debug("SHIB-AUTH: Found a username and password, using password-based authentication.");
			return passwordAuthentication(context, username, password, request);
		}

		String uidHeader = ConfigurationManager.getProperty("authentication.shib.unique-key");
		String emailHeader = ConfigurationManager.getProperty("authentication.shib.email-header");
		String metaDataPrefix = ConfigurationManager.getProperty("authentication.shib.metadata.startswith");
		String netId = request.getHeader(uidHeader);

		String email = getHeaderValue(64, emailHeader, request);

		int metadataMaxLength = ConfigurationManager.getIntProperty("authentication.shib.metadata.max-length");

		// does the header exists, if not then no authentication
		if ((netId == null) || ("".equals(netId))) {
			log.error("SHIB-AUTH: NetId header was not found. ("+uidHeader+"="+netId+")");
			//	    FailedAuthentication.registerErrorCode(
			//		    FailedAuthentication.NO_SUCH_USER, request);
			return AuthenticationMethod.NO_SUCH_USER;
		}

		if (netId.length() > 64) {
			// we need the whole netId so if we cant store it because it is
			// longer than 64 bytes we
			log.error("SHIB-AUTH: Fail to authorize user with netId because it is too long (>64 bytes):"
					+ netId + " length: " + netId.length());
			//	    FailedAuthentication.registerErrorCode(
			//		    FailedAuthentication.BAD_ARGUMENTS, request);
			return AuthenticationMethod.BAD_ARGS;
		}

		EPerson eperson = null;
		try {
			// netIDs are automatically stored in lowecase by dspace
			eperson = EPerson.findByNetid(context, netId.toLowerCase());
		} catch (Exception e) {
			// nothing to do here, may create an eperson later
		}

		// find user or create new account for them
		if (eperson == null) {
			if ((email == null) || ("".equals(email))) {
				log.error("SHIB-AUTH: The netId did not resolve to a valid EPerson, and no email address was found. ("+uidHeader+"="+netId+","+emailHeader+"="+email+")");
				//		FailedAuthentication.registerErrorCode(
				//			FailedAuthentication.NO_SUCH_USER, request);
				return AuthenticationMethod.NO_SUCH_USER;
			}

			// before we create a new EPerson lets see if one exists with the
			// same email address
			try {
				eperson = EPerson.findByEmail(context, email);
			} catch (Exception e) {
				// nothing to do here, may create an eperson later
			}

			if (eperson != null) {
				// we found one with email
				if (!netId.equalsIgnoreCase(eperson.getNetid())
						&& eperson.getNetid() != null) {
					// they already have a netID they cannot log on
					log.error("SHIB-AUTH: An EPerson object was found for '"+email+"', but has another netID associated with it.");
					//		    FailedAuthentication.registerErrorCode(
					//			    FailedAuthentication.BAD_ARGUMENTS, request);
					return AuthenticationMethod.BAD_ARGS;
				} else {
					// they do not have a netID lets update the record
					log.info("SHIB-AUTH: An EPerson object was found for '"+email+"', associating this EPerson with new netID: "+netId);
					context.setIgnoreAuthorization(true);
					try {
						eperson.setNetid(netId);
					} catch (Exception e) {
						log.error("SHIB-AUTH: Fail to update user with netID: " + netId	+ " exception message: " + e.getMessage());
						context.setCurrentUser(null);
						eperson = null;
						//			FailedAuthentication.registerErrorCode(
						//				FailedAuthentication.NO_SUCH_USER, request);
						return AuthenticationMethod.NO_SUCH_USER;
					} finally {
						context.setIgnoreAuthorization(false);
					}
				}
			} else {
				// create a new EPerson
				// TEMPORARILY turn off authorization
				log.info("SHIB-AUTH: No existing eperson object was found for '"+email+"', creating a new account.");
				context.setIgnoreAuthorization(true);
				try {
					eperson = EPerson.create(context);
					eperson.setEmail(email);
					eperson.setNetid(netId);
					eperson.setCanLogIn(true);
				} catch (Exception e) {
					log.error("SHIB-AUTH: Fail to authorize user with netID: '"+netId+"' exception message: " + e.getMessage());
					context.setCurrentUser(null);
					eperson = null;
					//		    FailedAuthentication.registerErrorCode(
					//			    FailedAuthentication.NO_SUCH_USER, request);
					return AuthenticationMethod.NO_SUCH_USER;
				} finally {
					context.setIgnoreAuthorization(false);
				}
			}
		}

		// we have a valid user log them on
		AuthenticationManager.initEPerson(context, request, eperson);
		context.setCurrentUser(eperson);
		log.info("SHIB-AUTH: Authentication successfull via shibboleth. (email='"+username+"', netID='"+netId+"')");

		// now update it with shibb values
		// update EPerson
		// make sure EPerson metadata is up to date with Shibb attributes
		String surnameHeader = ConfigurationManager.getProperty("authentication.shib.surname");
		String givenNameHeader = ConfigurationManager.getProperty("authentication.shib.given-name");
		String phoneHeader = ConfigurationManager.getProperty("authentication.shib.telephone-number");

		String surname = getHeaderValue(metadataMaxLength, surnameHeader,
				request);
		String givenName = getHeaderValue(metadataMaxLength, givenNameHeader,
				request);
		String phone = getHeaderValue(metadataMaxLength, phoneHeader, request);

		// update the email in case the user changed it at the identity provider
		// and is already logged in
		context.setIgnoreAuthorization(true);
		if (!((email == null) || (email.length() <= 0)))
			eperson.setEmail(email);
		// only update the values if we get them from shibb
		if (!((surname == null) || (surname.length() <= 0)))
			eperson.setLastName(surname.indexOf(';') > -1 ? surname.substring(
					0, surname.indexOf(';')) : surname);
		if (!((givenName == null) || (givenName.length() <= 0)))
			eperson.setFirstName(givenName.indexOf(';') > -1 ? givenName
					.substring(0, givenName.indexOf(';')) : givenName);
		if (!((phone == null) || (phone.length() <= 0)))
			eperson.setMetadata("phone", phone);

		java.util.Enumeration names = request.getHeaderNames();
		String name;
		while (names.hasMoreElements()) {
			name = names.nextElement().toString();
			if (name.startsWith(metaDataPrefix)) {
				// we know we care about it
				if (!((request.getHeader(name) == null) || (request.getHeader(
						name).length() <= 0))) {
					String metadataName = name.substring(metaDataPrefix.length(),
							name.length());
					String metadataValue = getHeaderValue(metadataMaxLength,
							name, request);
					
					log.debug("SHIB-AUTH: Setting eperson metadata: "+metadataName+"= '"+metadataValue+"'");

					eperson.setMetadata(metadataName, metadataValue);
				}
			}

		}

		try {
			eperson.update();
		} catch (Exception e) {
			log.error("SHIB-AUTH: Fail to update user with new shibboleth attributes exception message:" + e.getMessage());
			context.setCurrentUser(null);
			eperson = null;
			//	    FailedAuthentication.registerErrorCode(
			//		    FailedAuthentication.NO_SUCH_USER, request);
			return AuthenticationMethod.NO_SUCH_USER;
		} finally {
			context.setIgnoreAuthorization(false);
		}

		// commit the context
		context.commit();

		// ok now we have logged them in and it is all good
		// we can now go through and create the special groups ArrayList
		// which the getSpecialGroups method above can access since
		// it will not have access to the same request that was used by shibb
		// we dont want this part to cause the login to fail so we try catch

		try {
			String affiliationHeader = ConfigurationManager.getProperty("authentication.shib.affiliation-header");
			log.debug("SHIB-AUTH: Found DSpace configuration affiliationHeader: " + affiliationHeader);
			String affiliationString = request.getHeader(affiliationHeader);
			log.debug("SHIB-AUTH: Recieved Shibboleth affiliationString: " + affiliationString);
			String[] affiliations = affiliationString.split(";");
			if ((affiliations != null) && (affiliations.length > 0)) {

				ArrayList<Integer> groups = new ArrayList<Integer>();

				for (String aff : affiliations) {
					if (VALID_AFFILIATIONS.get(aff)) {
						try {
							Group g = Group.findByName(context, aff);
							if (g != null) {
								log.debug("SHIB-AUTH: Adding EPerson "+eperson.getEmail()+" to special group: " + g.getName()
										+ " (" + g.getID() + ")");
								groups.add(g.getID());
							}
						} catch (SQLException e) {
							// nothing to do, we just keep moving
							log.error("SHIB-AUTH: Unable to get Group by name: " + aff
									+ ". ", e);
						}
					}
				}

				request.getSession().setAttribute("TDL-SPECIAL-GROUPS", groups);

			}
		} catch (Exception e) {
			// swallow exception
			log.error("SHIB-AUTH: recieved exception while assigning special grounps, error="+e.getMessage());
		}

		return AuthenticationMethod.SUCCESS;
	}

	/**
	 * Get login page to which to redirect. Returns URL (as string) to which to
	 * redirect to obtain credentials (either password prompt or e.g. HTTPS port
	 * for client cert.); null means no redirect.
	 * 
	 * @param context
	 *            DSpace context, will be modified (ePerson set) upon success.
	 * 
	 * @param request
	 *            The HTTP request that started this operation, or null if not
	 *            applicable.
	 * 
	 * @param response
	 *            The HTTP response from the servlet method.
	 * 
	 * @return fully-qualified URL or null
	 */
	public String loginPageURL(Context context, HttpServletRequest request,
			HttpServletResponse response) {

		// If this server is configured for lazy sessions then use this to
		// login, otherwise
		// default to the protected shibboleth url.

		if (ConfigurationManager.getBooleanProperty("authentication.shib.lazysession",
				false)) {

			String shibURL = ConfigurationManager
					.getProperty("authentication.shib.loginurl");
			if (shibURL == null || shibURL.length() == 0)
				shibURL = "/Shibboleth.sso/Login";
			shibURL.trim();
			boolean secure = ConfigurationManager.getBooleanProperty(
					"xmlui.force.ssl", false);
			String host = request.getServerName();
			int port = request.getServerPort();
			String contextPath = request.getContextPath();

			String returnURL = "https://";
			if (!secure)
				returnURL = "http://";
			returnURL += host;
			if (!(port == 443 || port == 80))
				returnURL += ":" + port;
			returnURL += "/" + contextPath + "/shibboleth-login";

			try {
				shibURL += "?target="+URLEncoder.encode(returnURL, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				log.error("Unable to generate lazysession authentication",uee);
			}
			
			return response.encodeRedirectURL(shibURL);
		} else {
			return response.encodeRedirectURL(request.getContextPath()
					+ "/shibboleth-login");
		}
	}

	/**
	 * Get title of login page to which to redirect. Returns a <i>message
	 * key</i> that gets translated into the title or label for "login page" (or
	 * null, if not implemented) This title may be used to identify the link to
	 * the login page in a selection menu, when there are multiple ways to
	 * login.
	 * 
	 * @param context
	 *            DSpace context, will be modified (ePerson set) upon success.
	 * 
	 * @return title text.
	 */
	public String loginPageTitle(Context context) {
		return "org.tdl.dspace.eperson.ShibbolethAuthentication.title";
	}



	protected int passwordAuthentication(Context context, String username, String password, HttpServletRequest request) throws SQLException {

		EPerson eperson = null;
		log.debug("SHIB-PASS: Attempting to login user "+username+"using password-based authentication.");
		try {
			eperson = EPerson.findByEmail(context, username.toLowerCase());
		} catch (AuthorizeException ae) {
			// ignore exception, treat it as lookup failure.
		}

		if (eperson == null) {
			// lookup failed.
			log.error("SHIB-PASS: Authentication failed for user "+username+" because no such user exists.");
			return NO_SUCH_USER;
		} else if (!eperson.canLogIn()) {
			// cannot login this way
			log.error("SHIB-PASS: Authentication failed for user "+username+" because the EPerson object is not allowed to login.");
			return BAD_ARGS;
		} else if (eperson.getRequireCertificate()) {
			// this user can only login with x.509 certificate
			log.error("SHIB-PASS: Authentication failed for user "+username+" because the EPerson object requires a certificate to authenticate.");
			return CERT_REQUIRED;
		}

		else if (eperson.checkPassword(password)) {
			// Password matched
			AuthenticationManager.initEPerson(context, request, eperson);
			context.setCurrentUser(eperson);
			log.info("SHIB-PASS: Authentication successfull via password. (email='"+username+")");
			return SUCCESS;
		} else {
			// Passsword failure
			log.error("SHIB-PASS: Authentication failed for user "+username+" because a bad password was supplied.");
			return BAD_CREDENTIALS;
		}

	}



	/**
	 * Initialize shibboleth tables when needed. This method may be called multiple
	 * times with no ill-side effect.
	 * 
	 * @param context The DSpace context
	 */
	protected static void initalizeShibbolethAuthentication(Context context)
	throws SQLException {

		if (!initialized) {
			// this authentication method depends on an altered EPerson table
			// lets check to make sure the columns are there, if not lets add
			// them
			boolean batch = false;
			boolean cn = false;
			boolean edupersonorgdn = false;
			boolean edupersonorgunitdn = false;
			boolean edupersonaffiliation = false;
			boolean tdlhomepostaladdress = false;
			boolean initials = false;
			boolean tdledupersongraduationsem = false;
			boolean tdledupersonmajor = false;
			boolean tdledupersonmajorcode = false;
			boolean tdlDepartmentName = false;

			ResultSet rs = DatabaseManager.getConnection().getMetaData()
			.getColumns(DatabaseManager.getConnection().getCatalog(),
					null, "eperson", "%");

			while (rs.next()) {
				String name = rs.getString("COLUMN_NAME");
				String type = rs.getString("TYPE_NAME");
				int size = rs.getInt("COLUMN_SIZE");
				cn = cn ? cn
						: ("cn".equals(name) && "varchar".equals(type) && (size == 1024));
				edupersonorgdn = edupersonorgdn ? edupersonorgdn
						: (("edupersonorgdn".equals(name)
								&& "varchar".equals(type) && size == 1024));
				edupersonorgunitdn = edupersonorgunitdn ? edupersonorgunitdn
						: (("edupersonorgunitdn".equals(name)
								&& "varchar".equals(type) && size == 1024));
				edupersonaffiliation = edupersonaffiliation ? edupersonaffiliation
						: (("edupersonaffiliation".equals(name)
								&& "varchar".equals(type) && size == 1024));
				tdlhomepostaladdress = tdlhomepostaladdress ? tdlhomepostaladdress
						: (("tdlhomepostaladdress".equals(name)
								&& "varchar".equals(type) && size == 1024));
				initials = initials ? initials : (("initials".equals(name)
						&& "varchar".equals(type) && size == 1024));
				tdledupersongraduationsem = tdledupersongraduationsem ? tdledupersongraduationsem
						: (("tdledupersongraduationsem".equals(name)
								&& "varchar".equals(type) && size == 1024));
				tdledupersonmajor = tdledupersonmajor ? tdledupersonmajor
						: (("tdledupersonmajor".equals(name)
								&& "varchar".equals(type) && size == 1024));
				tdledupersonmajorcode = tdledupersonmajorcode ? tdledupersonmajorcode
						: (("tdledupersonmajorcode".equals(name)
								&& "varchar".equals(type) && size == 1024));
				tdlDepartmentName = tdlDepartmentName ? tdlDepartmentName
						: (("tdldepartmentname".equals(name)
								&& "varchar".equals(type) && size == 1024));
			}
			Connection con = DatabaseManager.getConnection();
			Statement stmt = con.createStatement();
			if (!cn) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'cn'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN cn varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;
			}

			if (!edupersonorgdn) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'edupersonorgdn'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN edupersonorgdn varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}

			if (!edupersonorgunitdn) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'edupersonorgunitdn'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN edupersonorgunitdn varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}
			if (!edupersonaffiliation) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'edupersonaffiliation'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN edupersonaffiliation varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}
			if (!tdlhomepostaladdress) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'tdlhomepostaladdress'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN tdlhomepostaladdress varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}
			if (!initials) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'initials'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN initials varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}
			if (!tdledupersongraduationsem) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'tdledupersongraduationsem'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN tdledupersongraduationsem varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}
			if (!tdledupersonmajor) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'tdledupersonmajor'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN tdledupersonmajor varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}
			if (!tdledupersonmajorcode) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'tdledupersonmajorcode'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN tdledupersonmajorcode varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}
			if (!tdlDepartmentName) {
				log.info("SHIB-INIT: Shibboleth login adding eperson column 'tdlDepartmentName'...");
				String sqlQuery = "ALTER TABLE eperson ADD COLUMN tdldepartmentname varchar(1024)";
				stmt.addBatch(sqlQuery);
				batch = true;

			}

			if (batch) {
				stmt.executeBatch();
				stmt.close();
				con.commit();
				context.commit();
			}

			initialized = true;
		}
	}

	/**
	 * Method allows us to truncate the length of the header values.
	 * 
	 * @param maxLength
	 * @param headerName
	 * @param request
	 * @return
	 */

	private String getHeaderValue(int maxLength, String headerName,
			HttpServletRequest request) {
		String value = request.getHeader(headerName);
		if ((value != null) && (value.length() > maxLength))
			return value.substring(0, maxLength);

		return value;
	}

}
