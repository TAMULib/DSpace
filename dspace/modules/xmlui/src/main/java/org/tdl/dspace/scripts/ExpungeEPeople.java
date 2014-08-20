package org.tdl.dspace.scripts;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.EPersonDeletionException;
import org.dspace.eperson.Group;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.workflow.WorkflowItem;
import org.dspace.workflow.WorkflowManager;

/**
 * 
 * This is a one time script created for TAMU's upgrade to Shibboleth. We needed a way to
 * remove all the non @tamu.edu accounts before hooking the repository up to shibboleth.
 * This may be usefull elsewhere, but it will probably require testing before being run
 * again.
 * 
 * 
 * @author Scott Phillips 
 *
 */

public class ExpungeEPeople {
	
	private static final int COMMIT_EVERY = 100;
	
	private static String onbehalf = null;
	private static boolean test = true;
	private static boolean not = false;
	
	
	public static void main(String[] argv) throws Exception
    {
    	// create an options object and populate it
    	CommandLineParser parser = new GnuParser();

    	Options options = new Options();

    	options.addOption("e", "eperson", true,	"The email of the person performing these operations");
    	options.addOption("t","test", false, "Just test the operations, nothing will be deleted. (Either this or 'commit' must be defined)");
    	options.addOption("c","commit", false, "Actually perform the operations and permanently remove the identified epeople. Commits will be performed every "+COMMIT_EVERY + " epeople.");
    	
	   	options.addOption("not", false, "Reverse the filter operations, what they previously selected now they do not.");
    	
    	options.addOption("i","filter_id", true, "Remove the eperson with this specific id");
    	options.addOption("n","filter_netid", true, "Remove the epeople who's netid matches the regular expression (ex: '^.+@tdl\\.org$' selects all users from TDL)");
    	options.addOption("m","filter_email", true, "Remove the epeople who's email matches the regular expression (ex: '^.+@tamu\\.edu$' selects all users with emails at TAMU)");
    	
    	options.addOption("h", "help", false, "help");

    	CommandLine line = parser.parse(options, argv);

    	if (line.hasOption('h'))
    	{
    		HelpFormatter myhelp = new HelpFormatter();
    		myhelp.printHelp("ExpungeEPeople\n", options);
    		
    		echo("");
    		echo("Examples: ");
    		echo("");
    		echo("ExpungeEPeople -e <you> -i <eperson-id> -t");
    		echo("");
    		echo("ExpungeEPeople -e <you> -n \"^.+@tdl\\.org$\" -t");
    		echo("");
    		echo("ExpungeEPeople -e <you> -m \"^.+@tamu\\.edu$\" -t");

    		System.exit(0);
    	}
    	
    	if (line.hasOption('e'))
    		onbehalf = line.getOptionValue('e');
    	else
    		fail("An eperson must be specified who will perform these operations");

    	
    	if ( !(line.hasOption("t") ^ line.hasOption("c")) )
    		fail("Either 'commit' or 'test' MUST be specified");
    	
    	if (line.hasOption("c"))
    		test = false;
    	
    	// XXXX Setup or DSpace context
    	Context context = new Context();
    	// First find our on-behalf of
    	EPerson onbehalfEPerson = EPerson.findByEmail(context, onbehalf);
    	if (onbehalfEPerson == null)
    		fail("Unable to find eperson '"+onbehalf+"'.");
    	context.setCurrentUser(onbehalfEPerson); 
    	context.ignoreAuthorization(); 
    	
    	
    	// Reverse the filter operations
    	if (line.hasOption("not"))
    		not = true;
    	else 
    		not = false;
    	
    	
    	// XXXX Determine which epeople are to be removed
    	List<EPerson> epeople = new ArrayList<EPerson>();
    	if (line.hasOption("i"))
    	{
    		if (not)
    			fail("Using the 'not' operator with a specific eperson id is not supported.");
    		
    		EPerson eperson = EPerson.find(context,Integer.parseInt(line.getOptionValue("i")));
    		if (eperson == null)
    			fail("Unable to find eperson '"+line.getOptionValue("i")+"'.");
    		
    		epeople.add(eperson);
    	}
    	else if (line.hasOption("n"))
    	{
    		String filterNetid = line.getOptionValue("n");
    		
    		echo("Searching for all epeople who's netid "+ (not ? "does NOT match" : "matches") +" the pattern '"+filterNetid+"': ", false);
            Pattern pattern = Pattern.compile(filterNetid);
            
    		EPerson[] allEPeople = EPerson.findAll(context, EPerson.ID);
    		for (EPerson eperson : allEPeople)
    			if (pattern.matcher(eperson.getEmail()).matches() ^ not)
    				epeople.add(eperson);
    		echo("Done.");
    	}
    	else if (line.hasOption("m"))
    	{
    		String filterEmail = line.getOptionValue("m");
    		
            echo("Searching for all epeople who's email "+ (not ? "does NOT match" : "matches") +" the pattern '"+filterEmail+"': ", false);
            Pattern pattern = Pattern.compile(filterEmail);
            
    		EPerson[] allEPeople = EPerson.findAll(context, EPerson.ID);
    		for (EPerson eperson : allEPeople)
    			if (pattern.matcher(eperson.getEmail()).matches() ^ not)
    				epeople.add(eperson);
    		echo("Done.");
    	}
    	else
    	{
    		fail("A filter must be specified.");
    	}
    	
    	
    	// XXXX Remove all the identified epeople
    	if (epeople.size() == 0)
	    	fail("No EPeople found matching the filter criteria.");
    	
    	echo("Removing "+epeople.size()+" epeople from the repository");
    	int count=0;
    	for (EPerson eperson : epeople)
    	{
    		count++;
    		echo(count+") Removing eperson, "+ eperson.getEmail());
    		expungeEPerson(context, eperson);
    		
    		if (!test && count % COMMIT_EVERY == 0)
    		{
    			echo("Commiting changes: ",false);
    			context.commit();
    			echo("Committed.");
    		}
    		
    	}
    	
    	// XXXX Either commit or rollback the changes
    	if (test)
    	{
    		echo("Rolling back changes");
    		context.abort();
    	} else {
    		echo("Commiting changes: ",false);
    		context.complete();
    		echo(" Committed.");
    	}
    }
	
	
	/**
	 * Forcefully expunge EPerson from DSpace removing everything associated with that user. This may result in the deletion of items
	 * in the workflow process. It is irrevocable.
	 * 
	 * @param context The current database context.
	 * @param eperson The EPerson to expunge
	 */
	public static void expungeEPerson(Context context, EPerson eperson) throws SQLException, AuthorizeException, EPersonDeletionException, IOException
	{
		TableRowIterator tri;
		
		// We can't remove the current user, probably id=1
		if (context.getCurrentUser().getID() == eperson.getID()	)
			fail("Unable to remove the current user.");
		
		
        // 1) find all workspace items this eperson is the owner of.
        tri = DatabaseManager.query(context,"SELECT ws.workspace_item_id FROM workspaceitem ws, item i WHERE ws.item_id = i.item_id AND i.submitter_id= ? ", eperson.getID());
        while (tri.hasNext())
        {
        	TableRow tr = tri.next();
            int workspaceID = tr.getIntColumn("workspace_item_id");
           
            WorkspaceItem workspaceitem = WorkspaceItem.find(context, workspaceID);
            Collection collection = workspaceitem.getCollection();
            
            echo(" - Removing workspaceitem "+workspaceID+" for collection '"+collection.getName()+"': ",false);
            
            
            tr = DatabaseManager.querySingle(context, "select count(*) as count from workspaceitem where item_id = ?", workspaceitem.getItem().getID());
            if (tr.getLongColumn("count") > 1)
            {
            	// Okay, this is a very special case that should not occure but it appears to have happened in the A&M repository, one item has two 
            	// workspaceitems associated with it. In this case we need to just remove the wrapper on the first one, then on the next workspace item 
            	// this condition will no longer be true and it can be deleted regularly.
            	workspaceitem.deleteWrapper();
            	echo("Removed wrapper (one item maps to two workspaces).");
            	continue;
            }

            workspaceitem.deleteAll();
    		
            echo("Removed.");
        }
        tri.close();
        
        // 2) Find all workflow items this user owns, return them to the pool.
        List<WorkflowItem> ownedworkflows = WorkflowManager.getOwnedTasks(context, eperson);
        for(WorkflowItem ownedworkflow : ownedworkflows)
        {
        	WorkflowManager.unclaim(context, ownedworkflow, eperson);
        }
        
        // Now find all pooled items and if no one else is left to process these items then remove them.
        List<WorkflowItem> pooledworkflows = WorkflowManager.getPooledTasks(context, eperson);
        for(WorkflowItem pooledworkflow : pooledworkflows)
        {
        	TableRow tr;
        	tr = DatabaseManager.querySingle(context,"SELECT count(*) AS count FROM tasklistitem WHERE workflow_id = ?", pooledworkflow.getID());
        	
     	
        	if (tr.getLongColumn("count") == 1)
        	{
        		// No one else is able to to review these items, so we remove them	
				Item item = pooledworkflow.getItem();
				Collection collection = pooledworkflow.getCollection();
				String collectionName = (collection != null) ? collection.getName() : "None";
				
				echo(" - Removing workflowitem "+pooledworkflow.getID()+" intended for collection '"+collectionName+"': ",false);
				
			    // Remove any tasks for this workflow
		        DatabaseManager.updateQuery(context, "DELETE FROM TaskListItem WHERE workflow_id= ? ", pooledworkflow.getID());
				pooledworkflow.deleteWrapper();
				collection.removeItem(item);
				collection.update();
				
				echo("Removed.");
        	}
        	else
        	{
        		// Someone else can handle the pooled workflow items, just remove this user from the tasklist.
        		
                Collection collection = pooledworkflow.getCollection();
                String collectionName = (collection != null) ? collection.getName() : "None";
        		
        		echo(" - Removing task "+pooledworkflow.getID()+" for collection '"+collectionName+"': ",false);
        		DatabaseManager.updateQuery(context, "DELETE FROM TaskListItem WHERE workflow_id = ? AND eperson_id = ? ",pooledworkflow.getID(), eperson.getID());
        		echo("Removed.");
        	}
        }
        
        
        
		// 3) find all the items in the repository associated with this user.
        tri = DatabaseManager.query(context,"SELECT item_id FROM item WHERE submitter_id= ? ",eperson.getID());
        while (tri.hasNext())
        {
            TableRow tr = tri.next();
            int itemID = tr.getIntColumn("item_id");
            Item item = Item.find(context, itemID);
            Collection collection = item.getOwningCollection();
            String collectionName = (collection != null) ? collection.getName() : "None";
            
            echo(" - Removing association with item "+itemID+" in '"+collectionName+"': ",false);
            item.setSubmitter(context.getCurrentUser());
            item.update();
            echo("Removed.");
        }
        tri.close();
        
        // 3) find any groups this user is a member of and remove them.
        Group[] groups = Group.allMemberGroups(context, eperson);
        for (Group group : groups)
        { 		
        	echo(" - Removing eperson from group '"+group.getName()+"': ",false);
    		group.removeMember(eperson);
    		group.update();
    		echo("Removed.");
        }
        
        // 4) find any resource policies associated with this eperson and remove them
        AuthorizeManager.removeAllPolicies(context, eperson);
        tri = DatabaseManager.query(context, "SELECT policy_id FROM resourcepolicy WHERE eperson_id = ? ",eperson.getID());
        while (tri.hasNext())
        {
        	TableRow tr = tri.next();
        	int resourcePolicyID = tr.getIntColumn("policy_id");
        	ResourcePolicy policy = ResourcePolicy.find(context, resourcePolicyID);

        	echo(" - Removing resource policy ("+
        	     Constants.typeText[policy.getResourceType()]+" "+policy.getResourceID()+","+Constants.actionText[policy.getAction()]+
        		 "): ",false);
        	policy.delete();
        	echo("Removed.");
        }
        
        
        // since we've gone behind the eperson object and deleted stuff, let's reload the object.
        context.clearCache();
        eperson = EPerson.find(context, eperson.getID());
        try {
        	eperson.delete();
        } catch (EPersonDeletionException epde)
        {
        	String tables = "";
        	for (String table : ((Vector<String>) epde.getTables()))
        		tables += table + ",";
        	fail("EPerson delete because of the following constraints: "+tables);
        }
	}
	

	/**
	 * Short hand method for printing something out to the screen.
	 * 
	 * @param message The textual message to print
	 */
	public static void echo(String message)
	{
		echo(message,true);
	}
	
	/**
	 * Short hand method for printing something out to the screen, with a switch to append a newline or not.
	 * 
	 * @param message The textual message to print
	 * @param newline Whether to append a newline or not
	 */
	public static void echo(String message, boolean newline)
	{
		System.out.print(message);
		if (newline)
			System.out.println();
		System.out.flush();
	}

	/**
	 * Short hand message for printing an error message and then exiting the program.
	 * 
	 * @param message Error message
	 */
	public static void fail(String message)
	{
		System.out.println("FAILURE: "+message);
		System.exit(0);
	}
	
}
