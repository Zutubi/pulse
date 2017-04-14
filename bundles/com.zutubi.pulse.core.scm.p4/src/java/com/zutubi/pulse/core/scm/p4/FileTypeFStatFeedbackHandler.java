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

package com.zutubi.pulse.core.scm.p4;

/**
 * A handler for p4 fstat output which determines the type of a file.
 */
public class FileTypeFStatFeedbackHandler extends AbstractPerforceFStatFeedbackHandler
{
    private boolean text = false;

    public boolean isText()
    {
        return text;
    }

    protected void handleCurrentItem()
    {
        String type = getCurrentItemType();
        if (type == null)
        {
            type = getCurrentItemHeadType();
        }

        text = fileIsText(type);
    }
}
