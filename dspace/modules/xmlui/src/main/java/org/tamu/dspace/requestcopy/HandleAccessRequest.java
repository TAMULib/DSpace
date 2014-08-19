/*
 * RequestCopyForm.java
 */

package org.tamu.dspace.requestcopy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Composite;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.app.xmlui.wing.element.TextArea;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.core.Utils;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.Subscribe;
import org.xml.sax.SAXException;

// Should this be an Action instead? Yeah, this really ought to be an action.
public class HandleAccessRequest extends AbstractDSpaceTransformer
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(RequestCopyForm.class);


    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) 
    throws ProcessingException, SAXException, IOException
    {
	super.setup(resolver, objectModel, src, parameters);
    }

    // Do we put anything in the metadata? Perhaps the item's submitter? Their email address could be useful.
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
    WingException, UIException, SQLException, IOException,
    AuthorizeException
    {
	// TODO: trail information goes here

    }

    /** What to add at the end of the body */
    public void addBody(Body body) throws SAXException, WingException,
    UIException, SQLException, IOException, AuthorizeException
    {
	Request request = ObjectModelHelper.getRequest(objectModel);
	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

	Division mainDiv = null;

	Item item = (Item)dso;
	if (!(dso instanceof Item))
	{
	    // return in shame?
	    return;
	}

	// 1. check database, check status. If already approved, show status. Else, are we accepting a request?
	// (step might be unnecessary)

	// 2. Else, process the answer

	// If we got an answer back 
	if (request.getParameter("answer") != null)
	{
	    String answer = request.getParameter("answer");

	    int requestId = Integer.parseInt(request.getParameter("rid"));
	    String requestHash = request.getParameter("token");
	    AccessRequest ar = AccessRequest.findById(context, requestId);
	    
	    if (ar == null || !ar.getEmailHash().equals(requestHash)) {
		// TODO: Murp? Is somebody being sneaky?
		return;
	    }
	    
	    EPerson requestor = EPerson.find(context, ar.getRequestorId());
	    EPerson author = EPerson.find(context, ar.getAuthorId());
	    
	    if ("temp".equals(answer))
	    {
		ar.makeDecision(AccessRequest.STATUS_TEMPORARY_ACCESS);

		ResourcePolicy rp = ResourcePolicy.create(context);

		rp.setResource(item);
		rp.setAction(Constants.READ);
		rp.setEPerson(requestor);

		// Set the start and end dates as "now" and "X days from now"
		// TODO: make this a dspace.cfg param
		rp.setStartDate(new Date());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, 7);
		rp.setEndDate(calendar.getTime());

		// and write out new policy
		rp.update();

		for (Bundle bundle : item.getBundles()) {
		    for (Bitstream bs : bundle.getBitstreams()) {
			rp = ResourcePolicy.create(context);

			rp.setResource(bs);
			rp.setAction(Constants.READ);
			rp.setEPerson(requestor);

			rp.setStartDate(new Date());
			calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, 7);
			rp.setEndDate(calendar.getTime());

			// and write out new policy
			rp.update();
		    }
		}
	    }
	    else if ("perm".equals(answer))
	    {
		ar.makeDecision(AccessRequest.STATUS_PERMANENT_ACCESS);
		AuthorizeManager.addPolicy(context, item, Constants.READ, requestor);
		for (Bundle bundle : item.getBundles()) {
		    for (Bitstream bs : bundle.getBitstreams()) {
			AuthorizeManager.addPolicy(context, bs, Constants.READ, requestor);
		    }
		}
	    }
	    else if ("open".equals(answer))
	    {
		// Remove all policies of type "READ", then add "Anonymous" to everything
		ar.makeDecision(AccessRequest.STATUS_OPEN_ACCESS);
		AuthorizeManager.removePoliciesActionFilter(context, item, Constants.READ);
		AuthorizeManager.addPolicy(context, item, Constants.READ, Group.findByName(context, "Anonymous"));

		for (Bundle bundle : item.getBundles()) {
		    for (Bitstream bs : bundle.getBitstreams()) {
			AuthorizeManager.removePoliciesActionFilter(context, bs, Constants.READ);
			AuthorizeManager.addPolicy(context, bs, Constants.READ, Group.findByName(context, "Anonymous"));
		    }
		}
	    }
	    else 
	    {
		// Deny request >.<
		ar.makeDecision(AccessRequest.STATUS_DENIED);                    
	    }
	    
	    ar.update();

	    // Send email from the associated template
	    sendEmail(ar);

	    mainDiv = body.addDivision("acknowledge-request", "primary");
	    mainDiv.setHead("Request for Access Acknowledged");
	    
	    Division ackDiv = mainDiv.addDivision("acknowledge-info");
	    ackDiv.addPara("Thank you for acknowledging this access request. The user has been notified of the change in item status.");
	    
	    List rafList = ackDiv.addList("acknowledge-info-list");
            rafList.addLabel("Request ID");
            rafList.addItem("" + ar.getRequestId());
            rafList.addLabel("Item name");
            rafList.addItem(Item.find(context, ar.getItemId()).getName());
            rafList.addLabel("Request time");
            rafList.addItem(Utils.formatISO8601Date(ar.getRequestDate()));
            rafList.addLabel("Request status");
            rafList.addItem(AccessRequest.statusToString(ar.getStatus()));
            rafList.addLabel("Decision time");
            rafList.addItem(Utils.formatISO8601Date(ar.getDecisionDate()));
 
	}
    }


    
    public void sendEmail(AccessRequest ar) throws SQLException, IOException 
    {
	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
	EPerson requestor = EPerson.find(context, ar.getRequestorId());
	EPerson submitter = EPerson.find(context, ar.getAuthorId());
	Locale supportedLocale = I18nUtil.getEPersonLocale(requestor);
	String itemURL = ConfigurationManager.getProperty("dspace.url") + "/handle/" + ((Item)dso).getHandle();
	
        Email email;
        switch(ar.getStatus()) {
        	case AccessRequest.STATUS_DENIED: email = Email.getEmail(I18nUtil.getEmailFilename(supportedLocale, "request_access_deny")); break;
        	case AccessRequest.STATUS_TEMPORARY_ACCESS: email = Email.getEmail(I18nUtil.getEmailFilename(supportedLocale, "request_access_temp")); break;
        	case AccessRequest.STATUS_PERMANENT_ACCESS: email = Email.getEmail(I18nUtil.getEmailFilename(supportedLocale, "request_access_perm")); break;
        	case AccessRequest.STATUS_OPEN_ACCESS: email = Email.getEmail(I18nUtil.getEmailFilename(supportedLocale, "request_access_open")); break;
        	default: email = Email.getEmail(I18nUtil.getEmailFilename(supportedLocale, "request_access_error"));
        }
        
        email.addRecipient(requestor.getEmail());

        /*
         * Parameters: 
         {0} requestor's name
         {1} submitter'sname
         {2} item title
         {3} item handle
         {4} item link
         */
        email.addArgument(requestor.getFullName());
        email.addArgument(submitter.getFullName());
        email.addArgument(dso.getName());
        email.addArgument(dso.getHandle());
        email.addArgument(itemURL);

        
        try {
		email.send();
	    } catch (MessagingException ex) {
		log.error("Failed sending email during Temporary Access Request.", ex);
		ar.setStatus(AccessRequest.STATUS_ERROR);
		return;
	    }
	
	
    }








}
