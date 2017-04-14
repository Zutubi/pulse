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

package com.zutubi.pulse.master.notifications.renderer;

import com.zutubi.pulse.master.model.BuildResult;

import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * A BuildResultRenderer converts a build model into a displayable form, based
 * on a specified template.
 *
 * @author jsankey
 */
public interface BuildResultRenderer
{
    void render(BuildResult result, Map<String, Object> dataMap, String templateName, Writer writer);
    boolean hasTemplate(String template, boolean personal);
    List<TemplateInfo> getAvailableTemplates(boolean personal);
    TemplateInfo getTemplateInfo(String templateName, boolean personal);
}