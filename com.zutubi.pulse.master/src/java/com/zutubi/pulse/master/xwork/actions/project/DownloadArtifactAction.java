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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.webwork.Urls;

/**
 * An action to download a raw artifact.  Actually just checks that a known
 * artifact is specified, and if so redirects to the file/ URL so the artifact
 * is served by Jetty.
 */
public class DownloadArtifactAction extends FileArtifactActionBase
{
    private String url;

    public String getUrl()
    {
        return url;
    }

    public String execute()
    {
        url = Urls.getBaselessInstance().fileFileArtifact(getRequiredArtifact(), getRequiredFileArtifact());
        return SUCCESS;
    }
}