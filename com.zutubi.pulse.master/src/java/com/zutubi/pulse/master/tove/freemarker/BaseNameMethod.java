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

package com.zutubi.pulse.master.tove.freemarker;

import com.zutubi.tove.type.record.PathUtils;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import freemarker.template.Configuration;

import java.util.List;

/**
 * A freemarker method model that gets the base name for a path.  It can also
 * be passed a {@link Configuration} instance in which case it uses that
 * instance's configuration path.
 */
public class BaseNameMethod implements TemplateMethodModelEx
{
    public TemplateModel exec(List args) throws TemplateModelException
    {
        int argCount = args.size();
        if (argCount == 1)
        {
            // Arg should be a string key
            Object arg = args.get(0);
            if(arg instanceof SimpleScalar)
            {
                return getBaseName(((SimpleScalar) args.get(0)).getAsString());
            }
            else if(arg instanceof BeanModel)
            {
                Object unwrapped = ((BeansWrapper)ObjectWrapper.BEANS_WRAPPER).unwrap((TemplateModel) args.get(0));
                if(unwrapped instanceof com.zutubi.tove.config.api.Configuration)
                {
                    return getBaseName(((com.zutubi.tove.config.api.Configuration)unwrapped).getConfigurationPath());
                }
                else
                {
                    throw new TemplateModelException("Unexpected bean type '" + unwrapped.getClass().getName() + "': expecting Configuration instance");
                }
            }
            else
            {
                throw new TemplateModelException("Unexpected argument model type '" + arg.getClass().getName() + "': expecting SimpleScalar or BeanModel (wrapping Configuration instance)");
            }
        }
        else
        {
            throw new TemplateModelException("Unexpected number arguments: expecting 1, got " + argCount);
        }
    }

    private SimpleScalar getBaseName(String path)
    {
        return new SimpleScalar(PathUtils.getBaseName(path));
    }
}
