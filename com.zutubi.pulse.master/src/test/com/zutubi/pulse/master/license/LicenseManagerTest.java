package com.zutubi.pulse.master.license;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.util.junit.ZutubiTestCase;

/**
 *
 *
 */
public class LicenseManagerTest extends ZutubiTestCase
{
    private LicenseManager licenseManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        licenseManager = new LicenseManager();
        licenseManager.setEventManager(new DefaultEventManager());
        licenseManager.setLicenseKeyStore(new DefaultLicenseKeyStore());
    }

    protected void tearDown() throws Exception
    {
        licenseManager = null;

        super.tearDown();
    }

    public void test()
    {
        
    }

    private static class DefaultLicenseKeyStore extends AbstractLicenseKeyStore
    {
        private String key;

        public String getKey()
        {
            return key;
        }

        public void setKey(String licenseKey) throws LicenseException
        {
            key = licenseKey;
            notifyListeners();
        }
    }
}
