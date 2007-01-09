package com.zutubi.pulse.plugins.update;

/**
 * Constants to specify how strictly to compare versions in dependency
 * resolution.
 */
public enum VersionMatch
{
    COMPATIBLE,
    EQUIVALENT,
    GREATER_OR_EQUAL,
    PERFECT
}
