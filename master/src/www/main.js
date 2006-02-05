function activateTab(tab)
{
    document.getElementById(tab + "-tab").className = "active";
}

function deactivateTab(tab)
{
    document.getElementById(tab + "-tab").className = "";
}

function hideLayer(whichLayer)
{
    document.getElementById(whichLayer).style.display = "none";
    deactivateTab(whichLayer);
}

function showLayer(whichLayer)
{
    document.getElementById(whichLayer).style.display = "block";
    activateTab(whichLayer);
}


//function chooseTab(tab)
//{
//    if (tab == "current-build")
//    {
//        showLayer("current-build");
//        hideLayer("history");
//        hideLayer("configuration");
//    }
//    else if (tab == "history")
//    {
//        hideLayer("current-build");
//        showLayer("history");
//        hideLayer("configuration");
//    }
//    else if (tab == "configuration")
//    {
//        hideLayer("current-build");
//        hideLayer("history");
//        showLayer("configuration");
//    }
//}
