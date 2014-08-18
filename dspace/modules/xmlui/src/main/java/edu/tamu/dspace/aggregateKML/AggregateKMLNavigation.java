package edu.tamu.dspace.aggregateKML;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Options;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;


/**
 * 
 * Navigation to invoke the AggregateKMLGenerator to get aggregated KML for all
 * the referencedLocations KML files on the items in a collection
 * 
 * @author James Creel
 */

public class AggregateKMLNavigation extends AbstractDSpaceTransformer 
{
	private static final Message T_my_account = message("xmlui.EPerson.Navigation.my_account");
	
	/** log4j category */
	private static Logger log = Logger.getLogger(ConfigurationManager.class);
    
	public void addOptions(Options options) throws SAXException, WingException,
    UIException, SQLException, IOException, AuthorizeException
	{
    	/* Create skeleton menu structure to ensure consistent order between aspects,
    	 * even if they are never used 
    	 */
        options.addList("browse");
        List account = options.addList("account");
        List context = options.addList("context");
        List admin = options.addList("administrative");
        account.setHead(T_my_account);
        
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
		if (dso != null)
		{
		    if (dso instanceof Collection)
		    {
		    	if (((Collection) dso).canEditBoolean(true))
	            {
		    		context.addItemXref(contextPath+"/metadata/handle/" + dso.getHandle() + "/georeferences.kml", "Georeferences as KML");
	            }
		    }
		}
	}
}
