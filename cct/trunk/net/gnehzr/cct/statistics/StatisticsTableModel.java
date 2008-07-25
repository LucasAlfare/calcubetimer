package net.gnehzr.cct.statistics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListIterator;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

public class StatisticsTableModel extends DraggableJTableModel implements ActionListener {
	Statistics stats;
	private Session sesh;
	public void setSession(Session sesh) {
		this.sesh = sesh;
		if(stats != null) {
			stats.setUndoRedoListener(null);
			stats.setTableListener(null);
			stats.setStatisticsUpdateListeners(null);
		}
		stats = sesh.getStatistics();
		stats.setTableListener(this);
		stats.setUndoRedoListener(l);
		stats.setStatisticsUpdateListeners(statsListeners);
		stats.notifyListeners(false);
	}
	public Session getCurrentSession() {
		return sesh;
	}
	public Statistics getCurrentStatistics() {
		return stats;
	}
	private UndoRedoListener l;
	public void setUndoRedoListener(UndoRedoListener l) {
		this.l = l;
	}
	private ArrayList<StatisticsUpdateListener> statsListeners = new ArrayList<StatisticsUpdateListener>();
	public void addStatisticsUpdateListener(StatisticsUpdateListener l) {
		//This nastyness is to ensure that PuzzleStatistics have had a chance to see the change (see notifyListeners() in Statistics)
		//before the dynamicstrings
		if(l instanceof PuzzleStatistics)
			statsListeners.add(0, l);
		else
			statsListeners.add(l);
	}
	public void removeStatisticsUpdateListener(StatisticsUpdateListener l) {
		statsListeners.remove(l);
	}
	//this is needed to update the i18n text
	public void fireStringUpdates() {
		for(StatisticsUpdateListener sul : statsListeners)
			sul.update();
		l.refresh();
	}
	
	private String[] columnNames = new String[] { "StatisticsTableModel.times", "StatisticsTableModel.ra0", "StatisticsTableModel.ra1", "StatisticsTableModel.comment" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private Class<?>[] columnClasses = new Class<?>[] { SolveTime.class, SolveTime.class, SolveTime.class, String.class };
	public String getColumnName(int column) {
		return StringAccessor.getString(columnNames[column]);
	}
	public int getColumnCount() {
		return columnNames.length;
	}
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses[columnIndex];
	}
	public int getSize() {
		return getRowCount();
	}
	public int getRowCount() {
		return stats == null ? 0 : stats.getAttemptCount();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
		case 0: //get the solvetime for this index
			return stats.get(rowIndex);
		case 1: //falls through
		case 2: //get the RA for this index in this column
			return stats.getRA(rowIndex, columnIndex - 1);
		case 3:
			return stats.get(rowIndex).getComment();
		default:
			return null;
		}
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0 || columnIndex == 3;
	}
	public boolean isRowDeletable(int rowIndex) {
		return true;
	}
	public void insertValueAt(Object value, int rowIndex) {
		stats.add(rowIndex, (SolveTime) value);
		fireTableRowsInserted(rowIndex, rowIndex);
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 0 && value instanceof SolveTime)
			stats.set(rowIndex, (SolveTime) value);
		else if(columnIndex == 3 && value instanceof String)
			stats.get(rowIndex).setComment((String) value);
	}
	public void deleteRows(int[] indices) {
		stats.remove(indices);
	}
	public void removeRows(int[] indices) {
		deleteRows(indices);
	}
	public String getToolTip(int rowIndex) {
		String t = stats.get(rowIndex).getComment();
		return t.isEmpty() ? null : t;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == edit) {
			timesTable.editCellAt(timesTable.getSelectedRow(), 0);
		} else {
			if(source == discard) {
				timesTable.deleteSelectedRows(false);
			} else { //one of the jradio buttons
				ArrayList<SolveType> types = new ArrayList<SolveType>();
				for(SolveType key : typeButtons.keySet())
					if(typeButtons.get(key).isSelected())
						types.add(key);

				stats.setSolveTypes(timesTable.getSelectedRow(), types);
			}
			prevFocusOwner.requestFocusInWindow();
		}
	}

	private JMenuItem edit, discard;
	private DraggableJTable timesTable;
	private Component prevFocusOwner;
	private HashMap<SolveType, JMenuItem> typeButtons;
	public void showPopup(MouseEvent e, DraggableJTable timesTable, Component prevFocusOwner) {
		this.timesTable = timesTable;
		this.prevFocusOwner = prevFocusOwner;
		JPopupMenu jpopup = new JPopupMenu();
		int[] selectedSolves = timesTable.getSelectedRows();
		if(selectedSolves.length == 0)
			return;
		else if(selectedSolves.length == 1) {
			SolveTime selectedSolve = stats.get(timesTable.getSelectedRow());
			JMenuItem rawTime = new JMenuItem(StringAccessor.getString("StatisticsTableModel.rawtime") //$NON-NLS-1$
					+ Utils.formatTime(selectedSolve.rawSecondsValue()));
			rawTime.setEnabled(false);
			jpopup.add(rawTime);

			ArrayList<SolveTime> split = selectedSolve.getSplits();
			if (split != null) {
				ListIterator<SolveTime> splits = split.listIterator();
				while (splits.hasNext()) {
					SolveTime next = splits.next();
					rawTime = new JMenuItem(StringAccessor.getString("StatisticsTableModel.split") + splits.nextIndex() //$NON-NLS-1$
							+ ": " + next + "\t" + next.getScramble()); //$NON-NLS-1$ //$NON-NLS-2$
					rawTime.setEnabled(false);
					jpopup.add(rawTime);
				}
			}
			
			edit = new JMenuItem(StringAccessor.getString("StatisticsTableModel.edittime")); //$NON-NLS-1$
			edit.addActionListener(this);
			jpopup.add(edit);

			jpopup.addSeparator();
			
			typeButtons = new HashMap<SolveType, JMenuItem>();
			ButtonGroup independent = new ButtonGroup();
			ButtonGroup attributes = new ButtonGroup();
			JMenuItem attr = new JRadioButtonMenuItem(StringAccessor.getString("StatisticsTableModel.none"), selectedSolve.getTypes().isEmpty());
			attr.setEnabled(!selectedSolve.isTrueWorstTime());
			independent.add(attr);
			Collection<SolveType> types = SolveType.getSolveTypes();
			for(SolveType type : types) {
				if(type.isIndependent()) {
					attr = new JRadioButtonMenuItem(type.toString(), selectedSolve.isType(type));
					attr.setEnabled(!selectedSolve.isTrueWorstTime());
					independent.add(attr);
				} else {
					attr = new JCheckBoxMenuItem(type.toString(), selectedSolve.isType(type));
					attributes.add(attr);
				}
				attr.addActionListener(this);
				typeButtons.put(type, attr);
			}
			Enumeration<AbstractButton> buttons = independent.getElements();
			while(buttons.hasMoreElements())
				jpopup.add(buttons.nextElement());
			
			jpopup.addSeparator();
			
			buttons = attributes.getElements();
			while(buttons.hasMoreElements())
				jpopup.add(buttons.nextElement());
			
			jpopup.addSeparator();
		}

		discard = new JMenuItem(StringAccessor.getString("StatisticsTableModel.discard")); //$NON-NLS-1$
		discard.addActionListener(this);
		jpopup.add(discard);
		timesTable.requestFocusInWindow();
		jpopup.show(e.getComponent(), e.getX(), e.getY());
	}
}
