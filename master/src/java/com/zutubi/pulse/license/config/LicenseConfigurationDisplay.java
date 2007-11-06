package com.zutubi.pulse.license.config;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        return LicenseHolder.getLicense().getType().name().toLowerCase().replace("_", " ");
    }

    public String getExpiry()
    {
        License license = LicenseHolder.getLicense();
        Date expiryDate = license.getExpiryDate();

        if (expiryDate != null)
        {
            DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");

            Calendar cal = Calendar.getInstance();
            cal.setTime(expiryDate);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return dateFormatter.format(cal.getTime());
        }
        
        Messages messages = Messages.getInstance(LicenseConfiguration.class);
        return messages.format("no.expiry");
    }

    public String getStatus()
    {
        License license = LicenseHolder.getLicense();
        if (license.isExpired())
        {
            Messages messages = Messages.getInstance(LicenseConfiguration.class);
            return messages.format(license.isEvaluation() ? "license.expired" : "license.support.expired");
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
