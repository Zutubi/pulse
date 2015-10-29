// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        SELECT = "select",
        ADD = "add",
        SCOPE_SELECTED = "scope-selected";

    Zutubi.admin.ScopeSelector = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaScopeSelector"
        },

        events: [
            SELECT
        ],

        _create: function()
        {
            var that = this,
                i;

            that.selected = "projects";

            that.outer = $('<div class="k-split-container"></div>');
            that.mainButton = $('<a class="k-split-button">projects</a>');
            that.outer.append(that.mainButton);
            that.arrowButton = $('<a class="k-split-button-arrow"><span class="k-icon k-i-arrow-s"></span></a>');
            that.outer.append(that.arrowButton);

            that.element.append(that.outer);

            that.scopes = ["projects", "agents"];
            if (that.options.isAdmin)
            {
                that.scopes.push("settings", "users", "groups", "plugins");
            }

            that.popupEl = $("<ul class='selector-popup'></ul>");
            for (i = 0; i < that.scopes.length; i++)
            {
                that.popupEl.append("<li>" + that.scopes[i] + "</li>");
            }

            that.popup = that.popupEl.kendoPopup({
                anchor: that.element,
                origin: "top left",
                position: "bottom left",
                animation: {
                    open: {
                        effects: "slideIn:up"
                    }
                }
            }).data("kendoPopup");

            that.popupEl.on("click", jQuery.proxy(that._popupClicked, that));
            that.mainButton.on("click", jQuery.proxy(that._mainClicked, that));
            that.arrowButton.on("click", jQuery.proxy(that._arrowClicked, that));
        },

        selectScope: function(scope)
        {
            this.selected = scope;
            this.mainButton.text(scope);
        },

        _popupClicked: function(e)
        {
            var that = this,
                target = kendo.eventTarget(e),
                item = $(target).closest("li");

            e.preventDefault();

            if (item)
            {
                that.popup.close();
                that.selectScope(item.text());
                that.trigger(SELECT, {scope: that.selected});
            }
        },

        _mainClicked: function(e)
        {
            e.preventDefault();
            this.trigger(SELECT, {scope: this.selected});
        },

        _arrowClicked: function(e)
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

    Zutubi.admin.Navbar = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

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
            name: "ZaNavbar"
        },

        events: [
            ADD,
            SCOPE_SELECTED
        ],

        _create: function()
        {
            var that = this;

            that.list = $('<ul class="navlist"></ul>');
            that.element.append(that.list);
            that.pulseLink = $('<li><a href="' + window.baseUrl + '/admina/">:: pulse admin ::</a></li>');
            that.list.append(that.pulseLink);

            that.scopeSelectorItem = $('<li></li>');
            that.scopeSelector = that.scopeSelectorItem.kendoZaScopeSelector({
                isAdmin: that.options.isAdmin
            }).data("kendoZaScopeSelector");
            that.scopeSelector.bind(SELECT, function(e)
            {
                that._updateAddButton();
                that.trigger(SCOPE_SELECTED, {scope: e.scope});
            });

            that.list.append(that.scopeSelectorItem);

        },

        selectScope: function(scope)
        {
            this.scopeSelector.selectScope(scope);
            this._updateAddButton(scope);
        },

        _updateAddButton: function()
        {
            var that = this,
                scope = that.scopeSelector.selected;

            if (that.addButton)
            {
                that.addButton.destroy();
                kendo.destroy(that.addButtonItem);
                that.addButtonItem.remove();

                that.addButton = that.addButtonItem = null;
            }

            if (that.createAllowed.indexOf(scope) >= 0)
            {
                that.addButtonItem = $('<li><button class="k-primary"><span class="fa fa-plus-circle"></span> add new ' + scope.substring(0, scope.length - 1) + '</li>');
                that.addButton = that.addButtonItem.find("button").kendoButton().data("kendoButton");
                that.addButton.bind("click", jQuery.proxy(that._addClicked, that));
                that.list.append(that.addButtonItem);
            }
        },

        _addClicked: function()
        {
            this.trigger(ADD, {scope: this.scopeSelector.selected});
        }
    });

    ui.plugin(Zutubi.admin.ScopeSelector);
    ui.plugin(Zutubi.admin.Navbar);
}(jQuery));
