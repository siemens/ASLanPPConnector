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

package org.avantssar.aslan;

import java.util.ArrayList;
import java.util.List;

import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public abstract class AbstractRepresentable implements IRepresentable {

	private final List<ICommentEntry> commentLines = new ArrayList<ICommentEntry>();
	private final LocationInfo location;
	private ErrorGatherer err;

	protected AbstractRepresentable(LocationInfo location, ErrorGatherer err) {
		this.location = location;
		this.err = err;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public ErrorGatherer getErrorGatherer() {
	    if (err == null)
		err = new ErrorGatherer(ASLanErrorMessages.DEFAULT);
		return err;
	}

	public String getRepresentation() {
		PrettyPrinter pp = new PrettyPrinter(true);
		accept(pp);
		return pp.toString();
	}

	public void addCommentLine(String s) {
		MetaInfo mi = extractMetainfo(s);
		if (mi != null) {
			commentLines.add(mi);
		}
		else {
			commentLines.add(new CommentLine(s));
		}
	}

	public MetaInfo addMetaInfo(String name) {
		MetaInfo mi = new MetaInfo(name);
		commentLines.add(mi);
		return mi;
	}

	public List<ICommentEntry> getCommentLines() {
		return commentLines;
	}

	@Override
	public int compareTo(IRepresentable o) {
		return getRepresentation().compareTo(o.getRepresentation());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IRepresentable) {
			return compareTo((IRepresentable) other) == 0;
		}
		return false;
	}

	private MetaInfo extractMetainfo(String commentLine) {
		return MetaInfo.fromString(commentLine);
	}

	@Override
	public String toString() {
		return getRepresentation();
		/* does not help to get original names:
		PrettyPrinter pp = new PrettyPrinter(false);
		accept(pp);
		return pp.toString(); */
	}

}
