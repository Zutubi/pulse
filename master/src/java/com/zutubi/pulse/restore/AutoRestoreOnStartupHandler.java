package com.zutubi.pulse.restore;

import java.io.File;

/**
 * Handler that will trigger an automatic restore from a backup when Pulse starts up.
 *
 * The restore is triggered by the existance of a backup file located at PULSE_DATA/restore/archive.zip
 */
public class AutoRestoreOnStartupHandler
{
    private File pulseDataDirectory;

    
}
