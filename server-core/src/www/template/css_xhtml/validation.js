function clearErrorMessages(form)
{
    var divs = form.getElementsByTagName("div");

    // clear out any rows with an "errorFor" attribute
    var paragraphsToDelete = new Array();

    for (var i = 0; i < divs.length; i++)
    {
        var p = divs[i];
        if (p.getAttribute("errorFor"))
        {
            paragraphsToDelete.push(p);
        }
    }

    // now delete the paragraphsToDelete
    for (var i = 0; i < paragraphsToDelete.length; i++)
    {
        var r = paragraphsToDelete[i];
        var parent = r.parentNode;
        parent.removeChild(r);
    }
}

function clearErrorLabels(form)
{
    // set all labels back to the normal class
    var labels = form.getElementsByTagName("label");
    for (var i = 0; i < labels.length; i++)
    {
        var label = labels[i];
        if (label)
        {
            if (label.getAttribute("class") == "errorLabel")
            {
                label.setAttribute("class", "label");
                //standard way.. works for ie mozilla
                label.setAttribute("className", "label");
                //ie hack cause ie does not support setAttribute
            }
        }
    }

}

function addError(e, errorText)
{
    try
    {
        // clear out any rows with an "errorFor" of e.id
        var div = e.parentNode;
        var error = document.createTextNode(errorText);
        var errorDiv = document.createElement("div");
        var label = div.getElementsByTagName("label")[0];
        label.setAttribute("class", "errorLabel");
        //standard way.. works for ie mozilla
        label.setAttribute("className", "errorLabel");
        //ie hack cause ie does not support setAttribute

        errorDiv.setAttribute("class", "errorMessage");
        //standard way.. works for ie mozilla
        errorDiv.setAttribute("className", "errorMessage");
        //ie hack cause ie does not support setAttribute
        errorDiv.setAttribute("errorFor", e.id);
        ;
        errorDiv.appendChild(error);
        div.insertBefore(errorDiv, label);
    }
    catch (e)
    {
        alert(e);
    }
}
