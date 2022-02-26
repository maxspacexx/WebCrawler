package assignment;

import java.net.URL;
import java.util.HashMap;

/**
 * A web-index which efficiently stores information about pages. Serialization is done automatically
 * via the superclass "Index" and Java's Serializable interface.
 *
 */
public class WebIndex extends Index {
    /**
     * Needed for Serialization (provided by Index) - don't remove this!
     */
    private static final long serialVersionUID = 1L;

    HashMap<URL, StringBuilder> index;

    public WebIndex()
    {
        index = new HashMap<>();
    }

    public HashMap<URL, StringBuilder> map()
    {
        return index;
    }


}
