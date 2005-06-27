package com.cinnamonbob.core2.config;

/**
 * 
 *
 */
public interface Command
{
    CommandResult execute() throws CommandException;
}
