# Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

ASLanPPConnector
================

Translator from the ASLan++ specification language to ASLan (and back), 
developed in the AVANTSSAR and SPaCIoS EU projects


RUNNING THE TRANSLATOR
======================

$ java -jar aslanpp-connector.jar -h

for initial help.


GETTING STARTED WITH THE SOURCES
================================

The source code of the translator is located under the "src" folder.
The translator is written in Java and uses ANTLR and StringTemplate
libraries. You will need maven for building it and you can use Eclipse 
for editing the code.

Building the translator
-----------------------

Go into the "src" folder and run
    ./build.sh

You need to have maven installed for this. You should be able to install 
it through you system's package manager, or from http://maven.apache.org/.

maven will automatically download all needed libraries. If the build script 
runs successfully, you will find in the "binaries" folder a jar file 
which contains the compiled code of the translator, together with all needed
libraries.

Try to not commit binaries, except the fully packaged jar that results from
the build process. maven keeps all generated binaries into a "src/*/target"
folder.

Don't forget to increment the version number in the POM file after each new 
release.

Editing the source code
-----------------------

You can use Eclipse for editing the code of the translator. Just set your
Eclipse workspace into this (".") directory, and import into it the existing
project from the "src" folder. You will need to add the M2_REPO variable
that points to the folder where maven keeps its downloaded libraries. This
should be "~/.m2/repository/" under Linux.

If you need to re-generate the Eclipse project files, go into the "src"
folder and type
    mvn eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true

The translator is based on:
- ANTLR: http://www.antlr.org/
- StringTemplate: http://www.stringtemplate.org/

There are chances that there exist Eclipse plugins for ANTLR and/or
StringTemplate, however I didn't search for them. I used Eclipse only as a 
facility to easier edit the code, and I did all building from the command
line. However, this means that after a build (when maven regenerates the
classes based on ANTLR grammars) you will need to do a refresh in Eclipse
to see the latest version of the code.
