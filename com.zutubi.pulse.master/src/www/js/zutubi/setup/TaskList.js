// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ICON_CLS = {
            success: "fa-check-circle",
            failed: "fa-exclamation-circle",
            aborted: "fa-minus-circle",
            running: "fa-spinner fa-spin",
            pending: "fa-circle-o"
        };

    Zutubi.setup.TaskList = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaTaskList",
            template: '<div id="#= id #" class="k-task-list"><h2>task overview</h2><ul></ul></div>',
            taskTemplate: '<li><span class="fa #= iconCls #"></span> #: name #</li>'
        },

        _create: function()
        {
            var that = this;

            that.template = kendo.template(that.options.template);
            that.taskTemplate = kendo.template(that.options.taskTemplate);

            that.element.html(that.template({id: that.options.id}));
            that.list = that.element.find("ul");

            that.setData(that.options.data);
        },

        setData: function(data)
        {
            var i,
                task;

            this.list.empty();

            if (data)
            {
                for (i = 0; i < data.length; i++)
                {
                    task = data[i];
                    task.iconCls = task.status ? ICON_CLS[task.status] : "fa-dot-circle-o";
                    this.list.append(this.taskTemplate(task));
                }
            }
        }
    });

    ui.plugin(Zutubi.setup.TaskList);
}(jQuery));
