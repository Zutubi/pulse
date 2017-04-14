/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.type.record.DefaultRecordSerialiser;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipInputStream;

/**
 * Abstract base for upgrade tasks that load records from a pre-canned archive
 * to insert them into the persistent config store.
 */
public abstract class AbstractPredefinedRecordsUpgradeTask extends AbstractUpgradeTask
{
    protected SystemPaths systemPaths;
    protected RecordManager recordManager;

    public void execute() throws TaskException
    {
        File tempDir = new File(systemPaths.getTmpRoot(), getClass().getName());
        if (tempDir.exists())
        {
            try
            {
                FileSystemUtils.rmdir(tempDir);
            }
            catch (IOException e)
            {
                throw new TaskException("Unable to create clean up old temp directory: " + e.getMessage(), e);
            }
        }

        if (!tempDir.mkdirs())
        {
            throw new TaskException("Unable to create temp directory '" + tempDir.getAbsolutePath() + "'");
        }

        try
        {
            execute(tempDir);
        }
        finally
        {
            try
            {
                FileSystemUtils.rmdir(tempDir);
            }
            catch (IOException e)
            {
                // Ignore.
            }
        }
    }

    protected abstract void execute(File tempDir) throws TaskException;

    protected MutableRecord loadRecords(File tempDir, String name) throws TaskException
    {
        ZipInputStream stream = null;
        try
        {
            Class<? extends AbstractPredefinedRecordsUpgradeTask> clazz = getClass();
            stream = new ZipInputStream(clazz.getResourceAsStream(clazz.getSimpleName() + "." + name + ".zip"));
            ZipUtils.extractZip(stream, tempDir);
            DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(tempDir);
            return serialiser.deserialise();
        }
        catch (IOException e)
        {
            throw new TaskException(e);
        }
        finally
        {
            IOUtils.close(stream);
        }
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}

