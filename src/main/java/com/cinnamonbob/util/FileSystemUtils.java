package com.cinnamonbob.util;

import java.io.File;

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
        String[] contents = dir.list();

        assert(contents != null);
        
        for(String child: contents)
        {
            File file = new File(dir, child);
            
            if(file.isDirectory())
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
}
