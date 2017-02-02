package edu.tamu.dspace.curate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.net.URL;
import java.net.URLConnection;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.curate.Suspendable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PelicanGeoparserTask extends AbstractCurationTask {

    /** log4j category */
    private static Logger log = Logger.getLogger(PelicanGeoparserTask.class);

    /** used for output of curation task results */
    private static StringBuilder sb = new StringBuilder();

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        super.init(curator, taskId);
    }

    /**
     * Perform the curation task
     **/
    @Override
    public int perform(DSpaceObject dso) throws IOException {
        this.log = Logger.getLogger(ConfigurationManager.class);

        if (dso.getType() == Constants.ITEM) {
            Item item = (Item) dso;
            int count = 0;

            String handle = item.getHandle();
            if (handle == null) {
                // we are still in workflow - no handle assigned
                handle = "in workflow";
            }
            sb.append("Geoparsing item ").append(handle).append("\n");

            String metadataFieldValue = "";

            String documentText = getTextFromDSpaceObject(item);

            String connectionString = null;

            // Pass the text to Pelican API
            try {
                connectionString = "http://localhost:9000/api?" + URLEncoder.encode("There is a City named Omaha", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            sb.append("\nConnection String:\n" + connectionString);

            String content = null;
            URLConnection connection = null;
            try {
                connection = new URL(connectionString).openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
                scanner.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode actualObj = null;
            try {
                actualObj = mapper.readTree(content);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Iterator<String> fieldNames = actualObj.fieldNames();

            Map<String, String> namesToWinners = new HashMap<String, String>();

            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                com.fasterxml.jackson.databind.JsonNode node = actualObj.get(name);
                System.out.println(name);
                // System.out.println(node);

                Iterator<com.fasterxml.jackson.databind.JsonNode> candidates = node.elements();

                String answer = "";
                com.fasterxml.jackson.databind.JsonNode topCandidate = null;
                Double maxScore = Double.MIN_VALUE;

                while (candidates.hasNext()) {
                    com.fasterxml.jackson.databind.JsonNode candidate = candidates.next();

                    // System.out.println("He has score of " + candidate.get("scoreAsDouble") + " and lat " + candidate.get("position").get("lat"));

                    Double myScore = candidate.get("scoreAsDouble").asDouble();
                    if (myScore > maxScore) {
                        topCandidate = candidate;
                    }
                }

                if (topCandidate != null)
                    answer += topCandidate.get("name").asText() + " (" + topCandidate.get("uri").toString().replace("\"", "") + "): " + topCandidate.get("details").get("position").get("lat") + ", " + topCandidate.get("details").get("position").get("lon");

                namesToWinners.put(name, answer);

            }

            for (String name : namesToWinners.keySet()) {

                // write to the dspace item
                sb.append("Assign field value \"" + namesToWinners.get(name) + "\"\n");
                item.addMetadata("local", "suggested", "DCCoverageSpatial", "en", namesToWinners.get(name));
                try {
                    item.update();
                } catch (SQLException e) {
                    // Auto-generated catch block from getting the textBundle from the item's bundles
                    log.error("SQL exception updating item " + item.getHandle());
                    e.printStackTrace();
                    count++;
                } catch (AuthorizeException e) {
                    // Auto-generated catch block from getting the next token from the Scanner
                    log.error("Authorization Exception updating item " + item.getHandle());
                    e.printStackTrace();
                    count++;
                }

            }

            report(sb.toString());
            setResult(sb.toString());

            return (count == 0) ? Curator.CURATE_SUCCESS : Curator.CURATE_FAIL;
        } // end of if DSO is an item
        else {
            setResult("Object skipped");
            return Curator.CURATE_SKIP;
        }
    }

    public String getTextFromDSpaceObject(DSpaceObject dso) {
        if (dso.getType() != Constants.ITEM) {
            return null;
        }
        Item item = (Item) dso;

        String handle = item.getHandle();
        if (handle == null) {
            // we are still in workflow - no handle assigned
            handle = "in workflow";
        }

        String fullText = "";

        try {
            // get all the extracted text
            for (Bundle textBundle : item.getBundles("TEXT")) {
                for (Bitstream textBitstream : textBundle.getBitstreams()) {
                    // Convert the bitstream's InputStream to a String. \A is the "beginning of the input boundary" thus giving us one token for the entire contents of the stream.
                    fullText += new Scanner(textBitstream.retrieve()).useDelimiter("\\A").next();
                }
            }
        } catch (SQLException e) {
            // Auto-generated catch block from getting the textBundle from the item's bundles
            System.err.println("SQL exception getting text bundle for item " + item.getHandle());
            e.printStackTrace();
        } catch (IOException e) {
            // Auto-generated catch block from getting the next token from the Scanner
            reportError("IO Exception getting next token from Scanner when processing text bundle for item " + item.getHandle());
            e.printStackTrace();
        } catch (AuthorizeException e) {
            // Auto-generated catch block from getting the next token from the Scanner
            reportError("Authorization Exception getting next token from Scanner when processing text bundle for item " + item.getHandle());
            e.printStackTrace();
        } catch (Exception e) {
            // Auto-generated catch block from the obtainGISFromGeoNames within the for loop
            reportError("Exception thrown by obtainGISFromGeoNames or applyHeuristicsToNameList.");
            e.printStackTrace();
        }

        return fullText;
    }

    protected void report(String message) {
        if (log != null) {
            log.info(message);
        } else {
            System.out.println(message);
        }
    }

    protected void reportError(String errorMsg) {
        if (log != null) {
            log.error(errorMsg);
        } else {
            System.err.println(errorMsg);
        }
    }

}