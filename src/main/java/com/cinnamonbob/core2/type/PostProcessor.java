package com.cinnamonbob.core2.type;

import com.cinnamonbob.core2.BobException;

import java.util.List;
import java.util.LinkedList;

/**
 * 
 *
 */
public class PostProcessor extends AbstractType
{
    private List<Regex> regexs = new LinkedList<Regex>();
    private String name;

    public void setName(String s)
    {
        this.name = s;
    }

    public Regex createRegex()
    {
        Regex r = new Regex();
        regexs.add(r);
        return r;
    }

    public void execute() throws BobException
    {

    }
}
