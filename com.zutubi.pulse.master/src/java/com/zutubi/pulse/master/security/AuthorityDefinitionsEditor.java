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

package com.zutubi.pulse.master.security;

import com.zutubi.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A text property converter to allow easy configuration of the authority definitions
 * within a spring context file.
 */
public class AuthorityDefinitionsEditor  extends PropertyEditorSupport
{
    public void setAsText(String text) throws IllegalArgumentException
    {
        AuthorityDefinitions instance = new AuthorityDefinitions();
        try
        {
            text = text.trim();
            BufferedReader reader = new BufferedReader(new StringReader(text));
            String line;
            while (StringUtils.stringSet(line = reader.readLine()))
            {
                StringTokenizer tokens = new StringTokenizer(line, ",", false);
                String path = tokens.nextToken().trim();
                String role = tokens.nextToken().trim();
                List<String> methods = new LinkedList<String>();
                while (tokens.hasMoreTokens())
                {
                    methods.add(tokens.nextToken().trim());
                }
                instance.addPrivilege(path, role, methods.toArray(new String[methods.size()]));
            }
        }
        catch (IOException e)
        {
            // this will not happen.
        }

        setValue(instance);
    }
}

