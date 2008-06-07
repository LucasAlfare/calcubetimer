package net.gnehzr.cct.configuration;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

@SuppressWarnings("serial") //$NON-NLS-1$
public class SortedProperties extends Properties {
	public SortedProperties(SortedProperties defaults) {
		super(defaults);
	}

	public SortedProperties() {}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public synchronized Enumeration keys() {
		Enumeration keysEnum = super.keys();
		Vector keyList = new Vector();
		while (keysEnum.hasMoreElements()) {
			keyList.add(keysEnum.nextElement());
		}
		Collections.sort(keyList);
		return keyList.elements();
	}
}
