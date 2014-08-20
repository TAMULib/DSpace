/*
 * Copyright Texas Digital Library
 */

package org.tdl.dspace.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.DCValue;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.search.DSIndexer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


import edu.harvard.hul.ois.mets.Checksumtype;
import edu.harvard.hul.ois.mets.FLocat;
import edu.harvard.hul.ois.mets.FileGrp;
import edu.harvard.hul.ois.mets.FileSec;
import edu.harvard.hul.ois.mets.Loctype;
import edu.harvard.hul.ois.mets.Mets;
import edu.harvard.hul.ois.mets.helper.MetsException;
import edu.harvard.hul.ois.mets.helper.MetsReader;
import edu.harvard.hul.ois.mets.helper.MetsValidator;
import edu.harvard.hul.ois.mets.helper.MetsWriter;


/** 
 * MetadataFilter is the class that invokes the metadata filters over 
 * the repositories entire conents. 
 * 
 * This class was derived from the MediaFilter class present in the standard 
 * DSpace code base.
 * 
 *  
 *  @author Adam Mikeal
 *  @author Scott Phillips
 */
public class MetadataFilter
{
	
	private static Logger log = Logger.getLogger(MetadataFilter.class);

    public static boolean createIndex = true; // default to creating index

    public static boolean isVerbose = false; // default to not verbose

    public static boolean isForce = false; // default to not forced

    private static MetadataFilterInterface[] metadataFilters;
    
