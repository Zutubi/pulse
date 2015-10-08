// dependency: ./namespace.js
// dependency: ./ajax.js
// dependency: ./ContextPanel.js
// dependency: ./HierarchySelector.js
// dependency: ./ProjectOverviewPanel.js

(function($)
{
    var Observable = kendo.Observable,
        SELECT = 'select';

    Zutubi.admin.ScopePanel = Observable.extend({
        init: function (containerSelector)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="outer-split" style="height: 100%; width: 100%">' +
                    '<div id="left-pane">' +
                        '<div id="hierarchy-tree" class="pane-content">' +
                        '</div>' +
                    '</div>' +
                    '<div id="center-pane">' +
                        '<div id="center-pane-content" class="pane-content">' +
                        '</div>' +
                    '</div>' +
                    '<div id="right-pane">' +
                        '<div id="right-pane-content" class="pane-content">' +
                        '</div>' +
                    '</div>' +
                '</div>', {wrap: false});

            that.view.render($(containerSelector));

            $("#outer-split").kendoSplitter({
                panes: [
                    { collapsible: true, size: "350px" },
                    { collapsible: false },
                    { collapsible: true, size: "250px" }
                ]
            });

            that.hierarchySelector = $("#hierarchy-tree").kendoZaHierarchySelector().data("kendoZaHierarchySelector");
            that.contextPanel = $("#right-pane-content").kendoZaContextPanel().data("kendoZaContextPanel");

            that.hierarchySelector.bind("bound", function(e)
            {
                if (!that.name)
                {
                    that._selectItem(that.hierarchySelector.getRootName());
                }
            });
            that.hierarchySelector.bind("nodeselect", function(e)
            {
                that._selectItem(e.name);
            });
        },

        events: [
            SELECT
        ],

        destroy: function()
        {
            // FIXME kendo need we do more?
            this.view.destroy();
        },

        setScope: function(scope, name)
        {
            this.scope = scope;
            this.hierarchySelector.setScope(scope);
            if (name)
            {
                this.setItem(name);
            }
            else
            {
                this.name = null;
            }
        },

        _selectItem: function(name)
        {
            this.setItem(name);
            // To simplify the URL for the root (and avoid useless history) we prefer to omit the
            // name, using the empty string in its place.
            if (name === this.hierarchySelector.getRootName())
            {
                name = "";
            }
            this.trigger(SELECT, {scope: this.scope, name: name});
        },

        setItem: function(name)
        {
            this.hierarchySelector.selectItem(name);
            this._loadContentPanes(name);
        },

        _loadContentPanes: function(name)
        {
            var that = this;

            this.name = name;

            Zutubi.admin.ajax({
                type: "GET",
                url: "/api/config/" + that.scope + "/" + that.name + "?depth=-1",
                success: function (data)
                {
                    if (data.length === 1)
                    {
                        that._showContent(data[0]);
                    }
                    else
                    {
                        Zutubi.admin.reportError("Unexpected result for config lookup, length = " + data.length);
                    }
                },
                error: function (jqXHR)
                {
                    Zutubi.admin.reportError("Could not load configuration: " + Zutubi.admin.ajaxError(jqXHR));
                }
            });
        },

        _showContent: function(data)
        {
            var links;

            this._clearContent();
            this.data = data;

            if (this.scope === "projects")
            {
                this.contentPanel = new Zutubi.admin.ProjectOverviewPanel({
                    containerSelector: "#center-pane-content",
                    project: data
                });
            }

            links = data.links || [];
            // FIXME kendo it would be nicer not to do a full page load here, since we point back into
            // admin.  Can this be done in a navigation handler?
            links.splice(0, 0, {
                name: "config",
                label: "configuration",
                url: "admina/config/" + this.scope + "/" + this.name
            });

            // Deliberately pick out the subset of data we want to show in the scope view.
            this.contextPanel.setData(null, {links: links});
        },

        _clearContent: function()
        {
            var contentEl = $("#center-pane-content");

            if (this.contentPanel)
            {
                this.contentPanel.destroy();
                this.contentPanel = null;
            }

            kendo.destroy(contentEl);
            contentEl.empty();
        }
    });
}(jQuery));
