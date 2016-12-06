package edu.tamu.dspace.curate;

import java.io.IOException;
import java.sql.SQLException;

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

	@Override
	public void init(Curator curator, String taskId) throws IOException {
		super.init(curator, taskId);
		sb = new StringBuilder();
	}

	@Override
	public int perform(DSpaceObject dso) throws IOException {
		if (dso.getType() == Constants.SITE) {
			sb.append("Cannot perform this task at site level.");
			this.setResult(sb.toString());
			return Curator.CURATE_FAIL;
		} else {
			distribute(dso);
			this.setResult(sb.toString());
			return result;
		}
	}

	@Override
	protected void performItem(Item item) throws SQLException, IOException {
		Context context = Curator.curationContext();
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