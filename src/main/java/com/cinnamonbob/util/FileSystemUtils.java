package com.cinnamonbob.util;

import java.io.File;
import java.io.IOException;

/**
 * Miscellaneous utilities for manipulating the file system.
 * 
 * @author jsankey
 */
public class FileSystemUtils
{
    /**
     * Recursively delete a directory and its contents.
     * 
     * @param dir
     *        the directory to delete
     * @return true iff the whole directory was successfully delete
     */
    public static boolean removeDirectory(File dir)
    {
        if (!dir.exists())
        {
            return true;
        }
        if (!dir.isDirectory())
        {
            return false;
        }

        String[] contents = dir.list();

        assert(contents != null);
        
        for(String child: contents)
        {
            File file = new File(dir, child);
            String canonical;
      
            // The canonical path lets us distinguish symlinks from actual
            // directories.
            try
            {
                canonical = file.getCanonicalPath();
            }
            catch(IOException e)
            {
                return false;
            }
            
            //TODO: Fix the following condition - fails in windows since its case insensitive... just ignore case?...
            // do not follow symlinks when deleting directories.            
            if(file.isDirectory() && canonical.equals(file.getAbsolutePath()))
            {
                if(!removeDirectory(file))
                {
                    return false;
                }
            }
            else
            {
                if(!file.delete())
                {
                    return false;
                }
            }
        }

        return dir.delete();
    }
    
    public static final File createTmpDirectory(String prefix, String suffix) throws IOException
    {
        File file = File.createTempFile(prefix, suffix);
        if (!file.exists())
        {
            throw new IOException();
        }
        if (!file.delete())
        {
            throw new IOException();
        }
        if (!file.mkdirs())
        {
            throw new IOException();
        }
        return file;
    }
}
