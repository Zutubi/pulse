package com.cinnamonbob.core.config;

/**
 * 
 *
 */
public interface Command
{
    CommandResult execute() throws CommandException;
}
