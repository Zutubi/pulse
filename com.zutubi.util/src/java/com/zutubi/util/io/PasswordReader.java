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

package com.zutubi.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class to read a password from the console.  Uses JVM support if
 * available (Java 6), and falls back on a workaround if not.
 */
public class PasswordReader
{
    private BufferedReader reader;
    private Object console;
    private Method readPasswordMethod;

    /**
     * Wraps the given reader with a utility for reading passwords.
     *
     * @param reader reader to wrap, which should be the same reader used for
     *               reading other input -- note that the reader is not used
     *               if JVM support for reading passwords is available
     */
    public PasswordReader(BufferedReader reader)
    {
        this.reader = reader;

        try
        {
            Method getConsoleMethod = System.class.getMethod("console");
            console = getConsoleMethod.invoke(null);
            Class<?> consoleClass = Class.forName("java.io.Console");
            readPasswordMethod = consoleClass.getMethod("readPassword", String.class, Object[].class);
        }
        catch (Exception e)
        {
            // OK, use dodgy masking
        }
    }

    /**
     * Reads a password from standard input.
     *
     * @param prompt a string to print to standard output to prompt the user
     * @param echo   if true, the password typed will be echoed, otherwise it
     *               will be hidden
     * @return the entered password string
     */
    public String readPassword(String prompt, boolean echo)
    {
        if (echo)
        {
            System.out.print(prompt);
            try
            {
                return reader.readLine();
            }
            catch (IOException e)
            {
                return null;
            }
        }
        else
        {
            if (readPasswordMethod == null)
            {
                return maskedPassword(prompt);
            }
            else
            {
                try
                {
                    char[] password = (char[]) readPasswordMethod.invoke(console, prompt, new Object[0]);
                    return new String(password);
                }
                catch (IllegalAccessException e)
                {
                    return null;
                }
                catch (InvocationTargetException e)
                {
                    return null;
                }
            }
        }
    }

    private String maskedPassword(String prompt)
    {
        EraserThread eraserThread = new EraserThread(prompt);

        try
        {
            Thread t = new Thread(eraserThread);
            t.start();
            return reader.readLine();
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            eraserThread.stopMasking();
        }
    }

    private class EraserThread implements Runnable
    {
        private volatile boolean run;

        /**
         * @param prompt the prompt displayed to the user
         */
        public EraserThread(String prompt)
        {
            System.out.print(prompt);
        }

        public void run()
        {
            run = true;
            while (run)
            {
                System.out.print("\b ");
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException ie)
                {
                    // Ignore
                }
            }
        }

        public void stopMasking()
        {
            this.run = false;
        }
    }
}
