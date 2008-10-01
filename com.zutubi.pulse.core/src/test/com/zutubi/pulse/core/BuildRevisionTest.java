package com.zutubi.pulse.core;

import com.zutubi.pulse.core.test.PulseTestCase;

public class BuildRevisionTest extends PulseTestCase
{
    public void testLockUnlock()
    {
        BuildRevision buildRevision = new BuildRevision();
        assertFalse(buildRevision.isLocked());
        buildRevision.lock();
        assertTrue(buildRevision.isLocked());
        buildRevision.unlock();
        assertFalse(buildRevision.isLocked());
    }
    
    public void testUnlockAlreadyUnlocked()
    {
        BuildRevision buildRevision = new BuildRevision();
        try
        {
            buildRevision.unlock();
            fail("Should not be able to unlock an unlocked revision");
        }
        catch (IllegalMonitorStateException e)
        {
        }

        // Now check that if we lock it it looks locked: i.e. the internal
        // state was not affected by our bogus unlock above.
        buildRevision.lock();
        assertTrue(buildRevision.isLocked());
    }

    public void testLockAlreadyLocked()
    {
        // Locks are reentrant, check isLocked in this case.
        BuildRevision buildRevision = new BuildRevision();
        buildRevision.lock();
        assertTrue(buildRevision.isLocked());
        buildRevision.lock();
        assertTrue(buildRevision.isLocked());
        buildRevision.unlock();
        assertTrue(buildRevision.isLocked());
        buildRevision.unlock();
        assertFalse(buildRevision.isLocked());
    }

}
