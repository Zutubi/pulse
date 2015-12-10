// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./WorkflowWindow.js

(function($)
{
    var WorkflowWindow = Zutubi.admin.WorkflowWindow;

    Zutubi.admin.DeleteWindow = WorkflowWindow.extend({
        init: function (options)
        {
            var that = this;

            that.taskTemplate = kendo.template("<tr><td>#= indent # #: path #</td><td>#: summary #</td></tr>");

            WorkflowWindow.fn.init.call(that, {
                url: "/api/action/delete/" + Zutubi.config.encodePath(options.path),
                label: options.label,
                title: "confirm " + options.label,
                continueLabel: "confirm " + options.label,
                width: 600,
                render: jQuery.proxy(that._render, that),
                success: function()
                {
                    that.close();
                    options.confirm();
                }
            });
        },

        _render: function(data, el)
        {
            var tbody,
                postLabel = this.options.label === "delete" ? "deleted" : "hidden";

            el.html("<p>Are you sure you would like to " + this.options.label + " this item? Any descendants will also be " + postLabel + ". The following tasks are required:</p>" +
                "<table class='k-cleanup-tasks'><tbody><tr><th>affected path</th><th>action</th></tr></tbody></table>"
            );

            tbody = el.find("tbody");
            this._renderTask(data, tbody, "");
        },

        _renderTask: function(task, tbody, indent)
        {
            var i;

            tbody.append(this.taskTemplate({indent: indent, path: task.path, summary: task.summary}));

            if (task.children)
            {
                for (i = 0; i < task.children.length; i++)
                {
                    this._renderTask(task.children[i], tbody, indent + "&nbsp;&nbsp");
                }
            }
        }
    });
}(jQuery));

