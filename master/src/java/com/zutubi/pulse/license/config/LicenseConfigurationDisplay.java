package com.zutubi.pulse.license.config;

import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.License;

/**
 *
 *
 */
public class LicenseConfigurationDisplay
{
    public String getName()
    {
        return LicenseHolder.getLicense().getHolder();
    }

    public String getType()
    {
        return LicenseHolder.getLicense().getType().toString();
    }

    public String getExpiry()
    {
        return LicenseHolder.getLicense().getExpiryDate().toString();
    }

    public String getStatus()
    {
        License license = LicenseHolder.getLicense();
        if (license.isExpired())
        {
            return "expired";
        }
        return "active";
    }

    public String getSupportedUsers()
    {
        License license = LicenseHolder.getLicense();
        if (license.getSupportedUsers() == License.UNRESTRICTED)
        {
            return "unrestricted";
        }
        return String.valueOf(license.getSupportedUsers());
    }
    
    public String getSupportedProjects()
    {
        License license = LicenseHolder.getLicense();
        if (license.getSupportedProjects() == License.UNRESTRICTED)
        {
            return "unrestricted";
        }
        return String.valueOf(license.getSupportedProjects());
    }

    public String getSupportedAgents()
    {
        License license = LicenseHolder.getLicense();
        if (license.getSupportedAgents() == License.UNRESTRICTED)
        {
            return "unrestricted";
        }
        return String.valueOf(license.getSupportedAgents());
    }
}
