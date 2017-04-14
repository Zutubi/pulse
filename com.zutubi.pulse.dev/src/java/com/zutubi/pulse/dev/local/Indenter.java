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

package com.zutubi.pulse.dev.local;

import java.io.PrintStream;

/**
 */
public class Indenter
{
    private PrintStream stream;
    private String currentIndent;
    private String indentString;

    public Indenter(PrintStream stream, String indentString)
    {
        this.stream = stream;
        this.indentString = indentString;
        currentIndent = "";
    }


    public void indent()
    {
        currentIndent += indentString;
    }


    public void dedent()
    {
        if(currentIndent.length() > 0)
        {
            currentIndent = currentIndent.substring(indentString.length());
        }
    }


    public void println(String s)
    {
        stream.println(currentIndent + s);
    }


    public void println(Object o)
    {
        stream.println(currentIndent + o.toString());
    }


    public void println()
    {
        stream.println();
    }
}
