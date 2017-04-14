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

package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 * Resolves actions for a build result.
 */
public class BuildActionResolver extends StaticMapActionResolver
{
    public BuildActionResolver(String id)
    {
        super("viewBuildSummary");

        addMapping("summary", new BuildSummaryActionResolver());
        addMapping("details", new BuildDetailsActionResolver());
        addMapping("logs", new BuildLogsActionResolver());
        addMapping("changes", new BuildChangesActionResolver());
        addMapping("tests", new BuildTestsActionResolver());
        addMapping("file", new BuildPulseFileActionResolver());
        addMapping("artifacts", new BuildArtifactsActionResolver());
        addMapping("downloads", new BuildDownloadsActionResolver());

        addParameter("buildVID", id);
    }
}
