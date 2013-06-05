#!/bin/bash
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

rm -f aslanpp-translator/target/aslanpp-*-jar-with-dependencies.jar
rm -f aslanpp-translator/target/aslanpp-*.war
mvn -DincludeReferenceTrailInErrors install #-Dmaven.test.skip=true
mkdir -p ../binaries
cp aslanpp-translator/target/aslanpp-*-jar-with-dependencies.jar         ../binaries/aslanpp-connector.jar
cp aslanpp-translator/target/aslanpp-*.war                               ../binaries/aslanpp-connector.war
cp aslanpp-tester/target/aslanpp-*-jar-with-dependencies.jar             ../binaries/aslan-tester.jar
cp aslan-xml/target/aslan-*-jar-with-dependencies.jar                    ../binaries/aslan-xml.jar
cp aslan-xml/target/aslan-*.war                                          ../binaries/aslan-xml.war
cp aslan-xml-wsclient/target/aslan-*-jar-with-dependencies.jar           ../binaries/aslan-xml-wsclient.jar
cp aslanpp-connector-wsclient/target/aslanpp-*-jar-with-dependencies.jar ../binaries/aslanpp-connector-wsclient.jar
cp aslan-core/target/aslan-*-jar-with-dependencies.jar                   ../binaries/aslan-core.jar
cp aslan-core/target/aslan-*-javadoc.jar                                 ../binaries/aslan-core-javadoc.jar
