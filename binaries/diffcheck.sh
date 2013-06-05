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

ASLANPP_OLD=aslanpp-connector.previous.jar
ASLANPP=aslanpp-connector.jar
ROOT=../../../case-studies/
NOPP=1

function checkone {
    FILE=$1
    echo $FILE
    BASE=`basename $1 .aslan++`
    if [ ! "$NOPP" -eq 1 ]; then
	OLDPP="${BASE}.old.aslan++"
	NEWPP="${BASE}.new.aslan++"
	echo "  pretty-printing with old parser into $OLDPP"
	java -jar $ASLANPP_OLD -o $OLDPP -gas -pp $FILE
	echo "  pretty-printing with new parser into $NEWPP"
	java -jar $ASLANPP -o $NEWPP -gas -pp $FILE
	echo "  checking differences"
	diff $OLDPP $NEWPP
    fi
    OLD="${BASE}.old.aslan"
    NEW="${BASE}.new.aslan"
    echo "  translating with old parser into $OLD"
    java -jar $ASLANPP_OLD -o $OLD -gas $FILE
    echo "  translating with new parser into $NEW"
    java -jar $ASLANPP -o $NEW -gas $FILE
    echo "  checking differences"
    diff $OLD $NEW
    echo
}

#checkone "/home/gabi/sandbox/avantssar/trunk/shared/case-studies/misc/testsuite/Math_Unsafe.aslan++"
#exit
rm -f $ASLANPP_OLD
cp $ASLANPP a.jar
svn revert $ASLANPP
cp $ASLANPP $ASLANPP_OLD
mv a.jar $ASLANPP
FILES=`find $ROOT -name '*.aslan++' -print`
for i in $FILES
do
    if [ `echo $i | grep testing | wc -l` -eq 0 ]; then
	checkone $i
    fi
done
