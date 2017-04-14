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

package com.zutubi.pulse.core.ui.api;

import java.util.List;

/**
 * An interface for interaction with a user that is running Pulse operations.
 * Can be used to query for user input such as passwords or confirmation, or
 * to provide feedback during long-running operations.
 */
public interface UserInterface
{

    /**
     * Indicates if debug messages will be used by this interface.  If not, and
     * they are expensive to calculate, their calculation can be skipped.
     *
     * @return true iff debug messages will be used by this ui
     */
    boolean isDebugEnabled();

    /**
     * Reports a line of debugging-level feedback to the user.  The user will
     * not typically see this unless they request extra verbosity of feedback.
     *
     * @param message the message to report
     */
    void debug(String message);

    /**
     * Reports a line of informational feedback to the user.  The user will
     * typically see this unless they request supression of feedback.
     *
     * @param message the message to report
     */
    void status(String message);

    /**
     * Reports a line of warning-level feedback to the user.  The user will
     * typically see this unless they request supression of feedback.
     *
     * @param message the message to report
     */
    void warning(String message);

    /**
     * Reports a line of error-level feedback to the user.  The user will
     * always see this type of output.
     *
     * @param message the message to report
     */
    void error(String message);

    /**
     * Reports a line of error-level feedback to the user, optionally with a
     * full trace for the given throwable.  The user will always see the error
     * message, but will typically only see the full trace if they request
     * verbose feedback.
     *
     * @param message   the message to report
     * @param throwable the throwable that indicates the error, a trace of
     *                  which may be reported to the user
     */
    void error(String message, Throwable throwable);

    /**
     * Call to indicate that following messages fall within a common context.
     * This can be used to group related messages for the aid of the user.  For
     * example, if reporting on a directory, a caller may report a status
     * message with the directory name, then call this method before reporting
     * on each file within the directory, before finally calling
     * {@link #exitContext}. to return to the original level.  For the console
     * UI, indention is used to delineate contexts, so the output would appear
     * like:
     * <pre>
     * {@literal Processing directory foo...
     *     processing bar.txt
     *     processing baz.txt
     * Directory foo processed.}
     * </pre>
     */
    void enterContext();

    /**
     * Call to indicate that a context has completed, and the UI should return
     * to the previous context.
     *
     * @see #enterContext()
     */
    void exitContext();

    /**
     * Prompts the user for free-form input.
     *
     * @param prompt the prompt to present to the user
     * @return the raw string entered by the user, may be empty
     */
    String inputPrompt(String prompt);

    /**
     * Prompts the user for free-form input, providing them with a default
     * response which will be returned if they do not provide one.
     *
     * @param prompt          the prompt to present to the user, the UI itself
     *                        will provide details of the default response
     * @param defaultResponse the response to return if the user accepts the
     *                        default
     * @return the raw string entered by the user, which may be the default if
     *         they choose to accept it
     */
    String inputPrompt(String prompt, String defaultResponse);

    /**
     * Prompts the user for a password.  Their input will not be echoed in the
     * UI.
     *
     * @param prompt the prompt to present to the user
     * @return the raw string entered by the user
     */
    String passwordPrompt(String prompt);

    /**
     * Prompts the user for a yes or no response, with a default option
     * provided.  The prompt may optionally show always and/or never options,
     * which are just like yes and no respectively except that they should
     * persist.  Persistance of the response is the responsibility of the
     * caller.
     *
     * @param question        the question to ask the user, the UI itself will
     *                        provide details of the default response
     * @param showAlways      if true, an always option is added
     * @param showNever       if true, a never option is added
     * @param defaultResponse the response to return if the user accepts the
     *                        default
     * @return the user's response     */
    YesNoResponse yesNoPrompt(String question, boolean showAlways, boolean showNever, YesNoResponse defaultResponse);

    /**
     * Prompts the user to make a choice from a menu consisting of the given
     * options.  The choice returned may optionally indicate that the user
     * would like the choice to persist.  In this case persistence of the
     * choice is the responsibility of the caller.
     * 
     * @param question question to introduce the choices with: should not end
     *                 with any punctuation
     * @param options  a list of available options
     * @param <T> the type of value carried by the options
     * @return the user's choice, the value of which will be taken from one of
     *         the given options
     */
    <T> MenuChoice<T> menuPrompt(String question, List<MenuOption<T>> options);
}
