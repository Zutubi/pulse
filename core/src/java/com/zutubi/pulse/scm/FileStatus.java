package com.zutubi.pulse.scm;

import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 */
public class FileStatus
{
    public enum State
    {
        ADDED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return true;
            }
        },
        DELETED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        INCOMPLETE
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        MERGED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        MISSING
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        MODIFIED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return true;
            }
        },
        OBSTRUCTED
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        REPLACED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return true;
            }
        },
        UNRESOLVED
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        UNSUPPORTED
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean requiresFile()
            {
                return false;
            }
        };

        public abstract boolean isConsistent();
        public abstract boolean requiresFile();
    }

    /**
     * Path of the file relative to the base of the working copy and
     * normalised to use / as a separator.
     */
    private String path;
    private State state;
    private boolean directory;

    public FileStatus(String path, State state, boolean directory)
    {
        this.path = FileSystemUtils.normaliseSeparators(path);
        this.state = state;
        this.directory = directory;
    }

    public String getPath()
    {
        return path;
    }

    public State getState()
    {
        return state;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public void apply(File base) throws IOException
    {
        // TODO dev-personal: better to ensure things are deleted, but this
        // requires ordering or normalisation or something...
        if(state == State.DELETED || state == State.REPLACED)
        {
            File f = new File(base, path);
            if(directory)
            {
                FileSystemUtils.removeDirectory(f);
            }
            else
            {
                f.delete();
            }
        }
    }
}
