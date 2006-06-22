package com.zutubi.pulse.license;

import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.core.ObjectFactory;

import java.io.IOException;

/**
 *
 *
 */
public class LicenseManager
{
    private DataResolver resolver;

    private ObjectFactory objectFactory;

    public License getLicense()
    {
        return resolver.getData().getLicense();
    }

    public LicenseEnforcer getEnforcer() throws LicenseException
    {
        try
        {
            License license = resolver.getData().getLicense();
            if (license.getType() == LicenseType.EVALUATION)
            {
                return objectFactory.buildBean(EvaluationLicenseEnforcer.class);
            }
            else if (license.getType() == LicenseType.COMMERCIAL)
            {
                return objectFactory.buildBean(CommercialLicenseEnforcer.class);
            }
            else if (license.getType() == LicenseType.NON_PROFIT)
            {
                return null;
            }
            else if (license.getType() == LicenseType.PERSONAL)
            {
                return null;
            }
            return null;
        }
        catch (Exception e)
        {
            throw new LicenseException(e);
        }
    }

    public boolean isLicensed() throws LicenseException
    {
        LicenseEnforcer e = getEnforcer();
        if (e != null)
        {
            return e.isLicensed();
        }
        return false;
    }

    public void updateLicenseKey(String newKey) throws LicenseException
    {
        try
        {
            resolver.getData().updateLicenseKey(newKey);
        }
        catch (IOException e)
        {
            throw new LicenseException("Failed to update license key. Cause: " + e.getClass().getName() +
                    "; " + e.getMessage(), e);
        }
    }

    /**
     * Required resource.
     *
     * @param resolver
     */
    public void setResolver(DataResolver resolver)
    {
        this.resolver = resolver;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
