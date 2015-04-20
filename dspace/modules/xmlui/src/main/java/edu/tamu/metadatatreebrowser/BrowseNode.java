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

//	    private static final Message T_search_label =
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

//        pageMeta.addTrail().addContent(T_trail);
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
     * Attach a division to the given search division named "search-results"
     * which contains results for this search query.
     *
     * @param search The search division to contain the search-results division.
     */
    /*
    protected void buildSearchResultsDivision(Division search)
            throws IOException, SQLException, WingException, SearchServiceException {

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

        Division results = search.addDivision("search-results", "primary");
        buildSearchControls(results);


        DSpaceObject searchScope = getScope();

        int displayedResults;
        long totalResults;
        float searchTime;

        if(queryResults != null && 0 < queryResults.getTotalSearchResults())
        {
            displayedResults = queryResults.getDspaceObjects().size();
            totalResults = queryResults.getTotalSearchResults();
            searchTime = ((float) queryResults.getSearchTime() / 1000) % 60;

            if (searchScope instanceof Community)
            {
                Community community = (Community) searchScope;
                String communityName = community.getMetadata("name");
                results.setHead(T_head1_community.parameterize(displayedResults, totalResults, communityName, searchTime));
            } else if (searchScope instanceof Collection){
                Collection collection = (Collection) searchScope;
                String collectionName = collection.getMetadata("name");
                results.setHead(T_head1_collection.parameterize(displayedResults, totalResults, collectionName, searchTime));
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

            List<DSpaceObject> commCollList = new ArrayList<DSpaceObject>();
            List<Item> itemList = new ArrayList<Item>();
            for (DSpaceObject resultDso : queryResults.getDspaceObjects())
            {
                if(resultDso.getType() == Constants.COMMUNITY || resultDso.getType() == Constants.COLLECTION)
                {
                    commCollList.add(resultDso);
                }else
                if(resultDso.getType() == Constants.ITEM)
                {
                    itemList.add((Item) resultDso);
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

                        renderCollection((Collection) dso, highlightedResults, collectionMetadata);
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
                for (Item resultDso : itemList)
                {
                    DiscoverResult.DSpaceObjectHighlightResult highlightedResults = queryResults.getHighlightedResults(resultDso);
                    renderItem(itemWingList, resultDso, highlightedResults);
                }
            }

        } else {
            results.addPara(T_no_results);
        }
        //}// Empty query
    }
*/    
     

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
       //}// Empty query
       
       
       
       /*
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
       */
   }
       
    
    protected void addFilterRow(java.util.List<DiscoverySearchFilter> filterFields, int index, Row row, String selectedFilterType, String relationalOperator, String value) throws WingException {
        Select select = row.addCell("", Cell.ROLE_DATA, "selection").addSelect("filtertype_" + index);

        //For each field found (at least one) add options
        for (DiscoverySearchFilter searchFilter : filterFields)
        {
            select.addOption(StringUtils.equals(searchFilter.getIndexFieldName(), selectedFilterType), searchFilter.getIndexFieldName(), message("xmlui.ArtifactBrowser.SimpleSearch.filter." + searchFilter.getIndexFieldName()));
        }
        Select typeSelect = row.addCell("", Cell.ROLE_DATA, "selection").addSelect("filter_relational_operator_" + index);
        typeSelect.addOption(StringUtils.equals(relationalOperator, "contains"), "contains", T_filter_contain);
        typeSelect.addOption(StringUtils.equals(relationalOperator, "equals"), "equals", T_filter_equals);
        typeSelect.addOption(StringUtils.equals(relationalOperator, "authority"), "authority", T_filter_authority);
        typeSelect.addOption(StringUtils.equals(relationalOperator, "notcontains"), "notcontains", T_filter_notcontain);
        typeSelect.addOption(StringUtils.equals(relationalOperator, "notequals"), "notequals", T_filter_notequals);
        typeSelect.addOption(StringUtils.equals(relationalOperator, "notauthority"), "notauthority", T_filter_notauthority);
         



        //Add a box so we can search for our value
        row.addCell("", Cell.ROLE_DATA, "discovery-filter-input-cell").addText("filter_" + index, "discovery-filter-input").setValue(value == null ? "" : value);

        //And last add an add button
        Cell buttonsCell = row.addCell("filter-controls_" + index, Cell.ROLE_DATA, "filter-controls");
        buttonsCell.addButton("add-filter_" + index, "filter-control filter-add").setValue(T_filter_controls_add);
        buttonsCell.addButton("remove-filter_" + index, "filter-control filter-remove").setValue(T_filter_controls_remove);

    }    
 
    @Override
    protected String getBasicUrl() throws SQLException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        return request.getContextPath() + (dso == null ? "" : "/handle/" + dso.getHandle()) + "/discover";
    }

    protected Map<String, String[]> getParameterFilterQueries(){
        return DiscoveryUIUtils.getParameterFilterQueries(ObjectModelHelper.getRequest(objectModel));

    }
    /**
     * Returns all the filter queries for use by discovery
     *  This method returns more expanded filter queries then the getParameterFilterQueries
     * @return an array containing the filter queries
     */
    protected String[] getFilterQueries() {
        return DiscoveryUIUtils.getFilterQueries(ObjectModelHelper.getRequest(objectModel), context);
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
   protected String generateURL(Map<String, String> parameters)
   throws UIException {
   	Request request = ObjectModelHelper.getRequest(objectModel);

   	parameters.put("node",request.getParameter("node"));

   	if (parameters.get("page") == null)
   		parameters.put("page", request.getParameter("page"));

   		return AbstractDSpaceTransformer.generateURL("mdbrowse", parameters);

//   	return super.generateURL("mdbrowse", parameters);
   }

  
    /**
     * Since the layout is creating separate forms for each search part
     * this method will add hidden fields containing the values from other form parts
     *
     * @param type the type of our form
     * @param request the request
     * @param fqs the filter queries
     * @param division the division that requires the hidden fields
     * @throws WingException will never occur
     */
    private void addHiddenFormFields(String type, Request request, Map<String, String[]> fqs, Division division) throws WingException {
        if(type.equals("filter") || type.equals("sort")){
            if(request.getParameter("query") != null){
                division.addHidden("query").setValue(request.getParameter("query"));
            }
            if(request.getParameter("scope") != null){
                division.addHidden("scope").setValue(request.getParameter("scope"));
            }
        }

        //Add the filter queries, current search settings so these remain saved when performing a new search !
        if(type.equals("search") || type.equals("sort"))
        {
            for (String parameter : fqs.keySet())
            {
                String[] values = fqs.get(parameter);
                for (String value : values) {
                    division.addHidden(parameter).setValue(value);
                }
            }
        }

        if(type.equals("search") || type.equals("filter")){
            if(request.getParameter("rpp") != null){
                division.addHidden("rpp").setValue(request.getParameter("rpp"));
            }
            if(request.getParameter("sort_by") != null){
                division.addHidden("sort_by").setValue(request.getParameter("sort_by"));
            }
            if(request.getParameter("order") != null){
                division.addHidden("order").setValue(request.getParameter("order"));
            }
        }
    }

    protected String getSuggestUrl(String newQuery) throws UIException {
        Map parameters = new HashMap();
        parameters.put("query", newQuery);
        return addFilterQueriesToUrl(generateURL(parameters));
    }
}



