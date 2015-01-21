/*
 * FilterSearch.java
 */

package org.tdl.dspace.filtersearch;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.dspace.app.xmlui.aspect.artifactbrowser.AbstractSearch;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;

/**
 * The Texas Digital Library filter search. This extension to the AbstractSearch
 * mechanism enables 'filter' searches to be performed. A filter search is
 * basically any search that can be expressed as "or clauses" "anded" together.
 * 
 * Thus something like this: (index1:one) AND ((index2:two) OR (index2:three))
 * 
 * The filtersearch is configured by the "filtersearch.xconf" configuration file
 * located in your dspace config directory. This configuration determines what 
 * filters will be used for the search. See the example configuration file for
 * documentation on how to format the config.
 * 
 * This transfromer is designed to be installed on both the community/collection
 * handles and on the filtersearch url. One installation is for the container view 
 * and the other is for the results display. An example is shown below:
 * 
 * <map:match pattern="handle/*//*">
 *   <map:transform type="FilterSearch"/>
 * </map:match>
 *			
 *
 * <map:match pattern="handle/*//*/filter-search">
 *   <map:transform type="FilterSearch">
 *     <map:parameter name="results" value="true"/>
 *   </map:transform>
 * </map:match>
 * 
 * FIXME: This class should support i18n translations.
 * 
 * @author Scott Phillips
 */

public class FilterSearch extends AbstractSearch implements Configurable
{
	/** Where the configuration file is located inside the dspace.dir */
	private static final String CONFIG_PATH = File.separatorChar + "config" + File.separatorChar + "filtersearch.xconf";
	
    /** The giant static list of configured filters. */
    private static Map<String,Search> searches = new HashMap<String,Search>();
    
    /** The filtersearch that is currently being run */
    private Search currentSearch;
    
