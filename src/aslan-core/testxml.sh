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

if [ $# != 2 ]; then
    echo "Usage: $0 <directory to scan for ASLan++ models> <aslan-xml jar>"
    exit
fi

XSD=`readlink -f src/main/resources/aslan-xml.xsd`
TARGET=`readlink -f $1`
JAR=`readlink -f $2`
VALIDATOR=`readlink -f validate.rb`
echo "Testing in directory $TARGET"
echo "Using JAR $JAR"
cd $TARGET
for file in `dir -1 *.aslan++`; do
    MODEL=`basename $file .aslan++`
    make $MODEL.aslan
    if [ -f $MODEL.aslan ]; then
	java -jar $JAR -o $MODEL.xml $MODEL.aslan
	java -jar $JAR -x -o $MODEL.back.aslan $MODEL.xml
	diff $MODEL.aslan $MODEL.back.aslan
	ruby $VALIDATOR $XSD $MODEL.xml
    fi
    echo $MODEL
done
make clean
svn update
