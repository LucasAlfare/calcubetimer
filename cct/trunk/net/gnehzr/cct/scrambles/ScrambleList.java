package net.gnehzr.cct.scrambles;

import java.util.ArrayList;

@SuppressWarnings("serial") //$NON-NLS-1$
public class ScrambleList {
	public static class ScrambleString {
		private String scramble;
		private boolean imported;
		private int length;
		public ScrambleString(String scramble, boolean imported, int length) {
			this.scramble = scramble;
			this.imported = imported;
			this.length = length;
		}
		public String getScramble() {
			return scramble;
		}
		public boolean isImported() {
			return imported;
		}
		public int getLength() {
			return length;
		}
	}
	private ArrayList<ScrambleString> scrambles = new ArrayList<ScrambleString>();
	private ScrambleCustomization custom;
	private int scrambleNumber = 0;

	public ScrambleCustomization getScrambleCustomization() {
		return custom;
	}
	//this should only be called if we're on the last scramble in this list
	public void setScrambleCustomization(ScrambleCustomization sc) {
		if(custom == null || !sc.getScrambleVariation().equals(custom.getScrambleVariation())) {
			if(scrambleNumber != scrambles.size())
				scrambles.remove(scrambles.size() - 1);
		}
		custom = sc;
	}
	
	public void removeLatestAndFutureScrambles() {
		//nullify the current scramble, and anything imported
		//setting to null is easier than modifying the length of the list, we'll generate the scrams in getCurrent() when we need 'em
		for(int c = scrambleNumber; c < scrambles.size(); c++)
			scrambles.set(c, null);
	}
	
	public void setScrambleLength(int l) {
		ScrambleVariation sv = custom.getScrambleVariation();
		if(l != sv.getLength()) {
			sv.setLength(l);
			removeLatestAndFutureScrambles();
		}
	}
	public void clear() {
		scrambleNumber = 0;
		scrambles.clear();
	}
	
	public int size() {
		return scrambles.size();
	}
	
	public void addScramble(String scramble) {
		scrambles.add(new ScrambleString(scramble, false, scramble.length()));
	}
	
	public ScrambleString getCurrent() {
		if(scrambleNumber == scrambles.size())
			scrambles.add(null); //ensure that there's capacity for the current scramble
		ScrambleString c = scrambles.get(scrambleNumber);
		if(c == null) {
			Scramble s = custom.getScrambleVariation().generateScramble();
			c = new ScrambleString(s.toString(), false, s.getLength());
			scrambles.set(scrambleNumber, c);
		}
		return c;
	}
	
	public void importScrambles(ArrayList<Scramble> scrams) {
		removeLatestAndFutureScrambles();
		for(Scramble s : scrams)
			scrambles.add(new ScrambleString(s.toString(), true, s.getLength()));
	}

	public ScrambleString getNext() {
		scrambleNumber++;
		return getCurrent();
	}

	public int getScrambleNumber() {
		return scrambleNumber + 1;
	}

	public void setScrambleNumber(int scrambleNumber) {
		this.scrambleNumber = scrambleNumber - 1;
	}
}
