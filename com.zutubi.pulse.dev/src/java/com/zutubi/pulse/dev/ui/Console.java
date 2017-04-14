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

import java.io.IOException;

/**
 * Abstracts an underlying text console for interacting with a user.
 */
public interface Console
{
    /**
     * Reads a single line of input from the user.
     *
     * @return the entered line
     * @throws IOException on error
     */
    String readInputLine() throws IOException;

    /**
     * Prompts for and reads a password, optionally masking the typed text.
     *
     * @param prompt the prompt to display
     * @param echo   if true, the typed text is echoed, if not it should be
     *               hidden/masked
     * @return the entered password
     */
    String readPassword(String prompt, boolean echo);

    /**
     * Prints the given string as output.
     *
     * @param output string to print
     */
    void printOutput(String output);

    /**
     * Prints the given string as output and appends a newline.
     *
     * @param output string to print
     */
    void printOutputLine(String output);

    /**
     * Prints the given string as an error.
     *
     * @param error string to print
     */
    void printError(String error);

    /**
     * Prints the given string as an error and appends a newline.
     *
     * @param error string to print
     */
    void printErrorLine(String error);
}
