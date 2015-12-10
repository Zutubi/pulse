//dependency: ext/package.js
//dependency: ./namespace.js
//dependency: ./FloatManager.js

/**
 * Provides central management of pop-down menus.  Implements lazy, cached rendering, and allows
 * menus to be toggled from simple onclick handlers.  To place a menu button in the dom, you must:
 * 
 * - create a link with id <menu id>-link
 * - set the link's onclick handler to "Zutubi.MenuManager.toggleMenu(this); return false"
 * - wrap that link around an empty image with id <menu id>-button and class "popdown floating-widget"
 *
 * This allows the menu manager to find/render the corresponding menu and place it on the page.
 */
Zutubi.MenuManager = (function() {
    var menusById = {};
    
    function appendMenuItem(el, menuId, item)
    {
        var child, childEl;

        if (!item.title)
        {
            item.title = item.id;
        }

        child = {
            tag: 'a',
            id: item.id + '-' + menuId,
            cls: 'unadorned',
            href: '#',
            title: item.title,
            children: [{
                tag: 'img',
                src: window.baseUrl + '/images/' + item.image
            }, ' ' + item.title]
        };

        if (item.url !== undefined)
        {
            child.href = window.baseUrl + '/' + item.url;
        }
        if (typeof item.onclick === "string")
        {
            child.onclick = item.onclick;
        }

        childEl = el.createChild({tag: 'li', children: [child]});

        if (typeof item.onclick === "function")
        {
            childEl.dom.onclick = item.onclick;
        }
    }

    function renderMenu(id)
    {
        var menu, menuEl, listEl, items, i;

        menu = menusById[id];
        if (menu)
        {
            menuEl = menu.el;
            if (!menuEl)
            {
                menuEl = Ext.getBody().createChild({tag: 'div',  id: id, style: 'display: none'});
                listEl = menuEl.createChild({tag: 'ul', cls: 'actions'});
                items = menu.itemCallback();
                for (i = 0; i < items.length; i++)
                {
                    appendMenuItem(listEl, id, items[i]);
                }
            }
        
            menu.el = menuEl;            
        }
    
        return menu;
    }

    return {
        /**
         * Registers a menu with this manager.
         *
         * @param id           a unique identifier for the menu
         * @param itemCallback a function that when called will return an array of item configs for
         *                     the menu (configs must contain an id, image and link, and may also
         *                     contain a title, href, and/or onclick)
         * @param imageClass   if provided, the class of the button image (defaults to popdown)
         */
        registerMenu: function(id, itemCallback, imageClass)
        {
            menusById[id] = {itemCallback: itemCallback, imageClass: imageClass};
        },
        
        /**
         * Toggles the display of a menu.  This should be called from an anchor's onclick handler,
         * with the Link object passed, e.g.:
         *
         * <a id="my-menu-link" onclick="Zutubi.MenuManager.toggleMenu(this); return false">
         *
         * @param link the anchor item clicked on, the id of which is used to derive the menu id
         */
        toggleMenu: function(link)
        {
            var id, buttonId, button, menu;

            id = link.id.replace(/-link$/, '');
            buttonId = id + '-button';
            button = Ext.get(buttonId);
            if (button && !button.hasClass('x-item-disabled'))
            {
                menu = renderMenu(id);
                Zutubi.FloatManager.showHideFloat('menus', id, 'tl-bl?', menu.imageClass);
            }
        }
    };
}());