    private static boolean isNewMetadata = false;
    
    
    /**
     * Comand line interface.
     */
    public static void main(String[] argv) throws Exception
    {
        // set headless for non-gui workstations
        System.setProperty("java.awt.headless", "true");

        // create an options object and populate it
        CommandLineParser parser = new PosixParser();

        Options options = new Options();

        options.addOption("c", "collection", true, 
        		"The collection to be filtered (REQUIRED)");
        options.addOption("v", "verbose", false,
                "print all extracted text and other details to STDOUT");
        options.addOption("n", "noindex", false,
                "do NOT re-create search index after filtering bitstreams");
        options.addOption("h", "help", false, "help");
        
        options.addOption("m", "metadataSchema", false, "Expect new style metadta schema (i.e. dc.creator, etd.degree.* fields used instead of old school ones)");

        
        
        CommandLine line = parser.parse(options, argv);

        if (line.hasOption('h') || !line.hasOption('c'))
        {
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.printHelp("MetadataFilter\n", options);

            System.exit(0);
        }
        
        if (line.hasOption('v'))
        {
            isVerbose = true;
        }

        if (line.hasOption('n'))
        {
            createIndex = false;
        }
        
        if (line.hasOption('m'))
        {
        	isNewMetadata = true;
       	}
        
        // TODO: using a temporary method to access the list of filters
        // until the PluginManager is incorporated into the main trunk
        
        //MetadataFilters = (MetadataFilter[])  PluginMananger.getPluginSequence(MetadataFilter.class);

        metadataFilters = new MetadataFilterInterface[2];
        metadataFilters[0] = new METSFilter();
        metadataFilters[1] = new MODSFilter();
        
        for (MetadataFilterInterface filter : metadataFilters)
            System.out.println("Metadata Filter loaded: " + filter.getClass().getName());
        
        
        Context c = null;

        try
        {
            c = new Context();

            // Determine the collection
            String handle = line.getOptionValue('c');
            DSpaceObject dso = HandleManager.resolveToObject(c, handle);
            if ( dso == null || !(dso instanceof Collection))
            	throw new Exception("Handle "+handle+" either does not exist or is not a collection.");
            
            // have to be super-user to do the filtering
            c.setIgnoreAuthorization(true);

            // now apply the filters
            applyFilters(c,(Collection) dso);

            // create search index?
            if (createIndex)
            {
                System.out.println("Creating search index:");
                DSIndexer.createIndex(c);
            }

            c.complete();
            c = null;
        }
        finally
        {
            if (c != null)
            {
                c.abort();
            }
        }
    }

    
    /**
     * Iterate over all items in the repository and apply each filter to them.
     * 
     * @param c The current DSpace context.
     */
    public static void applyFilters(Context c,Collection collection) throws Exception
    {
        //ItemIterator i = Item.findAll(c);
        ItemIterator i = collection.getItems();
    	
        log.info("Applying MetadataFilters on all items (" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()) + ")");        
        while (i.hasNext())
        {
            Item myItem = i.next();
            
            System.out.println("Inside item: " + myItem.getHandle());
            for (MetadataFilterInterface filter : metadataFilters)
                    filter.processFilter(c, myItem);
            c.commit();
        }
    }
    
    
    /**
     * This interface provides the structure for the individual metadata filters
     */
    private interface MetadataFilterInterface
    {
    	/**
    	 * Performs the main action of the particular filter
    	 * 
    	 * @param c
    	 * @param item
    	 */
    	public void processFilter(Context c, Item item)
    		throws IOException, SQLException, AuthorizeException;
    	
    }
    
    
    /**
     * Filter DSpace items into a METS document for structural metadata about bitstreams & bundles.
     * 
     * @author Adam Mikeal
     */
    private static class METSFilter implements MetadataFilterInterface
    {
        
        /* (non-Javadoc)
         * @see edu.tamu.dspace.metadatafilter.MetadataFilter#processFilter(org.dspace.core.Context, org.dspace.content.Item)
         */
        public void processFilter(Context c, Item item) throws IOException,
            SQLException, AuthorizeException
    {
        try
        {
            // Create a vars to hold the metadata bundle and bitstream (for later)
            Bundle md_bundle = null;
            Bitstream md_bitstream = null;
            
            // Create a new Mets object
            Mets mets = new Mets();   
            
            if (MetadataFilter.isVerbose)
                System.out.println("Creating new METS object...");
            
            // Create a fileSec
            FileSec fileSec = new FileSec();
            
            // Loop over all the bundles in this item
            for (Bundle bundle : item.getBundles())
            {
                // Skip the thumbnail filegroup
                if (bundle.getName().equals("THUMBNAIL"))
                    continue;
                
                // Save the metadata filegroup (and skip it, too)
                if (bundle.getName().equals("METADATA"))
                {
                    if (MetadataFilter.isVerbose)
                        System.out.println("  Found METADATA bundle, recording for later use");
                    
                    md_bundle = bundle;
                    continue;
                }
                
                // Create a fileGrp
                FileGrp fileGrp = new FileGrp();
    
                // Bundle name for USE attribute
                if ((bundle.getName() != null) && !bundle.getName().equals(""))
                    fileGrp.setUSE(bundle.getName());
                
                // Loop over all the bitstreams in this bundle
                for (Bitstream bitstream : bundle.getBitstreams())
                {
                    // Create the METS File element for this bitstream
                    edu.harvard.hul.ois.mets.File file = new edu.harvard.hul.ois.mets.File();
                    
                    // Make the handle valid for xsd:ID
                    String xmlIDstart = item.getHandle().replaceAll("/", "_") + "_";
                    
                    // Make a groupID for the File element
                    String groupID = "GROUP_" + xmlIDstart + bitstream.getSequenceID();
                    
                    // Make the bitstream name safe
                    String encodedName = Util.encodeBitstreamName(bitstream.getName(), "UTF-8");
                    
                    // Set properties on the File element
                    file.setID("remote_"+xmlIDstart + bitstream.getSequenceID());
                    file.setGROUPID(groupID);
                    file.setOWNERID(encodedName);
                    
                    file.setMIMETYPE(bitstream.getFormat().getMIMEType());
                    file.setSIZE(bitstream.getSize());
                    file.setCHECKSUM(bitstream.getChecksum());
                    file.setCHECKSUMTYPE(Checksumtype.MD5);
    
                    // FLocat: the url to the original file
                    String url = ConfigurationManager.getProperty("dspace.url")
	                    + "/bitstream/"
	                    + item.getHandle() + "/"
	                    + bitstream.getSequenceID() + "/"
	                    + encodedName;
                    FLocat flocat = new FLocat();
                    flocat.setLOCTYPE(Loctype.URL);
                    flocat.setXlinkHref(url);
    
                    // Add FLocat to File, and File to FileGrp
                    file.getContent().add(flocat);
                    fileGrp.getContent().add(file);
                    
                    // Output
                    if (MetadataFilter.isVerbose)
                        System.out.println("  Added bitstream (as file element) to the METS object");
                }
    
                // Add fileGrp to fileSec (but only is there is at least one bitstream)
                if (bundle.getBitstreams().length > 0)
                    fileSec.getContent().add(fileGrp);
                
            }
    
            // Add fileSec to document
            mets.getContent().add(fileSec);
            
            
            // Make sure that the md_bundle we want really exists
            if (md_bundle == null)
            {
                if (MetadataFilter.isVerbose)
                    System.out.println("  METADATA bundle doesn't exist, creating it");
                
                // Create a new bundle on this item
                md_bundle = item.createBundle("METADATA");
            }
            // otherwise, look for the METS.xml bitstream in the found bundle
            else
            {
                for (Bitstream bitstream : md_bundle.getBitstreams())
                {
                    if (bitstream.getName().equals("METS.xml"))
                    {
                        if (MetadataFilter.isVerbose)
                            System.out.println("  Found 'METS.xml' bitstream, comparing file contents");
                        
                        // If the METS.xml bitstream already exists, compare it to the one we built
                        if (MetsEquivalent(mets, Mets.reader(new MetsReader(bitstream.retrieve()))))
                        {
                            if (MetadataFilter.isVerbose)
                                System.out.println("  Existing 'METS.xml' equivalent; quitting");
                            
                            return;
                        }
                        else
                        {
                            if (MetadataFilter.isVerbose)
                                System.out.println("  Existing 'METS.xml' out of date; replacing with new copy");
                            
                            // The two METS objects are different, so remove it so we can write it back
                            md_bundle.removeBitstream(bitstream);
                        }
                        
                        continue;
                    }
                }
            }
            
            // Validate the METS before proceeding
            mets.validate(new MetsValidator());
            
            if (MetadataFilter.isVerbose)
                System.out.println("  Validated METS object; writing to new bitstream");
                        
            // Set up the output stream to move the METS object into the DSpace bitstream
            ByteArrayOutputStream os = new ByteArrayOutputStream(); 
                       
            // Write out the METS object to the output stream
            mets.write(new MetsWriter(os));
            
            // Read the captured METS object to an input stream
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray()); 
            
            // Create the new bitstream in the metadata bundle
            md_bitstream = md_bundle.createBitstream(is);
            
            // Set properties for new bitstream
            md_bitstream.setName("METS.xml");
            md_bitstream.setUserFormatDescription("DSpace METS file");
            md_bitstream.setFormat(BitstreamFormat.find(c, 4));
            md_bitstream.setSource(this.getClass().getName());
            md_bitstream.setDescription("METS representation for this asset (all bitstreams and their relationships");
            md_bitstream.update();

            if (MetadataFilter.isVerbose)
                System.out.println("  Bitstream created, name set to 'METS.xml'");
            
            // Set this new bitstream to be the primary for this bundle
            md_bundle.setPrimaryBitstreamID(md_bitstream.getID());
            
            if (MetadataFilter.isVerbose)
                System.out.println("  Setting as primary bitstream for the 'METADATA' bundle");
            
            // Clean up
            os.close();
            is.close();
            
        }
        catch (MetsException e)
        {
            // We don't pass up a MetsException, so callers don't need to
            // know the details of the METS toolkit
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
        
    }
 



   public boolean MetsEquivalent(Mets found, Mets generated) throws MetsException
   {
      ByteArrayOutputStream f_os = new ByteArrayOutputStream();
      found.write(new MetsWriter(f_os));

      ByteArrayOutputStream g_os = new ByteArrayOutputStream();
      generated.write(new MetsWriter(g_os));

      if (f_os.toString().equals(g_os.toString()))
        return true;
      else
        return false;
   }






    }

    
    
    /**
     * Filter DSpace items into a descriptive MODS record. The new metadata record is stored 
     * back to the item as MODS.xml inside the metadata bundle.
     * 
     * @author Adam Mikeal
     */
    private static class MODSFilter implements MetadataFilterInterface
    {    

        // The genre used by the MODS "Genre" element
        String genre = "theses";
        
        // The MARC agency for the "Record Information" section of the MODS record
        String MARCAgency = "TxCM";
        
        // Create the namespaces for use in the MODS creation
        Namespace modsNamespace = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
        Namespace etdNamespace = Namespace.getNamespace("etd", "http://www.ndltd.org/standards/metadata/etdms/1.0/");
        
        // Holder for the creation date for an existing MODS file (used in the re-creation process)
        String mods_creation_date = null;
        

        /* (non-Javadoc)
         * @see edu.tamu.dspace.metadatafilter.MetadataFilter#processFilter(org.dspace.core.Context, org.dspace.content.Item)
         */
        public void processFilter(Context c, Item item) throws IOException,
                SQLException, AuthorizeException
        {
            /*
                 + create a new data structure with all the significant meta ( getETDdata() )
                 + open the item and check for an existing MODS bitstream; read in record information
                 + compare the existing MODS bitstream to the data structure
                 + if equivilant, do nothing
                 + if different, create a new MODS bitstream to replace existing ( createDOM() )
            */
            
            if (MetadataFilter.isVerbose)
                System.out.println("Starting process of MODS creation (loading significant metadata)");
                    
            // Create a new data structure with the significant meatdata
            MetadataMap map = getETDdata(item);
            
            // Open the 'METADATA' bitstream bundle (or make it if necessary)
            Bundle md_bundle = item.getBundles("METADATA")[0];
            if (md_bundle == null)
            {
                if (MetadataFilter.isVerbose)
                    System.out.println("  No 'METADATA' bundle found; creating it");
                
                // Create a new bundle on this item
                md_bundle = item.createBundle("METADATA");
            }
            else
            {
                if (MetadataFilter.isVerbose)
                    System.out.println("  'METADATA' bundle found; storing for later use");
            }
            
            // Check for an existing MODS document
            Bitstream mods_bitstream = md_bundle.getBitstreamByName("MODS.xml");
            if (mods_bitstream != null)
            {
                if (MetadataFilter.isVerbose)
                    System.out.println("  Found 'MODS.xml' bitstream, comparing file contents");
                
                // If the MODS.xml bitstream already exists, compare it to the datastructure we built
                if (ModsEquivalent(mods_bitstream, map))
                {
                    if (MetadataFilter.isVerbose)
                        System.out.println("  Existing 'MODS.xml' equivalent; quitting");
                    
                    return;
                }
                else
                {
                    if (MetadataFilter.isVerbose)
                        System.out.println("  Existing 'MODS.xml' out of date; replacing with new copy");
                    
                    // The two MODS objects are different, so remove the existing one so we can write it back
                    md_bundle.removeBitstream(mods_bitstream);
                }
            }
            else
            {
                if (MetadataFilter.isVerbose)
                    System.out.println("  No 'MODS.xml' bitstream in 'METADATA' bundle; creating it");
            }
            
            // Create the DOM from the data in 'map'
            Document mods_dom = createDOM(map);
            
            // Set up the output stream to move the MODS object into the DSpace bitstream
            ByteArrayOutputStream os = new ByteArrayOutputStream(); 
                       
            // Write out the MODS object to the output stream
            XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
            output.output(mods_dom, os);
            
            // Read the captured MODS object to an input stream
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray()); 
                    
            // Create the new bitstream in the metadata bundle
            mods_bitstream = md_bundle.createBitstream(is);
            
            // Set properties for new bitstream
            mods_bitstream.setName("MODS.xml");
            mods_bitstream.setUserFormatDescription("TDL-ETD MODS file");
            mods_bitstream.setFormat(BitstreamFormat.find(c, 4));
            mods_bitstream.setSource(this.getClass().getName());
            mods_bitstream.setDescription("MODS metadata for this ETD");
            mods_bitstream.update();

            if (MetadataFilter.isVerbose)
                System.out.println("  Bitstream created, name set to 'MODS.xml' (DSpace ID " + mods_bitstream.getID() + ")");
            
            
        }
         
        
        /**
         * Load all the significant ETD metadata fields into our custom data structure.
         * 
         * @param item
         * @return
         */
        public MetadataMap getETDdata(Item item)
        {
            // Create the custom HashMap to be returned
            MetadataMap map = new MetadataMap();
            
            // Grab the title info
            map.put("title", parseTitle(getDCValue(item, "title", null)));
            
            // Author's name
            if (isNewMetadata)
            	map.put("author", parseName(getDCValue(item, "creator", null)));
            else
				map.put("author", parseName(getDCValue(item, "contributor", "author")));	
		            
            // Advisor's name(s)
            if (isNewMetadata)
            {
	            if (item.getMetadata("etd","contributor", "committeeChair", Item.ANY).length == 0)
	                map.put("committee_chair", parseName("Unknown"));
	            else
	                for (DCValue dc : item.getMetadata("etd","contributor", "committeeChair", Item.ANY))
	                    map.put("committee_chair", parseName(dc.value));
	            
	            // Committee member's name(s)
	            for (DCValue dc : item.getMetadata("etd","contributor", "committeeMember", Item.ANY))
	                map.put("committee_member", parseName(dc.value));
            }
            else
            {
	            if (item.getDC("contributor", "committeeChair", Item.ANY).length == 0)
	                map.put("committee_chair", parseName("Unknown"));
	            else
	                for (DCValue dc : item.getDC("contributor", "committeeChair", Item.ANY))
	                    map.put("committee_chair", parseName(dc.value));
	            
	            // Committee member's name(s)
	            for (DCValue dc : item.getDC("contributor", "committeeMember", Item.ANY))
	                map.put("committee_member", parseName(dc.value));
            }
            
            // Add the degree information
            if (isNewMetadata)
            {
	            map.put("degree_name", new Metadata(convertToFullDegree(getDCValue(item, "thesis", "degree", "name"))));
	            map.put("degree_level", new Metadata(getDCValue(item, "thesis", "degree", "level")));
	            map.put("degree_grantor", new Metadata(getDCValue(item, "thesis", "degree", "grantor")));
	            map.put("degree_discipline", new Metadata(getDCValue(item, "thesis", "degree", "discipline")));
            }
            else
            {
	            map.put("degree_name", new Metadata(convertToFullDegree(getDCValue(item, "degree", "name"))));
	            map.put("degree_level", new Metadata(getDCValue(item, "degree", "level")));
	            map.put("degree_grantor", new Metadata(getDCValue(item, "degree", "grantor")));
	            map.put("degree_discipline", new Metadata(getDCValue(item, "degree", "discipline")));
            }
            
            // Add the origin info        
            if (isNewMetadata)
            	map.put("date_submitted", new Metadata(parseDate(getDCValue(item, "date", "created"))));
            else
            	map.put("date_submitted", new Metadata(parseDate(getDCValue(item, "date", "submitted"))));
            map.put("date_issued", new Metadata(getDCValue(item, "date", "issued")));
            
            // Add the language part
            map.put("language", new Metadata(convertToPartTwo(getDCValue(item, "language", "iso"))));
            
            // Physical description items
            map.put("mime_type", new Metadata(getDCValue(item, "format", "mimetype")));
            
            // Add the abstract
            map.put("abstract", new Metadata(getDCValue(item, "description", "abstract")));
            
            // Add the subject terms
            for (DCValue dc : item.getDC("subject", null, Item.ANY))
                map.put("subject", new Metadata(dc.value));
            
            // Add the identifier (handle)
            map.put("identifier", new Metadata(getDCValue(item, "identifier", "uri")));
            
            // Add the location element (the URL)
            map.put("location", new Metadata(getDCValue(item, "identifier", "uri")));
            
            // return the custom hash map
            return map;
        }
        
        
        /**
         * Safely return a string to represent the particular DC metadata value
         * 
         * The function will also add an item to the DSpace log if a DC value was
         * requested and the function returned "Unknown".
         * 
         * @param item
         * @param element
         * @param qualifier
         */
        public String getDCValue(Item item, String element, String qualifier)
        {
            return getDCValue(item,"dc",element,qualifier);
        }
        
        
        /**
         * Safely return a string to represent the particular DC metadata value
         * 
         * The function will also add an item to the DSpace log if a DC value was
         * requested and the function returned "Unknown".
         * 
         * @param item
         * @param schema
         * @param element
         * @param qualifier
         */
        public String getDCValue(Item item, String schema, String element, String qualifier)
        {
            if (item.getMetadata(schema, element, qualifier, Item.ANY).length < 1)
            {
                log.info("No value found in DC "+schema+"." + element + "." + qualifier + 
                         " for item " + item.getHandle() + "; returned 'Unknown'");
                if (MetadataFilter.isVerbose)
                    System.out.println("  *** Found blank "+schema+"." + element + "." + qualifier + 
                                       " for item " + item.getHandle() + " ***");
                return "Unknown";
            }
            else
            {
                return item.getMetadata(schema, element, qualifier, Item.ANY)[0].value;
            }
        }
        
        /**
         * Compare an existing MODS bitstream with the metadata data structure for equivilency
         * @param bs
         * @param map
         * @return
         */
        public boolean ModsEquivalent(Bitstream bs, MetadataMap map)
        {    
            Document doc;
            Element rIdentifier = null;
            
            try {
                doc = new SAXBuilder().build(bs.retrieve());
            }
            catch (Exception e) {
                System.out.println("  Failed to load MODS from bitstream into DOM: " + e.getMessage());
                return false;
            }
            
            try {
                rIdentifier = (Element) XPath.selectSingleNode(doc, "//mods:mods/mods:recordInfo/mods:recordIdentifier");
            }
            catch (JDOMException e) {
                System.out.println("  Failed to locate the recordIdentifier element with the XPath expression: " + e.getMessage());
                return false;
            }
            
            if (rIdentifier == null)
            {
                System.out.println("  Unable to resolve XPath query to recordIdentifier element");
                return false;
            }
            
            String mapHash = hashMeta(map);
            
            if (!mapHash.equals(rIdentifier.getText()))
            {
                return false;
            }
            else
            {
                this.mods_creation_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
                return true;
            }
            
        }
        
        
        /**
         * Create a DOM that contsains all metadata inside the passed-in MetadataMap structure
         * 
         * @param meta
         * @return
         */
        public Document createDOM(MetadataMap meta)
        {   
            // Create the document with a root mods element
            Element mods = new Element("mods", modsNamespace); 
            Document mods_doc = new Document(mods);
            
            
            // Add the title section
            Element titleInfo = new Element("titleInfo", modsNamespace);
                Element title = new Element("title", modsNamespace);
                    Metadata tm = meta.get("title");
                    String ts = tm.getTitle();
                    title.setText(ts);
                titleInfo.addContent(title);
                
                // Only add the subtitle if there is one
                if (meta.get("title").getSubTitle() != null)
                {
                    Element subTitle = new Element("subTitle", modsNamespace);
                        subTitle.setText(meta.get("title").getSubTitle());
                    titleInfo.addContent(subTitle);
                }
            mods.addContent(titleInfo);
            
            
            // Add the Author's name
            Element author = createModsName("personal", null, createModsRole("Author", "marcrelator"));
                author.addContent(createNamePart("given", meta.get("author").getGivenName()));
                author.addContent(createNamePart("family", meta.get("author").getFamilyName()));
                if (meta.get("author").getDatePart() != null)
                    author.addContent(createNamePart("date", meta.get("author").getDatePart()));
            mods.addContent(author);       
            
                    
            // Add the advisor's name(s)
            for (Metadata md : meta.getMultiple("committee_chair"))
            {
                Element advisor = createModsName("personal", null, createModsRole("Thesis advisor", "marcrelator"));
                    advisor.addContent(createNamePart("given", md.getGivenName()));
                    advisor.addContent(createNamePart("family", md.getFamilyName()));
                    if (md.datePart != null)
                        advisor.addContent(createNamePart("date", md.getDatePart()));
                mods.addContent(advisor);  
            }
            

            // Add the committee member's name(s)
            for (Metadata md : meta.getMultiple("committee_member"))
            {
                Element advisor = createModsName("personal", null, createModsRole("Committee member", "marcrelator"));
                    advisor.addContent(createNamePart("given", md.getGivenName()));
                    advisor.addContent(createNamePart("family", md.getFamilyName()));
                    if (md.datePart != null)
                        advisor.addContent(createNamePart("date", md.getDatePart()));
                mods.addContent(advisor);  
            }        
            
            
            // Add the degree grantor information
            Element grantor = createModsName("corporate", "lcnaf", createModsRole("Degree Grantor", "marcrelator"));
                grantor.addContent(createNamePart(null, meta.get("degree_grantor").getValue()));
            mods.addContent(grantor);
            
            
            //  Add the resource type (static for now)
            Element typeOfResource = new Element("typeOfResource", modsNamespace);
                typeOfResource.setText("text");
            mods.addContent(typeOfResource);
            
            
            // Add the genre
            Element genre = new Element("genre", modsNamespace);
                genre.setAttribute("authority", "marcgt");
                genre.setText(this.genre);
            mods.addContent(genre);
            
            
            // Add the origin info
            Element originInfo = new Element("originInfo", modsNamespace);
                Element dateCreated = new Element("dateCreated", modsNamespace);
                    dateCreated.setAttribute("encoding", "iso8601");
                    dateCreated.setText(meta.get("date_submitted").getValue());
                Element dateIssued = new Element("dateIssued", modsNamespace);
                    dateIssued.setAttribute("encoding", "iso8601");
                    dateIssued.setText(meta.get("date_issued").getValue());
                originInfo.addContent(dateCreated);
                originInfo.addContent(dateIssued);
            mods.addContent(originInfo);
            
            
            // Add the language part
            Element language = new Element("language", modsNamespace);
                Element languageTerm = new Element("languageTerm", modsNamespace);
                    languageTerm.setAttribute("type", "code");
                    languageTerm.setAttribute("authority", "iso639-2b");
                    languageTerm.setText(meta.get("language").getValue());
                language.addContent(languageTerm);
            mods.addContent(language);
            
            
            // Add the physical description section
            Element physicalDescription = new Element("physicalDescription", modsNamespace);
                Element form = new Element("form", modsNamespace);
                    form.setAttribute("authority", "marcform");
                    form.setText("electronic");
                Element type = new Element("internetMediaType", modsNamespace);
                    type.setText(meta.get("mime_type").getValue());
                Element origin = new Element("digitalOrigin", modsNamespace);
                    origin.setText("born digital");
                physicalDescription.addContent(form);
                physicalDescription.addContent(type);
                physicalDescription.addContent(origin);
            mods.addContent(physicalDescription);

            
            // Add the abstract
            Element abstractElmt = new Element("abstract", modsNamespace);
                abstractElmt.setAttribute("lang", meta.get("language").getValue());
                abstractElmt.setText(meta.get("abstract").getValue());
            mods.addContent(abstractElmt);
            
            
            // Add the subject terms
            Element subject = new Element("subject", modsNamespace);
            for (Metadata md : meta.getMultiple("subject"))
            {        
                Element topic = new Element("topic", modsNamespace);
                    topic.setText(md.getValue());
                subject.addContent(topic);
            }
            mods.addContent(subject);
            
            
            // Add the identifier (handle)
            Element identifier = new Element("identifier", modsNamespace);
                identifier.setAttribute("type", "hdl");
                identifier.setText(meta.get("identifier").getValue());
            mods.addContent(identifier);
            
            
            // Add the location element
            Element location = new Element("location", modsNamespace);
                Element url = new Element("url", modsNamespace);
                    url.setText(meta.get("identifier").getValue());
                location.addContent(url);
            mods.addContent(location);
                            
            
            // Add the degree information
            Element degreeExt = new Element("extension", modsNamespace);
                Element degree = new Element("degree", etdNamespace);
                    Element degName = new Element("name", etdNamespace);
                        degName.setText(meta.get("degree_name").getValue());
                    Element degLevel = new Element("level", etdNamespace);
                        degLevel.setText(meta.get("degree_level").getValue());
                    Element degDiscipline = new Element("discipline", etdNamespace);
                        degDiscipline.setText(meta.get("degree_discipline").getValue());
                degree.addContent(degName);
                degree.addContent(degLevel);
                degree.addContent(degDiscipline);
            degreeExt.addContent(degree);
            mods.addContent(degreeExt);
            
            
            // Add the record information
            Element recordInfo = new Element("recordInfo", modsNamespace);
                Element rContentSource = new Element("recordContentSource", modsNamespace);
                    rContentSource.setAttribute("authority", "marcorg");
                    rContentSource.setText(this.MARCAgency);
                Element rCreationDate = new Element("recordCreationDate", modsNamespace);
                    rCreationDate.setAttribute("encoding", "iso8601");
                    if (this.mods_creation_date != null)
                        rCreationDate.setText(this.mods_creation_date);
                    else
                        rCreationDate.setText(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
                Element rChangeDate = new Element("recordChangeDate", modsNamespace);
                    rChangeDate.setAttribute("encoding", "iso8601");
                    rChangeDate.setText(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
                Element rIdentifier = new Element("recordIdentifier", modsNamespace);
                    rIdentifier.setText(hashMeta(meta));
                recordInfo.addContent(rContentSource);
                recordInfo.addContent(rCreationDate);
                recordInfo.addContent(rChangeDate);
                recordInfo.addContent(rIdentifier);
            mods.addContent(recordInfo);
            
            
            // Return the created DOM
            return mods_doc;              
        }
        
        
        /**
         * Convert an ISO part 1 language code (two-letter) into an ISO part 2 (three-letter) code
         * 
         * If the isoPartOne argument is 5 characters in length, only the first two are used
         * 
         * @param isoPartOne
         * @param isoPartTwo
         * @return
         */
        public String convertToPartTwo(String isoPartOne)
        {
            String isoPartTwo = "";
            
            if (isoPartOne.length() == 5)
                isoPartOne = isoPartOne.substring(0, 2);
            
            if (isoPartOne.equals("en") || isoPartOne.equals("EN"))
                isoPartTwo = "eng";
            
            // More cases can be added to this egregious hack as necessary
            
            return isoPartTwo;
        }
        
        
        /**
         * Convert a degree abbreviation into a full degree name
         * 
         * @param abbr
         * @return 
         */
        public String convertToFullDegree(String abbr)
        {
            String fullName = "Unknown";
            
            if (abbr.matches("^M[\\s\\.]*S[\\s\\.]*$"))
            {
                fullName = "Master of Science";
            }
            else if (abbr.matches("^M[\\s\\.]*A[\\s\\.]*$"))
            {
                fullName = "Master of Arts";
            }
            else if (abbr.matches("^Ph[\\s\\.]*D[\\s\\.]*$"))
            {
                fullName = "Doctor of Philosophy";
            }
            
            return fullName;
        }
        
        
        /**
         * Parse a title from DSpace and split it into two parts on the colon (if present)
         * 
         * This function will return an array with one or two elements:
         *  0 -> The main part of the title (always present)
         *  1 -> The optional subtitle (whatever follows the first colon)
         * 
         * @param origTitle
         * @return
         */
        public Metadata parseTitle(String origTitle)
        {
            String title = "Unknown";
            String subTitle = null;
            
            int colon;
            
            // Find the first colon in the title
            colon = origTitle.indexOf(":");
            
            // either there is a subtitle or not
            if (colon != -1)
            {
                // There is a subtitle; add the first part
                title = origTitle.substring(0, colon);
                
                // Now add the subtitle as the second element
                subTitle = origTitle.substring(colon+1);
            }
            else
            {
                // No subtitle
                title = origTitle;
            }
            
            return new Metadata(title, subTitle);
        }
        
        
        /**
         * Parse a full name string for its component parts
         * 
         * This function will return an array with 2 or 3 string parts, organized as follows:
         *  0 -> last name
         *  1 -> rest of name + any suffix
         *  2 -> date, if present
         *  
         * Index 2 is an optional part, and will only be included if the date was present in the passed argument.
         * 
         * @param fullName
         * @return
         */
        public Metadata parseName(String fullName)
        {
            String familyName = null;
            String givenName = null;
            String datePart = null;
            
            String part;
            int comma;
            
            // Find the first comma (delimits the last name)
            comma = fullName.indexOf(",");
            
            // If there is no comma (like "Unknown") just return the original string
            if (comma == -1)
                return new Metadata(fullName, null, null);
            
            // Grab the family name part
            familyName = fullName.substring(0, comma);
            
            // Get the rest of the string to work with
            part = fullName.substring(comma+1);
            
            // Test for more commas (complex name)
            comma = part.lastIndexOf(",");
            
            if (comma == -1)
            {
                // No more commas, what's left is the givenName
                givenName = part;
            }
            else
            {
                // We found a date; add it after the rest of the name
                if (part.substring(comma+1).matches("^\\s*\\d{4}-(\\d{4})?$"))
                {
                    givenName = part.substring(0, comma);
                    datePart = part.substring(comma+1);
                }
                // Comma but no date; we don't care about the suffix (just add it)
                else
                {
                    givenName = part;
                }                
            }
            
            return new Metadata(familyName, givenName, datePart);
        }
        
        
        /**
         * Parse a "Month Year" date string into a ISO-compatable date
         * 
         * @param origDate
         * @return
         */
        public String parseDate(String origDate)
        {
            String parsedDate = "Unknown";
            String formatType = "Month, YEAR";
            
            // Look for a space (indicates whether a month is involved)
            int space = origDate.indexOf(" ");
            
            try 
            {
                if (space == -1)
                {
                    formatType = "YEAR only";
                    SimpleDateFormat df = new SimpleDateFormat("yyyy");
                    Date dt = df.parse(origDate);
                    parsedDate = new SimpleDateFormat("yyyy").format(dt);                
                }
                else
                {
                    SimpleDateFormat df = new SimpleDateFormat("MMM yyyy");
                    Date dt = df.parse(origDate);
                    parsedDate = new SimpleDateFormat("yyyy-MM").format(dt);
                }
            }
            catch (Exception e) 
            {
               System.out.println("Unable to parse date into '" + formatType  + "' format: " + e.getMessage()); 
            }
            
            return parsedDate;
        }
        
        
        /**
         * Create and return a MODS role element (used to build the name parts)
         * 
         * @param roleName
         * @param authorityName
         * @return
         */
        public Element createModsRole(String roleName, String authorityName)
        {
            Element roleTerm = new Element("roleTerm", this.modsNamespace);
                roleTerm.setAttribute("authority", authorityName);
                roleTerm.setAttribute("type", "text");
                roleTerm.setText(roleName);        
            
            return roleTerm;
        }
        
        
        /**
         * Create and return a new MODS name element (used to build the name parts)
         * @param type
         * @param roleElement
         * @return
         */
        public Element createModsName(String type, String authority, Element roleElement)
        {
            Element name = new Element("name", this.modsNamespace);
                name.setAttribute("type", type);
                if (authority != null)
                    name.setAttribute("authority", authority);
                name.addContent(roleElement);
                
            return name;
        }
        
        
        /**
         * Create and return a MODS namePart element
         * @param type
         * @param value
         * @return
         */
        public Element createNamePart(String type, String value)
        {
            Element namePart = new Element("namePart", this.modsNamespace);
                if (type != null)  
                    namePart.setAttribute("type", type);
                namePart.setText(value);
            
            return namePart;
        }

        
        /**
         * Create a data structure to hold the metadata values for the simple case,
         * as well as the complex cases (parsed titles and names).
         * 
         * @author adam
         */
        private class Metadata
        {       
            private static final int SIMPLE = 0;
            private static final int NAME   = 1;
            private static final int TITLE  = 2;
           
            private int type;
            
            private String familyName;
            private String givenName;
            private String datePart;
            
            private String title;
            private String subTitle;
            
            private String value;
            
            // Metadata as a name
            public Metadata(String familyName, String givenName, String datePart) 
            {
                this.type = NAME;
                this.familyName = familyName;
                this.givenName = givenName;
                this.datePart = datePart;
            }
            
            // Metadata as a title
            public Metadata(String title, String subTitle) 
            {
                this.type = TITLE;
                this.title = title;
                this.subTitle = subTitle;
            }
            
            // Metadata as everything else
            public Metadata(String value) 
            {
                this.type = SIMPLE;
                this.value = value;
            }
            
            public boolean isName()   { return type == NAME;   }
            public boolean isTitle()  { return type == TITLE;  }
            public boolean isSimple() { return type == SIMPLE; }
            
            public String getFamilyName() {
                return familyName;
            }
            
            public String getGivenName() {
                return givenName;
            }
            
            public String getDatePart() {
                return datePart;
            }
            
            public String getTitle() {
                return title;
            }
            
            public String getSubTitle() {
                return subTitle;
            }
            
            public String getValue() {
                return value;
            }
            
            public String toString()
            { 
                return type + familyName + givenName + datePart + title + subTitle + value;
            }
            
        }
      
        
        /**
         * Extend the Java HashMap to handle the ETD metadata (some values strings, some arrays)
         * 
         * @author adam
         */
        private class MetadataMap extends HashMap<String, ArrayList<Metadata>>
        {
            // To make serializable happy:
            private static final long serialVersionUID = 1;
            
            public void put(String key, Metadata metadata)
            {
                ArrayList<Metadata> metadataList;
                if (super.containsKey(key))
                    metadataList = super.get(key);
                else
                {
                    metadataList = new ArrayList<Metadata>();
                    super.put(key, metadataList);
                }
                
                metadataList.add(metadata);
            }
            
            public boolean isMultiple(String key)
            {
                ArrayList<Metadata> metadataList = super.get(key);
                
                if (metadataList != null && metadataList.size() > 1)
                    return true;
                else 
                    return false;
            }
            
            public Metadata get(String key) 
            {
                ArrayList<Metadata> metadataList = super.get(key);
                if (metadataList != null && metadataList.size() > 0)
                    return metadataList.get(0);
                return null;
            }
            
            public ArrayList<Metadata> getMultiple(String key) 
            {
                if (super.containsKey(key))
                    return super.get(key);
                else
                    return new ArrayList<Metadata>();
            }
            
            public String toString()
            {
                String strReturn = "";
                
                for (String key : this.keySet())
                {
                    strReturn += key;
                    for (Metadata vals : super.get(key))
                        strReturn += vals.toString();
                }
                
                return strReturn;
            }
            
        }   
        
        
        /**
         * Return the object has as a string
         * @param meta
         * @return
         */
        public String hashMeta(MetadataMap meta)
        {
            String hash = "";
            
            try
            {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] md5 = md.digest(meta.toString().getBytes());
                hash = md.toString();
                //hash = Base64.encode(md5).trim();
                
                //if (MediaFilterManager.isVerbose)
                //    System.out.println("  Generated hash for MetadataMap object: " + hash);
            }
            catch (Exception e)
            {
                System.out.println("  Unable to hash (MD5) MetadataMap object: " + e.getMessage());
            }
            
            return hash;
        }

    }
    
    
}
