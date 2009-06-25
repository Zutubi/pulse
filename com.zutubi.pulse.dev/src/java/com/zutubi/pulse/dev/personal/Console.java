package com.zutubi.pulse.dev.personal;

import java.io.IOException;

/**
 * Abstracts an underlying text console for interacting with a user.
 */
public interface Console
{
    String readInputLine() throws IOException;
    String readPassword(String prompt, boolean echo);
    void printOutput(String output);
    void printOutputLine(String output);
    void printError(String error);
    void printErrorLine(String error);
}
