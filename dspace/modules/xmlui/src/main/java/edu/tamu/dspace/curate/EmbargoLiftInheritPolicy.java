package edu.tamu.dspace.curate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dspace.app.xmlui.aspect.administrative.authorization.AuthorizationMain;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Collection;
import org.dspace.content.DCValue;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.eperson.Group;

import org.apache.log4j.Logger;

public class EmbargoLiftInheritPolicy extends AbstractCurationTask {
	
    /** log4j category */
    private static final Logger log = Logger.getLogger(EmbargoLiftInheritPolicy.class);
    
    private int result = Curator.CURATE_SUCCESS;
    private StringBuilder sb = new StringBuilder();
    
    public static int count;
    
    @Override 
    public void init(Curator curator, String taskId) throws IOException
    {
    	count = 0;
    	sb = new StringBuilder();    	
        super.init(curator, taskId);
    }

    @Override
    public int perform(DSpaceObject dso) throws IOException 
    {
    	if (dso.getType() == Constants.SITE) {    	
        	sb.append("Cannot perform this task at site level.");
        	//this.setResult(sb.toString());
        	return Curator.CURATE_FAIL;    	
        } else {
        	distribute(dso); 
        	sb.append(count);
        	//this.setResult(sb.toString());
        	return result;
	    }
    }
    
    @Override
    protected void performItem(Item item) throws SQLException, IOException
    {
		boolean hasDefaultPolicies = false;
    	Context context = Curator.curationContext();
    	Item workingItem = Item.find(context, item.getID());
    	DCValue[] embargoLift = workingItem.getMetadata("local.embargo.lift");    	
		if (embargoLift.length == 0) {
			count++;			
			Collection c = null;	    	
	    	c = workingItem.getOwningCollection();
	    	log.debug("Collection " + c.getID() + " (" + c.getHandle() + ")");
			
			List<ResourcePolicy> defaultCollectionPolicies = AuthorizeManager.getPoliciesActionFilter(context, c, Constants.DEFAULT_BITSTREAM_READ);

			if (defaultCollectionPolicies.size() < 1) {
				hasDefaultPolicies = false;
				log.debug("Collection " + c.getID() + " (" + c.getHandle() + ")" + " has no default bitstream READ policies");
				throw new SQLException("Collection " + c.getID() + " (" + c.getHandle() + ")" + " has no default bitstream READ policies");
			}
			else {
				hasDefaultPolicies = true;
			}
			
			try
			{
				for (Bundle bundle : workingItem.getBundles()) {				
					if(hasDefaultPolicies) {
						try {
							log.debug("Adding collections default policy to bundle");
							
							List<ResourcePolicy> policiesBundleToAdd = new ArrayList<ResourcePolicy>();
					        for (ResourcePolicy rp : defaultCollectionPolicies){
					            rp.setAction(Constants.READ);
					            // if an identical policy is already in place don't add it
					            if(!AuthorizeManager.isAnIdenticalPolicyAlreadyInPlace(context, bundle, rp)){
					                rp.setRpType(ResourcePolicy.TYPE_INHERITED);
					                policiesBundleToAdd.add(rp);
					            }
					        }
					        
							AuthorizeManager.addPolicies(context, policiesBundleToAdd, bundle);
						} catch(AuthorizeException ae) { }
					}
					else {				
						try {
							log.debug("Adding policy to bundle");
							log.debug("Policy Constants.READ, Group Anonymous");
							AuthorizeManager.addPolicy(context, bundle, Constants.READ, Group.findByName(context, "Anonymous"));							
						} catch(AuthorizeException ae) { }
					}
            	
					for (Bitstream bs : bundle.getBitstreams()) {					
						if(hasDefaultPolicies) {
							try {
								log.debug("Adding collections default policy to bitstream");
																
								List<ResourcePolicy> policiesBitstreamToAdd = new ArrayList<ResourcePolicy>();
						        for (ResourcePolicy rp : defaultCollectionPolicies){
						            rp.setAction(Constants.READ);
						            // if an identical policy is already in place don't add it
						            if(!AuthorizeManager.isAnIdenticalPolicyAlreadyInPlace(context, bs, rp)){
						                rp.setRpType(ResourcePolicy.TYPE_INHERITED);
						                policiesBitstreamToAdd.add(rp);
						            }
						        }								
								
								AuthorizeManager.addPolicies(context, policiesBitstreamToAdd, bs);
							} catch(AuthorizeException ae) { }
						}
						else {					
							try {
								log.debug("Adding policy to bitstream");
								log.debug("Bitstream id: " + bs.getID());
								log.debug("Bitstream name: " + bs.getName());
								log.debug("Policy Constants.READ, Group Anonymous");
								AuthorizeManager.addPolicy(context, bs, Constants.READ, Group.findByName(context, "Anonymous"));							
							} catch(AuthorizeException ae) { }
						}
						workingItem.update();
						context.commit();			            
						sb.append(workingItem.getHandle() + " authorized for group \"Anonymous\"\n");
					}
            	}
    		}
			catch (AuthorizeException e) {
	    		result = Curator.CURATE_ERROR;
	    		sb.append("Authorization failure on item: " + item.getHandle() + "\nAborting...");
	    	}
    	}
    }
}


