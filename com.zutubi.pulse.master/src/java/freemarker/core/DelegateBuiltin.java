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

package freemarker.core;

import freemarker.template.*;

import java.util.Arrays;

/**
 * A way to wrap a freemarker method to make it a String builtin.  This makes
 * it possible to call the method like:
 *
 * ${<string expression>?<builtin name>}
 */
public class DelegateBuiltin extends StringBuiltins.StringBuiltIn
{
    /**
     * Name of the method to delegate to.
     */
    private final String delegateKey;

    /**
     * Wraps the method as a String builtin.  Note that the builtin must be
     * regsitered before use, so normally you would use
     * {@link #conditionalRegistration(String, String)} instead of this
     * constructor directly.
     *
     * @param delegateKey name of the method to wrap.  This method must be
     *                    found in the template model for delegation to work.
     */
    public DelegateBuiltin(String delegateKey)
    {
        this.delegateKey = delegateKey;
    }

    TemplateModel calculateResult(String s, Environment env) throws TemplateException
    {
        // delegate if possible.
        TemplateMethodModel method = (TemplateMethodModel)env.__getitem__(delegateKey);
        if (method != null)
        {
            Object arg = method instanceof TemplateMethodModelEx ? new SimpleScalar(s) : s;
            return (TemplateModel) method.exec(Arrays.asList(arg));
        }

        return new SimpleScalar(s);
    }

    /**
     * Creates a new DelegateBuiltin and registers it under the given builtin
     * name if it does not already exist.
     *
     * @param builtinKey builtin name, i.e. what is used after the ? when
     *                   invoking the builtin in a template
     * @param methodKey  name of the method to wrap
     */
    public static void conditionalRegistration(String builtinKey, String methodKey)
    {
        if (!BuiltIn.builtins.containsKey(builtinKey))
        {
            BuiltIn.builtins.put(builtinKey, new DelegateBuiltin(methodKey));
        }
    }
}
