package com.zutubi.pulse.master.xwork.actions.upgrade;

import com.zutubi.pulse.master.upgrade.UpgradeManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper base for actions that implement the upgrade UI.
 */
public class UpgradeActionSupport extends ActionSupport
{
    private static final Set<String> RUN_TOKENS = new HashSet<String>();

    protected UpgradeManager upgradeManager;

    /**
     * Runs teh given task, ensuring it is only ever run once in this invocation of the JVM.
     *
     * @param r the task to run
     * @param token a unique name for this task to identify if it has run before
     */
    protected void runOnce(Runnable r, String token)
    {
        boolean run = false;

        synchronized (RUN_TOKENS)
        {
            if (!RUN_TOKENS.contains(token))
            {
                RUN_TOKENS.add(token);
                run = true;
            }
        }

        if (run)
        {
            r.run();
        }
    }

    public void setUpgradeManager(UpgradeManager upgradeManager)
    {
        this.upgradeManager = upgradeManager;
    }
}
