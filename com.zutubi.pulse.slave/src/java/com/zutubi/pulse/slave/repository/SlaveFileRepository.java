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

package com.zutubi.pulse.slave.repository;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.servercore.repository.FileRepository;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 */
public class SlaveFileRepository implements FileRepository
{
    private File recipeDir;
    private String masterUrl;
    private ServiceTokenManager serviceTokenManager;

    public SlaveFileRepository(File recipeDir, String masterUrl, ServiceTokenManager serviceTokenManager)
    {
        this.recipeDir = recipeDir;
        this.masterUrl = masterUrl;
        this.serviceTokenManager = serviceTokenManager;
    }

    public File getPatchFile(long userId, long number) throws PulseException
    {
        if(!recipeDir.isDirectory())
        {
            recipeDir.mkdirs();
        }

        try
        {
            URL patchUrl = new URL(masterUrl +  "/patch?token=" + serviceTokenManager.getToken() + "&user=" + userId + "&number=" + number);
            File patchFile = new File(recipeDir, "patch.zip");

            IOUtils.downloadFile(patchUrl, patchFile);

            return patchFile;
        }
        catch (IOException e)
        {
            throw new PulseException("Error downloading patch from master: " + e.getMessage(), e);
        }
    }
}
