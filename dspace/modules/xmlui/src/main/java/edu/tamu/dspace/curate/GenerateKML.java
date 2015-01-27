package edu.tamu.dspace.curate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Metadatum;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

public class GenerateKML extends AbstractCurationTask
{
	/** log4j category */
    private static Logger log = Logger.getLogger(ConfigurationManager.class);
    
    /** used for output of curation task results */
    private static StringBuilder sb = new StringBuilder();
    
    private ArrayList<Location> locations = new ArrayList<Location>();
    
    String kml = "";
    ByteArrayInputStream itemKMLInputStream;

    @Override 
    public void init(Curator curator, String taskId) throws IOException
    {
        super.init(curator, taskId);        
    }
    

    /**
     * Perform the curation task upon passed DSO
     *
     * @param dso the DSpace object
     * @throws IOException
     */
	@SuppressWarnings("deprecation")
	@Override
	public int perform(DSpaceObject dso) throws IOException
	{
		locations.clear();
		sb.replace(0, sb.length(), "");

		if (dso.getType() == Constants.ITEM)
        {
			
            Item item = (Item)dso;
            int errorCount = 0;
            
            log.info("Generating KML for place references in item " + item.getHandle());
            sb.append("Generating KML for place references in item " + item.getHandle());
            
			//read local.suggested.DCCoverageSpatial fields
            Metadatum[] spatialMetadata = item.getMetadata("local", "suggested", "DCCoverageSpatial", Item.ANY);
            
            
			//read title in order to label the point
            Metadatum[] titleMetadata = item.getMetadata("dc", "title", null, Item.ANY);
            String title = "Untitled";
            if(titleMetadata.length > 0)
            {
            	title = titleMetadata[0].value;
            }
            
            //read all the other necessary metadata fields: Author, Advisor, Year, Department, Url
            HashMap<String, ArrayList<String>> itemMetadata = new HashMap<String, ArrayList<String>>();
            
            Metadatum[] authorNamesDC = item.getMetadata("dc", "creator", null, Item.ANY);
            for(Metadatum value : authorNamesDC)
            {
            	if(!itemMetadata.containsKey("Author"))
            	{
            		itemMetadata.put("Author", new ArrayList<String>());
            	}
            	
            	itemMetadata.get("Author").add(value.value);
            	            	
            }
        	
            Metadatum[] advisorNamesDC = item.getMetadata("dc", "contributor", "advisor", Item.ANY);
            for(Metadatum value : advisorNamesDC)
            {
            	if(!itemMetadata.containsKey("Advisor"))
            	{
            		itemMetadata.put("Advisor", new ArrayList<String>());
            	}
            	
            	itemMetadata.get("Advisor").add(value.value);
            }
        	
            Metadatum[] yearsDC = item.getMetadata("dc", "date", "created", Item.ANY);
            for(Metadatum value : yearsDC)
            {
            	if(!itemMetadata.containsKey("Year"))
            	{
            		itemMetadata.put("Year", new ArrayList<String>());
            	}
            	
            	itemMetadata.get("Year").add(value.value);
            }
        	
            Metadatum[] departmentsDC = item.getMetadata("thesis", "degree", "department", Item.ANY);
            for(Metadatum value : departmentsDC)
            {
            	if(!itemMetadata.containsKey("Department"))
            	{
            		itemMetadata.put("Department", new ArrayList<String>());
            	}
            	
            	itemMetadata.get("Department").add(value.value);
            }
            
            Metadatum[] levelsDC = item.getMetadata("thesis", "degree", "level", Item.ANY);
            for(Metadatum value : levelsDC)
            {
            	if(!itemMetadata.containsKey("Level"))
            	{
            		itemMetadata.put("Level", new ArrayList<String>());
            	}
            	
            	itemMetadata.get("Level").add(value.value);
            }
        	
            Metadatum[] urlsDC = item.getMetadata("dc", "identifier", "uri", Item.ANY);
            for(Metadatum value : urlsDC)
            {
            	if(!itemMetadata.containsKey("Url"))
            	{
            		itemMetadata.put("Url", new ArrayList<String>());
            	}
            	
            	itemMetadata.get("Url").add(value.value);
            }
            
            
			//parse metadata
            String value = "";
            String latAndLong = "";
            for(Metadatum metadatum : spatialMetadata)
            {
            	Location location = new Location();
            	value = metadatum.value;
            	//First, split on the colon to get the textual description separated from the coordinates
            	String[] splitTextAndCoordinates = value.split(":");
            	location.setDesignation(splitTextAndCoordinates[0]);
            	latAndLong = splitTextAndCoordinates[1];
            	
            	//Next split the coordinates and convert them to numbers
            	String[] splitCoordinates = latAndLong.split(",");
            	location.setLatitude(Double.valueOf(splitCoordinates[0]));
            	location.setLongitude(Double.valueOf(splitCoordinates[1]));
            	
            	locations.add(location);
            }
			
			//build kml
            kml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			kml += "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n";
			kml += "\t<Document>\n\t\t<name>" + org.apache.commons.lang.StringEscapeUtils.escapeXml(title) + "</name>\n";
			
			/* Example of MetadataType schema - we should put one of these in there.
			   <!-- Declare the type "MetadataType" with 3 fields -->
			  <Schema name="MetadataType" id="MetadataId">     
			    <SimpleField type="string" name="Author">       
			      <displayName><![CDATA[<b>Author</b>]]></displayName>     
			    </SimpleField>     
			    <SimpleField type="int" name="Year">       
			      <displayName><![CDATA[<i>Year</i>]]></displayName>     
			    </SimpleField>     
			    <SimpleField type="string" name="College">       
			      <displayName><![CDATA[<i>College</i>]]></displayName>     
			    </SimpleField>   
			  </Schema> 
			*/
			
			kml += "\t\t<Folder>\n\t\t\t<name>" + org.apache.commons.lang.StringEscapeUtils.escapeXml(title) + " - Placemarks</name>\n";
            for(Location location : locations)
            {
            	
            	String placemarkNameCDATA = "<![CDATA[<a href=\"" + itemMetadata.get("Url").get(0) + "\" target=\"_blank\">"  
				            		 + org.apache.commons.lang.StringEscapeUtils.escapeXml(title)
				            		 + "</a>]]>";
            	
            	String placeName = org.apache.commons.lang.StringEscapeUtils.escapeXml(location.getDesignation());
            	
            	//loop through each type of metadata and add a SimpleData element for each occurrence
			    String extendedDataXMLSnippet = "\t\t\t\t\t\t<SimpleData name=\"Place\">" + placeName + "</SimpleData>\n";
            	String descriptionTableInteriorSnippet = "";
			    for(String key : itemMetadata.keySet())
			    {
			    	 for(String datum : itemMetadata.get(key))
			    	 {
			    		 extendedDataXMLSnippet += "\t\t\t\t\t\t<SimpleData name = \"" + key + "\">" + org.apache.commons.lang.StringEscapeUtils.escapeXml(datum) + "</SimpleData>\n";
			    		 if(!key.equals("Url"))
			    		 {
			    			 descriptionTableInteriorSnippet += "\t\t\t\t\t\t<tr><td>" + key + ":</td><td>" + org.apache.commons.lang.StringEscapeUtils.escapeXml(datum) + "</td></tr>\n";
			    		 }
			    	 }
			    }
            	
            	
            	//begin constructing the kml xml output for this location by opening a Placemark element tag.
            	kml += "\t\t\t<Placemark id=\"" + location.hashCode() + "\">\n"
    			
            		 //name is a link within CDATA
            	
				     + "\t\t\t\t<name>" + placemarkNameCDATA + "</name>\n"
				     
				     //description is an extended table within CDATA - here is the example from Maps team
				     /*
				      	<description><![CDATA[<table border="0" cellpadding="0" cellspacing="0" width="500" align="left">
							<tr><td>Mason Mountain Wildlife Management Area; Texas, Mason County (park)
							<br><br>Author: Harper, Rebecca Anne
							<br>Degree level: Masters
							<br>Year: 2011
							<br>Academic department: Geology and Geophysics
							</td></tr></table>]]>
						</description>
				      */
				     
				     + 	"\t\t\t\t<description><![CDATA[<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"500\" align=\"left\">" +
				     	
				     	"\t\t\t\t\t\t<tr><td id=\"descriptionTablePlaceName\">" + placeName + "</td></tr>\n" +  
				     	
				     	descriptionTableInteriorSnippet +
				     	
				     	
				     	 "\t\t\t\t\t</table>]]>\n" + 
				     	"\t\t\t\t</description>\n" +
				     
				     //After the description element comes the ExtendedData element wherein we find the structured metadata - here is the example from Maps team
				    /*
				    	<ExtendedData>
							<SchemaData schemaUrl="#MetadataId">
							  <SimpleData name="Place">Mason Mountain Wildlife Management Area; Texas, Mason County (park) </SimpleData>
							  <SimpleData name="Author">Harper, Rebecca Anne</SimpleData>
							  <SimpleData name="Year">2011</SimpleData>
							  <SimpleData name="Department">Geology and Geophysics</SimpleData>
							  <SimpleData name="Url">http://repository.tamu.edu/handle/1969.1/ETD-TAMU-2011-08-10181?show=full</SimpleData>
							</SchemaData>
						</ExtendedData>
				     */
				     	
				     "\t\t\t\t<ExtendedData>\n" +
				     "\t\t\t\t\t<SchemaData schemaURL=\"#MetadataId\">\n" +
				     
				     extendedDataXMLSnippet +
				     	
				     "\t\t\t\t\t</SchemaData>\n" +
				     "\t\t\t\t</ExtendedData>\n"
				     
				     
				     //Point is a tag containing the coordinates
				     
				     + location.getKMLPoint()+"\n"
				     
				     //finally, we close the Placemark element.
				     
				     + "\t\t\t</Placemark>\n"; 
            }
			kml += "\t\t</Folder>\n\t</Document>\n</kml>";
        
			// convert kml String to InputStream
			InputStream is = new ByteArrayInputStream(kml.getBytes());
			
			//get the current date to append to the file name and make it unique
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			Date date = new Date();
			String dateString = dateFormat.format(date);
			
			//write kmlInputStream to bitstream in METADATA bundle
			//TODO:  delete old KML file if present
			try 
			{
				item.createSingleBitstream(is, "METADATA").setName("referencedLocations-" + dateString + ".kml");
				item.update();
				sb.append("Successfully wrote referencedLocations-" + dateString + ".kml to METADATA bundle.");
			} 
			catch (AuthorizeException e) 
			{
				// Auto-generated catch block for attempting to add the kml bitstream to the METADATA bundle
				e.printStackTrace();
			} catch (SQLException e) 
			{
				// Auto-generated catch block for attempting to add the kml bitstream to the METADATA bundle
				e.printStackTrace();
			}
			
		    report(sb.toString());
            setResult(sb.toString());
            
            return (errorCount == 0) ? Curator.CURATE_SUCCESS : Curator.CURATE_FAIL;
        }
        else
        {
           setResult("Object skipped");
           return Curator.CURATE_SKIP;
        }
	}
	
	private class Location
	{
		private String designation;
		private Double latitude;
		private Double longitude;
		
		public String getDesignation() 
		{
			return designation;
		}
		public void setDesignation(String designation) 
		{
			this.designation = designation;
		}
		public Double getLatitude() 
		{
			return latitude;
		}
		public void setLatitude(Double latitude) 
		{
			this.latitude = latitude;
		}
		public Double getLongitude() 
		{
			return longitude;
		}
		public void setLongitude(Double longitude) 
		{
			this.longitude = longitude;
		}
		
		public String getKMLPoint()
		{
			return "\t\t\t\t<Point>\n\t\t\t\t\t<coordinates>"+this.getLongitude()+","+this.getLatitude()+",0.00</coordinates>\n\t\t\t\t</Point>";
		}

	}

}