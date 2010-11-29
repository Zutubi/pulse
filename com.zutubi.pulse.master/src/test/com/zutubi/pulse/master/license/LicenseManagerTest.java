package com.zutubi.pulse.master.license;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.license.events.LicenseEventListener;
import com.zutubi.util.junit.ZutubiTestCase;

public class LicenseManagerTest extends ZutubiTestCase
{
    private static final Messages I18N = Messages.getInstance(LicenseManager.class);
    
    private LicenseManager licenseManager;
    private EventManager eventManager;
    private InMemoryLicenseKeyStore licenseKeyStore;

    protected void setUp() throws Exception
    {
        super.setUp();

        licenseManager = new LicenseManager();
        eventManager = new DefaultEventManager();
        licenseKeyStore = new InMemoryLicenseKeyStore();
        
        licenseManager.setEventManager(eventManager);
        licenseManager.setLicenseKeyStore(licenseKeyStore);
        licenseManager.init();
    }

    @Override
    protected void tearDown() throws Exception
    {
        LicenseHolder.setLicense(null);
        
        super.tearDown();
    }

    public void testInvalidLicenseKey()
    {
        try
        {
            licenseManager.installLicense("");
            fail("Invalid license key should trigger an exception.");
        }
        catch (LicenseException e)
        {
            assertEquals(I18N.format("invalid.key"), e.getMessage());
        }
    }

    public void testInstallLicense()
    {
        TestLicenseListener listener = new TestLicenseListener();
        eventManager.register(listener);

        LicenseEncoder encoder = new LicenseEncoder();
        String licenseKey = new String(encoder.encode(new License(LicenseType.EVALUATION, "holder")));
        licenseManager.installLicense(licenseKey);

        assertTrue(listener.licenseUpdated);
    }

    public void testLicenseHolderInSync()
    {
        assertNull(LicenseHolder.getLicense());

        LicenseEncoder encoder = new LicenseEncoder();
        License license = new License(LicenseType.EVALUATION, "holder");
        
        String licenseKey = new String(encoder.encode(license));
        licenseManager.installLicense(licenseKey);

        assertNotNull(LicenseHolder.getLicense());
        assertEquals("holder", LicenseHolder.getLicense().getHolder());
        assertEquals(LicenseType.EVALUATION, LicenseHolder.getLicense().getType());
    }

    private static class TestLicenseListener extends LicenseEventListener
    {
        boolean licenseUpdated = false;

        @Override
        public void licenseUpdated()
        {
            licenseUpdated = true;
        }
    }

    private static class InMemoryLicenseKeyStore extends AbstractLicenseKeyStore
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
