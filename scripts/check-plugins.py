#! /usr/bin/env python

# Simple script to check that all plugins in the source tree are packaged up
# in at least one package.

import os
import os.path
import sys
import xml.sax
import xml.sax.handler

allPlugins = set()
packagedPlugins = set()

class PackageHandler(xml.sax.handler.ContentHandler):
    def startElement(self, name, attrs):
        if name == "dependency" and attrs.getValue("conf").endswith("->bundle"):
            packagedPlugins.add(attrs.getValue("name"))


class BundleHandler(xml.sax.handler.ContentHandler):
    def startElement(self, name, attrs):
        if name == "info":
            allPlugins.add(attrs.getValue("module"))


def processIvyFiles(dir, handler):
    for root, dirs, files in os.walk(dir):
        for name in files:
            if name == "ivy.xml":
                ivyFile = os.path.join(root, name)
                xml.sax.parse(ivyFile, handler)
        if 'build' in dirs:
            dirs.remove('build')
    

def main():
    scriptDir = os.path.dirname(sys.argv[0])
    topDir = os.path.dirname(scriptDir)
    bundlesDir = os.path.join(topDir, "bundles")
    packageDir = os.path.join(topDir, "package")
    processIvyFiles(packageDir, PackageHandler())    
    processIvyFiles(bundlesDir, BundleHandler())    
    unpackaged = allPlugins - packagedPlugins
    for plugin in unpackaged:
        print plugin
    sys.exit(len(unpackaged))
    

if __name__ == "__main__":
    main()
