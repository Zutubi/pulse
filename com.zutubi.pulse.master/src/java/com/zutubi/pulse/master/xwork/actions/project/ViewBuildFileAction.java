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

import com.uwyn.jhighlight.renderer.XmlXhtmlRenderer;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * An action to show the build file used for a build.  The file is
 * highlighted beautifully for the benefit of the user.
 */
public class ViewBuildFileAction extends BuildActionBase
{
    private String highlightedFile;
    private MasterConfigurationManager configurationManager;

    public String getHighlightedFile()
    {
        return highlightedFile;
    }

    public String execute()
    {
        BuildResult result = getRequiredBuildResult();
        FileInputStream is = null;
        ByteArrayOutputStream os = null;

        try
        {
            is = new FileInputStream(new File(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), RecipeProcessor.PULSE_FILE));
            os = new ByteArrayOutputStream();
            XmlXhtmlRenderer renderer = new XmlXhtmlRenderer();
            renderer.highlight(RecipeProcessor.PULSE_FILE, is, os, null, true);
            highlightedFile = os.toString();
        }
        catch (IOException e)
        {
            // Ignore.
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
