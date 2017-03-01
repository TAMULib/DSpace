package org.dspace.rest.common;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "metadatarecord")
public class MetadataRecord
{
	List<MetadataEntry> entries;
	
	public MetadataRecord()
	{
	}
	
	public MetadataRecord(List<MetadataEntry> entries)
	{
		this.entries = entries;
	}
	
	public List<MetadataEntry> getEntries()
	{
		return entries;
	}
	
	public void setEntries(List<MetadataEntry> entries)
	{
		this.entries = entries;
	}
	
}
