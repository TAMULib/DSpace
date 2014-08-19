package edu.tamu.dspace.aggregateKML;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.SAXOutputter;
import org.xml.sax.SAXException;

/**
 * 
 * Loops through all the referencedLocations KML files in the METADATA bundles
 * of the items in a collection and aggregates them into one big KML file.
 * 
 * @author James Creel
 */

public class AggregateKMLGenerator extends AbstractGenerator{
	
	/** log4j category */
	private static Logger log = Logger.getLogger(ConfigurationManager.class);

	@Override
	public void generate() throws IOException, SAXException,
			ProcessingException {
		
		
		// Open a new context.
		try 
		{
			Context context = ContextUtil.obtainContext(objectModel);
			
			DSpaceObject dso = HandleManager.resolveToObject(context, parameters.getParameter("handle",null));
			
			if( dso != null )
			{
				if( dso.getType() != Constants.COLLECTION)
	            {
	            	//Can't aggregate a non-collection object
	            	return;
	            }
	            
	            Collection collection = (Collection) dso;
	            	            
	            ItemIterator collectionIterator = collection.getItems();
				
	            HashMap<String, Document> handlesToDocuments = new HashMap<String, Document>();
	            
	            while(collectionIterator.hasNext())
	            {
	            	Item i = collectionIterator.next();
	            	
	            	Bundle[] metadataBundles = i.getBundles("METADATA");
	            	
	            	for(Bundle bundle : metadataBundles)
	            	{
	            		Bitstream[] bitstreams = bundle.getBitstreams();
	            		
	            		for(Bitstream b : bitstreams)
	            		{
	            			if(b.getName().contains("referencedLocations"))
	            			{
	            				SAXBuilder saxb = new SAXBuilder();
	            				
	            				//The builder may throw a JDOMException or an AuthorizeException
	            				handlesToDocuments.put(i.getHandle(), saxb.build(b.retrieve()));
	            				
	            				//we will only care about one of the bitstreams for now, as there ought to be just one anyway.
	            				break;
	            			}
	            		}
	            	}
	            }
	            
				SAXOutputter out = new SAXOutputter(contentHandler);
	            
	            /*
	             *  The final output:
	                
	              	<kml xmlns="http://www.opengis.net/kml/2.2">
						<Document>
							<name>
								Referenced Locations
							</name>
							<Folder>...
						</Document>
					</kml>
	             */
	            Element root = new Element("kml");
	            Namespace kmlNamespace = Namespace.getNamespace("http://www.opengis.net/kml/2.2");
	            
	            root.setNamespace(kmlNamespace);
	            
	            Element documentElement = new Element("Document");
	            
	            Element documentNameElement = new Element("name");
	            documentNameElement.addContent("Referenced locations for " + dso.getName() + " (" + dso.getHandle() + ")");
	            
	            documentElement.addContent(documentNameElement);
	            
	            
	            for(Document d : handlesToDocuments.values())
	            {
	            	Filter elementFilter = new ElementFilter();
	            	Iterator<Element> docIterator = (Iterator<Element>) d.getDescendants(elementFilter);
	            	while(docIterator.hasNext())
	            	{
	            		Element e = docIterator.next();
	            		if(e.getName().equals("Folder"))
	            		{
	            			documentElement.addContent((Element) e.clone());
	            			//we only need the single Folder element
	            			break;
	            		}
	            	}
	            }
	            
	            root.addContent(documentElement);
	            
	            
            	//may throw a JDOMException
				out.output(root);
			    
			}
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JDOMException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (AuthorizeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
