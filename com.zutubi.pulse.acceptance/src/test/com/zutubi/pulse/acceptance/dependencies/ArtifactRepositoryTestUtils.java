package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.pulse.acceptance.AcceptanceTestUtils;

import java.io.IOException;
import java.io.File;

public class ArtifactRepositoryTestUtils
{
    public static boolean waitUntilInRepository(String path, int timeout) throws IOException
    {
        File artifact = new File(getArtifactRepository(), path);
        System.out.println("Waiting for: " + artifact.getCanonicalPath());
        try
        {
            long startTime = System.currentTimeMillis();
            while (!artifact.exists() && System.currentTimeMillis() - startTime < timeout)
            {
                Thread.sleep(200);
            }
        }
        catch (InterruptedException e)
        {
            // noop.            
        }
        System.out.println("Artifact located: " + artifact.exists());
        if (!artifact.exists())
        {
            list(getArtifactRepository());
        }
        return artifact.exists();
    }

    private static void list(File file) throws IOException
    {
        System.out.println(file.getCanonicalPath() + ": " + file.exists());
        if (file.isDirectory())
        {
            for (File listing : file.listFiles())
            {
                list(listing);
            }
        }
    }

    public static boolean isInArtifactRepository(String path) throws IOException
    {
        return new File(getArtifactRepository(), path).exists();
    }

    public static boolean isNotInArtifactRepository(String path) throws IOException
    {
        return !new File(getArtifactRepository(), path).exists();
    }

    public static void clearArtifactRepository() throws IOException
    {
        File repository = getArtifactRepository();
        if (repository.isDirectory())
        {
            if (!FileSystemUtils.rmdir(repository))
            {
                throw new IOException("Failed to remove dir: " + repository.getCanonicalPath());
            }
        }
        if (!repository.mkdirs())
        {
            throw new IOException("Failed to create dir: " + repository.getCanonicalPath());
        }
    }

    public static void createArtifactFile(String path) throws IOException
    {
        File file = new File(getArtifactRepository(), path);
        File parentFile = file.getParentFile();
        if (!parentFile.mkdirs())
        {
            throw new IOException("Failed to create directory: " + parentFile.getCanonicalPath());
        }
        if (!file.createNewFile())
        {
            throw new IOException("Failed to create file: " + file.getCanonicalPath());
        }
    }

    public static File getArtifactRepository() throws IOException
    {
        return new File(AcceptanceTestUtils.getDataDirectory(), "repository");
    }
}
