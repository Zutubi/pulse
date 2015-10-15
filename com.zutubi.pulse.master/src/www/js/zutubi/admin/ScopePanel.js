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
            that.contentEl = $("#center-pane-content");
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

        getItem: function()
        {
            return this.name || "";
        },

        setItem: function(name)
        {
            this.hierarchySelector.selectItem(name);
            this._loadContentPanes(name);
        },

        beginNavigation: function()
        {
            this._clearContent();
            kendo.ui.progress(this.contentEl, true);
        },

        endNavigation: function(error)
        {
            kendo.ui.progress(this.contentEl, false);
            if (error)
            {
                $('<p class="nav-error"></p>').appendTo(this.contentEl).text(error);
            }
        },

        _loadContentPanes: function(name)
        {
            var that = this;

            that.name = name;

            Zutubi.admin.navigate("/api/config/" + that.scope + "/" + that.name + "?depth=-1", [that, that.contextPanel], function(data)
            {
                if (data.length === 1)
                {
                    that._showContent(data[0]);
                    return null;
                }
                else
                {
                    return "Unexpected result for config lookup, length = " + data.length;
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
            if (this.contentPanel)
            {
                this.contentPanel.destroy();
                this.contentPanel = null;
            }

            kendo.destroy(this.contentEl);
            this.contentEl.empty();
        }
    });
}(jQuery));
