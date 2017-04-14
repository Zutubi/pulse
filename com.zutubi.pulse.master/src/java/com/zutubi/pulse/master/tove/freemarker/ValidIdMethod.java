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

import com.zutubi.util.WebUtils;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * A freemarker method model that converts a string to a valid HTML name for
 * use in IDs.
 */
public class ValidIdMethod implements TemplateMethodModelEx
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
                return new SimpleScalar(WebUtils.toValidHtmlName(((SimpleScalar) args.get(0)).getAsString()));
            }
            else
            {
                throw new TemplateModelException("Unexpected argument model type '" + arg.getClass().getName() + "': expecting SimpleScalar");
            }
        }
        else
        {
            throw new TemplateModelException("Unexpected number arguments: expecting 1, got " + argCount);
        }
    }
}
