// dependency: ./namespace.js
// dependency: zutubi/config/package.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        BOUND = "bound",
        NODESELECT = "nodeselect";

    Zutubi.admin.HierarchyTree = TreeView.extend({
        init: function(element, options)
        {
            TreeView.fn.init.call(this, element, options);

            this.bound = false;

            if (options && options.scope)
            {
                this.setScope(options.scope);
            }
        },

        events: [
            // FIXME kendo: to subscribe to our own select event we need to have this here, is there a better way?
            "dataBound",
            "select",
            NODESELECT
        ],

        options: {
            name: "ZaHierarchyTree",
            dataTextField: "name",
            loadOnDemand: false,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node.
                if (!this.bound && !e.node)
                {
                    kendo.ui.progress(this.element, false);
                    this.expand(this._getRoot());
                    this.bound = true;
                    this._updateSelected();
                    this.trigger(BOUND);
                }
            },
            select: function(e)
            {
                var that = this;
                that.trigger(NODESELECT, {name: that.dataItem(e.node).name});
            }
        },


        setScope: function(scope)
        {
            if (this.scope !== scope)
            {
                this._setScope(scope);
            }
        },

        _setScope: function (scope)
        {
            var dataSource;

            this.scope = scope;
            this.bound = false;

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: {
                        url: window.baseUrl + "/api/template/" + scope,
                        dataType: "json",
                        headers: {
                            Accept: "application/json; charset=utf-8",
                            "Content-Type": "application/json; charset=utf-8"
                        }
                    }
                },
                schema: {
                    model: {
                        children: "nested"
                    }
                }
            });

            dataSource.bind('error', function (e) {
                Zutubi.core.reportError('Could not load hierarchy tree: ' + Zutubi.core.ajaxError(e.xhr));
            });

            kendo.ui.progress(this.element, true);
            this.setDataSource(dataSource);
        },

        _getRoot: function()
        {
            return this.wrapper.find(".k-item:first");
        },

        getRootName: function()
        {
            return this.dataSource.at(0).name;
        },

        selectItem: function(name)
        {
            this.item = name;
            if (this.bound)
            {
                this._updateSelected();
            }
        },

        _updateSelected: function()
        {
            var that = this,
                root = that._getRoot(),
                node;

            if (that.item)
            {
                node = that.findByText(that.item);
            }

            if (node)
            {
                if (node !== root)
                {
                    that.expandTo(that.dataItem(node));
                }

                that.select(node);
            }
        },

        prefixFilter: function(s)
        {
            if (s)
            {
                this._prefixFilterSubtree(this.root.children("li").first(), this.dataSource.at(0), s.toLowerCase());
            }
            else
            {
                this.root.find("li").css("display", "");
            }
        },

        _prefixFilterSubtree: function(root, dataItem, text)
        {
            var visible = root.find(".k-in").first().text().toLowerCase().indexOf(text) === 0,
                nested,
                childVisible = false,
                children,
                items,
                child,
                item,
                i;

            nested = root.children("ul");
            if (nested.length > 0)
            {
                children = dataItem.children.data();
                items = nested.children("li");

                for (i = 0; i < items.length; i++)
                {
                    item = items.eq(i);
                    child = children[i];

                    childVisible = this._prefixFilterSubtree(item, child, text) || childVisible;
                }

                if (childVisible && !dataItem.expanded)
                {
                    this._toggle(root, dataItem, true);
                }

                visible = visible || childVisible;
            }

            root.css("display", visible ? "" : "none");
            return visible;
        },

        applyDelta: function(delta)
        {
            var i, path;

            // If there is an addition or rename that applies to us, we just do a full reload (overkill but easy).
            // Deletes are easy to handle directly.
            if (delta.addedPaths)
            {
                for (i = 0; i < delta.addedPaths.length; i++)
                {
                    path = delta.addedPaths[i];
                    if (this.scope === Zutubi.config.parentPath(path))
                    {
                        this._setScope(this.scope);
                        return;
                    }
                }
            }

            if (delta.renamedPaths)
            {
                for (path in delta.renamedPaths)
                {
                    if (delta.renamedPaths.hasOwnProperty(path) && this.scope === Zutubi.config.parentPath(path))
                    {
                        this._setScope(this.scope);
                        return;
                    }
                }
            }

            if (delta.deletedPaths)
            {
                for (i = 0; i < delta.deletedPaths.length; i++)
                {
                    path = delta.deletedPaths[i];
                    if (this.scope === Zutubi.config.parentPath(path))
                    {
                        this.remove(this.findByText(Zutubi.config.subPath(path, 1, 2)));
                    }
                }
            }
        }
    });

    ui.plugin(Zutubi.admin.HierarchyTree);
}(jQuery));
