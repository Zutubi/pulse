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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.TempFileInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * An action to download the pulse file used in a specific build.
 */
public class DownloadDirectoryArtifactAction extends ProjectActionSupport
{
    private long commandId;
    private long id;
    private InputStream inputStream;
    private long contentLength;
    private String filename;
    private MasterConfigurationManager configurationManager;

    public void setCommandId(long commandId)
    {
        this.commandId = commandId;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return "application/zip";
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public String getFilename()
    {
        return filename;
    }

    public String execute()
    {
        CommandResult command = getBuildManager().getCommandResult(commandId);
        if(command == null)
        {
            addActionError("Unknown command [" + id + "]");
            return ERROR;
        }

        StoredArtifact artifact = getBuildManager().getArtifact(id);
        if(artifact == null)
        {
            addActionError("Unknown artifact [" + id + "]");
            return ERROR;
        }

        File outputDir = command.getAbsoluteOutputDir(configurationManager.getDataDirectory());
        File artifactDir = new File(outputDir, artifact.getName());

        if(!artifactDir.isDirectory())
        {
            addActionError("Artifact directory '" + artifactDir.getAbsolutePath() + "' does not exist.");
            return ERROR;
        }

        File tmpRoot = configurationManager.getSystemPaths().getTmpRoot();
        if(!tmpRoot.exists())
        {
            tmpRoot.mkdirs();
        }

        File temp = new File(tmpRoot, RandomUtils.insecureRandomString(5) + id + ".zip");

        try
        {
            PulseZipUtils.createZip(temp, outputDir, artifact.getName());
            contentLength = temp.length();
            filename = artifact.getName() + ".zip";
            inputStream = new TempFileInputStream(temp);
        }
        catch(IOException e)
        {
            addActionError("I/O error zipping directory artifact: " + e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
