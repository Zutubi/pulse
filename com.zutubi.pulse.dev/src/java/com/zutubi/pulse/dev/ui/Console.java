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
