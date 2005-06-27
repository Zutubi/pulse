package com.cinnamonbob.core.config;


/**
 * 
 *
 */
public class Description implements ProjectComponent, InitComponent
{
    private Project project;
    private String text;
    
    public void init()
    {
        project.setDescription(text);
    }

    public void setProject(Project project)
    {
        this.project = project;
    }
    
    public void addText(String text)
    {
        this.text = text;
    }
}
