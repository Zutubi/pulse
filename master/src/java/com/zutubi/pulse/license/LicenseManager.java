package com.zutubi.pulse.license;

import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.license.authorisation.CommercialLicenseAuthorisation;
import com.zutubi.pulse.license.authorisation.EvaluationLicenseAuthorisation;
import com.zutubi.pulse.license.authorisation.NonProfitLicenseAuthorisation;
import com.zutubi.pulse.license.authorisation.PersonalLicenseAuthorisation;

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

    public LicenseAuthorisation getAuthorisation() throws LicenseException
    {
        try
        {
            License license = resolver.getData().getLicense();
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
                return objectFactory.buildBean(NonProfitLicenseAuthorisation.class);
            }
            else if (license.getType() == LicenseType.PERSONAL)
            {
                return objectFactory.buildBean(PersonalLicenseAuthorisation.class);
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
        LicenseAuthorisation e = getAuthorisation();
        if (e != null)
        {
            return e.canRunPulse();
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
