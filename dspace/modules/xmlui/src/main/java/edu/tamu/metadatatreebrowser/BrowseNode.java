/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package edu.tamu.metadatatreebrowser;

import org.dspace.app.xmlui.aspect.discovery.AbstractSearch;
import org.dspace.app.xmlui.aspect.discovery.DiscoveryUIUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.discovery.*;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

/**
 * Perform a simple search of the repository. The user provides a simple one
 * field query (the url parameter is named query) and the results are processed.
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Adán Román Ruiz <aroman@arvo.es> (Bugfix)
 */
public class BrowseNode extends AbstractSearch implements CacheableProcessingComponent {
	private static final Logger log = Logger.getLogger(BrowseNode.class);

	private MetadataTreeNode node;
	
    private static final Message T_head1_none =
            message("xmlui.Discovery.AbstractSearch.head1_none");

	private static final Message T_title =
            message("xmlui.ArtifactBrowser.SimpleSearch.title");

    private static final Message T_dspace_home =
            message("xmlui.general.dspace_home");

    private static final Message T_trail =
            message("xmlui.ArtifactBrowser.SimpleSearch.trail");

    private static final Message T_search_scope =
        message("xmlui.Discovery.SimpleSearch.search_scope");

    private static final Message T_head =
            message("xmlui.ArtifactBrowser.SimpleSearch.head");

    private static final Message T_no_results =
            message("xmlui.ArtifactBrowser.AbstractSearch.no_results");
    
    private static final Message T_result_head_3 = message("xmlui.Discovery.AbstractSearch.head3");
    private static final Message T_result_head_2 = message("xmlui.Discovery.AbstractSearch.head2");

//  private static final Message T_search_label =
//	            message("xmlui.discovery.SimpleSearch.search_label");

    private static final Message T_go = message("xmlui.general.go");
    private static final Message T_filter_label = message("xmlui.Discovery.SimpleSearch.filter_head");
    private static final Message T_filter_help = message("xmlui.Discovery.SimpleSearch.filter_help");
    private static final Message T_filter_current_filters = message("xmlui.Discovery.AbstractSearch.filters.controls.current-filters.head");
    private static final Message T_filter_new_filters = message("xmlui.Discovery.AbstractSearch.filters.controls.new-filters.head");
    private static final Message T_filter_controls_apply = message("xmlui.Discovery.AbstractSearch.filters.controls.apply-filters");
    private static final Message T_filter_controls_add = message("xmlui.Discovery.AbstractSearch.filters.controls.add-filter");
    private static final Message T_filter_controls_remove = message("xmlui.Discovery.AbstractSearch.filters.controls.remove-filter");
    private static final Message T_filters_show = message("xmlui.Discovery.AbstractSearch.filters.display");
    private static final Message T_filter_contain = message("xmlui.Discovery.SimpleSearch.filter.contains");
    private static final Message T_filter_equals = message("xmlui.Discovery.SimpleSearch.filter.equals");
    private static final Message T_filter_notcontain = message("xmlui.Discovery.SimpleSearch.filter.notcontains");
    private static final Message T_filter_notequals = message("xmlui.Discovery.SimpleSearch.filter.notequals");
    private static final Message T_filter_authority = message("xmlui.Discovery.SimpleSearch.filter.authority");
    private static final Message T_filter_notauthority = message("xmlui.Discovery.SimpleSearch.filter.notauthority");
    private static final Message T_did_you_mean = message("xmlui.Discovery.SimpleSearch.did_you_mean");

    private SearchService searchService = null;

    public BrowseNode() {
        DSpace dspace = new DSpace();
        searchService = dspace.getServiceManager().getServiceByName(SearchService.class.getName(),SearchService.class);
    }


    /**
     * Add Page metadata.
     */
    public void addPageMeta(PageMeta pageMeta) throws WingException, SQLException {
       	pageMeta.addMetadata("title").addContent(node.getName());
       	
        // Add the trail back to the repository root.
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if ((dso instanceof org.dspace.content.Collection) || (dso instanceof Community)) {
            HandleUtil.buildHandleTrail(dso, pageMeta, contextPath, true);
        }
    }

