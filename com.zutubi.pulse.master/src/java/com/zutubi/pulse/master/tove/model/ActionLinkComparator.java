package com.zutubi.pulse.master.tove.model;

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 * Compares action links by label.
 */
public class ActionLinkComparator implements Comparator<ActionLink>
{
    private Comparator<String> labelComparator = new Sort.StringComparator();

    public int compare(ActionLink link1, ActionLink link2)
    {
        return labelComparator.compare(link1.getLabel(), link2.getLabel());
    }
}
