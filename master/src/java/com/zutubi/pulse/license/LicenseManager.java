package com.zutubi.pulse.license;

import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.license.authorisation.*;

import java.io.IOException;

/**
 *
 *
 */
public class LicenseManager implements LicenseProvider
{
    private DataResolver resolver;

    private ObjectFactory objectFactory;

    public License getLicense()
    {
        return resolver.getData().getLicense();
    }

    public LicenseAuthorisation getAuthorisation() throws LicenseException
    {
        try
        {
            License license = this.getLicense();
            if (license == null)
            {
                return objectFactory.buildBean(NoAuthorisation.class);
            }

            if (license.getType() == LicenseType.EVALUATION)
            {
                return objectFactory.buildBean(EvaluationLicenseAuthorisation.class);
            }
            else if (license.getType() == LicenseType.COMMERCIAL)
            {
                return objectFactory.buildBean(CommercialLicenseAuthorisation.class);
            }
            else if (license.getType() == LicenseType.NON_PROFIT)
            {
                return objectFactory.buildBean(CustomLicenseAuthorisation.class);
            }
            else if (license.getType() == LicenseType.PERSONAL)
            {
                return objectFactory.buildBean(CustomLicenseAuthorisation.class);
            }

            return objectFactory.buildBean(NoAuthorisation.class);
        }
        catch (Exception e)
        {
            throw new LicenseException(e);
        }
    }

    public boolean isLicensed() throws LicenseException
    {
        return getAuthorisation().canRunPulse();
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

    /**
     * Required resource.
     *
     * @param objectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
