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

rm -rf ROOT
mkdir -p ROOT
cp ../src/web/*.java .
cp ../src/web/*.sh .
cp ../src/aslan-core/src/main/resources/aslan-xml.xsd .
javac -cp aslan-core.jar *.java
java -cp aslan-core.jar:. ASLanFirst
java -cp aslan-core.jar:. ASLanSecond
java -cp aslan-core.jar:. ASLanThird
pygmentize -O noclasses -o ASLanFirst.html ASLanFirst.java
pygmentize -O noclasses -o ASLanSecond.html ASLanSecond.java
pygmentize -O noclasses -o ASLanThird.html ASLanThird.java
pygmentize -O noclasses -l scheme -o simple_pt.html simple.aslan
pygmentize -O noclasses -o simple_xml.html simple.xml
pygmentize -O noclasses -o aslan-xml-help.html aslan-xml-help.sh
pygmentize -O noclasses -o aslan-xml-usage-1.html aslan-xml-usage-1.sh
pygmentize -O noclasses -o aslan-xml-usage-2.html aslan-xml-usage-2.sh
pygmentize -O noclasses -o aslan-xml-piped.html aslan-xml-piped.sh
pygmentize -O noclasses -o aslan-xml-ws-help.html aslan-xml-ws-help.sh
./aslan-xml-help.sh
./aslan-xml-usage-1.sh
./aslan-xml-usage-2.sh
./aslan-xml-piped.sh
./aslan-xml-ws-help.sh
sed s/\\\$connector-version\\\$/`java -jar aslanpp-connector.jar -v | cut '-d ' -f4`/ ../src/web/index.template.html > a1.html
sed s/\\\$connector-size\\\$/`ls -lh aslanpp-connector.jar | awk '{ print $5 }'`/ a1.html > a2.html
sed s/\\\$connector-client-size\\\$/`ls -lh aslanpp-connector-wsclient.jar | awk '{ print $5 }'`/ a2.html > a3.html
sed s/\\\$converter-version\\\$/`java -jar aslan-xml.jar -v | cut '-d ' -f4`/ a3.html > a4.html
sed s/\\\$converter-size\\\$/`ls -lh aslan-xml.jar | awk '{ print $5 }'`/ a4.html > a5.html
sed s/\\\$converter-client-size\\\$/`ls -lh aslan-xml-wsclient.jar | awk '{ print $5 }'`/ a5.html > a6.html
sed s/\\\$aslan-library-size\\\$/`ls -lh aslan-core.jar | awk '{ print $5 }'`/ a6.html > a7.html
sed s/\\\$aslan-library-javadoc-size\\\$/`ls -lh aslan-core-javadoc.jar | awk '{ print $5 }'`/ a7.html > a8.html
sed s/\\\$aslan-schema-size\\\$/`ls -lh aslan-xml.xsd | awk '{ print $5 }'`/ a8.html > a9.html
sed '/\$aslan-xml-example-1\$/ {
	r ASLanFirst.html
	d
}' < a9.html > a10.html
sed '/\$aslan-xml-simple-aslan\$/ {
	r simple_pt.html
	d
}' < a10.html > a11.html
sed '/\$aslan-xml-example-2\$/ {
	r ASLanSecond.html
	d
}' < a11.html > a12.html
sed '/\$aslan-xml-simple-xml\$/ {
	r simple_xml.html
	d
}' < a12.html > a13.html
sed '/\$aslan-xml-example-3\$/ {
	r ASLanThird.html
	d
}' < a13.html > a14.html
sed '/\$aslan-xml-sh-help\$/ {
	r aslan-xml-help.html
	d
}' < a14.html > a15.html
sed '/\$aslan-xml-sh-usage-1\$/ {
	r aslan-xml-usage-1.html
	d
}' < a15.html > a16.html
sed '/\$aslan-xml-sh-usage-2\$/ {
	r aslan-xml-usage-2.html
	d
}' < a16.html > a17.html
sed '/\$aslan-xml-ws-sh-help\$/ {
	r aslan-xml-ws-help.html
	d
}' < a17.html > a18.html
sed '/\$aslan-xml-sh-piped\$/ {
	r aslan-xml-piped.html
	d
}' < a18.html > index.html
cp index.html ROOT
cp aslan-xml.xsd ROOT
cp aslanpp-connector.jar ROOT
cp aslanpp-connector-wsclient.jar ROOT
cp aslan-xml.jar ROOT
cp aslan-xml-wsclient.jar ROOT
cp aslan-core.jar ROOT
cp aslan-core-javadoc.jar ROOT
rm -rf a*.html
rm -rf *.xml
rm -rf *.aslan
rm -rf simple*.html
rm -rf ASLan*.html
rm -rf *.java
rm -rf *.class
rm -rf aslan-xml*.sh
rm -rf aslan.log
rm -rf ieat.tar
tar cvf ieat.tar aslanpp-connector.war aslan-xml.war ROOT
rm -rf ieat.tar.gz
gzip -9 ieat.tar
rm -rf ROOT
scp ieat.tar.gz avantssar.ieat.ro:~
ssh -t avantssar.ieat.ro 'sudo ./deploy.sh `pwd`/ieat.tar.gz'
