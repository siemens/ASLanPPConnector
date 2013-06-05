# Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
# Licensed under the Apache License, Version 2.0.

require 'nokogiri'

unless ARGV.length == 2
  puts "Usage: ruby validate.rb <XML Schema> <XML file>"
  exit
end

xsd = Nokogiri::XML::Schema(open(ARGV[0]))
doc = Nokogiri::XML(open(ARGV[1]))

unless xsd.valid?(doc)
  xsd.validate(doc).each do |error|
    printf "line %d: %s\n", error.line,  error.message
  end
end
