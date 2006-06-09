package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

/**
 */
public class PostProcessorManager
{
    private static final String DISPLAY_KEY = "display";

    private static final Logger LOG = Logger.getLogger(PostProcessorManager.class);

    private MasterConfigurationManager configurationManager;

    public Map<String, String> getAvailableProcessors()
    {
        Map<String, String> result = new TreeMap<String, String>();

        SystemPaths systemPaths = configurationManager.getSystemPaths();
        for(File templateRoot: systemPaths.getTemplateRoots())
        {
            File processorsDir = new File(templateRoot, FileSystemUtils.composeFilename("pulse-file", "post-processors"));
            String[] list = processorsDir.list();
            if(list != null)
            {
                for (String name : list)
                {
                    if(name.endsWith(".vm"))
                    {
                        processFile(new File(processorsDir, name), result);
                    }
                }
            }
        }

        return result;
    }

    private void processFile(File file, Map<String, String> processors)
    {
        if(file.isFile())
        {
            String name = file.getName().substring(0, file.getName().length() - 3);
            String display = name;

            String path = file.getAbsolutePath();
            File propertiesFile = new File(path.substring(0, path.length() - 2) + "properties");

            if(propertiesFile.isFile())
            {
                FileInputStream in = null;
                try
                {
                    in = new FileInputStream(propertiesFile);
                    Properties properties = new Properties();
                    properties.load(in);
                    if(properties.containsKey(DISPLAY_KEY))
                    {
                        display = (String) properties.get(DISPLAY_KEY);
                    }
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to load properties from '" + propertiesFile.getAbsolutePath() + "': " + e.getMessage());
                }
                finally
                {
                    IOUtils.close(in);
                }
            }

            processors.put(name, display);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
