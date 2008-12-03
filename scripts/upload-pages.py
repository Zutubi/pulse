import sys
import os
from getpass import getpass
from xmlrpclib import *

CONFLUENCE_URL = "http://confluence.zutubi.com/rpc/xmlrpc"
PARENT_PAGE = 'Remote API Functions'
DEST_SPACE = 'pulse0200'
EXCLUDE_AREAS = ['Early Access Program', 'Release Notes', 'Remote API Guide']
DIR = 'build/docs/api'


def fatal(message):
    print >> sys.stderr, message
    sys.exit(1)


def getExistingPage(server, token, title):
    try:
        return server.confluence1.getPage(token, DEST_SPACE, title)
    except Error:
        return None


def uploadPages(server, token):
    destParentPage = server.confluence1.getPage(token, DEST_SPACE, PARENT_PAGE)
    parentId = destParentPage["id"]
    for name in os.listdir(DIR):
        title = "RemoteApi." + name
        file = open(DIR + '/' + name, 'r')
        content = file.read()
        file.close()
        existingPage = getExistingPage(server, token, title)
        if existingPage:
            if existingPage["content"] == content:
                print "Unchanged: " + title
            else:
                print "Updated  : " + title
                existingPage["content"] = content
                server.confluence1.storePage(token, existingPage)
        else:
            print "New     : " + title
            newPage = {"parentId": parentId, "space": DEST_SPACE, "title": title, "content": content}
            server.confluence1.storePage(token, newPage)
    
        
def main(user):
    password = getpass("Pulse password for %s: " % user)
    server = ServerProxy(CONFLUENCE_URL)
    token = ''
    
    try:
        token = server.confluence1.login(user, password)
        uploadPages(server, token)
    except Error, v:
        print "Error:", v
    finally:
        if token:
            server.confluence1.logout(token)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print >> sys.stderr, "Usage: %s <username>" % sys.argv[0]
        sys.exit(1)

    main(sys.argv[1])
