package edu.tamu.metadatatreebrowser;

import java.util.Comparator;

public class MetadataTreeNodeComparator implements Comparator<MetadataTreeNode> {

    @Override
    public int compare(MetadataTreeNode o1, MetadataTreeNode o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }

}
