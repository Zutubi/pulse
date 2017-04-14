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

package com.zutubi.pulse.master.logging;

import com.zutubi.pulse.master.bootstrap.MasterLogConfiguration;
import com.zutubi.pulse.servercore.util.logging.LogConfigurationManager;

/**
 * Extension of the shared log configuration manager that handles master-
 * specific details.
 */
public class MasterLogConfigurationManager extends LogConfigurationManager
{
    @Override
    public void applyConfig()
    {
        super.applyConfig();
        MasterLogConfiguration logConfig = (MasterLogConfiguration) getLogConfig();
        setLoggingEnabled(MasterLoggers.getConfigAuditLogger(), logConfig.isConfigAuditLoggingEnabled());        
    }
}
