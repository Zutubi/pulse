// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ns = ".kendoZaNavbar",
        CLICK = "click" + ns,
        SELECT = "select",
        DATA = "data" + ns;

    // A basic item that can be clicked to navigate (to either a URL, or via a click callback).
    //
    // options:
    //   - model: argument for the template (required unless create() is overridden in a subclass)
    //     - click: if the model has a click property, this will be invoked when the item is clicked
    //   - template: override the default template (the default creates a link with a url and content)
    Zutubi.core.NavbarItem = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that.template = kendo.template(that.options.template);
            that.create();
        },

        options: {
            name: "ZaNavbarItem",
            template: '<a href="#= url #">#= content #</a>'
        },

        create: function()
        {
            var model = this.options.model;

            this.innerElement = $(this.template(model));
            this.element.append(this.innerElement);
            if (typeof model.click !== "undefined")
            {
                this.innerElement.on(CLICK, jQuery.proxy(model.click, this));
            }
        },

        destroy: function()
        {
            this.innerElement.off(ns);
            Widget.fn.destroy.call(this);
        },

        hide: function()
        {
            this.element.hide();
        },

        show: function()
        {
            this.element.show();
        },

        applyDelta: function(delta)
        {
            // Default does nothing.
        }
    });

    // An item that presents a dropdown menu of textual items.  Items can be either plain links or raise select events
    // on click.  The content of the item can be set in the original model and/or updated to the text of the selected
    // item.
    //
    // You can use this class as a base for other navbar items with popups that are not simple menus by overriding
    // hasPopup and createPopup as desired.  In that case no items are required, or they can be repurposed.
    //
    // options:
    //   - items: array of items. Each one may be just text (will be escaped when creating the menu), or an object with
    //            text plus other optional properties:
    //     - url: url the item links to (if not provided a select event is raised on item click)
    //     - inactive: if set to true, clicking on the item does nothing (ignored if a url is provided)
    //   - model: model passed to template, may define content (HTML) to populate the item
    //   - updateOnSelect: if true, the content of the item is updated to the text of the selected item on select
    //   - itemTemplate: template used to create menu items (when the item has no URL)
    //   - urlItemTemplate: template used to create menu items (when the item has a url)
    Zutubi.core.MenuNavbarItem = Zutubi.core.NavbarItem.extend({
        options: {
            name: "ZaMenuNavbarItem",
            template: '<div class="k-split-container"><a class="k-split-button">#= content #</a><a class="k-split-button-arrow"><span class="fa fa-caret-down"></span></a></div>',
            model: {
                content: ''
            },
            updateOnSelect: true,
            itemTemplate: '<li><span class="k-selector-popup-item">#: text #</span></li>',
            urlItemTemplate: '<li><a class="k-selector-popup-item" href="#= url #">#: text #</a></li>'
        },

        events: [
            SELECT
        ],

        create: function()
        {
            var that = this;

            that.itemTemplate = kendo.template(that.options.itemTemplate);
            that.urlItemTemplate = kendo.template(that.options.urlItemTemplate);

            Zutubi.core.NavbarItem.fn.create.call(that);

            that.mainButton = that.innerElement.find(".k-split-button");
            that.arrowButton = that.innerElement.find(".k-split-button-arrow");

            if (that.hasPopup())
            {
                that.mainButton.on(CLICK, jQuery.proxy(that._clicked, that));
                that.arrowButton.on(CLICK, jQuery.proxy(that._clicked, that));

                that.popup = that.createPopup();
            }
            else
            {
                that.arrowButton.hide();
            }
        },

        destroy: function()
        {
            this.mainButton.off(ns);
            this.arrowButton.off(ns);
            if (this.popupEl)
            {
                this.popupEl.off(ns);
            }
            Zutubi.core.NavbarItem.fn.destroy.call(this);
        },

        hasPopup: function()
        {
            return this.options.items && this.options.items.length > 0;
        },

        createPopup: function()
        {
            var that = this,
                i,
                items = that.options.items,
                item,
                model,
                template,
                el,
                anchor,
                side,
                popup;

            that.popupEl = $("<ul class='k-selector-popup'></ul>");
            for (i = 0; i < items.length; i++)
            {
                item = items[i];
                if (typeof item === "string")
                {
                    model = {
                        text: item
                    }
                }
                else
                {
                    model = item;
                }

                template = model.url ? that.urlItemTemplate : that.itemTemplate;
                el = $(template(model));
                el.data(DATA, model);
                that.popupEl.append(el);
            }

            anchor = that.element.closest(".k-navitem");
            side = anchor.hasClass("k-navright") ? "right" : "left";
            popup = that.popupEl.kendoPopup({
                anchor: anchor,
                origin: "bottom " + side,
                position: "top " + side
            }).data("kendoPopup");

            that.popupEl.on(CLICK, jQuery.proxy(that._itemClicked, that));
            return popup;
        },

        select: function(text)
        {
            this.selected = text;
            if (this.options.updateOnSelect)
            {
                this.mainButton.text(text);
            }
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
        },

        _itemClicked: function(e)
        {
            var that = this,
                target = kendo.eventTarget(e),
                el = $(target).closest("li"),
                item;

            if (el.length > 0)
            {
                item = el.data(DATA);
                console.dir(item);
                if (item && !item.url && !item.inactive)
                {
                    e.preventDefault();
                    that.popup.close();
                    that.select(item.text);
                    that.trigger(SELECT, {scope: that.selected});
                }
            }
        }
    });

    // An item that displays a button with icon and text that raises a click event when pressed.
    //
    // options:
    //   - model: required model passed to the template, should define spriteCssClass and title for the default template
    //   - template: can be set to override the default template
    Zutubi.core.ButtonNavbarItem = Zutubi.core.NavbarItem.extend({
        options: {
            name: "ZaButtonNavbarItem",
            template: '<button class="k-primary"><span class="#= spriteCssClass #"></span> <span class="k-button-navitem-title">#: title #</span></button>'
        },

        events: [
            "click"
        ],

        create: function()
        {
            var that = this;

            Zutubi.core.NavbarItem.fn.create.call(that);

            that.button = that.innerElement.kendoButton().data("kendoButton");
            that.button.bind("click", function(e) { that.trigger("click", e); });
        },

        setTitle: function(title)
        {
            this.element.find(".k-button-navitem-title").html(kendo.htmlEncode(title));
        }
    });

    // A bar for top-level navigation of the UI, which sits right at the top of the screen and has some fixed items at
    // both left and right ends, plus additional items between based on context.
    //
    // options:
    //   - userName: name of the logged in user (unset if the user is a guest)
    //   - userCanLogout: set to true if the current user may logout
    //   - itemTemplate: template used to make elements on which items are instantiated (passed cls)
    //   - extraItems: optional array of extra item configurations, each of which may contain:
    //       - type: the item widget name (e.g. ZaMenuNavbarItem)
    //       - cls: cls to pass to itemTemplate when making the element for the item
    //       - options: passed to the item initialiser
    Zutubi.core.Navbar = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that.create();

            if (options.section)
            {
                that.setSection(options.section);
            }
        },

        options: {
            name: "ZaNavbar",
            itemTemplate: '<span class="k-navitem #: cls #"></span>'
        },

        create: function()
        {
            var that = this,
                sectionItems = [],
                userItems = [];

            that.itemTemplate = kendo.template(that.options.itemTemplate);

            if (that.options.userName)
            {
                sectionItems.push({
                    text: "dashboard",
                    url: "/dashboard/"
                }, {
                    text: "my builds",
                    url: "/dashboard/my/"
                });

                userItems.push({
                    text: that.options.userName,
                    inactive: true
                }, {
                    text: "preferences",
                    url: "/dashboard/preferences/"
                });

                if (that.options.userCanLogout)
                {
                    userItems.push({
                        text: "logout",
                        url: "/logout"
                    });
                }
            }
            else
            {
                userItems.push({
                    text: "Guest",
                    inactive: true
                }, {
                    text: "login",
                    url: "/login!input.action"
                });
            }

            sectionItems.push({
                text: "projects",
                url: "/browse/"
            }, {
                text: "server",
                url: "/server/"
            }, {
                text: "agents",
                url: "/agents/"
            }, {
                text: "administration",
                url: "/admin/"
            });

            that.list = $('<div class="k-navlist"></div>');
            that.element.append(that.list);

            that.homeItem = that._addSimpleItem({
                url: window.baseUrl + '/',
                content: '<span class="fa fa-heartbeat"></span>'
            });

            that.sectionItem = that._addItemElement().kendoZaMenuNavbarItem({
                items: sectionItems
            }).data("kendoZaMenuNavbarItem");

            that.userItem = that._addItemElement("k-navright").kendoZaMenuNavbarItem({
                model: {
                    content: '<span class="fa fa-user"></span>'
                },
                items: userItems
            }).data("kendoZaMenuNavbarItem");

            that.helpItem = that._addSimpleItem({
                url: "#",
                click: function(e)
                {
                    var popup;

                    e.preventDefault();

                    popup = window.open(window.baseUrl + '/popups/reference.action', '_pulsereference', 'status=yes,resizable=yes,top=100,left=100,width=900,height=600,scrollbars=yes');
                    popup.focus();
                },
                content: '<span class="fa fa-question-circle"></span>'
            });

            that._createExtraItems();
        },

        _addItemElement: function(cls)
        {
            return $(this.itemTemplate({cls: cls || ''})).appendTo(this.list);
        },

        _addSimpleItem: function(model, cls)
        {
            var el = this._addItemElement(cls);
            el.kendoZaNavbarItem({model : model}).data("kendoZaNavbarItem");
        },

        _createExtraItems: function()
        {
            var that = this,
                items = this.options.extraItems,
                i;

            this.extraItems = [];
            if (items)
            {
                for (i = 0; i < items.length; i++)
                {
                    that.addExtraItem(items[i]);
                }
            }
        },

        addExtraItem: function(item)
        {
            var el,
                type,
                result;

            el = $(this.itemTemplate({cls: item.cls || ''}));
            el.insertBefore(this.list.find(".k-navright"));
            type = item.type || "kendoZaNavbarItem";
            result = el[type](item.options || {}).data(type);
            this.extraItems.push(result);

            return result;
        },

        setSection: function(section)
        {
            this.sectionItem.select(section);
        },

        applyDelta: function(delta)
        {
            var i;
            for (i = 0; i < this.extraItems.length; i++)
            {
                this.extraItems[i].applyDelta(delta);
            }
        }
    });

    ui.plugin(Zutubi.core.NavbarItem);
    ui.plugin(Zutubi.core.MenuNavbarItem);
    ui.plugin(Zutubi.core.ButtonNavbarItem);
    ui.plugin(Zutubi.core.Navbar);
}(jQuery));
