package org.tdl.dspace.scripts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;

/**
 * 
 * This is a simple script to delete a collection from the command line. Sometimes 
 * big collections are not able to complete the delete transaction before timming out 
 * from the web-based interface, this is usefull in those situations.
 * 
 * 
 * @author Scott Phillips 
 */

public class DeleteCollection {
	
	private static String onbehalf;
	private static String handle;
	
	public static void main(String[] argv) throws Exception
    {
    	// create an options object and populate it
    	CommandLineParser parser = new GnuParser();

    	Options options = new Options();

    	options.addOption("e", "eperson", true,	"The email of the person performing the delete");
    	options.addOption("h", "handle", true, "The handle of the item, community, or collection to delete");
    	options.addOption("h", "help", false, "help");

    	CommandLine line = parser.parse(options, argv);

    	if (line.hasOption('h'))
    	{
    		HelpFormatter myhelp = new HelpFormatter();
    		myhelp.printHelp("DeleteCollection\n", options);
    		
    		echo("");
    		echo("Examples: ");
    		echo("");
    		echo("DeleteCollection -e <you> -i <handle>");
    		echo("");
    		System.exit(0);
    	}
    	
    	if (line.hasOption('e'))
    		onbehalf = line.getOptionValue('e');
    	else
    		fail("An eperson must be specified who will perform these operations");
    	
    	if (line.hasOption('h'))
    		handle = line.getOptionValue('h');
    	else
    		fail("A handle of a community, collection, or item must be supplied");

    	// XXXX Setup or DSpace context
    	Context context = new Context();
    	// First find our on-behalf of
    	EPerson onbehalfEPerson = EPerson.findByEmail(context, onbehalf);
    	if (onbehalfEPerson == null)
    		fail("Unable to find eperson '"+onbehalf+"'.");
    	context.setCurrentUser(onbehalfEPerson); 
    	context.ignoreAuthorization(); 
    	
    	DSpaceObject dso = HandleManager.resolveToObject(context, handle);
    	
    	if (dso == null)
    		fail("Unable to resolve handle '"+handle+"'.");
    	
    	if (dso instanceof Item) 
    	{
    		echo("Found item '"+handle+"', deleting.");
    		Item item = (Item) dso;
    		Collection[] collections = item.getCollections();
    		for (Collection collection : collections)
    		{
    			// Removing the item from the last collection will delete it.
    			echo(" + Removing from collection '"+collection.getHandle()+"'.");
    			collection.removeItem(item);
    		}
    	}
    	else if (dso instanceof Collection)
    	{
    		echo("Found collection '"+handle+"', deleting. (This could take a long time)");
    		Collection collection = (Collection) dso;
    		Community[] communities = collection.getCommunities();
    		for (Community community : communities)
    		{
    			echo(" + Removing from collection '"+community.getHandle()+"'.");
    			community.removeCollection(collection);
    		}
    	}
    	else if (dso instanceof Community)
    	{
    		echo("Found community '"+handle+"', deleting. (This could take a long time)");
    		Community community = (Community) dso;
    		community.delete();
    	}
    	
    	echo("Commiting changes: ",false);
    	context.complete();
    	echo(" Committed.");
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
