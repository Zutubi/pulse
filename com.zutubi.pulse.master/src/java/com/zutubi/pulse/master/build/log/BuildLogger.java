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

package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.master.model.BuildResult;
import org.apache.ivy.util.MessageLogger;

public interface BuildLogger extends HookLogger
{
    void preamble(BuildResult build);
    
    void preBuild();
    void preBuildCompleted();

    void commenced(BuildResult build);

    void status(String message);

    void completed(BuildResult build);

    void postBuild();
    void postBuildCompleted();

    /**
     * Called prior to Ivy dependency resolution.
     */
    void preIvyResolve();
    /**
     * Called after Ivy dependency resolution.
     *
     * @param errors all problem messages reported by Ivy, empty on successful resolve
     */
    void postIvyResolve(String... errors);

    void preIvyPublish();
    void postIvyPublish(String... errors);

    MessageLogger getMessageLogger();
}
