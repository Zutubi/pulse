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

package com.zutubi.pulse.dev.ui;

import com.zutubi.util.io.PasswordReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A default console implementation that sits on the system I/O streams;
 */
public class DefaultConsole implements Console
{
    private BufferedReader inputReader;
    private PasswordReader passwordReader;

    public DefaultConsole()
    {
        inputReader = new BufferedReader(new InputStreamReader(System.in));
        passwordReader = new PasswordReader(inputReader);
    }

    public String readInputLine() throws IOException
    {
        return inputReader.readLine();
    }

    public String readPassword(String prompt, boolean echo)
    {
        return passwordReader.readPassword(prompt, echo);
    }

    public void printOutput(String output)
    {
        System.out.print(output);
    }

    public void printOutputLine(String output)
    {
        System.out.println(output);
    }

    public void printError(String error)
    {
        System.err.print(error);
    }

    public void printErrorLine(String error)
    {
        System.err.println(error);        
    }
}
