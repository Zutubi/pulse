package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * <class-comment/>
 */
public interface InitConfiguration
{
    public static final String BOB_HOME = "bob.home";

    File getBobHome();

    void setBobHome(File home);
}
