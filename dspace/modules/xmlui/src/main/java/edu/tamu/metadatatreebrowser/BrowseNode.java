package edu.tamu.metadatatreebrowser;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
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
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.ReferenceSet;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.search.DSQuery;
import org.dspace.search.QueryArgs;
import org.dspace.search.QueryResults;
import org.dspace.sort.SortException;
import org.dspace.sort.SortOption;
import org.xml.sax.SAXException;

/**
 * Display entire metadata browse tree in one nested list, typically for the community or collection homepage.s
 * 
 * @author Scott Phillips, http://www.scottphillips.com
 */

public class BrowseNode extends AbstractDSpaceTransformer implements CacheableProcessingComponent  {

	
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");

    public static final Message T_untitled = 
    	message("xmlui.general.untitled");
	
	/** Cached validity object */
	private SourceValidity validity;

	/** Member variables, these are created on setup, and then cleared out on recycle. The rest of the code uses these variables. **/
	private DSpaceObject scope;
	private MetadataTreeNode node;
	private QueryResults queryResults;
	
	
	/**
	 * Generate the unique caching key.
	 */
	public Serializable getKey() {
		try {
			DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

			if (dso == null) {
				return "0"; // no item, something is wrong
			}

			return HashUtil.hash(dso.getHandle() + ":"+scope.getID()+":"+node.getId());
		} catch (SQLException sqle) {
			// Ignore all errors and just return that the component is not
			// cachable.
			return "0";
		}
	}

	/**
	 * Generate the cache validity object.
	 */
	public SourceValidity getValidity() {
		if (this.validity == null) {
			
			DSpaceValidity validity = new DSpaceValidity();
			// Ten minute cache time
			validity.setAssumedValidityDelay(1000 * 60 * 10);
			this.validity = validity.complete();
		}
		return this.validity;
	}
	
	public void setup(SourceResolver resolver, Map objectModel, String src,
			Parameters parameters) throws ProcessingException, SAXException,
			IOException {
		super.setup(resolver, objectModel, src, parameters);
		try {

			Request request = ObjectModelHelper.getRequest(objectModel);
			String nodeString = request.getParameter("node");
			scope = HandleUtil.obtainHandle(objectModel);

			MetadataTreeNode root = MetadataTreeNode.generateBrowseTree(context, scope);

			node = root.findById(Integer.valueOf(nodeString));
			queryResults = performSearch();
			
		} catch (SQLException sqle) {
			handleException(sqle);
		} catch (AuthorizeException ae) {
			handleException(ae);
		} catch (UIException uie) {
			handleException(uie);
		}
	}
	
