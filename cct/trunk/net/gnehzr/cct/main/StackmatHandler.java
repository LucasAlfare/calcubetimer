package net.gnehzr.cct.main;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;

public class StackmatHandler implements PropertyChangeListener {
	private TimingListener tl;
	public StackmatHandler(TimingListener tl, StackmatInterpreter si) {
		si.addPropertyChangeListener(this);
		this.tl = tl;
		reset();
	}
	
	public void reset() {
		leftStart = rightStart = 0;
		stackmatInspecting = false;
	}

	private long leftStart, rightStart;
	private boolean stackmatInspecting;
	public void propertyChange(PropertyChangeEvent evt) {
		String event = evt.getPropertyName();
		boolean on = !event.equals("Off"); //$NON-NLS-1$
		boolean stackmatEnabled = Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false);
		tl.stackmatOn(on);
		if(!stackmatEnabled)
			return;

		if(evt.getNewValue() instanceof StackmatState) {
			StackmatState current = (StackmatState) evt.getNewValue();
			if(event.equals("Reset")) { //$NON-NLS-1$
				if(current.oneHand()) {
					if(stackmatInspecting) {
						
					} else if(current.leftHand()) {
						rightStart = 0;
						if(leftStart <= 0)
							leftStart = System.currentTimeMillis();
						else if(timeToStart(leftStart))
							current.clearLeftHand();
					} else { //the right hand is down
						leftStart = 0;
						if(rightStart <= 0)
							rightStart = System.currentTimeMillis();
						else if(timeToStart(rightStart))
							current.clearRightHand();
					}
					tl.refreshDisplay(current);
					return;
				} else if(current.bothHands()) {
					
				} else if(!stackmatInspecting && (timeToStart(leftStart) || timeToStart(rightStart))) {
					stackmatInspecting = true;
					tl.inspectionStarted();
				}
				tl.refreshDisplay(current);
			} else {
				tl.refreshDisplay(current);
				stackmatInspecting = false;
				if(event.equals("TimeChange")) { //$NON-NLS-1$
					tl.timerStarted();
				} else if(event.equals("Split")) { //$NON-NLS-1$
					tl.timerSplit(current);
				} else if(event.equals("New Time")) { //$NON-NLS-1$
					tl.timerStopped(current);
				} else if(event.equals("Current Display")) { //$NON-NLS-1$
				} else if(event.equals("Accident Reset")) { //$NON-NLS-1$
					tl.timerAccidentlyReset((StackmatState) evt.getOldValue());
				}
			}
			leftStart = current.leftHand() ? -1 : 0;
			rightStart = current.rightHand() ? -1 : 0;
		}
	}

	private boolean timeToStart(long time) {
		if(time <= 0 || !Configuration.getBoolean(VariableKey.COMPETITION_INSPECTION, false))
			return false;
		return (System.currentTimeMillis() - time >= Configuration.getInt(VariableKey.DELAY_UNTIL_INSPECTION, false));
	}
}
