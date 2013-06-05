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

INDIR=../../../case-studies/misc/testsuite
OUTDIR=/tmp/oftest
ASLANPP=aslanpp-connector.jar
CLATSE=../../inria/cl-atse_i486-linux

function checkone {
    FILE=$1
    echo $FILE
    BASE=`basename $1 .aslan++`
    ASLAN="$OUTDIR/$BASE.aslan"
    ATK="$OUTDIR/$BASE.atk"
    RES="$OUTDIR/$BASE.result"
    java -jar $ASLANPP -o $ASLAN -gas $FILE
    timelimit -t30 -T1 $CLATSE $ASLAN > $ATK
    java -jar $ASLANPP -o $RES -ar $ATK $ASLAN
    cat $RES
}

rm -rf $OUTDIR
mkdir -p $OUTDIR
cp $INDIR/*.aslan++ $OUTDIR
FILES=`find $OUTDIR -name '*.aslan++' -print`
for i in $FILES
do
    checkone $i
done
