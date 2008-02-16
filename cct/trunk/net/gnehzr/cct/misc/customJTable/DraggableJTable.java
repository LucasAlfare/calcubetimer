package net.gnehzr.cct.misc.customJTable;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class DraggableJTable extends JTable implements MouseListener, MouseMotionListener, KeyListener {
	private String addText;

	//You must set any editors or renderers before setting this table's model
	//because the preferred size is computed inside setModel()
	public DraggableJTable(String addText, boolean draggable) {
		this.addText = addText;
		this.addMouseListener(this);
		if(draggable) {
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.addMouseMotionListener(this);
		}
		this.addKeyListener(this);
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		this.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
	}

	private class JTableModelWrapper extends DraggableJTableModel {
		private DraggableJTableModel wrapped;
		public JTableModelWrapper(DraggableJTableModel wrapped) {
			this.wrapped = wrapped;
			wrapped.addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					int[] rows = getSelectedRows();
					//note that wrapped has already been updated
					int lastRow = getRowCount() - 2;
					//this is to prevent the selection from increasing when "add" is selected
					//and something is added
					boolean resetSelectedRows = rows.length == 1 && rows[0] == lastRow;
					fireTableChanged(e);
					if(resetSelectedRows) {
						lastRow++;
						setRowSelectionInterval(lastRow, lastRow);
					}
				}
			});
		}
		public boolean deleteRowWithElement(Object element) {
			return wrapped.deleteRowWithElement(element);
		}
		public int getColumnCount() {
			return wrapped.getColumnCount();
		}
		public int getRowCount() {
			return wrapped.getRowCount() + 1;
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(rowIndex == wrapped.getRowCount()) {
				if(columnIndex == 0)
					return addText;
				return "";
			} else
				return wrapped.getValueAt(rowIndex, columnIndex);
		}
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(rowIndex == wrapped.getRowCount()) {
				if(columnIndex == 0)
					return true;
				return false;
			} else
				return wrapped.isCellEditable(rowIndex, columnIndex);
		}
		public boolean isRowDeletable(int rowIndex) {
			if(rowIndex == wrapped.getRowCount())
				return false;
			else
				return wrapped.isRowDeletable(rowIndex);
		}
		public boolean removeRowWithElement(Object element) {
			return wrapped.removeRowWithElement(element);
		}
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			wrapped.setValueAt(value, rowIndex, columnIndex);
		}
		public void showPopup(MouseEvent e, DraggableJTable source) {
			if(rowAtPoint(e.getPoint()) != wrapped.getRowCount())
				wrapped.showPopup(e, source);
		}
		public Class<?> getColumnClass(int columnIndex) {
			return wrapped.getColumnClass(columnIndex);
		}
		public String getColumnName(int column) {
			return wrapped.getColumnName(column);
		}
		public void insertValueAt(Object value, int rowIndex) {
			wrapped.insertValueAt(value, rowIndex);
		}
	}

	public void promptForNewRow() {
		editCellAt(model.getRowCount() - 1, 0);
	}
	@Override
	public boolean editCellAt(int row, int column) {
		boolean temp = super.editCellAt(row, column);
		getEditorComponent().requestFocusInWindow();
		return temp;
	}

	private DraggableJTableModel model;
	public void setModel(TableModel tableModel) {
		if (tableModel instanceof DraggableJTableModel) {
			model = (DraggableJTableModel) tableModel;
			model = new JTableModelWrapper(model);
			super.setModel(model);

			Dimension rendDim = getCellRenderer(0, 0).getTableCellRendererComponent(
					this,
					addText,
					true,
					true,
					0,
					0).getPreferredSize();
			Dimension edDim = getCellEditor(0, 0).getTableCellEditorComponent(
					this,
					addText,
					true,
					0,
					0).getPreferredSize();

			this.setRowHeight(Math.max(rendDim.height, edDim.height));
			rendDim.height = 0;
			this.setPreferredScrollableViewportSize(rendDim);

			TableColumnModel columns = this.getColumnModel();
			for(int ch = 0; ch < columns.getColumnCount(); ch++) {
				columns.getColumn(ch).setPreferredWidth(getCellEditor(0, ch).getTableCellEditorComponent(
						this,
						null,
						true,
						0,
						ch).getPreferredSize().width);
			}
		} else
			super.setModel(tableModel);
	}

	private int fromRow;
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		fromRow = this.getSelectedRow();
		maybeShowPopup(e);
	}
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	public void mouseDragged(MouseEvent m) {
		int toRow = this.getSelectedRow();
		if (toRow == -1 || toRow == fromRow || fromRow == this.getRowCount() - 1 || toRow == this.getRowCount() - 1)
			return;
		Object element = model.getValueAt(fromRow, 0);
		model.removeRowWithElement(element);
		model.insertValueAt(element, toRow);
		fromRow = toRow;
	}
	public void mouseMoved(MouseEvent e) {}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
			deleteSelectedRows(true);
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	private void maybeShowPopup(MouseEvent e) {
		if(e.isPopupTrigger()) {
			if(getSelectedRows().length <= 1) {
				// if right clicking on a single cell, this will select it first
				int row = rowAtPoint(e.getPoint());
				setRowSelectionInterval(row, row);
			}
			model.showPopup(e, this);
		}
	}

	public void deleteSelectedRows(boolean prompt) {
		int[] selectedRows = this.getSelectedRows();
		ArrayList<Object> toDelete = new ArrayList<Object>();
		for(int row : selectedRows) {
			if (model.isRowDeletable(row)) {
				toDelete.add(model.getValueAt(row, 0));
			}
		}
		if(toDelete.size() == 0) //nothing to delete
			return;
		String temp = "";
		for (Object deleteMe : toDelete) {
			temp += ", " + deleteMe;
		}
		temp = temp.substring(2);
		int choice = JOptionPane.YES_OPTION;
		if(prompt)
			choice = JOptionPane.showConfirmDialog(null,
				"Are you sure you wish to remove " + temp + "?", "Confirm",
				JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			for (Object deleteMe : toDelete)
					model.deleteRowWithElement(deleteMe);
			if (selectedRows.length > 1) {
				clearSelection();
			} else if(selectedRows[0] < model.getRowCount() - 1) {
				setRowSelectionInterval(selectedRows[0], selectedRows[0]);
			} else if(selectedRows[0] != 0) {
				int newRow = model.getRowCount() - 2;
				setRowSelectionInterval(newRow, newRow);
			}
		}
	}
}