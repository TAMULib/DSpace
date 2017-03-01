package org.dspace.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.ItemIterator;
import org.dspace.rest.common.MetadataEntry;
import org.dspace.rest.common.MetadataRecord;
import org.dspace.rest.exceptions.ContextException;

/**
 * Class which provides an endpoint to access metadata by VIVO URI
 * 
 * @author Ryan Laddusaw (rladdusaw@library.tamu.edu)
 * 
 */

@Path("/advisor")
public class AdvisorResource extends Resource
{
	
	private static final Logger log = Logger.getLogger(AdvisorResource.class);
	
	@GET
	@Path("/{advisor_uri : .+}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<MetadataRecord> getAdvisorMetadataByAuthority(@PathParam("advisor_uri") String advisor_uri,
			@Context HttpHeaders headers, @QueryParam("expand") String expand)
	{
		log.info("Reading items (advisor uri: " + advisor_uri +")");
		org.dspace.core.Context context = null;
		List<MetadataRecord> records = new ArrayList<MetadataRecord>();
		
		try
		{
			context = createContext(getUser(headers));
			ItemIterator dspaceItems = org.dspace.content.Item.findByAuthorityValue(context, "dc", "contributor", "advisor", advisor_uri);
			while (dspaceItems.hasNext())
			{
				List<MetadataEntry> metadata = null;
				MetadataRecord record = null;
				org.dspace.content.Item dspaceItem = dspaceItems.next();
				metadata = new org.dspace.rest.common.Item(dspaceItem, "metadata", context).getMetadata();
				record = new org.dspace.rest.common.MetadataRecord(metadata);
				records.add(record);
				
			}
			context.complete();
		}
		catch (SQLException e)
		{
			processException("Could not retreive items for URI: " + advisor_uri + ". SQLException. Message: " + e, context);
		}
		catch (IOException e)
		{
			processException("Could not retreive items for URI: " + advisor_uri + ". IOException. Message: " + e, context);
		}
		catch (AuthorizeException e)
		{
			processException("Could not retreive items for URI: " + advisor_uri + ". AuthorizeException. Message: " + e, context);
		}
		catch (ContextException e)
		{
			processException("Could not retreive items for URI: " + advisor_uri + ". ContentException. Message: " + e, context);
		}
		finally
		{
			processFinally(context);
		}
		
		log.trace("Items were successfully read.");
		return records;
	}
}
