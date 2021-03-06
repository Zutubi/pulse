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

package com.zutubi.pulse.core.scm.config;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.annotations.Transient;

/**
 * SCM configuration used purely for testing.
 */
public class TestScmConfiguration extends ScmConfiguration
{
    @Transient
    public String getType()
    {
        return "test";
    }

    @Override
    public String getSummary()
    {
        return "test summary";
    }

    public String getPreviousRevision(String revision)
    {
        return null;
    }
}
