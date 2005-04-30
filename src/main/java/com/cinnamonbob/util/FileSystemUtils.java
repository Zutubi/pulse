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
}
