// dependency: ./namespace.js
// dependency: ./ConfigTree.js
// dependency: ./Form.js

(function($)
{
    var Observable = kendo.Observable,
        PATHSELECT = 'pathselect';

    Zutubi.admin.ConfigPanel = Observable.extend({
        init: function (containerSelector)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View('<div id="outer-split" style="height: 100%; width: 100%">' +
                                           '<div id="left-pane">' +
                                               '<div id="config-tree" class="pane-content">' +
                                               '</div>' +
                                           '</div>' +
                                           '<div id="center-pane">' +
                                               '<div id="center-pane-content" class="pane-content">' +
                                                   '<p>Main pane.</p>' +
                                               '</div>' +
                                           '</div>' +
                                           '<div id="right-pane">' +
                                               '<div class="pane-content">' +
                                                   '<p>Help pane.</p>' +
                                               '</div>' +
                                           '</div>' +
                                       '</div>', {wrap: false});

            that.view.render($(containerSelector));

            $("#outer-split").kendoSplitter({
                panes: [
                    { collapsible: true, size: "250px" },
                    { collapsible: false },
                    { collapsible: true, size: "250px" }
                ]
            });

            that.configTree = $("#config-tree").kendoZaConfigTree().data("kendoZaConfigTree");
            that.configTree.bind("pathselect", function(e)
            {
                that.trigger(PATHSELECT, {path: e.path});
                that.loadContentPanes(e.path);
            });
        },

        events: [
            PATHSELECT
        ],

        setPath: function(path)
        {
            this.configTree.setRootPath(subpath(path, 0, 2));
            this.configTree.selectConfig(subpath(path, 2));
            this.loadContentPanes(path);
        },

        loadContentPanes: function(path)
        {
            var that = this;

            jQuery.ajax({
                type: "GET",
                url: window.baseUrl + "/api/config/" + path + "?depth=-1",
                dataType: "json",
                headers: {
                    Accept: "application/json; charset=utf-8",
                    "Content-Type": "application/json; charset=utf-8"
                },
                success: function (data)
                {
                    if (data.length == 1)
                    {
                        that.showContent(data[0]);
                    }
                    else
                    {
                        zaReportError("Unexpected result for config lookup, length = " + data.length);
                    }
                },
                error: function (jqXHR, textStatus)
                {
                    if (jqXHR.status === 401)
                    {
                        showLoginForm();
                    }

                    zaReportError("Could not load configuration: " + zaAjaxError(jqXHR));
                }
            });
        },

        showContent: function(data)
        {
            if (data.kind === "composite")
            {
                this.showComposite(data);
            }
            else if (data.kind === "collection")
            {
                this.showCollection(data);
            }
            else if (data.kind === "type-selection")
            {
                this.showTypeSelection(data);
            }
            else
            {
                zaReportError("Unrecognised config kind: " + data.kind);
            }
        },

        showComposite: function(data)
        {
            console.log("composite");
            console.dir(data);

            this.form = $("#center-pane-content").kendoZaForm({
                structure: data.form,
                values: data.properties
            }).data("kendoZaForm");
        },

        showCollection: function(data)
        {
            console.log("collection");
            console.dir(data);
        },

        showTypeSelection: function(data)
        {
            console.log("type select");
            console.dir(data);
        }
    });
}(jQuery));
