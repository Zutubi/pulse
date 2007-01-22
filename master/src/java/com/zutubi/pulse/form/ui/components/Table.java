package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.RenderContext;
import com.zutubi.pulse.form.ui.TemplateRenderer;
import com.zutubi.pulse.form.ui.TemplateRendererContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Table extends UIComponent
{
    private static final String TEMPLATE = "table-end";

    private static final String OPEN_TEMPLATE = "table";

    private List<Column> columns = new ArrayList<Column>();

    public void render(RenderContext context, TemplateRenderer renderer) throws Exception
    {
        setContext(context);
        
        // generate the template renderer context
        evaluateParameters();

        renderTemplate(context, getDefaultOpenTemplate());

        List list = (List) context.get(name);

        for (Object item : list)
        {
            context.push(item);

            renderTemplate(context, "row");

            for (Column column : columns)
            {
                column.parent = this;
                column.setValue(null);
                column.getParameters().remove("value");
                column.render(context, renderer);
            }

            renderTemplate(context, "row-end");

            context.pop();
        }

        renderTemplate(context, getDefaultTemplate());
    }

    private void renderTemplate(RenderContext context, String templateName) throws Exception
    {
        TemplateRendererContext templateContext;
        templateContext = new TemplateRendererContext(templateName, getParameters(), context);
        context.getRenderer().render(templateContext);
    }

    public String getDefaultTemplate()
    {
        return TEMPLATE;
    }

    public String getDefaultOpenTemplate()
    {
        return OPEN_TEMPLATE;
    }

    public void addColumn(Column column)
    {
        columns.add(column);
    }
}
