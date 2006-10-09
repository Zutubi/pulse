package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.personal.PersonalBuildSupport;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.WorkingCopy;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import static com.zutubi.pulse.scm.p4.P4Constants.*;

import java.util.Properties;

/**
 */
public class P4WorkingCopy extends PersonalBuildSupport implements WorkingCopy
{
    private P4Client client;

    public P4WorkingCopy()
    {
        this.client = new P4Client();
    }

    public boolean matchesRepository(Properties repositoryDetails) throws SCMException
    {
        String port = (String) repositoryDetails.get(PROPERTY_PORT);
        if (port != null)
        {
            // $ p4 set
            // P4EDITOR=C:\WINDOWS\System32\notepad.exe (set)
            // P4JOURNAL=journal (set -s)
            // P4LOG=log (set -s)
            // P4PORT=10.0.0.3:1666
            // P4ROOT=C:\Program Files\Perforce (set -s)
            // P4USER=Jason (set)
            P4Client.P4Result result = client.runP4(null, P4_COMMAND, COMMAND_SET);
            String[] lines = client.splitLines(result);
            for(String line: lines)
            {
                int index = line.indexOf('=');
                if(index > 0 && index < line.length() - 1)
                {
                    String key = line.substring(0, index);
                    if(key.equals(ENV_PORT))
                    {
                        String value = line.substring(index + 1);
                        value = value.split(" ")[0];

                        if(!value.equals(port))
                        {
                            warning("P4PORT setting '" + value + "' does not match Pulse project's P4PORT '" + port + "'");
                            return false;
                        }
                    }
                }
            }
        }

        // TODO: check the client mapping?  This is difficult...many false positives methinks
        
        return true;
    }

    public WorkingCopyStatus getStatus() throws SCMException
    {
        WorkingCopyStatus status;
        NumericalRevision revision;
        NumericalRevision checkRevision;

        // A little strange, perhaps.  We first get the latest revision, then
        // run an fstat.  Unfortunately, restricting the fstat to the
        // revision prevents some required things being reported (e.g. files
        // that are open for add).  Instead, we double-check the revision
        // after the fstat.  In the unlikely event that it has changed, we
        // just go again.
        do
        {
            revision = client.getLatestRevisionForFiles(null);
            status = new WorkingCopyStatus(revision);
            P4FStatHandler handler = new P4FStatHandler(getUi(), status);
            client.runP4WithHandler(handler, null, P4_COMMAND, COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, "//...");

            checkRevision = client.getLatestRevisionForFiles(null);
        } while (!checkRevision.equals(revision));

        return status;
    }

    public void update() throws SCMException
    {
        client.runP4(false, null, P4_COMMAND, COMMAND_SYNC);
        // Post sync files my be unresolved.  Use CVS/Subversion style
        // automatic merging to try and resolve such files.
        client.runP4(false, null, P4_COMMAND, COMMAND_RESOLVE, FLAG_AUTO_MERGE);
    }

    P4Client getClient()
    {
        return client;
    }
}
