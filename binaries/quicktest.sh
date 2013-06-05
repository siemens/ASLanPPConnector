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
SKIPPP=0
SKIP=0

function doit {
    CONN=$1
    XML=$2
    INPUT=$3
    DIR=$4
    echo "ASLan++ connector:   $CONN"
    echo "ASLan-XML converter: $XML"
    echo "Input directory:     $INPUT"
    echo "Output directory:    $DIR"
    echo "-----------------------------------------------"
    rm -rf $DIR
    mkdir $DIR
    if [ ! "$SKIPPP" -eq "1" ]; then
	ALLPP=`find $INPUT -name '*.aslan++' -print`
	echo "Copying ASLan++ files..."
	cp -v $ALLPP $DIR
	echo "Done."
	echo "-----------------------------------------------"
	export ASLANPATH=`readlink -f $DIR`
	for i in $ALLPP
	do
	    echo $i
	    if [ ! `grep specification $i | wc -l` -eq 0 ]; then
		FILEPP=`basename $i`
		BASE=`basename $i .aslan++`
		echo "  Translating..."
		java -jar $CONN -o $DIR/$BASE.aslan $DIR/$FILEPP
		echo "  Checking ASLan..."
		java -jar $XML -c $DIR/$BASE.aslan
		echo "  Converting ASLan to XML..."
		java -jar $XML -o $DIR/$BASE.xml $DIR/$BASE.aslan
		echo "  Checking XML..."
		ruby $VAL $SCHEMA $DIR/$BASE.xml
		echo "  Converting ASLan back to plaintext..."
		java -jar $XML -x -o $DIR/$BASE.back.aslan $DIR/$BASE.xml
		echo "  Checking ASLan differences..."
		diff $DIR/$BASE.aslan $DIR/$BASE.back.aslan
		echo "  Done."
	    else
		echo "  Probably a module. Skipping."
	    fi
	echo
	done
	echo "-----------------------------------------------"
    fi
    if [ ! "$SKIP" -eq "1" ]; then
	ALL=`find $INPUT -name '*.aslan' -print`
	echo "Copying ASLan files..."
	cp -v $ALL $DIR
	echo "Done."
	echo "-----------------------------------------------"
	for i in $ALL
	do
	    echo $i
	    FILE=`basename $i`
	    BASE=`basename $i .aslan`
	    echo "  Checking ASLan..."
	    java -jar $XML -c $DIR/$BASE.aslan
	    echo "  Converting ASLan to XML..."
	    java -jar $XML -o $DIR/$BASE.xml $DIR/$BASE.aslan
	    echo "  Checking XML..."
	    ruby $VAL $SCHEMA $DIR/$BASE.xml
	    echo "  Converting ASLan back to plaintext..."
	    java -jar $XML -x -o $DIR/$BASE.back.aslan $DIR/$BASE.xml
	    echo "  Converting again ASLan to XML..."
	    java -jar $XML -o $DIR/$BASE.back.xml $DIR/$BASE.back.aslan
	    echo "  Checking again XML..."
	    ruby $VAL $SCHEMA $DIR/$BASE.back.xml
	    echo "  Converting again ASLan back to plaintext..."
	    java -jar $XML -x -o $DIR/$BASE.back.back.aslan $DIR/$BASE.back.xml
	    echo "  Checking ASLan differences..."
	    diff $DIR/$BASE.back.aslan $DIR/$BASE.back.back.aslan
	    echo "  Done."
	    echo
	done
    fi
    echo
}

function doboth {
    INPUT=$1
    BASEDIR=`basename $1`
    doit 'aslanpp-connector.jar'          'aslan-xml.jar'          $INPUT "${BASEDIR}_offline"
    doit 'aslanpp-connector-wsclient.jar' 'aslan-xml-wsclient.jar' $INPUT "${BASEDIR}_online"
}

#doboth '../../../case-studies/test-library'
#doboth '../../../case-studies/misc/testsuite'
#doboth '../../../case-studies/test-library/PublicBidding'
doboth '../../../case-studies/Portals/CRP'
