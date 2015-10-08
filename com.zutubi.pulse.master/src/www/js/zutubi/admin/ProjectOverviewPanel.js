// dependency: ./namespace.js


(function($)
{
    var Observable = kendo.Observable,
        ID = "project-overview";

    Zutubi.admin.ProjectOverviewPanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                project = options.project;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#= id #" class="k-overview-panel">' +
                    '<h1>#: name #</h1>' +
                    '<div id="#= id #-tabstrip">' +
                        '<ul>' +
                            '<li class="k-state-active">summary</li>' +
                            '<li>overrides</li>' +
                        '</ul>' +
                        '<div id="#= id #-summary">Summary of main project config</div>' +
                        '<div id="#= id #-overrides">What this project overrides</div>' +
                    '</div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: ID,
                        name: project.properties.name
                    }
                });

            that.view.render($(options.containerSelector));
            $("#" + ID + "-tabstrip").kendoTabStrip({});
        },

        events: [
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            this.view.destroy();
        }
    });
}(jQuery));
