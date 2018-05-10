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
                result += entry.getValue().toString() + (entry.getKey() == "" ? "" : ", " + entry.getKey());
            } else {
                result += "; " + entry.getValue().toString() + (entry.getKey() == "" ? "" : ", " + entry.getKey());
            }
        }
        return result;
    }
    
    private List<Pair<String, DCPersonName>> parseString(String string) {
        System.out.println("\n\nString: " + string);
        String[] entries = string.split("; ");
        List<Pair<String, DCPersonName>> tempList = new ArrayList<Pair<String, DCPersonName>>();
        if (!string.equals("")) {
            for (String entry : entries) {
                String[] chunks = entry.split(", ");
                if (!chunks[chunks.length-1].contains("@")) {
                    tempList.add(new ImmutablePair<String, DCPersonName>("", new DCPersonName(entry)));
                } else {
                    int last = entry.lastIndexOf(",");
                    System.out.println("\nString: " + entry + "\nString length: " + entry.length() + "\nlast entry: " + last + "\n\n");
                    tempList.add(new ImmutablePair<String, DCPersonName>(entry.substring(last+2), new DCPersonName(entry.substring(0, last))));
                }
            }
        }
        return tempList;
    }
    
    public List<Pair<String, DCPersonName>> getList() {
        return this.list;
    }
}
