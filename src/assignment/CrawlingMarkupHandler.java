package assignment;

import java.util.*;
import java.net.*;
import org.attoparser.simple.*;

/**
 * A markup handler which is called by the Attoparser markup parser as it parses the input;
 * responsible for building the actual web index.
 *
 */
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {

    WebIndex webIndex;
    List<URL> newURLs;
    StringBuilder currentString;
    URL currentURL;
    String currentElementName;
    //public static double totalTime = 0;

    public CrawlingMarkupHandler() {
        currentURL = null;
        webIndex = new WebIndex();
        newURLs = new ArrayList<>();
        currentString = new StringBuilder();
        currentElementName = "";
    }

    public CrawlingMarkupHandler(URL url)
    {
        this();
        currentURL = url;
    }

    /**
    * This method returns the complete index that has been crawled thus far when called.
    */
    public Index getIndex() {
        //System.out.println("webIndex size: " + webIndex.map().size());
        return webIndex;
    }

    /**
    * This method returns any new URLs found to the Crawler; upon being called, the set of new URLs
    * should be cleared.
    */
    public List<URL> newURLs() {
        List<URL> temp = new ArrayList<>(newURLs);
        newURLs.clear();
        return temp;
    }


    /**
    * Called when the parser first starts reading a document.
    * @param startTimeNanos  the current time (in nanoseconds) when parsing starts
    * @param line            the line of the document where parsing starts
    * @param col             the column of the document where parsing starts
    */
    public void handleDocumentStart(long startTimeNanos, int line, int col) {
        //System.out.println("Start of document at: " + currentURL);
        //add a space at the start
        currentString = new StringBuilder(" ");
    }

    /**
    * Called when the parser finishes reading a document.
    * @param endTimeNanos    the current time (in nanoseconds) when parsing ends
    * @param totalTimeNanos  the difference between current times at the start
    *                        and end of parsing
    * @param line            the line of the document where parsing ends
    * @param col             the column of the document where the parsing ends
    */
    public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {
        //System.out.println("End of document at: " + currentURL);
        //make new StringBuilder, so it isn't modified again later
        //append a space at the end
        StringBuilder temp = new StringBuilder(currentString + " ");
        //totalTime += totalTimeNanos*0.000000001;
        webIndex.map().put(currentURL, temp);
    }

    /**
    * Called at the start of any tag.
    * @param elementName the element name (such as "div")
    * @param attributes  the element attributes map, or null if it has no attributes
    * @param line        the line in the document where this elements appears
    * @param col         the column in the document where this element appears
    */
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
        currentElementName = elementName;
        //check if is a link element
        if("A".equalsIgnoreCase(elementName))
        {
            //change the map to lowercase
            Map<String, String> temp = new HashMap<>(attributes);
            for(String s:attributes.keySet())
            {
                temp.put(s.toLowerCase(),temp.remove(s));
            }
            //System.out.println(temp);
            //get the link and add it to the map and the set of new urls
            try
            {
                if(temp.get("href") != null && (temp.get("href").toLowerCase().endsWith(".html")|| temp.get("href").toLowerCase().endsWith(".htm")))
                {
                    URL next = new URL(currentURL, temp.get("href"));
                    if(webIndex.map().get(next) == null)
                    {
                        newURLs.add(next);
                        webIndex.map().putIfAbsent(next, new StringBuilder());
                    }
                }
            } catch (MalformedURLException e)
            {
                System.err.println("Bad URL in webpage: " + temp.get("href"));
            }
        }
    }

    /**
    * Called at the end of any tag.
    * @param elementName the element name (such as "div").
    * @param line        the line in the document where this elements appears.
    * @param col         the column in the document where this element appears.
    */
    public void handleCloseElement(String elementName, int line, int col) {
        //reset element name
        currentElementName = "";
        //System.out.println("End element:   " + elementName);
    }

    /**
    * Called whenever characters are found inside a tag. Note that the parser is not
    * required to return all characters in the tag in a single chunk. Whitespace is
    * also returned as characters.
    * @param ch      buffer containing characters; do not modify this buffer
    * @param start   location of 1st character in ch
    * @param length  number of characters in ch
    */
    public void handleText(char[] ch, int start, int length, int line, int col) {
        //System.out.print("Text:    ");
        //ignore text in non-display elements
        if(currentElementName.equalsIgnoreCase("script")||currentElementName.equalsIgnoreCase("style")||currentElementName.equalsIgnoreCase("video")
        ||currentElementName.equalsIgnoreCase("track")||currentElementName.equalsIgnoreCase("audio")||currentElementName.equalsIgnoreCase("source")
        ||currentElementName.equalsIgnoreCase("picture")||currentElementName.equalsIgnoreCase("img"))
        {
            //System.out.print("\n");
            return;
        }
        //add characters to the stringbuilder, removing punctuation with spaces
        for(int i = start; i < start + length; i++) {
            if(Character.isLetterOrDigit(ch[i]))
            {
                //System.out.print(ch[i]);
                currentString.append(Character.toLowerCase(ch[i]));
            }
            else
            {
                if (!currentString.toString().endsWith(" "))
                {
                    //System.out.print(" ");
                    currentString.append(" ");
                }
            }
        }

        //System.out.print("\n");
    }

    public void setCurrentURL(URL url)
    {
        currentURL = url;
        //add the mapping
        //webIndex.map().putIfAbsent(url, new StringBuilder());
    }

    public URL getCurrentURL()
    {
        return currentURL;
    }

    public StringBuilder remove(URL url)
    {
        return webIndex.map().remove(url);
    }

}
