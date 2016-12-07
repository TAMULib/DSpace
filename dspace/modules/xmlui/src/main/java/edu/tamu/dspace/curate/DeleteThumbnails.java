package edu.tamu.dspace.curate;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

public class DeleteThumbnails extends AbstractCurationTask {

	private int result = Curator.CURATE_SUCCESS;
	private StringBuilder sb = new StringBuilder();
	private static Logger log = Logger.getLogger(DeleteThumbnails.class);

	@Override
	public void init(Curator curator, String taskId) throws IOException {
		super.init(curator, taskId);
		sb = new StringBuilder();
	}

	@Override
	public int perform(DSpaceObject dso) throws IOException {
		int result = Curator.CURATE_UNSET;
		if (dso.getType() == Constants.SITE) {
			sb.append("Cannot perform this task at site level.");
			this.setResult(sb.toString());
			result = Curator.CURATE_FAIL;
		} else if(dso.getType() == Constants.ITEM) {
			log.info("Doing nothing because this is an ITEM and we are relying on distribute to have called performItem somehow.");
			result = Curator.CURATE_SUCCESS;
		}
		else {
			log.info("Distributing curation task among members of DSO handle (id): " + dso.getHandle() + " (" + dso.getID() + ")");
			distribute(dso);
			this.setResult(sb.toString());
			result = Curator.CURATE_SUCCESS;
		}
		return result;
	}

	@Override
	protected void performItem(Item item) throws SQLException, IOException {
		Context context = Curator.curationContext();
		log.info("Deleting thumbnails for item " + item.getID());
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
			context.commit();
			sb.append(item.getHandle() + ": " + count + " images deleted. \n");
		} catch (AuthorizeException e) {
			result = Curator.CURATE_ERROR;
			sb.append("Authorization failure on item: " + item.getHandle() + "\nAborting...");
		}
	}
}