package com.zutubi.pulse.master.tove.model;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.tove.type.record.PathUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Models a row of a configuration table.
 */
public class Row extends AbstractParameterised
{
    private static final String PARAM_PATH   = "path";
    private static final String PARAM_HIDDEN = "hidden";

    private List<Cell> cells = new LinkedList<Cell>();
    private List<ActionLink> actions = null;

    public Row()
    {
    }

    public Row(String path, boolean hidden, List<ActionLink> actions)
    {
        addParameter(PARAM_PATH, path);
        addParameter(PARAM_HIDDEN, hidden);
        this.actions = actions;
    }

    public String getPath()
    {
        return getParameter(PARAM_PATH, "");
    }

    public boolean isHidden()
    {
        return getParameter(PARAM_HIDDEN, false);
    }

    public String getBaseName()
    {
        return PathUtils.getBaseName(getParameter(PARAM_PATH, ""));
    }

    public List<Cell> getCells()
    {
        return cells;
    }

    public void addCell(Cell cell)
    {
        cells.add(cell);
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public ActionLink getAction(final String actionName)
    {
        return find(actions, new Predicate<ActionLink>()
        {
            public boolean apply(ActionLink actionLink)
            {
                return actionLink.getAction().equals(actionName);
            }
        }, null);
    }

    public void addAction(ActionLink action)
    {
        actions.add(action);
    }
}
