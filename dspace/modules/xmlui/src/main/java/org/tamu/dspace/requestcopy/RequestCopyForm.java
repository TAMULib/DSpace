/*
 * RequestCopyForm.java
 */

package org.tamu.dspace.requestcopy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.aspect.administrative.registries.MetadataRegistryMain;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Composite;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.app.xmlui.wing.element.TextArea;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.core.Utils;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Subscribe;
import org.xml.sax.SAXException;
 

public class RequestCopyForm extends AbstractDSpaceTransformer
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
        request.getAuthType();
        
        EPerson submitter = ((Item)dso).getSubmitter();
        EPerson requestor = context.getCurrentUser();

        // Necessary foo in order to not kill the current context
        Context tempContext = new Context();
        if (!AccessRequest.tablesExist(tempContext)) 
            AccessRequest.createTables(context);
            
        
        if (!(dso instanceof Item))
        {
            // return in shame?
            return;
        }
        
        
        // 0. If we just sent an email request, generate the email and set the database tables
        if (request.getParameter("submit_request") != null)
        {
            Locale supportedLocale = I18nUtil.getEPersonLocale(submitter);
            Email email = Email.getEmail(I18nUtil.getEmailFilename(supportedLocale, "request_access"));
            email.addRecipient(submitter.getEmail());
            String itemURL = ConfigurationManager.getProperty("dspace.url") + "/handle/" + ((Item)dso).getHandle();

            String message = "" + request.getParameter("message");
            
            AccessRequest accessRequest = AccessRequest.create(context, (Item)dso, message, AccessRequest.STATUS_ERROR);
            
            
            /*
             * Parameters: 
             {0} submitter'sname
             {1} requestor's name
             {2} item title
             {3} item handle
             {4} item link
             {5} request message
             
             {6} deny access link
             {7} grant temporary access link
             {8} grant permanent access link
             {9} switch to open-access link
             
             {10} requestor's email
             */
            email.addArgument(submitter.getFullName());
            email.addArgument(requestor.getFullName());
            email.addArgument(dso.getName());
            email.addArgument(dso.getHandle());
            email.addArgument(itemURL);
            email.addArgument(message);
            
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (!"".equals(request.getServerPort()))
        	baseUrl += ":" + request.getServerPort();
            
            baseUrl += request.getContextPath() + "/handle/" + dso.getHandle() + 
            	"/process-request?rid=" + accessRequest.getRequestId() + "&token=" + accessRequest.getEmailHash();
            
            email.addArgument(baseUrl + "&answer=deny");
            email.addArgument(baseUrl + "&answer=temp");
            email.addArgument(baseUrl + "&answer=perm");
            email.addArgument(baseUrl + "&answer=open");
            
            email.addArgument(requestor.getEmail());
            
            try {
		email.send();
	    } catch (MessagingException ex) {
		log.error("Failed sending email during Temporary Access Request.", ex);
		accessRequest.setStatus(AccessRequest.STATUS_ERROR);
		return;
	    }
	    
	    accessRequest.setStatus(AccessRequest.STATUS_SENT);
	    accessRequest.update();
	    
	    // TODO: add the "email has been sent message" 
	    // Possibly made unnecessary by the next if statement 
        }
        
        // 1. If a database entry exists for this Eperson on this item, display the status
        AccessRequest currentRequest = AccessRequest.findByRequestorAndItem(context, requestor.getID(), dso.getID());
        if (currentRequest != null)
        {
            mainDiv = body.addDivision("request-item", "primary");
            mainDiv.setHead("Access Request Status");
            Division requestAccessStatus = mainDiv.addDivision("request-status");
            
            List rafList = requestAccessStatus.addList("request-status-list");
            rafList.addLabel("Request ID");
            rafList.addItem("" + currentRequest.getRequestId());
            rafList.addLabel("Item name");
            rafList.addItem(Item.find(context, currentRequest.getItemId()).getName());
            rafList.addLabel("Request time");
            rafList.addItem(Utils.formatISO8601Date(currentRequest.getRequestDate()));
            rafList.addLabel("Request status");
            rafList.addItem(AccessRequest.statusToString(currentRequest.getStatus()));
            rafList.addLabel("Decision time");
            if (currentRequest.getDecisionDate() != null)
        	rafList.addItem(Utils.formatISO8601Date(currentRequest.getDecisionDate()));
            else
        	rafList.addItem("pending");
            
            
        }
        // 2. Otherwise, display the request form
        else 
        {
            mainDiv = body.addDivision("request-item", "primary");
            mainDiv.setHead("Submit Request for Temporary Access");
            Division requestAccessForm = mainDiv.addInteractiveDivision("request-item", contextPath + "/handle/"+dso.getHandle()+"/request-copy", Division.METHOD_POST);
            
            List rafList = requestAccessForm.addList("requestItemForm", List.TYPE_FORM);
            
            // TODO: add the actual eperson
            Text toField = rafList.addItem().addText("emailTo");
            toField.setLabel("To");
            toField.setValue(submitter.getEmail());
            toField.setDisabled(true);
            
            Text fromField = rafList.addItem().addText("emailFrom");
            fromField.setLabel("From");
            fromField.setValue(requestor.getEmail());
            fromField.setDisabled(true);
            
            TextArea msgField = rafList.addItem().addTextArea("message");
            msgField.setLabel("Message");
            msgField.setSize(10, 50);
            
            String emailBody = "Dear " + submitter.getFirstName() + " " + submitter.getLastName() +",\n\n" +
            		"I would like to request temporary access to this item. " +
            		"Please contact me at " + requestor.getEmail() + " if you have any questions.\n\n" +
            		"Sincerely,\n" +
            		requestor.getFirstName() + " " + requestor.getLastName();
            msgField.setValue(emailBody);
            
            
            rafList.addItem().addButton("submit_request").setValue("Request Access");            
        }
   
        
    }
    
    
    

    
    
    
    

    

}
