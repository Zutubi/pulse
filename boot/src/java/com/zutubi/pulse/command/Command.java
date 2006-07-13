package com.zutubi.pulse.command;

/**
 * The command interface for all commands that are to be executed from
 * the command line.
 *
 * @author Daniel Ostermeier
 */
public interface Command
{
    /**
     * Parse the command line arguments, recording any information required to
     * execute this command.
     *
     * @param argv
     *
     */
    public void parse(String... argv) throws Exception;

    /**
     * Execute this command
     *
     * @return the commands exit code. Return 0 if the command completed successfully,
     * any non-zero number if it not.
     */
    public int execute();
}
