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

import com.google.common.base.Function;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

/**
 * Simple helper class for calculating valid URLs.
 */
public class UrlEnumerator
{
    /**
     * Calculates the valid URLs starting at the base resolver.  The URLs may
     * be infinite, which will be expressed using the * wildcard.
     *
     * @param base resolver to start with
     * @return a list of all valid URLs reachable from the resolver
     */
    public static List<String> enumerate(ActionResolver base)
    {
        List<String> result = new LinkedList<String>();
        process(base, Collections.<Pair<String, Class>>emptyList(), result);
        return result;
    }

    private static void process(ActionResolver resolver, List<Pair<String, Class>> currentPath, List<String> paths)
    {
        for (String element: resolver.listChildren())
        {
            ActionResolver child = resolver.getChild(element);
            List<Pair<String, Class>> pathCopy = new LinkedList<Pair<String, Class>>(currentPath);
            Pair<String, Class> childPair = new Pair<String, Class>(element, child.getClass());
            if (currentPath.contains(childPair))
            {
                // We have repetition.  Go back to where it was, popping
                // incomplete paths on the way so we can add them as a single
                // repeating element.
                String repeatedPart = "";
                Pair<String, Class> pair;
                do
                {
                    pair = pathCopy.remove(pathCopy.size() - 1);
                    repeatedPart = pair.first + "/" + repeatedPart;
                    paths.remove(paths.size() - 1);
                }
                while(pair.second != child.getClass());

                paths.add(collapse(pathCopy) + "/(" + repeatedPart + ")*");
            }
            else
            {
                pathCopy.add(childPair);
                if (resolver.getAction() != null)
                {
                    paths.add(collapse(pathCopy) + "/");
                }
                
                process(child, pathCopy, paths);
            }
        }
    }

    private static String collapse(List<Pair<String, Class>> currentPath)
    {
        return StringUtils.join("/", transform(currentPath, new Function<Pair<String, Class>, String>()
        {
            public String apply(Pair<String, Class> pair)
            {
                return pair.first;
            }
        }));
    }
}
