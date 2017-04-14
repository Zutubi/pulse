/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions.admin.debug;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.health.ConfigurationHealthChecker;
import com.zutubi.tove.config.health.ConfigurationHealthReport;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;

/**
 * A simple admin command that triggers the configuration health checker.
 */
public class ConfigurationCheckAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(ConfigurationCheckAction.class);

    private ConfigurationHealthChecker configurationHealthChecker;

    @Override
    public String execute() throws Exception
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);
        ConfigurationHealthReport report = configurationHealthChecker.checkAll();
        if (report.isHealthy())
        {
            LOG.info("Configuration check: All clear.");
        }
        else
        {
            LOG.warning("Configuration check: Errors detected.");
            LOG.warning(report.toString());
        }

        return SUCCESS;
    }

    public String healAll() throws Exception
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);
        ConfigurationHealthReport report = configurationHealthChecker.healAll();
        if (report.isHealthy())
        {
            LOG.info("Configuration heal: All clear.");
        }
        else
        {
            LOG.warning("Configuration heal: Errors detected.");
            LOG.warning(report.toString());
        }

        return SUCCESS;
    }

    public void setConfigurationHealthChecker(ConfigurationHealthChecker configurationHealthChecker)
    {
        this.configurationHealthChecker = configurationHealthChecker;
    }
}
