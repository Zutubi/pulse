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

package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

import java.util.Arrays;
import java.util.List;

/**
 * A resolver that allows an arbitrary path to be appended to the url.  The
 * path is captured as a single parameter.
 */
public class PathConsumingActionResolver extends ActionResolverSupport
{
    private String paramName;
    private String currentPath;

    public PathConsumingActionResolver(String action, String paramName)
    {
        super(action);
        this.paramName = paramName;
    }

    public PathConsumingActionResolver(String action, String paramName, String currentPath)
    {
        super(action);
        this.paramName = paramName;
        this.currentPath = currentPath;
        addParameter(paramName, currentPath);
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<path element>");
    }

    public ActionResolver getChild(String name)
    {
        String path = WebUtils.uriComponentEncode(name);
        if(StringUtils.stringSet(currentPath))
        {
            path = currentPath + "/" + path;
        }
        
        return new PathConsumingActionResolver(getAction(), paramName, path);
    }
}
