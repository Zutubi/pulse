#! /usr/bin/env python

import os
import subprocess
import sys
import time
import random

def log(message):
    print '%s: %s' % (time.strftime('%d %b %Y %H:%M:%S'), message)

def walktree(top = ".", depthfirst = True):
    """Walk the directory tree, starting from top. Credit to Noah Spurrier and Doug Fort."""
    import os, stat, types
    names = os.listdir(top)
    if not depthfirst:
        yield top, names
    for name in names:
        try:
            st = os.lstat(os.path.join(top, name))
        except os.error:
            continue
        if stat.S_ISDIR(st.st_mode):
            for (newtop, children) in walktree (os.path.join(top, name), depthfirst):
                yield newtop, children
    if depthfirst:
        yield top, names    
    
if __name__ == "__main__":
    
    listing = []
    for base, names in walktree():
        for name in names:
            path = os.path.join(base, name)
            if os.path.isfile(path):
                if path[len(path) - 5:] == '.java':
                    listing.append(path)

    log(len(listing))
    copyright = '/********************************************************************************\n * Copyright (c) 2005-2007, Zutubi Pty Ltd.\n ********************************************************************************/\n '
    for filename in listing:
        log(filename)
        content = ''
        f = open(filename, 'r')
        for line in f.readlines():
            content = content + line
        f.close()

        f = open(filename, 'w')
        f.write(copyright)
        f.write('\n')
        f.write(content)
        f.close()
