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
import org.apache.commons.lang.StringUtils;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
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
	
		private MetadataTreeNode node;

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


        // Build the DRI Body
        Division search = body.addDivision("search", "primary");
        search.setHead(T_head);
        String searchUrl = ConfigurationManager.getProperty("dspace.url") + "/JSON/discovery/search";

        search.addHidden("discovery-json-search-url").setValue(searchUrl);

        if(currentScope != null){
            search.addHidden("discovery-json-scope").setValue(currentScope.getHandle());
        }
        search.addHidden("contextpath").setValue(contextPath);

        Map<String, String[]> fqs = getParameterFilterQueries();

        Division searchBoxDivision = search.addDivision("discovery-search-box", "discoverySearchBox");

        Division mainSearchDiv = searchBoxDivision.addInteractiveDivision("general-query",
                "discover", Division.METHOD_GET, "discover-search-box");

        org.dspace.app.xmlui.wing.element.List searchList = mainSearchDiv.addList("primary-search",  org.dspace.app.xmlui.wing.element.List.TYPE_FORM);

//        searchList.setHead(T_search_label);
        if (variableScope())
        {
            Select scope = searchList.addItem().addSelect("scope");
            scope.setLabel(T_search_scope);
            buildScopeList(scope);
        }

        org.dspace.app.xmlui.wing.element.Item searchBoxItem = searchList.addItem();
        Text text = searchBoxItem.addText("query");
        text.setValue(queryString);
        searchBoxItem.addButton("submit", "search-icon").setValue(T_go);
        if(queryResults != null && StringUtils.isNotBlank(queryResults.getSpellCheckQuery()))
        {
        	org.dspace.app.xmlui.wing.element.Item didYouMeanItem = searchList.addItem("did-you-mean", "didYouMean");
            didYouMeanItem.addContent(T_did_you_mean);
            didYouMeanItem.addXref(getSuggestUrl(queryResults.getSpellCheckQuery()), queryResults.getSpellCheckQuery(), "didYouMean");
        }

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        DiscoveryConfiguration discoveryConfiguration = SearchUtils.getDiscoveryConfiguration(dso);
        java.util.List<DiscoverySearchFilter> filterFields = discoveryConfiguration.getSearchFilters();
        java.util.List<String> filterTypes = DiscoveryUIUtils.getRepeatableParameters(request, "filtertype");
        java.util.List<String> filterOperators = DiscoveryUIUtils.getRepeatableParameters(request, "filter_relational_operator");
        java.util.List<String> filterValues = DiscoveryUIUtils.getRepeatableParameters(request,  "filter");

        if(0 < filterFields.size() && filterTypes.size() == 0)
        {
            //Display the add filters url ONLY if we have no filters selected & filters can be added
            searchList.addItem().addXref("display-filters", T_filters_show);
        }
        addHiddenFormFields("search", request, fqs, mainSearchDiv);


        if(0 < filterFields.size())
        {
            Division searchFiltersDiv = searchBoxDivision.addInteractiveDivision("search-filters",
                    "discover", Division.METHOD_GET, "discover-filters-box " + (0 < filterTypes.size() ? "" : "hidden"));

            Division filtersWrapper = searchFiltersDiv.addDivision("discovery-filters-wrapper");
            filtersWrapper.setHead(T_filter_label);
            filtersWrapper.addPara(T_filter_help);
            Table filtersTable = filtersWrapper.addTable("discovery-filters", 1, 4, "discovery-filters");


            //If we have any filters, show them
            if(filterTypes.size() > 0)
            {

                filtersTable.addRow(Row.ROLE_HEADER).addCell("", Cell.ROLE_HEADER, 1, 4, "new-filter-header").addContent(T_filter_current_filters);
                for (int i = 0; i <  filterTypes.size(); i++)
                {
                    String filterType = filterTypes.get(i);
                    String filterValue = filterValues.get(i);
                    String filterOperator = filterOperators.get(i);

                    if(StringUtils.isNotBlank(filterValue))
                    {
                        Row row = filtersTable.addRow("used-filters-" + i+1, Row.ROLE_DATA, "search-filter used-filter");
                        addFilterRow(filterFields, i+1, row, filterType, filterOperator, filterValue);
                    }
                }
                filtersTable.addRow("filler-row", Row.ROLE_DATA, "search-filter filler").addCell(1, 4).addContent("");
                filtersTable.addRow(Row.ROLE_HEADER).addCell("", Cell.ROLE_HEADER, 1, 4, "new-filter-header").addContent(T_filter_new_filters);
            }


            int index = filterTypes.size() + 1;
            Row row = filtersTable.addRow("filter-new-" + index, Row.ROLE_DATA, "search-filter");

            addFilterRow(filterFields, index, row, null, null, null);

            Row filterControlsItem = filtersTable.addRow("filter-controls", Row.ROLE_DATA, "apply-filter");
//            filterControlsItem.addCell(1, 3).addContent("");
            filterControlsItem.addCell(1, 4).addButton("submit_apply_filter", "discovery-apply-filter-button").setValue(T_filter_controls_apply);

            addHiddenFormFields("filter", request, fqs, searchFiltersDiv);

        }


//        query.addPara(null, "button-list").addButton("submit").setValue(T_go);

        // Build the DRI Body
        //Division results = body.addDivision("results", "primary");
        //results.setHead(T_head);
        buildMainForm(search);

        // Add the result division
        try {
            buildSearchResultsDivision(search);
        } catch (SearchServiceException e) {
            throw new UIException(e.getMessage(), e);
        }

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
     * Generate a url to the simple search url.
     *
    protected String generateURL(Map<String, String> parameters)
            throws UIException {
        String query = getQuery();
        if (!"".equals(query) && parameters.get("query") == null)
        {
            parameters.put("query", encodeForURL(query));
        }

        if (parameters.get("page") == null)
        {
            parameters.put("page", String.valueOf(getParameterPage()));
        }

        if (parameters.get("rpp") == null)
        {
            parameters.put("rpp", String.valueOf(getParameterRpp()));
        }


        if (parameters.get("group_by") == null)
        {
            parameters.put("group_by", String.valueOf(this.getParameterGroup()));
        }

        if (parameters.get("sort_by") == null && getParameterSortBy() != null)
        {
            parameters.put("sort_by", String.valueOf(getParameterSortBy()));
        }

        if (parameters.get("order") == null && getParameterOrder() != null)
        {
            parameters.put("order", getParameterOrder());
        }

        if (parameters.get("etal") == null)
        {
            parameters.put("etal", String.valueOf(getParameterEtAl()));
        }
        if(parameters.get("scope") == null && getParameterScope() != null)
        {
            parameters.put("scope", getParameterScope());
        }

        return AbstractDSpaceTransformer.generateURL("discover", parameters);
    }
*/
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