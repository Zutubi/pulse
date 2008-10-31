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
