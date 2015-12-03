// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.setup.RestorePanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                properties = options.properties;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="restore-view">' +
                    '<h1>restore from archive</h1>' +
                    '<p>You have requested to restore your Pulse configuration from an archive. Please review the archive details below before continuing.</p>' +
                    '<div id="archive-table"></div>' +
                    '<p><span class="fa fa-exclamation-triangle prop-info"></span> Please be aware that this restore will overwrite all of your existing configuration and settings.</p>' +
                    '<div class="preview-actions">' +
                        '<button id="continue-restore" type="button" value="continue">restore from archive</button>' +
                        '<button id="abort-restore" type="button" value="abort">abort restore (normal startup)</button>' +
                    '</div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        baseUrl: window.baseUrl
                    }
                });

            that.view.render($(options.containerSelector));

            $("#archive-table").kendoZaPropertyTable({
                id: "archive-properties",
                data: [{
                    key: "Archive Date",
                    value: properties.archiveCreated
                }, {
                    key: "Archive Name",
                    value: properties.archiveName
                }, {
                    key: "Archive Location",
                    value: properties.archiveLocation
                }]
            });

            $("#continue-restore").kendoZaButton({
                click: jQuery.proxy(Zutubi.setup.postAndUpdate, Zutubi.setup, "restore", "Starting restore process...")
            });
            $("#abort-restore").kendoZaButton({
                click: jQuery.proxy(Zutubi.setup.postAndUpdate, Zutubi.setup, "restoreAbort", "Aborting restore...")
            });

            that.htmlDocs = '<h3>Warning</h3>' +
                '<p>If you are importing data from a 1.2.x installation, this restore will drop all existing tables in ' +
                'your specified Pulse database.  If you have any custom tables in the same database as Pulse, please ' +
                'ensure that you back them up before continuing with this restore.</p>';
        },

        events: [

        ],

        destroy: function()
        {
            this.view.destroy();
        }
    });
}(jQuery));
