package com.zutubi.pulse.local;

import java.io.PrintStream;

/**
 */
public class Indenter
{
    private PrintStream stream;
    private String currentIndent;
    private String indentString;

    public Indenter(PrintStream stream, String indentString)
    {
        this.stream = stream;
        this.indentString = indentString;
        currentIndent = "";
    }


    public void indent()
    {
        currentIndent += indentString;
    }


    public void dedent()
    {
        if(currentIndent.length() > 0)
        {
            currentIndent = currentIndent.substring(indentString.length());
        }
    }


    public void println(String s)
    {
        stream.println(currentIndent + s);
    }


    public void println(Object o)
    {
        stream.println(currentIndent + o.toString());
    }


    public void println()
    {
        stream.println();
    }
}
