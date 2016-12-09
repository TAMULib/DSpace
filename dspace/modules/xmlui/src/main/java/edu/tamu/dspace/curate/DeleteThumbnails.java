package edu.tamu.dspace.curate;

import java.io.IOException;
import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

public class DeleteThumbnails extends AbstractCurationTask {

    private static int result;

    private static StringBuilder sb;

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        super.init(curator, taskId);
        result = Curator.CURATE_SUCCESS;
        sb = new StringBuilder();
    }

    @Override
    public int perform(DSpaceObject dso) throws IOException {
        switch (dso.getType()) {
        case Constants.SITE:
            sb.append("Cannot perform this task at site level.");
            setResult(sb.toString());
            result = Curator.CURATE_FAIL;
            break;
        case Constants.COMMUNITY:
            sb.append("Deleting thumbnails in community " + dso.getHandle() + "\n");
            setResult(sb.toString());
            break;
        case Constants.COLLECTION:
            sb.append("Deleting thumbnails in collection " + dso.getHandle() + "\n");
            setResult(sb.toString());
            break;
        case Constants.ITEM:
            Item item = (Item) dso;
            try {
                int count = 0;
                for (Bundle bundle : item.getBundles("THUMBNAIL")) {
                    for (Bitstream bitstream : bundle.getBitstreams()) {
                        bundle.removeBitstream(bitstream);
                        count++;
                    }
                    item.removeBundle(bundle);
                }
                item.update();
                sb.append(item.getHandle() + ": " + count + " images deleted.\n");
                setResult(sb.toString());
            } catch (SQLException e) {
                sb.append("Failed to persist change on item: " + item.getHandle() + "\nAborting...");
                setResult(sb.toString());
                result = Curator.CURATE_ERROR;
            } catch (AuthorizeException e) {
                sb.append("Authorization failure on item: " + item.getHandle() + "\nAborting...");
                setResult(sb.toString());
                result = Curator.CURATE_ERROR;
            }
            break;   
        
        default:
            
            break;
        }
        return result;
    }

}