    /**
     * Configure this filtersearch, see the class documentation for a
     * description of the configuration options.
     */
    public void configure(Configuration conf) throws ConfigurationException
    {
    	String configPath = ConfigurationManager.getProperty("dspace.dir") + CONFIG_PATH;
    	File configFile = new File(configPath);
    	if (configFile == null || !configFile.exists() || !configFile.canRead())
    		throw new ConfigurationException("Unable to access configuration file: "+configPath);
    	
    	try {
	        SAXBuilder builder = new SAXBuilder(true);
	        Document config = builder.build(configFile);
	        Element root = config.getRootElement();
	        
	        for(Element searchElement : (java.util.List<Element>) root.getChildren("search"))
	        {
	        	// Get the search filter attributes
	        	String handles = searchElement.getAttributeValue("handles");
	        	String head   = searchElement.getAttributeValue("head");
	        
	        	Search search = new Search(head);
	        	
	        	// Install this search filter set for each handle.
	        	for(String handle : handles.split(","))
	        		searches.put(handle.trim(), search);

	        	for(Element filterElement : (java.util.List<Element>) searchElement.getChildren("filter"))
		        {	        		
					String type = filterElement.getAttributeValue("type");
					String label = filterElement.getAttributeValue("label");
					String index = filterElement.getAttributeValue("index");
					String multipleString = filterElement.getAttributeValue("multiple");
					String sizeString = filterElement.getAttributeValue("size");
					
					boolean multiple = false;
					if (multipleString != null && multipleString.length() > 0)
						multiple = Boolean.valueOf(multipleString);
					
					int size = 1;
					if (sizeString != null && sizeString.length() > 0)
						size = Integer.valueOf(sizeString);
					
					// Create and install the filter
					Filter filter = new Filter(type,label,index,multiple,size);
					search.addFilter(filter);
					
					for(Element itemElement : (java.util.List<Element>) filterElement.getChildren("item"))
			        {	
						String value = itemElement.getAttributeValue("value");
						String year = itemElement.getAttributeValue("year");
						String defaultString = itemElement.getAttributeValue("default");
						String itemLabel = itemElement.getText();
						
						boolean defaultSelect = false;
						if (defaultString != null && defaultString.length() > 0)
							defaultSelect = Boolean.valueOf(defaultString);
						
						// Create and install the item
						Item item = new Item(itemLabel,value,year,defaultSelect);
						filter.addItem(item);
			        } // for itemElement
		        } // for filterElement
	        } // for searchElement
    	} 
    	catch (IOException ioe) 
    	{
    		throw new ConfigurationException("Unable to read configuration file: "+configPath,ioe);
    	} 
    	catch (JDOMException jdome) 
    	{
			throw new ConfigurationException("Unable to parse configuration file: "+configPath,jdome);
		}
    }

    
    /**
     * Determine which of the possible search sets should apply to this page. If one is found set the
     * currentSearch parameter to it, otherwise leave it null.
     */
	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) 
	throws ProcessingException, SAXException, IOException
	{
		super.setup(resolver, objectModel, src, parameters);
		
		// Clear any previous value out.
		this.currentSearch = null;
		try {
	    	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
	    	this.currentSearch = searches.get(dso.getHandle());
		} 
		catch (SQLException sqle)
		{
			// Just ignore the error and don't do anything on the page.
			this.currentSearch = null;
		}
	}
    
    /**
     * Page metadata
     * 
     * If this configured to show results, then add a page title, and trail. Otherwise 
     * let the community or collection viewer set those values.
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
    	if (this.currentSearch == null)
    		// No filter search applies to this page.
    		return;
    	
        if (parameters.getParameterAsBoolean("results", false))
        {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
            if (dso instanceof Community)
            {
                // Set up the major variables
                Community community = (Community) dso;
                // Set the page title
                pageMeta.addMetadata("title").addContent(community.getMetadata("name"));

                // Add the trail back to the repository root.
                pageMeta.addTrailLink(contextPath + "/", message("xmlui.general.dspace_home"));
                HandleUtil.buildHandleTrail(community, pageMeta,contextPath);
            }
            else if (dso instanceof Collection)
            {
                // Set up the major variables
                Collection collection = (Collection) dso;
                // Set the page title
                pageMeta.addMetadata("title").addContent(collection.getMetadata("name"));

                // Add the trail back to the repository root.
                pageMeta.addTrailLink(contextPath + "/", message("xmlui.general.dspace_home"));
                HandleUtil.buildHandleTrail(collection, pageMeta,contextPath);
            }
            else
            {
                // FIXME: I don't know what to do if it's not a community or
                // collection.
                pageMeta.addMetadata("title").addContent("Filter Search");
                
                pageMeta.addTrailLink(contextPath + "/", message("xmlui.general.dspace_home"));
                
            }
            
            pageMeta.addTrail().addContent("Filter Search");
        }
        
    }

    /** What to add at the end of the body */
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
       	if (this.currentSearch == null)
    		// No filter search applies to this page.
    		return;
    	
        Request request = ObjectModelHelper.getRequest(objectModel);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
    	
    	
        Division current = null;
        if (parameters.getParameterAsBoolean("results",false))
        {
        	// This is the results page so we are free to use our own divisions. But we have 
        	// to add our own headers.
        	current = body.addDivision("filter-search", "primary filter-search-results");
            if (dso instanceof Community)
            {
                Community community = (Community) dso;
                
                current.setHead("Filter search in " + community.getMetadata("name"));
            }
            else if (dso instanceof Collection)
            {
                Collection collection = (Collection) dso;
                
                current.setHead("Filter search in " + collection.getMetadata("name"));
            }
            else
            {
                current.setHead("Filter search");
            }
        }
        else
        {
        	// This is either the community or collection page, so add our 
        	// filter search parameters into the page.
            if (dso instanceof Community)
            {
            	current = body.addDivision("community-home","primary repository community");
            	current = current.addDivision("community-search-browse", "secondary search-browse");
            	current = current.addDivision("community-filter-search", "secondary filter-search");
            }
            else if (dso instanceof Collection)
            {
            	current = body.addDivision("collection-home","primary repository collection");
            	current = current.addDivision("collection-search-browse", "secondary search-browse");
            	current = current.addDivision("collection-filter-search", "secondary filter-search");	
            }
            else
            {
            	//FIXME: I don't know what to do if it's not a community or collection
            	current = body.addDivision("filter-search","primary repository");
            }
        }
        
       

        Division query = current.addInteractiveDivision("filter-search",contextPath + "/handle/"+dso.getHandle()+"/filter-search",Division.METHOD_POST, "secondary search filter-search");
        List list = query.addList("filter-search");
        if (currentSearch.head != null && currentSearch.head.length() > 0)
        	list.setHead(currentSearch.head);

        for (Filter filter : this.currentSearch.filters)
        {
            list.addLabel(filter.label);

            String[] values = request.getParameterValues(filter.index);

            if ("text".equals(filter.type))
            {
                Text text = list.addItem().addText(filter.index);
                if (!(values == null || values.length == 0))
                    text.setValue(values[0]);
            }
            else if ("select".equals(filter.type))
            {
                Select select = list.addItem().addSelect(filter.index);
                select.setMultiple(filter.multiple);
                select.setSize(filter.size);

                for (Item item : filter.items)
                {
                	if (item.year == null || item.year.length() == 0)
                	{
                		// None ranged item
	                    boolean selected = item.defaultSelect;
	                   
	                    if (values != null )
	                    {
	                        for (String value : values)
	                        {
	                        	if (value.equals(item.value))
	                                selected = true;
	                        }
	                    }
	                    select.addOption(selected, item.value).addContent(item.label);
                	}
                	else
                	{
                		//Year ranged item.
                		String[] parts =item.year.split("-");
                		
                		if (parts.length == 2)
                		{
	                		String startString = parts[0];
	                		String endString = parts[1];
	                		
	                		int start = Calendar.getInstance().get(Calendar.YEAR);
	                		int end = Calendar.getInstance().get(Calendar.YEAR);	
	                		
	                		if (!"present".equals(startString))
	                			start = Integer.valueOf(startString);
	                		if (!"present".equals(endString))
	                			end = Integer.valueOf(endString);
	                		
	                		for (int i = start; ((start < end) ? i <= end : i >= end);)
	                		{
	                			boolean selected = false;
	                			if (values != null )
	    	                    {
	    	                        for (String value : values)
	    	                        {
	    	                        	if (value.equals(item.value))
	    	                                selected = true;
	    	                        }
	    	                    }
	    	                    select.addOption(selected, ""+i).addContent(""+i);
	    	                    
	    	                    // Go up or down depending on start and end dates
	    	                    if (start < end)
	    	                    	i++;
	    	                    else
	    	                    	i--;
	                		}
	                		
	                		
                		}
                		 
                	}
                }
            }
        }

        query.addPara(null, "button-list").addButton("submit").setValue(
                message("xmlui.general.search"));

        // Add the result division only if they selected search
        buildSearchResultsDivision(current);

    }
    
    
    /**
     * Return the query string for this filterSearch based upon the user
     * selected filters.
     * 
     * The query string will be a "and" of "or"s.
     */
    protected String getQuery()
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String andQuery = "";
        boolean andFirst = true;
        for (Filter filter : this.currentSearch.filters)
        {
            String[] values = request.getParameterValues(filter.index);

            // If there are none this short curcit
            if (values == null || values.length == 0)
                continue;

            String subQuery = "";
            if ("text".equals(filter.type)) {
                // Text fields build a subquery with ANDed keywords. So we 
                //split the string up into words and AND each of them together.
                
                String[] words = values[0].split(" ");
                
                boolean subFirst = true;
                for (String word : words) 
                { 
                    //skip any empty elements
                    if (word == null || word.length() == 0)
                        continue;
                    
                    if (subFirst)
                    {
                        subFirst = false;
                    }
                    else
                    {
                        subQuery += " AND ";
                    }
                    if ("ANY".equals(filter.index)) 
                        subQuery += "(\""+word+"\")";
                    else 
                        subQuery += "(" + filter.index + ":\"" + word + "\")";
                    
                }
                
            } else if ("select".equals(filter.type)) {
                // Select fields produce a subquery with ORed values.
                // Build an OR list of possible values for this index.
                boolean subFirst = true;
                for (String value : values)
                {
                    // If it's the empty value then they didn't select anything for
                    // this filter.
                    if (value == null || value.length() == 0)
                        continue;

                    if (subFirst)
                    {
                        subFirst = false;
                    }
                    else
                    {
                        subQuery += " OR ";
                    }
                   
                    if ("ANY".equals(filter.index)) 
                        subQuery += "(\""+value+"\")";
                    else 
                        subQuery += "(" + filter.index + ":\"" + value + "\")";
                }
                
            }

            // If no orQuery was built then it dosn't need to be put into the
            // final query.
            if (subQuery == null || subQuery.length() == 0)
                continue;

            // Add the OR query to the and query;
            if (andFirst)
            {
                andFirst = false;
            }
            else
            {
                andQuery += " AND ";
            }
            andQuery += "(" + subQuery + ")";
        }

        return andQuery;
    }

    /**
     * Generate the url string for this page which includes all the filter
     * parameters.
     * 
     * @param parameters
     *            Any extra parameters to add to the URL.
     */
    protected String generateURL(Map<String, String> parameters)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        // Relay all the query parameters
        boolean triped = false;
        for (Filter filter : this.currentSearch.filters)
        {
            String[] values = request.getParameterValues(filter.index);

            // Pass on all the current values for the filter.
            int i=0;
            for (String value : values)
            {
        	String index = filter.index;
        	i++;
        	if (parameters.containsKey(filter.index))
        		index = filter.index + '?' + i; 
                parameters.put(index, value);
                triped = true;
            }
        }

        // Mimic the submit button.
        if (triped)
            parameters.put("submit", "go");

        try
        {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

            String baseURL = contextPath + "/handle/" + dso.getHandle() + "/filter-search";
            boolean first = true;
            for (String key : parameters.keySet())
            {
                if (first)
                {
                    baseURL += "?";
                    first = false;
                }
                else
                {
                    baseURL += "&";
                }
                
                String keyname = key;
                if (key.indexOf('?') > -1)
                {
                    key = key.substring(0, key.indexOf('?'));                    
                }

                baseURL += key + "=" + parameters.get(keyname);
            }

            return baseURL;
        }
        catch (SQLException sqle)
        {
            // FIXME: I don't know what to do with this very unlikely error.
            return "";
        }
    }



    /**
     * A private class repsenenting a set of filters
     */
    private class Search
    {	
    	/** The form label to use on this search */
    	protected String head;
    	
    	/** List of filters installed on this search */
    	protected java.util.List<Filter> filters = new ArrayList<Filter>();
    	
    	
    	/**
    	 * Search set constructor
    	 * @param head The form label for this search set.
    	 */
    	public Search(String head)
    	{
    		this.head = head;
    	}
    	
    	/** 
    	 * Add a new filter to the search set
    	 * @param filter The new filter
    	 */
    	public void addFilter(Filter filter)
    	{
    		filters.add(filter);
    	}
    }
    
    
    /**
     * A private filter class that represents a configured filter.
     */
    private class Filter
    {
        /** The filter type (either select or text) */
        protected String type;

        /** The filter's label, what's shown on the screen */
        protected String label;

        /** The internal DSpace index to search for this filter */
        protected String index;

        /** (select only) Weather multiple values are selectable by the user */
        protected boolean multiple;

        /** (select only) How many options to show on the screen */
        protected int size;

        /** (select only) All the possible items that may be selected */
        protected java.util.List<Item> items = new ArrayList<Item>();

        /**
         * Create a new filter.
         * 
         * @param type
         *            The filter's type (select or text)
         * @param label
         *            The filters label to describe it to the user.
         * @param index
         *            The internal DSpace index to search.
         * @param multiple
         *            If multiple values are possible.
         * @param size
         *            How many of those multiple values to show on the screen.
         */
        public Filter(String type, String label, String index,
                boolean multiple, int size)
        {
            this.type = type;
            this.label = label;
            this.index = index;
            this.multiple = multiple;
            this.size = size;
        }

        /**
         * (select only) Add a possible item to the select list.
         * 
         * @param item
         */
        public void addItem(Item item)
        {
            items.add(item);
        }

    }

    /**
     * Private class to represent an possible item of the select filter
     */
    private class Item
    {
    	/** A year range to represent many items */
    	protected String year;
    	
        /** The label shown to the user. */
        protected String label;

        /** The internal value used for the search */
        protected String value;

        /** Dose this item default to being selected */
        protected boolean defaultSelect;

        /**
         * Construct a new item.
         * 
         * @param label
         *            The label shown to the user.
         * @param value
         *            The value used in the search
         * @param year
         * 			  A possible year range.
         * @param defaultSelect
         *            If the item should default to being selected.
         */
        public Item(String label, String value, String year, boolean defaultSelect)
        {
            this.label = label;
            this.value = value;
            this.year = year;
            this.defaultSelect = defaultSelect;
        }
    }

}
