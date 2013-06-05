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

public class AbstractOwned extends AbstractNamed implements IOwned {

	private IScope owner;
	private boolean disambiguated;
	private boolean transfered;

	public AbstractOwned(IScope scope, String name) {
		super(name);
		transferTo(scope);
		transfered = false;
	}

	public IScope getOwner() {
		return owner;
	}

	public void transferTo(IScope newScope) {
		if (owner != null) {
			owner.removeEntry(this);
		}
		owner = newScope;
		if (newScope != null) {
			disambiguateAndStore(newScope);
			transfered = true;
		}
	}

	public boolean wasDisambiguated() {
		return disambiguated;
	}

	public boolean wasTransfered() {
		return transfered;
	}

	private void disambiguateAndStore(IScope owner) {
		boolean nameClash;
		boolean isLowerCase = Character.isLowerCase(getName().charAt(0));
		StringBuffer disambiguatedName = new StringBuffer(getName());
		disambiguated = false;
		boolean firstTry = true;
		do {
			nameClash = owner.isNameClash(disambiguatedName.toString(), this.getClass());
			// special case for functions + constants, which clash one another
			// TODO: this should be done more nicely
			if (!nameClash) {
				if (getClass().equals(ConstantSymbol.class)) {
					nameClash = owner.isNameClash(disambiguatedName.toString(), FunctionSymbol.class);
				}
				else if (getClass().equals(FunctionSymbol.class)) {
					nameClash = owner.isNameClash(disambiguatedName.toString(), ConstantSymbol.class);
				}
			}
			if (nameClash) {
				if (firstTry) {
					StringBuffer sb = new StringBuffer();
					buildPrefix(owner, sb, "_");
					String pref = sb.toString();
					if (isLowerCase) {
						pref = Character.toLowerCase(pref.charAt(0)) + pref.substring(1);
					}
					disambiguatedName.insert(0, pref);
					firstTry = false;
				}
				else {
					disambiguatedName.append("_");
				}
				disambiguated = true;
			}
		}
		while (nameClash);
		setPseudonym(disambiguatedName.toString());
		owner.storeEntry(this);
	}

	/**
	 * Recursively builds a String with abbreviated names of all parent owners.
	 * The topmost owner is not considered (that would be global context of the
	 * entire specification).
	 * 
	 * @param scope
	 *            The current scope to be visited.
	 * @param sb
	 *            The StringBuffer where to put the result.
	 * @param separator
	 *            A separator placed between the names of the owners.
	 */
	private void buildPrefix(IScope scope, StringBuffer sb, String separator) {
		if (scope == null || scope.getOwner() == null) {
			return;
		}
		buildPrefix(scope.getOwner(), sb, separator);
		sb.append(abbreviate(scope.getOriginalName()));
		sb.append(separator);
	}

	/**
	 * Abbreviates a given text by keeping only upper case letters and digits.
	 * If there are no upper case letters or digits, then the original text is
	 * kept.
	 * 
	 * @param text
	 *            The original text to abbreviate.
	 * @return A String containing only the upper case letters and digits from
	 *         the original string, if any. If no upper case letter or digit was
	 *         found, then the original string itself is returned.
	 */
	private String abbreviate(String text) {
		String initials = "";
		initials += text.charAt(0);
		for (int i = 1; i < text.length(); i++) {
			if (keep(text.charAt(i), text.charAt(i - 1))) {
				initials += text.charAt(i);
			}
		}
		return initials;
	}

	private boolean keep(char c, char prev) {
		return Character.isUpperCase(c) || Character.isDigit(c) || (Character.isLetter(c) && (prev == '_' || Character.isDigit(prev)));
	}
}
