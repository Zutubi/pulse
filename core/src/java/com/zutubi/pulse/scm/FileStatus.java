package com.zutubi.pulse.scm;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds status information about a file that has been changed in a working
 * copy.  This status may be propagated to and applied on a different working
 * copy (referred to as the "target").
 */
public class FileStatus
{
    /**
     * If set, specifies how line endings should be translated for the file.
     * Possible values are described in the EOLStyle enum (the property value
     * is just the enum value converted to a string.
     *
     *   - cr    :
     *   - crlf  :
     *   - lf    :
     *   - native:
     *   - text  :
     *
     * @see EOLStyle
     */
    public static final String PROPERTY_EOL_STYLE = "eol";
    /**
     * If set, and the target system supports an executable permission,
     * force the executable permission for the file to be on if this
     * property is "true" and off for any other property value.
     */
    public static final String PROPERTY_EXECUTABLE = "executable";
    private File file;


    public enum EOLStyle
    {
        /**
         * Treat the file as binary: i.e. do no translation.
         */
        BINARY
        {
            public void apply(File file, EOLStyle localEOL) throws IOException
            {
                // Do nothing.
            }
        },
        /**
         * Force carriage return (Old Macintosh style) endings.
         */
        CARRIAGE_RETURN
        {
            public void apply(File file, EOLStyle localEOL) throws IOException
            {
                translateEOLs(file, SystemUtils.CR_BYTES);
            }
        },
        /**
         * Force carriage return linefeed (Windows style) endings.
         */
        CARRIAGE_RETURN_LINEFEED
        {
            public void apply(File file, EOLStyle localEOL) throws IOException
            {
                translateEOLs(file, SystemUtils.CRLF_BYTES);
            }
        },
        /**
         * Force linefeed (Unix style) endings.
         */
        LINEFEED
        {
            public void apply(File file, EOLStyle localEOL) throws IOException
            {
                translateEOLs(file, SystemUtils.LF_BYTES);
            }
        },
        /**
         * Force the file line endings to match the target platform endings.
         */
        NATIVE
        {
            public void apply(File file, EOLStyle localEOL) throws IOException
            {
                translateEOLs(file, SystemUtils.LINE_SEPARATOR_BYTES);
            }
        },
        /**
         * Use the target side configuration for text files to determine what
         * to do (e.g. use Perforce client setting for text files on the
         * target side).
         */
        TEXT
        {
            public void apply(File file, EOLStyle localEOL) throws IOException
            {
                localEOL.apply(file, localEOL);
            }
        };

        public void translateEOLs(File file, byte[] lineSeparatorBytes) throws IOException
        {
            if(file.exists())
            {
                FileSystemUtils.translateEOLs(file, lineSeparatorBytes, true);
            }
        }

        public abstract void apply(File file, EOLStyle localEOL) throws IOException;
    }

    public enum State
    {
        ADDED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return true;
            }
        },
        BRANCHED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
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

            public boolean isInteresting()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        IGNORED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return false;
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

            public boolean isInteresting()
            {
                return true;
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

            public boolean isInteresting()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return true;
            }
        },
        MISSING
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean isInteresting()
            {
                return true;
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

            public boolean isInteresting()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return true;
            }
        },
        METADATA_MODIFIED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        OBSTRUCTED
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean isInteresting()
            {
                return true;
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

            public boolean isInteresting()
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

            public boolean isInteresting()
            {
                return true;
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

            public boolean isInteresting()
            {
                return true;
            }

            public boolean requiresFile()
            {
                return false;
            }
        },
        UNCHANGED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return false;
            }

            public boolean requiresFile()
            {
                return false;
            }
        };

        public abstract boolean isConsistent();
        public abstract boolean isInteresting();
        public abstract boolean requiresFile();
    }

    /**
     * Path of the file relative to the base of the working copy and
     * normalised to use / as a separator.
     */
    private String path;
    private State state;
    private boolean directory;

    /**
     * Path of the file relative to the base directory on the pulse server.  In particular, this is required by
     * cvs to map truncated local working copy paths to the full repository path used on the pulse server.
     *
     * This is also the path into which the file is placed within the zip.  
     */
    private String targetPath;

    /**
     * True iff there is a more recent revision of the file on the server.
     */
    private boolean outOfDate;
    /**
     * Extra file properties, used to carry metadata about the file which
     * may affect how this status is applied.  See the PROPERTY_* constants
     * for available properties and their meanings.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    public FileStatus(String path, State state, boolean directory)
    {
        if(path != null)
        {
            path = FileSystemUtils.normaliseSeparators(path);
        }
        
        this.path = path;
        this.state = state;
        this.directory = directory;
        this.outOfDate = false;
    }

    /**
     * The path into which this file is stored within the zip file (and hence extracted to in the servers
     * working directory). By default, this is the same as the local working copy path.
     *
     * @return
     */
    public String getTargetPath()
    {
        if (targetPath != null)
        {
            return targetPath;
        }
        return path;
    }

    public void setTargetPath(String targetPath)
    {
        if (targetPath != null)
        {
            this.targetPath = FileSystemUtils.normaliseSeparators(targetPath);
        }
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public void setDirectory(boolean directory)
    {
        this.directory = directory;
    }

    public boolean isOutOfDate()
    {
        return outOfDate;
    }

    public void setOutOfDate(boolean outOfDate)
    {
        this.outOfDate = outOfDate;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    public String getProperty(String name)
    {
        return properties.get(name);    
    }

    public void setProperty(String name, String value)
    {
        properties.put(name, value);
    }

    public boolean isInteresting()
    {
        return isOutOfDate() || state.isInteresting() || properties.size() > 0;
    }

    public void preApply(File base) throws IOException
    {
        // TODO dev-personal: better to ensure things are deleted, but this
        // requires ordering or normalisation or something...
        if(state == State.DELETED || state == State.REPLACED)
        {
            File f = new File(base, getTargetPath());
            if(directory)
            {
                FileSystemUtils.rmdir(f);
            }
            else
            {
                f.delete();
            }
        }
    }

    public void postApply(File base, EOLStyle localEOL) throws IOException
    {
        file = new File(base, getTargetPath());

        // Apply line ending settings
        String eolName = properties.get(PROPERTY_EOL_STYLE);
        if(eolName != null)
        {
            try
            {
                EOLStyle eol = EOLStyle.valueOf(eolName);
                eol.apply(file, localEOL);
            }
            catch (IllegalArgumentException e)
            {
                // Just ignore it values we don't support.
            }
        }

        // Handle the executable bit
        String executableValue = properties.get(PROPERTY_EXECUTABLE);
        if(executableValue != null)
        {
            boolean executable = Boolean.parseBoolean(executableValue);
            if(file.exists())
            {
                FileSystemUtils.setExecutable(file, executable);
            }
        }
    }

    public String toString()
    {
        return String.format("%-12s %s %s", state.toString(), outOfDate ? "*" : " ", path);
    }
}
