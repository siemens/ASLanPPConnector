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

VAL=../src/aslan-core/validate.rb
SCHEMA=aslan-xml.xsd
CLATSE=../../../src/inria/cl-atse_i486-linux
SKIPPP=0
SKIP=0

function doit {
    CONN=$1
    XML=$2
    INPUT=$3
    DIR=$4
    echo "ASLan++ connector:   $CONN"
    echo "ASLan-XML converter: $XML"
    echo "Input file:     $INPUT"
    echo "Output directory:    $DIR"
    echo "-----------------------------------------------"
    FILEPP=`basename $INPUT`
    BASE=`basename $INPUT .aslan++`
    rm -rf $DIR/$BASE.*
    echo "  Translating..."
    java -jar $CONN -o $DIR/$BASE.aslan $INPUT
    cat $DIR/$BASE.aslan
    echo "  Checking ASLan..."
    java -jar $XML -c $DIR/$BASE.aslan
    echo "  Converting ASLan to XML..."
    java -jar $XML -o $DIR/$BASE.xml $DIR/$BASE.aslan
    cat $DIR/$BASE.xml
    echo "  Checking XML..."
    ruby $VAL $SCHEMA $DIR/$BASE.xml
    echo "  Converting ASLan back to plaintext..."
    java -jar $XML -x -o $DIR/$BASE.back.aslan $DIR/$BASE.xml
    echo "  Checking ASLan differences..."
    diff $DIR/$BASE.aslan $DIR/$BASE.back.aslan
    echo "  Running CL-AtSe..."
    $CLATSE $DIR/$BASE.aslan > $DIR/$BASE.clatse.txt
    cat $DIR/$BASE.clatse.txt
    echo "  Translating back result..."
    java -jar $CONN $DIR/$BASE.aslan -ar $DIR/$BASE.clatse.txt -o $DIR/$BASE.result.txt
    cat $DIR/$BASE.result.txt
    echo "  Done."
    echo
}

function doboth {
    INPUT=$1
    doit 'aslanpp-connector.jar'          'aslan-xml.jar'          $INPUT "/tmp"
    doit 'aslanpp-connector-wsclient.jar' 'aslan-xml-wsclient.jar' $INPUT "/tmp"
}

#doboth '../../../case-studies/test-library'
#doboth '../../../case-studies/misc/testsuite'
#doboth '../../../case-studies/test-library/PublicBidding'
doboth '../../../case-studies/misc/testsuite/NSPK_Unsafe.aslan++'
