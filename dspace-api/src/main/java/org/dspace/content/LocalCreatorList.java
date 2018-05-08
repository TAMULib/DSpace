package org.dspace.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class LocalCreatorList {
    
    private List<Pair<String, DCPersonName>> list;
    
    public LocalCreatorList() {
        list = new ArrayList<Pair<String, DCPersonName>>();
    }

    public LocalCreatorList(String aggregate) {
        this();
        list.addAll(this.parseString(aggregate));
    }
    
    public LocalCreatorList(List<Pair<String, DCPersonName>> list) {
        this.list = list;
    }
    
    public void addCreator(Pair<String, DCPersonName> creator) {
        this.list.add(creator);
    }
    
    public String getStatusString() {
        String result = "";
        for (Pair<String, DCPersonName> entry : list) {
            if (result.equals("")) {
                result += entry.getValue().toString() + ", " + entry.getKey();
            } else {
                result += ", ;" + entry.getValue().toString() + ", " + entry.getKey();
            }
        }
        return result;
    }
    
    private List<Pair<String, DCPersonName>> parseString(String string) {
        String[] entries = string.split("; ");
        List<Pair<String, DCPersonName>> tempList = new ArrayList<Pair<String, DCPersonName>>();
        for (String entry : entries) {
            if (entry.endsWith(", ")) {
                tempList.add(new ImmutablePair<String, DCPersonName>("", new DCPersonName(entry.substring(entry.length()-2))));
            } else {
                int last = entry.lastIndexOf(",");
                tempList.add(new ImmutablePair<String, DCPersonName>(entry.substring(0, last), new DCPersonName(entry.substring(last+2))));
            }
        }
        return tempList;
    }
}
