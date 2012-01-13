package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;

import java.io.File;

/**
 * Type converted for files.  Converts between strings and files using the
 * string value for the file path.
 */
public class FileSqueezer implements TypeSqueezer
{
    public String squeeze(Object obj) throws SqueezeException
    {
        if (obj == null)
        {
            return "";
        }
        return ((File) obj).getPath();
    }

    public Object unsqueeze(String s) throws SqueezeException
    {
        return new File(s);
    }
}