/*
package edu.tamu.metadatatreebrowser;

import org.dspace.app.xmlui.aspect.discovery.AbstractSearch;

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

**
 * Display entire metadata browse tree in one nested list, typically for the community or collection homepage.s
 * 
 * @author Scott Phillips, http://www.scottphillips.com
 *

public class BrowseNode extends AbstractSearch implements CacheableProcessingComponent  {

	
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");

    public static final Message T_untitled = 
    	message("xmlui.general.untitled");

	** Member variables, these are created on setup, and then cleared out on recycle. The rest of the code uses these variables. **
	private DSpaceObject scope;
	private MetadataTreeNode node;
	private QueryResults queryResults;
	

    **
     * Add the community's title and trail links to the page's metadata
     *
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
    	pageMeta.addMetadata("title").addContent(node.getName());
    	
        // Add the trail back to the repository root.
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        HandleUtil.buildHandleTrail(scope, pageMeta,contextPath);
    }
    
    
	**
	 * Display an overview of the browse tree for community or collection
	 * homepage.
	 *
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
    
    **
     * 
     * Attach a division to the given search division named "search-results"
     * which contains results for this search query.
     * 
     * @param search
     *            The search division to contain the search-results division.
     *
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
    
    
    
    **
     * Generate a url to the simple search url.
     *
    protected String generateURL(Map<String, String> parameters)
    throws UIException {
    	Request request = ObjectModelHelper.getRequest(objectModel);

    	parameters.put("node",request.getParameter("node"));

    	if (parameters.get("page") == null)
    		parameters.put("page", request.getParameter("page"));


    	return super.generateURL("mdbrowse", parameters);
    }
    
    **
     * Get the search query from the URL parameter, if none is found the empty
     * string is returned.
     *
    protected String getQuery() throws UIException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String query = decodeFromURL(request.getParameter("node"));
        if (query == null)
        {
            return "";
        }
        return query.trim();
    }    
    
}
*/