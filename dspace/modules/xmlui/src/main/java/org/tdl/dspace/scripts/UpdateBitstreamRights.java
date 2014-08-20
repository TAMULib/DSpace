package org.tdl.dspace.scripts;

import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.handle.HandleManager;

/**
 * 
 * @author Jay Paz
 *
 */

public class UpdateBitstreamRights {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// create an options object and populate it
		CommandLineParser parser = new PosixParser();

		Options options = new Options();

		options.addOption("c", "collection", true,
				"destination collection(s) Handle or database ID, if not given then all collections will be processed");
		options
				.addOption("m", "metadata", true,
						"metadata element to match, if none given the 'rights.uri' will be used");
		options.addOption("v", "value", true, "metadata value to match");
		options
				.addOption(
						"g",
						"group",
						true,
						"the group(s) to add (if it is different from annonymous then annonymous will be removed.  If it is annonymous then all others will be removed.");
		options.addOption("h", "help", false, "help");

		CommandLine line = parser.parse(options, args);

		String[] collections = null; // db ID or handles
		String eperson = null; // db ID or email
		String metadata = null;
		String value = null;
		String[] groups = null;

		if (line.hasOption('h')) {
			printHelp(options);
		}

		collections = line.getOptionValues('c');
		eperson = line.getOptionValue('e');
		metadata = line.getOptionValue('m') == null ? "dc.rights.uri" : line
				.getOptionValue('m');
		value = line.getOptionValue('v');
		groups = line.getOptionValues('g');

		if (value == null || groups == null) {
			printHelp(options);
		}

		// ok we should be good to go
		// create Context

		Context c = new Context();

		
		Collection[] mycollections = null;
		// ok now get all the collections
		if (collections != null) {
			mycollections = new Collection[collections.length];

			// validate each collection arg to see if it's a real collection
			for (int i = 0; i < collections.length; i++) {
				// is the ID a handle?
				if (collections[i].indexOf('/') != -1) {
					// string has a / so it must be a handle - try and resolve
					// it
					mycollections[i] = (Collection) HandleManager
							.resolveToObject(c, collections[i]);

					// resolved, now make sure it's a collection
					if ((mycollections[i] == null)
							|| (mycollections[i].getType() != Constants.COLLECTION)) {
						mycollections[i] = null;
					}
				}
				// not a handle, try and treat it as an integer collection
				// database ID
				else if (collections[i] != null) {
					mycollections[i] = Collection.find(c, Integer
							.parseInt(collections[i]));
				}

				// was the collection valid?
				if (mycollections[i] == null) {
					throw new IllegalArgumentException("Cannot resolve "
							+ collections[i] + " to collection");
				}

			}
		} else {
			System.out.println("** No collection specified, will process all **");
			mycollections = Collection.findAll(c);
		}

		// collections are good

		for (Collection col : mycollections) {
			// print progress info
			System.out.println("Processing Collection: "
					+ col.getMetadata("name"));
			ItemIterator i = col.getItems();
			while (i.hasNext()) {
				final Item item = (Item) i.next();
				DCValue[] values = item.getMetadata(metadata);
				for (DCValue v : values) {
					if (value.equals(v.value)) {
						System.out.println("  Item: " + item.getHandle());
						Bundle[] bundles = item.getBundles();
						for (Bundle bundle : bundles) {
							for (String group : groups) {
								System.out.println("    Bundle: "
										+ bundle.getName());
								addPolicy(c, bundle, Constants.READ,
										new Integer(group));
							}
							Bitstream[] bitstreams = bundle.getBitstreams();
							for (Bitstream bit : bitstreams) {
								for (String group : groups) {
									System.out.println("    Bitstream: "
											+ bit.getName());
									addPolicy(c, bit, Constants.READ,
											new Integer(group));
								}
							}
						}
					}
				}
			}
		}

		c.complete();
		System.exit(0);
	}

	static void printHelp(Options options) {
		HelpFormatter myhelp = new HelpFormatter();
		myhelp.printHelp("UpdateBitstreamRights\n", options);
		System.out
				.println("\nupdating rights:    UpdateBitstreamRights -c collection -m element -v value -g group");

		System.exit(0);
	}

	static void addPolicy(Context c, DSpaceObject t, int myaction, int groupID)
			throws SQLException, AuthorizeException {
		Group group = Group.find(c, groupID);
		// group 0 is the anonymous group!
		if (groupID == 0) {
			System.out
					.println("      Annonymous group requested, removing all policies");
			AuthorizeManager.removeAllPolicies(c, t);
		} else {
			// they requested a group other than annonymous so we need to remove
			// annonymous
			System.out.println("      Removing annonymous group");
			AuthorizeManager.removeGroupPolicies(c, t, Group.find(c, 0));
		}

		System.out.println("      Adding group " + group.getName());

		// now create the default policies for submitted items
		ResourcePolicy myPolicy = ResourcePolicy.create(c);
		myPolicy.setResource(t);
		myPolicy.setAction(myaction);
		myPolicy.setGroup(group);
		myPolicy.update();
	}

}
