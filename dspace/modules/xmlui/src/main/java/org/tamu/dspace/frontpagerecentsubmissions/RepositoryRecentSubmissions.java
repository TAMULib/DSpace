
package org.tamu.dspace.frontpagerecentsubmissions;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.ReferenceSet;
import org.dspace.authorize.AuthorizeException;
import org.dspace.browse.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.sort.SortException;
import org.dspace.sort.SortOption;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Renders a list of recently submitted items to the repository. This code is derived from the CommunityRecentSubmissions class in DSpace.
 *
 * @author Scott Phillips, http://www.scottphillips.com
 */
public class RepositoryRecentSubmissions extends AbstractDSpaceTransformer implements CacheableProcessingComponent {

    private static final Logger log = Logger.getLogger(RepositoryRecentSubmissions.class);

    /** How many recent submissions to list */
    private static final int RECENT_SUBMISSIONS = 5;

    /** The cache of recently submitted items */
    private java.util.List<BrowseItem> recentSubmittedItems;

    /** Cached validity object */
    private SourceValidity validity;


    @Override
    public Serializable getKey() {
    	// This transformer works for the whole repository, so our key is just static.
    	return "1";
    }

    @Override
    public SourceValidity getValidity() {
        if (this.validity == null)
    	{
	        try {
	            DSpaceValidity validity = new DSpaceValidity();
	            // Recently submitted items
	            for (BrowseItem item : getRecentlySubmittedItems())
	            {
	                validity.add(item);
	            }

	            this.validity = validity.complete();
	        }
	        catch (Exception e)
	        {
	            // Ignore all errors and invalidate the cache.
	        }

    	}
        return this.validity;
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
    	
        Division recentsubmissions = body.addDivision("frontpage-recent-submissions", "primary");

        java.util.List<BrowseItem> items = getRecentlySubmittedItems();
        if(items.size() == 0)
        {
            return;
        }
        
        ReferenceSet lastSubmitted = recentsubmissions.addReferenceSet(
                "repository-last-submitted", ReferenceSet.TYPE_SUMMARY_LIST,
                null, "recent-submissions");
        for (BrowseItem item : items)
        {
            lastSubmitted.addReference(item);
        }

    }

    /**
     * Get the recently submitted items for the repository.
     *
     * @return List of recently submitted items
     */
    @SuppressWarnings("unchecked")
    private java.util.List<BrowseItem> getRecentlySubmittedItems()
            throws SQLException
    {
        if (recentSubmittedItems != null)
        {
            return recentSubmittedItems;
        }

        String source = ConfigurationManager.getProperty("recent.submissions.sort-option");
        int numRecentSubmissions = ConfigurationManager.getIntProperty("recent.submissions.count", RECENT_SUBMISSIONS);
        if(numRecentSubmissions == 0)
        {
            return new ArrayList<BrowseItem>();
        }
        BrowserScope scope = new BrowserScope(context);
        scope.setResultsPerPage(numRecentSubmissions);

        // FIXME Exception Handling
        try
        {
        	scope.setBrowseIndex(BrowseIndex.getItemBrowseIndex());
            for (SortOption so : SortOption.getSortOptions())
            {
                if (so.getName().equals(source))
                {
                    scope.setSortBy(so.getNumber());
                	scope.setOrder(SortOption.DESCENDING);
                }
            }

        	BrowseEngine be = new BrowseEngine(context);
        	this.recentSubmittedItems = be.browse(scope).getResults();
        }
        catch (SortException se)
        {
            log.error("Caught SortException", se);
        }
        catch (BrowseException bex)
        {
        	log.error("Caught BrowseException", bex);
        }

        return this.recentSubmittedItems;
    }

    @Override
    public void recycle() {
    	this.validity = null;
        this.recentSubmittedItems = null;
        super.recycle();
    }
}
