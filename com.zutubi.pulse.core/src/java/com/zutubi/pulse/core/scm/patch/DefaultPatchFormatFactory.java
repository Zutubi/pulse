package com.zutubi.pulse.core.scm.patch;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.util.Pair;
import com.zutubi.util.bean.ObjectFactory;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link PatchFormatFactory}.
 */
public class DefaultPatchFormatFactory implements PatchFormatFactory
{
    public static final String FORMAT_STANDARD = "standard";

    private Map<String, Class<? extends PatchFormat>> formatMapping = new HashMap<String, Class<? extends PatchFormat>>();
    private Map<String, String> scmMapping = new HashMap<String, String>();

    private ObjectFactory objectFactory;

    public DefaultPatchFormatFactory()
    {
        registerFormatType(FORMAT_STANDARD, StandardPatchFormat.class);
    }

    public Class<? extends PatchFormat> registerFormatType(String name, Class<? extends PatchFormat> clazz)
    {
        return formatMapping.put(name, clazz);
    }

    public Class<? extends PatchFormat> unregisterFormatType(String name)
    {
        return formatMapping.remove(name);
    }

    public String registerScm(String scmName, String patchFormat)
    {
        return scmMapping.put(scmName, patchFormat);
    }

    public boolean isValidFormatType(String formatType)
    {
        return formatMapping.containsKey(formatType);
    }

    public List<String> getFormatTypes()
    {
        return new LinkedList<String>(formatMapping.keySet());
    }

    public Pair<String, PatchFormat> createByScmType(String scmType)
    {
        String formatType = scmMapping.get(scmType);
        if (formatType == null)
        {
            return null;
        }

        return new Pair<String, PatchFormat>(formatType, createByFormatType(formatType));
    }

    public PatchFormat createByFormatType(String formatType)
    {
        Class<? extends PatchFormat> clazz = formatMapping.get(formatType);
        if (clazz == null)
        {
            return null;
        }

        try
        {
            return objectFactory.buildBean(clazz);
        }
        catch (Exception e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    public String guessFormatType(File patchFile)
    {
        String guess = null;
        for (String formatType: formatMapping.keySet())
        {
            PatchFormat format = createByFormatType(formatType);
            if (format.isPatchFile(patchFile))
            {
                if (guess == null)
                {
                    guess = formatType;
                }
                else
                {
                    // Ambiguous, bail out.
                    return null;
                }
            }
        }

        return guess;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
