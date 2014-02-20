package org.burgetr.segm.tagging.taggers;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.burgetr.segm.AreaNode;
import org.burgetr.segm.tagging.Tag;

/**
 *
 * @author burgetr
 */
public class SessionTagger implements Tagger
{

    protected final int MIN_WORDS = 2;
    /** The expression the whole area must start with */
    protected Pattern areaexpr = Pattern.compile("[A-Z0-9]"); //uppercase or number
    /** The expression describing the allowed title format */
    protected Pattern titleexpr = Pattern.compile("[A-Z][A-Za-z0-9\\s\\:\\-\\p{Pd}]*");  //p{Pd} ~ Unicode Punctuation-dashes category
    /** The expression describing the allowed format of the title continuation */
    protected Pattern contexpr = Pattern.compile("[A-Za-z\\s\\:\\-\\p{Pd}]+"); 

    /** Words that are required in the session title */
    protected Vector<String> whitelist;
    /** Words that are not allowed in the session title */
    protected Vector<String> blacklist;
    
    public SessionTagger()
    {
        whitelist = new Vector<String>();
        //whitelist.add("session");
        blacklist = new Vector<String>();
        blacklist.add("chair");
    }
    
    public Tag getTag()
    {
        return new Tag("session", this);
    }

    public double getRelevance()
    {
        return 0.6;
    }
    
    public boolean belongsTo(AreaNode node)
    {
        if (node.isLeaf())
        {
            String text = node.getArea().getBoxText().trim();
            if (areaexpr.matcher(text).lookingAt()) //check the allowed text start
            {
                //check if there is a substring with the allowed format
                Matcher match = titleexpr.matcher(text);
                while (match.find())
                {
                    String s = match.group();
                    String[] words = s.split("\\s+");
                    if (words.length >= MIN_WORDS
                        && !containsListedWord(blacklist, words)
                        /*&& containsListedWord(whitelist, words)*/)
                        return true;
                }
                return false;
            }
        }
        return false;
    }

    public boolean allowsContinuation(AreaNode node)
    {
        if (node.isLeaf())
        {
            String text = node.getArea().getBoxText().trim();
            if (contexpr.matcher(text).lookingAt()) //must start with the allowed format
            {
                return true;
            }
        }
        return false;
    }

    public boolean allowsJoining()
    {
        return true;
    }
    
    public boolean mayCoexistWith(Tag other)
    {
        return (!other.getValue().equals("persons") && !other.getValue().equals("title"));
    }
    
    public Vector<String> extract(String src)
    {
        Vector<String> ret = new Vector<String>();
        
        Matcher match = titleexpr.matcher(src);
        while (match.find())
        {
            String s = match.group();
            String[] words = s.split("\\s+");
            if (words.length >= MIN_WORDS)
                ret.add(s);
        }
        
        return ret;
    }
    
    //=================================================================================================
    
    protected boolean containsListedWord(Vector<String> list, String[] words)
    {
        for (String w : words)
        {
            if (list.contains(w.toLowerCase()))
                return true; 
        }
        return false;
    }
}