	/**
     * Query DSpace for a list of all items / collections / or communities that
     * match the given search query.
	 * @throws SQLException 
	 * @throws UIException 
	 * @throws IOException 
	 * @throws SortException 
     */
    protected QueryResults performSearch() throws SQLException, UIException, IOException
    {
		Request request = ObjectModelHelper.getRequest(objectModel);
        Context context = ContextUtil.obtainContext(objectModel);
        int page = 1;
        try {
        	page = Integer.valueOf(request.getParameter("page"));
        } catch (NumberFormatException nfe) { /* ingore */ };

        QueryArgs queryArgs = new QueryArgs();
        queryArgs.setPageSize(20); // 20 items per page
        try {
			queryArgs.setSortOption(SortOption.getSortOption(1)); // Sort by title
		} catch (SortException se) { throw new UIException(se);	}
        queryArgs.setSortOrder("ASC"); // assending

        queryArgs.setQuery("(ispartof: \""+node.getFieldValue()+"\")");
        if (page > 1)
        {
            queryArgs.setStart((Integer.valueOf(page) - 1) * queryArgs.getPageSize());
        }
        else
        {
            queryArgs.setStart(0);
        }

        QueryResults qResults = null;
        if (scope instanceof Community)
        {
            qResults = DSQuery.doQuery(context, queryArgs, (Community) scope);
        }
        else if (scope instanceof Collection)
        {
            qResults = DSQuery.doQuery(context, queryArgs, (Collection) scope);
        }
        else
        {
            qResults = DSQuery.doQuery(context, queryArgs);
        }

        return qResults;
    }
	
	
    /**
     * Add the community's title and trail links to the page's metadata
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
    	pageMeta.addMetadata("title").addContent(node.getName());
    	
        // Add the trail back to the repository root.
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        HandleUtil.buildHandleTrail(scope, pageMeta,contextPath);
    }
    
    
	/**
	 * Display an overview of the browse tree for community or collection
	 * homepage.
	 */
	public void addBody(Body body) throws SAXException, WingException,
			UIException, SQLException, IOException, AuthorizeException {
		
		if (node == null)
			return;
		
		String baseURL = contextPath + "/handle/" + scope.getHandle()+ "/mdbrowse";
		
		// Display the Parent bread crumb
		Division div = body.addDivision("metadata-tree-browser-node","primary");
		
		
		// Nested parent list
		Division parentDiv = div.addDivision("parent-div");
		List parentList = parentDiv.addList("parent-list");
		parentList.addItemXref(contextPath + "/handle/" + scope.getHandle(), scope instanceof Collection ? "Collection Home" : "Community Home");

		if (! (node.getParent() == null || node.getParent().isRoot())) {
			parentList = parentList.addList("parent-sub-list");


			for(MetadataTreeNode parent : node.getParents()) {

				if (!parent.isRoot()) {
					String nodeURL = baseURL + "?node=" + parent.getId();

					parentList.addItemXref(nodeURL, parent.getName());
					parentList = parentList.addList("parent-sub-list");
				}
			}
		}
		
		
		Division contentDiv = div.addDivision("node-content-div","primary");
		contentDiv.setHead(node.getName());
		
		// Display any children
		if (node.hasChildren()) {
			Division childDiv = contentDiv.addDivision("child-div");
			List childList = childDiv.addList("child-list");
			for(MetadataTreeNode child : node.getChildren()) {
				
				Bitstream thumbnail = Bitstream.find(context, child.getThumbnailId());
				String thumbnailURL = contextPath + "/bitstream/id/"+thumbnail.getID()+"/?sequence="+thumbnail.getSequenceID();
				String nodeURL = baseURL + "?node=" + child.getId();

				Item item = childList.addItem();
				item.addFigure(thumbnailURL, nodeURL, "node-thumbnail");
				item.addXref(nodeURL, child.getName(),"node-label");
			}
		}
		
		// Display any items
		if (node.hasContent()) {
			buildSearchResultsDivision(contentDiv);
			
		}
	}    
    
    /**
     * 
     * Attach a division to the given search division named "search-results"
     * which contains results for this search query.
     * 
     * @param search
     *            The search division to contain the search-results division.
     */
    protected void buildSearchResultsDivision(Division search)
            throws IOException, SQLException, WingException
    {
        
        Division results = search.addDivision("search-results","primary");
        
        if (queryResults.getHitCount() > 0)
        {
            // Pagination variables.
            int itemsTotal = queryResults.getHitCount();
            int firstItemIndex = queryResults.getStart() + 1;
            int lastItemIndex = queryResults.getStart()
                    + queryResults.getPageSize();
            if (itemsTotal < lastItemIndex)
            {
                lastItemIndex = itemsTotal;
            }
            int currentPage = (queryResults.getStart() / queryResults
                    .getPageSize()) + 1;
            int pagesTotal = ((queryResults.getHitCount() - 1) / queryResults
                    .getPageSize()) + 1;
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("page", "{pageNum}");
            String pageURLMask = generateURL(parameters);

            results.setMaskedPagination(itemsTotal, firstItemIndex,
                    lastItemIndex, currentPage, pagesTotal, pageURLMask);

            ReferenceSet referenceSet = results.addReferenceSet("search-results-repository", ReferenceSet.TYPE_SUMMARY_LIST,null,"repository-search-results");;            
            @SuppressWarnings("unchecked") // This cast is correct
            java.util.List<String> itemHandles = queryResults.getHitHandles();
            for (String handle : itemHandles) {
                DSpaceObject resultDSO = HandleManager.resolveToObject( context, handle);
                referenceSet.addReference(resultDSO);
            }
            
        }
        else
        {
            results.addPara("No content found.");
        }
    }
    
    
    
    /**
     * Generate a url to the simple search url.
     */
    protected String generateURL(Map<String, String> parameters)
    throws UIException {
    	Request request = ObjectModelHelper.getRequest(objectModel);

    	parameters.put("node",request.getParameter("node"));

    	if (parameters.get("page") == null)
    		parameters.put("page", request.getParameter("page"));


    	return super.generateURL("mdbrowse", parameters);
    }
    
    
    
    public void recyle() {
    	try {
    		validity = null;
    		scope = null;
    		node = null;
    		queryResults = null;
    	} finally {
    		super.recycle();
    	}
    	
    }
    
}