    /**
     * build the DRI page representing the body of the search query. This
     * provides a widget to generate a new query and list of search results if
     * present.
     */
    public void addBody(Body body) throws SAXException, WingException,
            SQLException, IOException, AuthorizeException {

        Request request = ObjectModelHelper.getRequest(objectModel);
        String queryString = getQuery();
        
		String nodeString = request.getParameter("node");
		
        DSpaceObject currentScope = getScope();
		
		MetadataTreeNode root = MetadataTreeNode.generateBrowseTree(context, currentScope);

		node = root.findById(Integer.valueOf(nodeString));

		if (node == null)
			return;
		
		String baseURL = contextPath + "/handle/" + currentScope.getHandle()+ "/mdbrowse";
		
		// Display the Parent bread crumb
		Division div = body.addDivision("metadata-tree-browser-node","primary");
		
		
		// Nested parent list
		Division parentDiv = div.addDivision("parent-div");
		org.dspace.app.xmlui.wing.element.List parentList = parentDiv.addList("parent-list");
		parentList.addItemXref(contextPath + "/handle/" + currentScope.getHandle(), currentScope instanceof org.dspace.content.Collection ? "Collection Home" : "Community Home");

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
			org.dspace.app.xmlui.wing.element.List childList = childDiv.addList("child-list");
			for(MetadataTreeNode child : node.getChildren()) {
				
				Bitstream thumbnail = Bitstream.find(context, child.getThumbnailId());
				String thumbnailURL = contextPath + "/bitstream/id/"+thumbnail.getID()+"/?sequence="+thumbnail.getSequenceID();
				String nodeURL = baseURL + "?node=" + child.getId();

				org.dspace.app.xmlui.wing.element.Item item = childList.addItem();
				item.addFigure(thumbnailURL, nodeURL, "node-thumbnail");
				item.addXref(nodeURL, child.getName(),"node-label");
			}
		}
		
