/*
 * RequestCopy.java
 */

package org.tamu.dspace.requestcopy;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.HashUtil;
import org.apache.excalibur.source.SourceValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;


public class RequestCopy extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    
    /** Cached validity object */
    private SourceValidity validity = null;

    
        
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) 
      	throws ProcessingException, SAXException, IOException
	{
		super.setup(resolver, objectModel, src, parameters);
	}
    
    
    
    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() {
        try {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

            if (dso == null)
            {
                return "0"; // no item, something is wrong.
            }

            return HashUtil.hash(dso.getHandle());
        }
        catch (SQLException sqle)
        {
            // Ignore all errors and just return that the component is not cachable.
            return "0";
        }
    }

    /**
     * Generate the cache validity object.
     *
     * The validity object will include the item being viewed,
     * along with all bundles & bitstreams.
     */
    public SourceValidity getValidity()
    {
        DSpaceObject dso = null;

        if (this.validity == null)
    	{
	        try {
	            dso = HandleUtil.obtainHandle(objectModel);

	            DSpaceValidity validity = new DSpaceValidity();
	            validity.add(dso);
	            this.validity =  validity.complete();
	        }
	        catch (Exception e)
	        {
	            // Ignore all errors and just invalidate the cache.
	        }

    	}
    	return this.validity;
    }
    
    
    // Do we put anything in the metadata? Perhaps the item's submitter? Their email address could be useful.
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
 
    }

    /** What to add at the end of the body */
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
    	
        Request request = ObjectModelHelper.getRequest(objectModel);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
    	    	
        Division current = null;
        request.getAuthType();
        
        
        // If the field is undefined, blank, or all items are considered "requestable"
        // Otherwise, we check the item to see if it has the metadata field in question
        String flagField = ConfigurationManager.getProperty("requestCopy.field.requestable");
        
        if (dso instanceof Item && !(
        	flagField == null || "".equals(flagField) || ((Item)dso).getMetadata(flagField).length == 0 
        	|| "false".equalsIgnoreCase(((Item)dso).getMetadata(flagField)[0].value) || "no".equalsIgnoreCase(((Item)dso).getMetadata(flagField)[0].value) ))
        {
            current = body.addDivision("request-item","primary");
            //current = current.addDivision("request-item", "secondary");
        }
        else
        {
            // If it's not an item or it didn't pass the "requestable" test, we simply exit
            return;
        }
        
        
        Division buttons = current.addInteractiveDivision("request-item-form", contextPath + "/handle/"+dso.getHandle()+"/request-copy", Division.METHOD_POST);
        buttons.setHead("Request Temporary Access");
        buttons.addPara("This item and its contents are restricted. However, you can email the author to request temporary access to the item.");
        buttons.addPara(null, "button-list").addButton("submit").setValue("Request Access");
        
        

    }
    
    
    /**
     * Recycle
     */
    public void recycle() {
    	this.validity = null;
    	super.recycle();
    }
   
}
