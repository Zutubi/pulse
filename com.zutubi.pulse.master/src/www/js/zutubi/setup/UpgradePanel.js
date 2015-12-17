// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.setup.UpgradePanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                properties = options.properties;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="upgrade-view">' +
                    '<h1>upgrade preview</h1>' +
                    '<p>The data directory contains existing data from an older Pulse version.</p>' +
                    '<table id="upgrade-versions">' +
                        '<tr><th>&nbsp;</th><th>Existing Data</th><th>New Installation</th></tr>' +
                        '<tr><th>Version</th><td>#: existingVersion #</td><td>#: newVersion #</td></tr>' +
                        '<tr><th>Build Number</th><td>#: existingBuild #</td><td>#: newBuild #</td></tr>' +
                        '<tr><th>Build Date</th><td>#: existingDate #</td><td>#: newDate #</td></tr>' +
                    '</table>' +
                    '<p>Before this new version of Pulse can use this data directory, it will need to run the following upgrade tasks:</p>' +
                    '<div id="upgrade-list-wrapper"></div>' +
                    '<p>This version of Pulse cannot run without completing these tasks. To abort the upgrade, shut down this Pulse version.</p>' +
                    '<div class="preview-actions">' +
                        '<button id="continue-upgrade" type="button" value="continue">start upgrade</button>' +
                    '</div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        existingVersion: properties.existingVersion.versionNumber,
                        existingBuild: properties.existingVersion.buildNumber,
                        existingDate: properties.existingVersion.buildDate,
                        newVersion: properties.newVersion.versionNumber,
                        newBuild: properties.newVersion.buildNumber,
                        newDate: properties.newVersion.buildDate
                    }
            });

            that.view.render(options.container);

            $("#upgrade-list-wrapper").kendoZaTaskList({
                id: "upgrade-list",
                data: properties.tasks
            });

            $("#continue-upgrade").kendoZaButton({
                click: jQuery.proxy(Zutubi.setup.postAndUpdate, Zutubi.setup, "upgrade", "Starting upgrade...")
            });
        },

        events: [

        ],

        destroy: function()
        {
            this.view.destroy();
        }
    });
}(jQuery));
