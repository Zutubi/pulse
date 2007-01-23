package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.bootstrap.UserPaths;
import com.zutubi.pulse.prototype.Scope;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;

/**
 */
public class DefaultRecordManager implements RecordManager
{
    // Records are stored in the user config root (i.e. PULSE_DATA/config/<scope>)
    private File configRoot;

    public Record load(Scope scope, String path)
    {
        File recordDir = getRecordDir(scope, path);
        return null;
    }

    public void store(Record record)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void delete(Record record)
    {
        throw new RuntimeException("Method not implemented.");
    }

    private File getRecordDir(Scope scope, String path)
    {
        File scopeRoot = new File(configRoot, FileSystemUtils.composeFilename(scope.getPath()));
        return new File(scopeRoot, FileSystemUtils.denormaliseSeparators(path));
    }

    public void setUserPaths(UserPaths userPaths)
    {
        configRoot = userPaths.getUserConfigRoot();
    }
}