        // Add the result division
		// Display any items
		if (node.hasContent()) {
			try {
	            buildSearchResultsDivision(contentDiv);
	        } catch (SearchServiceException e) {
	            throw new UIException(e.getMessage(), e);
	        }
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
   @Override 
   protected void buildSearchResultsDivision(Division search)
		   throws IOException, SQLException, WingException, SearchServiceException
   {
       try {
           if (queryResults == null) {

               DSpaceObject scope = getScope();
               this.performSearch(scope);
           }
       }
       catch (RuntimeException e) {
           log.error(e.getMessage(), e);
           queryResults = null;
       }
       catch (Exception e) {
           log.error(e.getMessage(), e);
           queryResults = null;
       }	   
	   
       Division results = search.addDivision("search-results","primary");
       
       DSpaceObject searchScope = getScope();

       int displayedResults;
       long totalResults;
       float searchTime;
       
       if(queryResults != null && 0 < queryResults.getTotalSearchResults())
       {
           displayedResults = queryResults.getDspaceObjects().size();
           totalResults = queryResults.getTotalSearchResults();
           searchTime = ((float) queryResults.getSearchTime() / 1000) % 60;

           if (searchScope instanceof org.dspace.content.Community)
           {
        	   org.dspace.content.Community community = (org.dspace.content.Community) searchScope;
               String communityName = community.getMetadata("name");
           } else if (searchScope instanceof org.dspace.content.Collection){
        	   org.dspace.content.Collection collection = (org.dspace.content.Collection) searchScope;
               String collectionName = collection.getMetadata("name");
           } else {
               results.setHead(T_head1_none.parameterize(displayedResults, totalResults, searchTime));
           }
       }

       if (queryResults != null && 0 < queryResults.getDspaceObjects().size())
       {

           // Pagination variables.
           int itemsTotal = (int) queryResults.getTotalSearchResults();
           int firstItemIndex = (int) this.queryResults.getStart() + 1;
           int lastItemIndex = (int) this.queryResults.getStart() + queryResults.getDspaceObjects().size();
           
           

           //if (itemsTotal < lastItemIndex)
           //    lastItemIndex = itemsTotal;
           int currentPage = this.queryResults.getStart() / this.queryResults.getMaxResults() + 1;
           int pagesTotal = (int) ((this.queryResults.getTotalSearchResults() - 1) / this.queryResults.getMaxResults()) + 1;
           Map<String, String> parameters = new HashMap<String, String>();
           parameters.put("page", "{pageNum}");
           String pageURLMask = generateURL(parameters);
           pageURLMask = addFilterQueriesToUrl(pageURLMask);

           results.setMaskedPagination(itemsTotal, firstItemIndex,
                   lastItemIndex, currentPage, pagesTotal, pageURLMask);

           // Look for any communities or collections in the mix
           org.dspace.app.xmlui.wing.element.List dspaceObjectsList = null;

           // Put it on the top of level search result list
           dspaceObjectsList = results.addList("search-results-repository",
                   org.dspace.app.xmlui.wing.element.List.TYPE_DSO_LIST, "repository-search-results");

           java.util.List<DSpaceObject> commCollList = new ArrayList<DSpaceObject>();
           java.util.List<org.dspace.content.Item> itemList = new ArrayList<org.dspace.content.Item>();
           for (DSpaceObject resultDso : queryResults.getDspaceObjects())
           {
               if(resultDso.getType() == Constants.COMMUNITY || resultDso.getType() == Constants.COLLECTION)
               {
                   commCollList.add(resultDso);
               }else
               if(resultDso.getType() == Constants.ITEM)
               {
                   itemList.add((org.dspace.content.Item) resultDso);
               }
           }

           if(CollectionUtils.isNotEmpty(commCollList))
           {
               org.dspace.app.xmlui.wing.element.List commCollWingList = dspaceObjectsList.addList("comm-coll-result-list");
               commCollWingList.setHead(T_result_head_2);
               for (DSpaceObject dso : commCollList)
               {
                   DiscoverResult.DSpaceObjectHighlightResult highlightedResults = queryResults.getHighlightedResults(dso);
                   if(dso.getType() == Constants.COMMUNITY)
                   {
                       //Render our community !
                       org.dspace.app.xmlui.wing.element.List communityMetadata = commCollWingList.addList(dso.getHandle() + ":community");

                       renderCommunity((Community) dso, highlightedResults, communityMetadata);
                   }else
                   if(dso.getType() == Constants.COLLECTION)
                   {
                       //Render our collection !
                       org.dspace.app.xmlui.wing.element.List collectionMetadata = commCollWingList.addList(dso.getHandle() + ":collection");

                       renderCollection((org.dspace.content.Collection) dso, highlightedResults, collectionMetadata);
                   }
               }
           }

           if(CollectionUtils.isNotEmpty(itemList))
           {
               org.dspace.app.xmlui.wing.element.List itemWingList = dspaceObjectsList.addList("item-result-list");
               if(CollectionUtils.isNotEmpty(commCollList))
               {
                   itemWingList.setHead(T_result_head_3);

               }
               for (org.dspace.content.Item resultDso : itemList)
               {
                   DiscoverResult.DSpaceObjectHighlightResult highlightedResults = queryResults.getHighlightedResults(resultDso);
                   renderItem(itemWingList, resultDso, highlightedResults);
               }
           }

       } else {
           results.addPara(T_no_results);
       }
   }


   @Override
   protected String getBasicUrl() throws SQLException {
       Request request = ObjectModelHelper.getRequest(objectModel);
       DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

       return request.getContextPath() + (dso == null ? "" : "/handle/" + dso.getHandle()) + "/discover";
   }
   

    /**
     * Get the search query from the URL parameter, if none is found the empty
     * string is returned.
     */
    protected String getQuery() throws UIException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String node = decodeFromURL(request.getParameter("node"));
        if (node == null)
        {
            return "";
        }
        return node.trim();
    }
    
    /**
    * Generate a url to the simple search url.
    */
   protected String generateURL(Map<String, String> parameters) throws UIException {
   	Request request = ObjectModelHelper.getRequest(objectModel);

   	parameters.put("node",request.getParameter("node"));

   	if (parameters.get("page") == null)
   		parameters.put("page", request.getParameter("page"));

   		return AbstractDSpaceTransformer.generateURL("mdbrowse", parameters);
   }

}
