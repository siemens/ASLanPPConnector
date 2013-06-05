#!/bin/bash
# Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
# Licensed under the Apache License, Version 2.0.

svn list -R | sed s/README.TXT/README.txt/ | grep -v .jar$ | grep -v .war$ | grep -v /$ | grep -v webservice.txt >src.txt
for F in `cat src.txt`; do
  [[ (-z `fgrep Copyright $F` || -z `fgrep Licensed $F`) && ! ($F =~ \.PNG) ]] && echo "Missing copyright or license info in file: $F"
done
tar czf aslanpp-translator.tgz -T src.txt License.txt NOTICE.txt
cat src.txt | zip aslanpp-translator.zip License.txt NOTICE.txt -@
