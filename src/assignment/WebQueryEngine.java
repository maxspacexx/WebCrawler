package assignment;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * A query engine which holds an underlying web index and can answer textual queries with a
 * collection of relevant pages.
 *
 */
public class WebQueryEngine {

    static HashMap<URL, StringBuilder> map;
    ArrayList<String> tokens;
    int currentIndex = 0;
    private class TreeNode
    {
        public String val;
        public TreeNode left;
        public TreeNode right;
        public TreeNode(String val)
        {
            this.val = val;
        }
        public TreeNode(String val, TreeNode left, TreeNode right)
        {
            this.val = val;
            this.left = left;
            this.right = right;
        }
        public void set(String val, TreeNode left, TreeNode right)
        {
            this.val = val;
            this.left = left;
            this.right = right;
        }

    }



    /**
     * Returns a WebQueryEngine that uses the given Index to construct answers to queries.
     *
     * @param index The WebIndex this WebQueryEngine should use.
     * @return A WebQueryEngine ready to be queried.
     */
    public static WebQueryEngine fromIndex(WebIndex index) {
        //initialize the map
        map = index.map();
        return new WebQueryEngine();
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the query expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public Collection<Page> query(String query) {
        //create tokens from the query
        this.createTokens(query);
        //get the roots for each query tree
        ArrayList<TreeNode> roots = makeTrees();
        if(roots.size() == 0)
        {
            return new HashSet<>();
        }
        //initialize urls to be the result from the first query tree
        Set<URL> urls = this.getPages(roots.get(0), map.keySet());
        //use the remaining query trees to further narrow down urls
        for (int i = 1; i < roots.size(); i++)
        {
            urls = (this.getPages(roots.get(i), urls));
        }
        //makes the set of pages to return
        Set<Page> results = new HashSet<>();
        for(URL url : urls)
        {
            results.add(new Page(url));
        }
        return results;
    }



    /**
     *
     * @param root query tree root
     * @param urls set of URLs to search from
     * @return set of URLs that satisfy query tree
     */
    private Set<URL> getPages(TreeNode root, Set<URL> urls)
    {
        if(root == null || root.val.isEmpty() || urls.isEmpty())
        {
            return new HashSet<>();
        }
        switch (root.val)
        {
            case "|" -> {
                Set<URL> temp = new HashSet<>();
                temp.addAll(getPages(root.left, urls));
                temp.addAll(getPages(root.right, urls));
                Set<URL> temp2 = new HashSet<>(urls);
                temp2.retainAll(temp);
                return temp2;
            }
            case "&" -> {
                Set<URL> left = getPages(root.left, urls);
                return getPages(root.right, left);
            }
            default -> {
                //case: the val is a word
                //check for " or !
                if(root.val.charAt(0) == '"')
                {
                    //return nothing if improper query (doesn't end with ")
                    if(!root.val.endsWith("\""))
                    {
                        return new HashSet<>();
                    }
                    //remove quotations
                    root.val = root.val.substring(1,root.val.length()-1);
                    StringBuilder query = new StringBuilder();
                    //replace non-alphanumeric with spaces
                    for(int i = 0; i < root.val.length(); i++)
                    {
                        if(Character.isLetterOrDigit(root.val.charAt(i)))
                        {
                            query.append(root.val.charAt(i));
                        }
                        else
                        {
                            if(!query.toString().endsWith(" "))
                            {
                                query.append(" ");
                            }
                        }
                    }
                    Set<URL> pages = new HashSet<>();
                    for(URL url : urls)
                    {
                        if(map.get(url).toString().contains(" " + query.toString().toLowerCase() + " "))
                        {
                            pages.add(url);
                        }
                    }
                    return pages;
                }
                else if(root.val.charAt(0) == '!')
                {
                    Set<URL> pages = new HashSet<>();
                    for(URL url : urls)
                    {
                        //check for pages that don't contain word, make sure word is alphanumeric
                        if(root.val.substring(1).matches("^[a-zA-Z0-9]*$") &&
                                !map.get(url).toString().contains(" " + root.val.substring(1).toLowerCase() + " "))
                        {
                            pages.add(url);
                        }
                    }
                    return pages;
                }
                //case: if there is no special character at the start
                //only search for alphanumeric queries
                else if(root.val.matches("^[a-zA-Z0-9]*$"))
                {
                    Set<URL> pages = new HashSet<>();
                    for (URL url : urls)
                    {
                        if (map.get(url).toString().contains(" " + root.val.toLowerCase() + " "))
                        {
                            pages.add(url);
                        }
                    }
                    return pages;
                }
                else
                {
                    return new HashSet<>();
                }
            }
        }
    }

    //makes the list of parse/query tree roots
    private ArrayList<TreeNode> makeTrees()
    {
        currentIndex = 0;
        ArrayList<TreeNode> trees = new ArrayList<>();
        while(currentIndex < tokens.size())
        {
            //
            trees.add(parseQuery());
            while(currentIndex < tokens.size() && !tokens.get(currentIndex).equals(" "))
            {
                currentIndex++;
            }
            currentIndex++;
        }
        return trees;
    }

    //creates the parse tree
    private TreeNode parseQuery()
    {
        if(currentIndex >= tokens.size())
        {
            return null;
        }
        if ("(".equals(tokens.get(currentIndex)))
        {
            try
            {
                currentIndex++;
                //recursive call for the left treenode
                TreeNode left = parseQuery();
                currentIndex++;
                //get the special character
                String val = tokens.get(currentIndex);
                if (!(val.equals("|") || val.equals("&")))
                {
                    return null;
                }
                currentIndex++;
                //recursive call for the right treenode
                TreeNode right = parseQuery();
                currentIndex++;
                return new TreeNode(val, left, right);
            }
            catch(IndexOutOfBoundsException e)
            {
                return null;
            }
        }
        //if the token at the current index is a word
        else if(tokens.get(currentIndex) != null)
        {
            return new TreeNode(tokens.get(currentIndex));
        }
        return null;
    }

    //creates the list of tokens
    private void createTokens(String s)
    {
        tokens = new ArrayList<>();
        String currentString = "";
        for(int i = 0; i < s.length(); i++)
        {
            switch (s.charAt(i))
            {
                //adds special characters as a token
                case '(',')','&','|',' ' -> {
                    if(!currentString.isEmpty())
                    {
                        tokens.add(currentString);
                        currentString = "";
                    }
                    tokens.add(s.substring(i, i+1));
                }
                //continues reading until it hits another quotation mark
                case '"' -> {
                    currentString += s.charAt(i);
                    while(i+1 < s.length() && s.charAt(i+1) != '"')
                    {
                        i++;
                        currentString += s.charAt(i);
                    }
                    i++;
                    if(i >= s.length())
                    {
                        tokens = new ArrayList<>();
                        return;
                    }
                    currentString += s.charAt(i);
                }
                //adds character to current token string if it is not special
                default -> {
                    currentString += s.charAt(i);
                }
            }
        }
        //add the current token string if loop finishes
        if(!currentString.isEmpty())
        {
            tokens.add(currentString);
        }
    }

    //MAIN METHOD USED ONLY IN TESTING
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        WebQueryEngine hi;
        hi = WebQueryEngine.fromIndex((WebIndex) Index.load("index.db"));
        String query = "doge";
        hi.createTokens(query);
        System.out.println(hi.tokens);
        ArrayList<TreeNode> roots = hi.makeTrees();
        Set<URL> set = new HashSet<>();
        if(roots.size() > 0)
        {
            set = hi.getPages(roots.get(0), map.keySet());
        }
        //set.retainAll(hi.getPages(roots.get(1), set));
        //set.retainAll(hi.getPages(roots.get(2), set));
        System.out.println("set: " + set);
        for(URL url:set)
        {
            if(map.get(url).toString().contains(" osama "))
            {
                //System.err.println("error");
            }
        }
        //System.out.println(set.size());
        System.out.println(hi.query(query).size());
    }
}
