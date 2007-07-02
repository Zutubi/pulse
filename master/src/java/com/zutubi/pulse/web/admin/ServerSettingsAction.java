package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.web.ActionSupport;

/**
 * @deprecated
 */
public class ServerSettingsAction extends ActionSupport
{

    public static class LicenseRestriction
    {
        private String entity;
        private int supported;
        private int inUse;

        public LicenseRestriction(String entity, int supported, int inUse)
        {
            this.entity = entity;
            this.supported = supported;
            this.inUse = inUse;
        }

        public String getEntity()
        {
            return entity;
        }

        public int getSupported()
        {
            return supported;
        }

        public int getInUse()
        {
            return inUse;
        }
    }
}
