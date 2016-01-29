// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./HierarchySelector.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        SELECT = "select",
        ADD = "add",
        SCOPE_SELECTED = "scope-selected",
        ITEM_SELECTED = "item-selected";

    Zutubi.admin.Crumb = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaCrumb"
        },

        _create: function()
        {
            var that = this;

            that.outer = $('<div class="k-split-container"></div>');
            that.mainButton = $('<a class="k-split-button"></a>');
            that.outer.append(that.mainButton);
            that.arrowButton = $('<a class="k-split-button-arrow"><span class="fa fa-caret-down"></span></a>');
            that.outer.append(that.arrowButton);

            that.element.append(that.outer);

            that.popup = that.createPopup();

            that.mainButton.on("click", jQuery.proxy(that._clicked, that));
            that.arrowButton.on("click", jQuery.proxy(that._clicked, that));
        },

        select: function(text)
        {
            this.selected = text;
            this.mainButton.text(text);
        },

        _clicked: function(e)
        {
            var that = this;

            e.preventDefault();

            if (that.popup.visible())
            {
                that.popup.close();
            }
            else
            {
                that.popup.open();
            }
        }
    });

    Zutubi.admin.ScopeCrumb = Zutubi.admin.Crumb.extend({
        init: function(element, options)
        {
            Zutubi.admin.Crumb.fn.init.call(this, element, options);

            this.select("projects");
        },

        options: {
            name: "ZaScopeCrumb"
        },

        events: [
            SELECT
        ],

        createPopup: function()
        {
            var that = this,
                i,
                popup;

            that.scopes = ["projects", "agents"];
            if (that.options.isAdmin)
            {
                that.scopes.push("settings", "users", "groups", "plugins");
            }

            that.popupEl = $("<ul class='k-selector-popup'></ul>");
            for (i = 0; i < that.scopes.length; i++)
            {
                that.popupEl.append("<li>" + that.scopes[i] + "</li>");
            }

            popup = that.popupEl.kendoPopup({
                anchor: that.element.closest(".k-navitem"),
                origin: "bottom left",
                position: "top left",
                animation: {
                    open: {
                        effects: "slideIn:down"
                    }
                }
            }).data("kendoPopup");

            that.popupEl.on("click", jQuery.proxy(that._popupClicked, that));
            return popup;
        },

        _onPopupClicked: function(e)
        {
            var that = this,
                target = kendo.eventTarget(e),
                item = $(target).closest("li");

            e.preventDefault();

            if (item)
            {
                that.popup.close();
                that.select(item.text());
                that.trigger(SELECT, {scope: that.selected});
            }
        }
    });

    Zutubi.admin.HierarchyCrumb = Zutubi.admin.Crumb.extend({
        init: function(element, options)
        {
            Zutubi.admin.Crumb.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaHierarchyCrumb"
        },

        events: [
            SELECT
        ],

        createPopup: function()
        {
            var that = this,
                popup;

            that.popupEl = $("<div class='k-hierarchy-popup'></div>");
            that.selector = that.popupEl.kendoZaHierarchySelector().data("kendoZaHierarchySelector");
            that.selector.bind("nodeselect", function(e)
            {
                that.select(e.name);
                that.popup.close();
                that.trigger(SELECT, {name: e.name});
            });

            popup = that.popupEl.kendoPopup({
                anchor: that.element.closest(".k-navitem"),
                origin: "bottom left",
                position: "top left",
                animation: {
                    open: {
                        effects: "slideIn:down"
                    }
                }
            }).data("kendoPopup");

            return popup;
        },

        select: function(text)
        {
            Zutubi.admin.Crumb.fn.select.call(this, text);
            this.selector.selectItem(text);
        },

        setScope: function(scope)
        {
            this.selector.setScope(scope);
        },

        applyDelta: function(delta)
        {
            var ourPath = this.selector.getScope() + "/" + this.selected,
                newPath;

            this.selector.applyDelta(delta);
            if (delta.renamedPaths)
            {
                newPath = delta.renamedPaths[ourPath];
                if (newPath)
                {
                    this.select(Zutubi.config.subPath(newPath, 1, 2));
                }
            }
        }
    });

    Zutubi.admin.Navbar = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that.isAdmin = options.isAdmin;
            that.createAllowed = [];
            if (options.projectCreateAllowed)
            {
                that.createAllowed.push("projects");
            }
            if (options.agentCreateAllowed)
            {
                that.createAllowed.push("agents");
            }

            that._create();
        },

        options: {
            name: "ZaNavbar",
            itemTemplate: '<span class="k-navitem #: cls #">#= content #</span>'
        },

        events: [
            SCOPE_SELECTED,
            ITEM_SELECTED
        ],

        _create: function()
        {
            var that = this;

            that.itemTemplate = kendo.template(that.options.itemTemplate);

            that.list = $('<div class="k-navlist"></div>');
            that.element.append(that.list);
            that.list.append(that.itemTemplate({cls: '', content: '<a href="' + window.baseUrl + '/admin/">:: pulse admin ::</a>'}));

            that.scopeCrumbItem = $(that.itemTemplate({cls: '', content: ''}));
            that.scopeCrumb = that.scopeCrumbItem.kendoZaScopeCrumb({
                isAdmin: that.isAdmin
            }).data("kendoZaScopeCrumb");
            that.scopeCrumb.bind(SELECT, function(e)
            {
                that._updateAddButton();
                that.trigger(SCOPE_SELECTED, {scope: e.scope});
            });

            that.list.append(that.scopeCrumbItem);

            that.hierarchyCrumbItem = $(that.itemTemplate({cls: '', content: ''}));
            that.hierarchyCrumb = that.hierarchyCrumbItem.kendoZaHierarchyCrumb({}).data("kendoZaHierarchyCrumb");
            that.hierarchyCrumb.bind(SELECT, function(e)
            {
                that.trigger(ITEM_SELECTED, {name: e.name});
            });

            that.list.append(that.hierarchyCrumbItem);

            that.addButtonItem = $(that.itemTemplate({cls: '', content: ''}));
            that.list.append(that.addButtonItem);

            that.list.append(that.itemTemplate({
                cls: 'k-navright',
                content: '<a href="' + window.baseUrl + '/dashboard/"><span class="fa fa-dashboard"></span> dashboard</a>'
            }));
        },

        selectScope: function(scope, hierarchyPath)
        {
            this.scopeCrumb.select(scope);
            if (typeof hierarchyPath === "undefined")
            {
                this.hierarchyCrumbItem.hide();
            }
            else
            {
                this.hierarchyCrumb.setScope(scope);
                this.hierarchyCrumb.select(Zutubi.config.subPath(hierarchyPath, 0, 1));
                this.hierarchyCrumbItem.show();
            }

            this._updateAddButton(scope);
        },

        applyDelta: function(delta)
        {
            this.hierarchyCrumb.applyDelta(delta);
        },

        _updateAddButton: function()
        {
            var that = this,
                scope = that.scopeCrumb.selected,
                title;

            if (that.addButton)
            {
                that.addButton.destroy();
                kendo.destroy(that.addButtonElement);
                that.addButtonElement.remove();

                that.addButton = that.addButtonElement = null;
            }

            if (that.createAllowed.indexOf(scope) >= 0 || (scope === "plugins" && that.isAdmin))
            {
                if (scope === "plugins")
                {
                    title = 'install plugin';
                }
                else
                {
                    title = 'add new ' + scope.substring(0, scope.length - 1);
                }

                that.addButtonElement = $('<button class="k-primary"><span class="fa fa-plus-circle"></span> ' + title + '</button>');
                that.addButtonItem.append(that.addButtonElement);
                that.addButton = that.addButtonElement.kendoButton().data("kendoButton");
                that.addButton.bind("click", jQuery.proxy(that._addClicked, that));
            }
        },

        _addClicked: function()
        {
            this.trigger(ADD, {scope: this.scopeCrumb.selected});
        }
    });

    ui.plugin(Zutubi.admin.Crumb);
    ui.plugin(Zutubi.admin.ScopeCrumb);
    ui.plugin(Zutubi.admin.HierarchyCrumb);
    ui.plugin(Zutubi.admin.Navbar);
}(jQuery));
