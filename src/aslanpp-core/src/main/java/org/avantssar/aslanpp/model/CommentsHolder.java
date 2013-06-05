// Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.avantssar.aslanpp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class CommentsHolder implements ICommentsHolder {

	private final ErrorGatherer err;
	private final String[] tags = new String[] { MetaInfo.MODELER,
			MetaInfo.VERBATIM, MetaInfo.CLATSE, MetaInfo.SATMC, MetaInfo.OFMC };
	private final String recognizedTags;
	private final List<MetaInfo> meta = new ArrayList<MetaInfo>();

	protected CommentsHolder(ErrorGatherer err) {
		this.err = err;

		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String s : tags) {
			if (!first) {
				sb.append(" ");
			}
			sb.append("@").append(s);
			first = false;
		}
		this.recognizedTags = sb.toString();
	}

	@Override
	public void addCommentLine(String comment, LocationInfo location) {
		Pattern p = Pattern.compile("^@([a-zA-Z0-9_]+)\\(([^\\)]*)\\)$");
		String commentContent = comment.trim();
		while (commentContent.startsWith("%")) {
			commentContent = commentContent.substring(1);
		}
		Matcher m = p.matcher(commentContent.trim());
		if (m.find()) {
			String tag = m.group(1);
			String value = m.group(2);

			boolean found = false;
			for (String s : tags) {
				if (s.equals(tag)) {
					found = true;
					break;
				}
			}
			if (!found) {
				err.addWarning(location, ErrorMessages.WRONG_TAG_IN_METAINFO,
						tag, recognizedTags);
			} else {
				meta.add(new MetaInfo(tag, value));
			}
		}
	}

	public List<MetaInfo> getMetaInfo() {
		return meta;
	}

}
