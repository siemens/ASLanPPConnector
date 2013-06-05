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

sudo apt-get install -y maven2
sudo apt-get install -y python-software-properties
sudo add-apt-repository -y ppa:ferramroberto/java
sudo apt-get update
sudo apt-get install -y sun-java6-jdk
echo "Please select the SUN option in the following two commands"
sudo update-alternatives --config java
sudo update-alternatives --config javac
