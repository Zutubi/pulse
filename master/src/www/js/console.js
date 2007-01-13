var console = new Object();
console.log = function(message)
{
    debug(message);
}

console.info = console.log;

// Show the debug window
function showDebug() {
  window.top.debugWindow =
      window.open("",
                  "Debug",
                  "left=0,top=0,width=300,height=700,scrollbars=yes,"
                  +"status=yes,resizable=yes");
  window.top.debugWindow.opener = self;
  // open the document for writing
  window.top.debugWindow.document.open();
  window.top.debugWindow.document.write(
      "<HTML><HEAD><TITLE>Debug Window</TITLE></HEAD><BODY><PRE>\n");
}

// If the debug window exists, then write to it
function debug(text) {
  if (window.top.debugWindow && ! window.top.debugWindow.closed) {
    window.top.debugWindow.document.write(text+"\n");
  }
}

// If the debug window exists, then close it
function hideDebug() {
  if (window.top.debugWindow && ! window.top.debugWindow.closed) {
    window.top.debugWindow.close();
    window.top.debugWindow = null;
  }
}

