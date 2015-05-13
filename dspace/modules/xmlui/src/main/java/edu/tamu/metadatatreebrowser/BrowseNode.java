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
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
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
import org.dspace.discovery.configuration.DiscoveryHitHighlightFieldConfiguration;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.discovery.configuration.DiscoverySortConfiguration;
import org.dspace.discovery.configuration.DiscoverySortFieldConfiguration;
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
    
    /**
     * Cached validity object
     */
    private SourceValidity validity;


    public BrowseNode() throws UIException {
        DSpace dspace = new DSpace();
        searchService = dspace.getServiceManager().getServiceByName(SearchService.class.getName(),SearchService.class);
/*        
        try {
	        Request request = ObjectModelHelper.getRequest(objectModel);
		    String nodeString = getQuery();
		    
			MetadataTreeNode root = MetadataTreeNode.generateBrowseTree(context, getScope());
		
			node = root.findById(Integer.valueOf(nodeString));
        } catch (UIException e) {
            log.error(e.getMessage(), e);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
*/
    }
    
    /**
     * Generate the cache validity object.
     * <p/>
     * This validity object should never "over cache" because it will
     * perform the search, and serialize the results using the
     * DSpaceValidity object.
     */
    public SourceValidity getValidity() {
        if (this.validity == null) {
            try {
                DSpaceValidity validity = new DSpaceValidity();

                DSpaceObject scope = getScope();

                Request request = ObjectModelHelper.getRequest(objectModel);
    		    String nodeString = getQuery();
    		    
    			MetadataTreeNode root = MetadataTreeNode.generateBrowseTree(context, scope);
    		
    			node = root.findById(Integer.valueOf(nodeString));
                
                validity.add(scope);

                performSearch(scope);

                java.util.List<DSpaceObject> results = this.queryResults.getDspaceObjects();

                if (results != null) {
                    validity.add("total:"+this.queryResults.getTotalSearchResults());
                    validity.add("start:"+this.queryResults.getStart());
                    validity.add("size:" + results.size());

                    for (DSpaceObject dso : results) {
                        validity.add(dso);
                    }
                }

                Map<String, java.util.List<DiscoverResult.FacetResult>> facetResults = this.queryResults.getFacetResults();
                for(String facetField : facetResults.keySet()){
                	java.util.List<DiscoverResult.FacetResult> facetValues = facetResults.get(facetField);
                    for (DiscoverResult.FacetResult facetResult : facetValues)
                    {
                        validity.add(facetField + facetResult.getAsFilterQuery() + facetResult.getCount());
                    }
                }

                this.validity = validity.complete();
            } catch (RuntimeException re) {
                throw re;
            }
            catch (Exception e) {
                this.validity = null;
            }

            // add log message that we are viewing the item
            // done here, as the serialization may not occur if the cache is valid
            logSearch();
        }
        return this.validity;
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

        DSpaceObject currentScope = getScope();

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
   
   /**
    * Query DSpace for a list of all items / collections / or communities that
    * match the given search query.
    *
    *
    * @param scope the dspace object parent
    */
   public void performSearch(DSpaceObject scope) throws UIException, SearchServiceException {

       if (queryResults != null)
       {
           return;
       }
       

       String query = getQuery();

       //DSpaceObject scope = getScope();

       int page = getParameterPage();

       java.util.List<String> filterQueries = new ArrayList<String>();

       String[] fqs = getFilterQueries();

       if (fqs != null)
       {
           filterQueries.addAll(Arrays.asList(fqs));
       }

       this.queryArgs = new DiscoverQuery();

       //Add the configured default filter queries
       DiscoveryConfiguration discoveryConfiguration = SearchUtils.getDiscoveryConfiguration(scope);
       java.util.List<String> defaultFilterQueries = discoveryConfiguration.getDefaultFilterQueries();
       queryArgs.addFilterQueries(defaultFilterQueries.toArray(new String[defaultFilterQueries.size()]));

       if (filterQueries.size() > 0) {
           queryArgs.addFilterQueries(filterQueries.toArray(new String[filterQueries.size()]));
       }


       queryArgs.setMaxResults(getParameterRpp());

       String sortBy = ObjectModelHelper.getRequest(objectModel).getParameter("sort_by");
       DiscoverySortConfiguration searchSortConfiguration = discoveryConfiguration.getSearchSortConfiguration();
       if(sortBy == null){
           //Attempt to find the default one, if none found we use SCORE
           sortBy = "score";
           if(searchSortConfiguration != null){
               for (DiscoverySortFieldConfiguration sortFieldConfiguration : searchSortConfiguration.getSortFields()) {
                   if(sortFieldConfiguration.equals(searchSortConfiguration.getDefaultSort())){
                       sortBy = SearchUtils.getSearchService().toSortFieldIndex(sortFieldConfiguration.getMetadataField(), sortFieldConfiguration.getType());
                   }
               }
           }
       }
       String sortOrder = ObjectModelHelper.getRequest(objectModel).getParameter("order");
       if(sortOrder == null && searchSortConfiguration != null){
           sortOrder = searchSortConfiguration.getDefaultSortOrder().toString();
       }

       if (sortOrder == null || sortOrder.equalsIgnoreCase("DESC"))
       {
           queryArgs.setSortField(sortBy, DiscoverQuery.SORT_ORDER.desc);
       }
       else
       {
           queryArgs.setSortField(sortBy, DiscoverQuery.SORT_ORDER.asc);
       }


       String groupBy = ObjectModelHelper.getRequest(objectModel).getParameter("group_by");


       // Enable groupBy collapsing if designated
       if (groupBy != null && !groupBy.equalsIgnoreCase("none")) {
           /** Construct a Collapse Field Query */
           queryArgs.addProperty("collapse.field", groupBy);
           queryArgs.addProperty("collapse.threshold", "1");
           queryArgs.addProperty("collapse.includeCollapsedDocs.fl", "handle");
           queryArgs.addProperty("collapse.facet", "before");

           //queryArgs.a  type:Article^2

           // TODO: This is a hack to get Publications (Articles) to always be at the top of Groups.
           // TODO: I think that can be more transparently done in the solr solrconfig.xml with DISMAX and boosting
           /** sort in groups to get publications to top */
           queryArgs.setSortField("dc.type", DiscoverQuery.SORT_ORDER.asc);

       }

       queryArgs.setQuery("dc.relation.ispartof: \""+node.getFieldValue()+"\"");

       if (page > 1)
       {
           queryArgs.setStart((page - 1) * queryArgs.getMaxResults());
       }
       else
       {
           queryArgs.setStart(0);
       }

       if(discoveryConfiguration.getHitHighlightingConfiguration() != null)
       {
           java.util.List<DiscoveryHitHighlightFieldConfiguration> metadataFields = discoveryConfiguration.getHitHighlightingConfiguration().getMetadataFields();
           for (DiscoveryHitHighlightFieldConfiguration fieldConfiguration : metadataFields)
           {
               queryArgs.addHitHighlightingField(new DiscoverHitHighlightingField(fieldConfiguration.getField(), fieldConfiguration.getMaxSize(), fieldConfiguration.getSnippets()));
           }
       }

       queryArgs.setSpellCheck(discoveryConfiguration.isSpellCheckEnabled());

       this.queryResults = SearchUtils.getSearchService().search(context, scope, queryArgs);
   }

}
