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

mvn clean
#echo >> aslanpp-connector-wsclient/src/main/java/org/avantssar/aslanpp/client/Main.java; cd aslanpp-connector-wsclient; mvn -DincludeReferenceTrailInErrors install; cd .. #do any change to the file, as a potential workaround for strange mvn compliation error: workaround for strange mvn compliation error: aslanpp-connector-wsclient/src/main/java/org/avantssar/aslanpp/client/Main.java:[121,14] error: cannot find symbol; variable result of type TranslatorOutput"
./build.sh
