package org.dspace.content;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

public class LocalCreatorList {
    
    private List<Triple<String, String, DCPersonName>> list;
    
    public LocalCreatorList() {
        list = new ArrayList<Triple<String, String, DCPersonName>>();
    }

    public LocalCreatorList(String aggregate, String status) {
        this();
        list.addAll(this.parseString(aggregate, status));
    }
    
    public LocalCreatorList(List<Triple<String, String, DCPersonName>> list) {
        this.list = list;
    }
    
    public void addCreator(Triple<String, String, DCPersonName> creator) {
        this.list.add(creator);
    }
    
    public void setCreators(String string, String status) {
        this.setList(parseString(string, status));
    }
    
    public String getStatusString() {
        String result = "";
        for (Triple<String, String, DCPersonName> entry : list) {
            if (result.equals("")) {
                result += entry.getRight().toString() + (entry.getLeft() == "" ? "" : ", " + entry.getLeft());
            } else if (!entry.getRight().toString().equals("")) {
                result += "; " + entry.getRight().toString() + (entry.getLeft() == "" ? "" : ", " + entry.getLeft());
            }
        }
        return result;
    }
    
    private List<Triple<String, String, DCPersonName>> parseString(String string, String status) {
        String[] entries = string.split("; ");
        List<Triple<String, String, DCPersonName>> tempList = new ArrayList<Triple<String, String, DCPersonName>>();
        if (!string.equals("")) {
            for (String entry : entries) {
                String[] chunks = entry.split(", ");
                if (!chunks[chunks.length-1].contains("@")) {
                    tempList.add(new ImmutableTriple<String, String, DCPersonName>("", status, new DCPersonName(entry)));
                } else {
                    int last = entry.lastIndexOf(",");
                    tempList.add(new ImmutableTriple<String, String, DCPersonName>(entry.substring(last+2), status, new DCPersonName(entry.substring(0, last))));
                }
            }
        }
        return tempList;
    }

    public List<Triple<String, String, DCPersonName>> getList() {
        return this.list;
    }
    
    public void setList(List<Triple<String, String, DCPersonName>> list) {
        this.list = list;
    }
}
