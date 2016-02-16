// dependency: ./namespace.js
// dependency: zutubi/core/package.js
// dependency: zutubi/config/package.js
// dependency: ./ContextPanel.js
// dependency: ./HierarchySelector.js
// dependency: ./AgentOverviewPanel.js
// dependency: ./ProjectOverviewPanel.js

(function($)
{
    var Observable = kendo.Observable,
        SELECT = 'select';

    Zutubi.admin.ScopePanel = Observable.extend({
        init: function (container)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div class="k-outer-split" style="height: 100%; width: 100%">' +
                    '<div>' +
                        '<div class="k-pane-content k-vbox">' +
                            '<div class="k-view-switch"><button class="k-right">configuration <span class="fa fa-angle-right"></span></button></div>' +
                            '<div class="k-hierarchy-tree k-vbox-grow"></div>' +
                        '</div>' +
                    '</div>' +
                    '<div>' +
                        '<div class="k-center-pane-content k-pane-content">' +
                        '</div>' +
                    '</div>' +
                    '<div>' +
                        '<div class="k-right-pane-content k-pane-content">' +
                        '</div>' +
                    '</div>' +
                '</div>', {wrap: false});

            that.view.render(container);

            that.view.element.kendoSplitter({
                panes: [
                    { collapsible: true, size: "350px" },
                    { collapsible: false },
                    { collapsible: true, size: "250px" }
                ]
            });

            that.configButton = that.view.element.find(".k-view-switch button").kendoButton({
                click: jQuery.proxy(this._openConfig, this)
            });
            that.hierarchySelector = that.view.element.find(".k-hierarchy-tree").kendoZaHierarchySelector({
                namespace: "ScopePanel"
            }).data("kendoZaHierarchySelector");
            that.contentEl = that.view.element.find(".k-center-pane-content");
            that.contextPanel = that.view.element.find(".k-right-pane-content").kendoZaContextPanel().data("kendoZaContextPanel");

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

            that.contextPanel.bind("action", jQuery.proxy(that._doAction, that));
        },

        events: [
            SELECT
        ],

        destroy: function()
        {
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

        _openConfig: function(name)
        {
            var item = this.getItem();
            if (!item)
            {
                item = this.hierarchySelector.getRootName();
            }

            Zutubi.admin.openConfigPath(this.scope + "/" + item);
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

        _doAction: function(e)
        {
            var that = this,
                path = this.scope + "/" + this.name,
                actionWindow;

            actionWindow = new Zutubi.config.ActionWindow({
                path: path,
                action: e.action,
                executed: jQuery.proxy(that._handleActionResult, that)
            });

            actionWindow.show();
        },

        _handleActionResult: function(data)
        {
            if (data.success)
            {
                Zutubi.core.reportSuccess(data.message);
            }
            else
            {
                Zutubi.core.reportError(data.message);
            }

            if (data.model)
            {
                this._showContent(data.model);
            }

            this.hierarchySelector.reload();
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
                $('<p class="k-nav-error"></p>').appendTo(this.contentEl).text(error);
            }
        },

        _loadContentPanes: function(name)
        {
            var that = this;

            that.name = name;

            Zutubi.admin.navigate("/api/config/" + Zutubi.config.encodePath(that.scope + "/" + that.name) + "?depth=-1", [that, that.contextPanel], function(data)
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
            var links,
                contextData,
                path;

            this._clearContent();
            this.data = data;

            if (this.scope === "projects")
            {
                this.contentPanel = new Zutubi.admin.ProjectOverviewPanel({
                    container: this.contentEl,
                    project: data
                });
            }
            else
            {
                this.contentPanel = new Zutubi.admin.AgentOverviewPanel({
                    container: this.contentEl,
                    agent: data
                });
            }

            // Deliberately pick out the subset of data we want to show in the scope view.
            links = data.links || [];
            path = this.scope + "/" + this.name;
            links.splice(0, 0, {
                name: "config",
                label: "configuration",
                url: "admin/config/" + Zutubi.config.encodePath(path),
                click: function()
                {
                    Zutubi.admin.openConfigPath(path);
                }
            });

            contextData = {links: links};
            if (this.hierarchySelector.getRootName() !== data.key)
            {
                contextData.actions = [{
                    action: "introduceParent",
                    label: "introduce parent template"
                }, {
                    action: "smartClone",
                    label: "smart clone"
                }];
            }

            this.contextPanel.setData(this.scope + "/" + data.key, contextData);
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
