// hack in a namespace
if (!window.ZUTUBI)
{
    var ZUTUBI = new Object();
}

ZUTUBI.fs = new Object();

ZUTUBI.fs.list = function(node)
{
    // take node data, and request a listing from the server.
    var loadById = node.data && node.data.id || "";

    // bypass loading of the virtual node.
    if (loadById == "virtualNode")
    {
        node.loadComplete();
        return;
    }

    // generate id path.
    var path = "";
    var sep = "";
    while (node)
    {
        if (node.data)
        {
            path = node.data.id + sep + path;
            sep = "/";
        }
        node = node.parent;
    }

    var ajax = new Ajax.Request(
        "http://localhost:8080/ajax/list.action",
        {
            method: 'get',
            onComplete: ZUTUBI.fs.listResponse,
            onFailure: handleFailure,
            onException: handleException,
            parameters: "pid=" + path //+ "&dirOnly=true"
        }
    );
}

// process the response from the server, creating nodes from the
// respones and adding them to the specified node.
ZUTUBI.fs.listResponse = function(response)
{
//    console.log("process data from server");
    var jsonObjs = eval("(" + response.responseText + ")");

    var tree = ZUTUBI.widget.View.getTreeById('browse');

    var results = $A(jsonObjs.results);
    results.each(function(jsonObj)
    {
        // locate the node identified by the server uid. This node will become the parent.
        // need access to all of the trees to look up the node.
        var parentNode = ZUTUBI.fs.getNode(jsonObj.uid, tree);
        if (!parentNode)
        {
            console.log("WARNING: failed to locate node '%s'", jsonObj.uid);
            return;
        }

        // create the new nodes and add them to the parent.
        for (var i = 0; i < jsonObj.listing.length; i++)
        {
            // sanity check that we do not add components to the model a second time. This should be
            // caught at an earlier stage.
            var existingNode = $A(parentNode.getChildren()).find(function(child)
            {
                return (child.data.id == jsonObj.listing[i].fid);
            });
            if (existingNode)
            {
                console.log("WARNING: skipping adding node a second time. UID: '%s'", jsonObj.listing[i].uid);
                continue;
            }

            var newNode = new ZUTUBI.widget.FileNode(parentNode, false);
            newNode.data = {
                "id":jsonObj.listing[i].fid,
                "name":jsonObj.listing[i].file,
                "type":jsonObj.listing[i].type,
                "separator":jsonObj.listing[i].separator
            };
            newNode.isLeaf = !newNode.isFolder();
        }
        // now we can mark the node as loaded.
        parentNode.loadComplete();
    });
}

ZUTUBI.fs.virtualAdd = function(node, folderName)
{
    if (node.expanded || node.isLoaded)
    {
        // create new node.
        var newNode = new ZUTUBI.widget.FileNode(node);
        newNode.data = {
            "id":"virtualNode",
            "name":folderName,
            "type":"folder"
        };
        newNode.isLeaf = !newNode.isFolder();

        // add and refresh.
        node._invalidate();
    }

    // open display to newly created node.
    node._expand();

}

ZUTUBI.fs.add = function(node, folderName)
{
    console.log("INFO: add: %s %s", node.getPath(), folderName);

    var loadById = node.data && node.data.id || "";

    var ajax = new Ajax.Request(
        "http://localhost:8080/ajax/add.action",
        {
            method: 'get',
            onComplete: ZUTUBI.fs.addResponse,
            onFailure: handleFailure,
            onException: handleException,
            parameters: "encodedPath=" + loadById +"&dirName=" + folderName
        }
    );
}

ZUTUBI.fs.addResponse = function(response)
{
    console.log("processing response for add new folder.");

    var jsonObj = eval("(" + response.responseText + ")");
    var tree = ZUTUBI.widget.View.getTreeById('browse');
    console.log("ParentUID: %s", jsonObj.parentuid);

    var parentNode = ZUTUBI.fs.getNode(jsonObj.parentuid, tree);
    if (!parentNode)
    {
        console.log("WARNING: unable to locate parent node.");
        return;
    }

    // only need to add this to the tree if the node is expanded. In fact, we should expand the node and node
    // it that way...
    if (parentNode.expanded || parentNode.isLoaded)
    {
        // create new node.
        var newNode = new ZUTUBI.widget.FileNode(parentNode);
        newNode.data = {
            "id":jsonObj.uid,
            "name":jsonObj.file,
            "type":jsonObj.type
        };
        newNode.isLeaf = !newNode.isFolder();

        // add and refresh.
        parentNode._invalidate();
    }

    // open display to newly created node.
    parentNode._expand();

    // set selected node.
}

ZUTUBI.fs.deleteFile = function(node)
{
    console.info("deleteFile: %s", node.getPath());

    if (!confirm("Are you sure you want to delete this file?"))
    {
        return;
    }

    var id = node.data.id;
    var ajax = new Ajax.Request(
        "http://localhost:8080/ajax/delete.action",
        {
            method: 'get',
            onComplete: ZUTUBI.fs.deleteFileResponse,
            onFailure: handleFailure,
            onException: handleException,
            parameters: "encodedPath=" + id
        }
    );
}

ZUTUBI.fs.deleteFileResponse = function(response)
{
    var jsonObj = eval("(" + response.responseText + ")");
    var tree = ZUTUBI.widget.View.getTreeById('browse');

    console.info("deleteFileResponse: %s", jsonObj.uid);

    // retrieve the node identified by the uid.
    var node = ZUTUBI.fs.getNode(jsonObj.uid, tree);
    if (!node)
    {
        console.log("WARNING: unable to locate node.");
        return;
    }

    // delete the node
    tree.removeNode(node);
}

ZUTUBI.fs.getNode = function(uid, tree)
{
    // locate the node identified by the server uid. This node will become the parent.
    // need access to all of the trees to look up the node.

    if (!uid)
    {
        return tree.root;
    }

    var n = tree.root;
    var pathElements = $A(uid.split("/"));
    pathElements.each(function(path)
    {
        var c = $A(n.children).find(function(child)
        {
            return (child.data.id == path);
        });
        if (c)
        {
            n = c;
        }
        else
        {
            // return virtual node?
            return null;
        }
    });

    return n;
};

handleFailure = function(e, e2)
{
    console.log("handleFailure: %s, %s", e, e2);
    alert("handleFailure");
}

handleException = function(e, e2)
{
    console.log("handleException: %s, %s", e, e2)
    alert("handleException: " + e2);
}
