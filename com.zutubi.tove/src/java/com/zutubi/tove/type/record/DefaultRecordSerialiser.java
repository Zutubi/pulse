package com.zutubi.tove.type.record;

import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Used to (de)serialise records to permanent storage.
 */
public class DefaultRecordSerialiser implements RecordSerialiser
{
    private static final Logger LOG = Logger.getLogger(DefaultRecordSerialiser.class);

    private static final int UNLIMITED = -1;

    private File baseDirectory;

    /**
     * The number of nested directories this serialiser will create when
     * serialising a record.  When the max depth is reached, any remaining
     * nested records will be serialised into a single record. 
     */
    private int maxPathDepth = UNLIMITED;

    private XmlRecordSerialiser xrs = new XmlRecordSerialiser();

    public DefaultRecordSerialiser(File baseDirectory)
    {
        this.baseDirectory = baseDirectory;
        if (!baseDirectory.isDirectory() && !baseDirectory.mkdirs())
        {
            throw new IllegalArgumentException("Failed to create base directory " + baseDirectory.getAbsolutePath());
        }
    }

    public void serialise(Record record, boolean deep)
    {
        serialise("", record, deep, 0);
    }

    public void serialise(String path, Record record, boolean deep, int depth)
    {
        File storageDir = getStorageDir(path);
        if (!storageDir.isDirectory() && !storageDir.mkdir())
        {
            // Only make one level: we want to fail fast if someone
            // requests nesting a record under a parent that does not
            // exist.
            throw new RecordSerialiseException("Could not create destination directory '" + storageDir.getAbsolutePath() + "'");
        }

        File file = getRecordFile(storageDir);
        if (deep && maxPathDepth == depth)
        {
            xrs.serialise(file, record, true);
        }
        else
        {
            xrs.serialise(file, record, false);
            if (deep)
            {
                // Clear out any existing child record directories.
                File[] childDirs = storageDir.listFiles(new SubrecordDirFileFilter());
                for (File childDir : childDirs)
                {
                    try
                    {
                        FileSystemUtils.rmdir(childDir);
                    }
                    catch (IOException e)
                    {
                        LOG.severe(e);
                    }
                }

                for (String key : record.keySet())
                {
                    Object value = record.get(key);
                    if (value instanceof Record)
                    {
                        serialise(PathUtils.getPath(path, key), (MutableRecord) value, deep, depth + 1);
                    }
                }
            }
        }
    }

    private File getStorageDir(String path)
    {
        path = WebUtils.encodeAndJoin(new Predicate<Character>()
        {
            public boolean satisfied(Character character)
            {
                if(StringUtils.isAsciiAlphaNumeric(character))
                {
                    return true;
                }
                else
                {
                    // A few more likely-used characters
                    switch(character)
                    {
                        case ' ':
                        case '-':
                        case '_':
                        case '.':
                            return true;
                    }
                }

                return false;
            }
        }, File.separatorChar, PathUtils.getPathElements(path));
        return new File(baseDirectory, path);
    }

    private File getRecordFile(File dir)
    {
        return new File(dir, "record.xml");
    }

    public MutableRecord deserialise()
    {
        return deserialise("");
    }

    public MutableRecord deserialise(String path)
    {
        File dir = getStorageDir(path);
        if (!dir.isDirectory())
        {
            throw new RecordSerialiseException("No record found at path '" + path + "': directory '" + dir.getAbsolutePath() + "' does not exist");
        }

        return deserialise(dir, "");
    }

    private MutableRecord deserialise(File dir, String path)
    {
        try
        {
            File recordFile = getRecordFile(dir);

            MutableRecord record = xrs.deserialise(recordFile);

            for (File childDir : dir.listFiles(new SubrecordDirFileFilter()))
            {
                String childKey = WebUtils.uriComponentDecode(childDir.getName());
                record.put(childKey, deserialise(childDir, PathUtils.getPath(path, childKey)));
            }
            return record;
        }
        catch (Exception e)
        {
            throw new RecordSerialiseException("Unable to parse record file: " + e.getMessage(), e);
        }
    }

    public void setMaxPathDepth(int depth)
    {
        this.maxPathDepth = depth;
    }

    private class SubrecordDirFileFilter implements FileFilter
    {
        public boolean accept(File pathname)
        {
            return pathname.isDirectory();
        }
    }
}
