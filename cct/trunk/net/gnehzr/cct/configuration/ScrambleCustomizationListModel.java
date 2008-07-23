package net.gnehzr.cct.configuration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.main.ScrambleChooserComboBox;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants;

public class ScrambleCustomizationListModel extends DraggableJTableModel implements TableCellRenderer, TableCellEditor, MouseListener {
	private ArrayList<ScrambleCustomization> customizations;
	public void setContents(ArrayList<ScrambleCustomization> contents) {
		this.customizations = contents;
		fireTableDataChanged();
	}
	public ArrayList<ScrambleCustomization> getContents() {
		return customizations;
	}

	public void deleteRows(int[] indices) {
		removeRows(indices);
	}
	public Class<?> getColumnClass(int columnIndex) {
		return ScrambleCustomization.class;
	}
	private String[] columnNames = new String[]{ StringAccessor.getString("ScrambleCustomizationListModel.scramblecustomization"), //$NON-NLS-1$
			StringAccessor.getString("ScrambleCustomizationListModel.length"), //$NON-NLS-1$
			StringAccessor.getString("ScrambleCustomizationListModel.generatorgroup"),
			"RA 0", "RA 1"};
	public int getColumnCount() {
		return columnNames.length;
	}
	public String getColumnName(int column) {
		return columnNames[column];
	}
	public int getRowCount() {
		return customizations == null ? 0 : customizations.size();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		return customizations.get(rowIndex);
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex >= 1)
			return true;
		
		return customizations.get(rowIndex).getCustomization() != null;
			
	}
	public boolean isRowDeletable(int rowIndex) {
		ScrambleCustomization sc = customizations.get(rowIndex);
		Collection<String> dbCustoms = Configuration.getSelectedProfile().getPuzzleDatabase().getCustomizations();
		return !dbCustoms.contains(sc.toString()) && (sc.getCustomization() != null || sc.equals(ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION));
	}
	public void removeRows(int[] indices) {
		for(int ch = indices.length - 1; ch >=0; ch--) {
			int i = indices[ch];
			if(i >= 0 && i < customizations.size()) {
				customizations.remove(i);
			}
		}
		fireTableRowsDeleted(indices[0], indices[indices.length - 1]);
	}
	public void insertValueAt(Object value, int rowIndex) {
		customizations.add(rowIndex, (ScrambleCustomization)value);
		fireTableRowsInserted(rowIndex, rowIndex);
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		ScrambleCustomization newVal = (ScrambleCustomization)value;
		if(rowIndex == customizations.size()) {
			customizations.add(rowIndex, newVal);
			fireTableRowsInserted(rowIndex, rowIndex);
		} else {
			customizations.set(rowIndex, newVal);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}
	public void showPopup(MouseEvent e, DraggableJTable source, Component prevFocusOwner) {}
	public String getToolTip(int rowIndex) {
		return null;
	}

	//******* Start of renderer/editor stuff ****************//
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String val = value == null ? "" : value.toString(); //$NON-NLS-1$
		if(value instanceof ScrambleCustomization) {
			ScrambleCustomization customization = (ScrambleCustomization) value;
			ScrambleVariation v = customization.getScrambleVariation();
			if(column == 0) { //scramble customization
				String bolded = v.getVariation();
				if(bolded.isEmpty())
					bolded = customization.getScramblePlugin().getPuzzleName();
				val = "<html><b>" + bolded + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
				if(customization.getCustomization() != null)
					val += ":" + customization.getCustomization(); //$NON-NLS-1$
				val += "<html>"; //$NON-NLS-1$
			} else if(column == 1) { //scramble length
				val = "" + v.getLength(); //$NON-NLS-1$
			} else if(column == 2) { //generator group
				
			} else if(column == 3) { //ra 0
				val = customization.getRASize(0) + " " + (customization.isTrimmed(0) ? "Trimmed" : "Untrimmed");
			} else if(column == 4) { //ra 1
				val = customization.getRASize(1) + " " + (customization.isTrimmed(1) ? "Trimmed" : "Untrimmed");
			}
		}
		return new JLabel(val, SwingConstants.CENTER);
	}

	private int editingColumn;
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if(value instanceof ScrambleCustomization)
			customization = (ScrambleCustomization) value;
		else
			customization = new ScrambleCustomization(ScramblePlugin.getCurrentScrambleCustomization().getScrambleVariation(), ""); //$NON-NLS-1$
		editingColumn = column;
		if(column == 0) //customization
			return getCustomizationPanel(customization);
		else if(column == 1) //length
			return getLengthPanel(customization);
		else if(column == 2) //generator
			return getGeneratorPanel(customization);
		else if(column == 3) //ra0
			return getRAPanel(0, customization);
		else if(column == 4) //ra1
			return getRAPanel(1, customization);
		
		return null;
	}
	
	private JTextField generator;
	private JPanel getGeneratorPanel(ScrambleCustomization sc) {
		JPanel temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
		temp.add(generator = new JTextField("generator")); //TODO
		disabledComponents = new ArrayList<Component>();
		listenToContainer(temp);
		return temp;
	}
	
	private int raIndex;
	JSpinner raSize;
	JCheckBox trimmed;
	private JPanel getRAPanel(final int index, final ScrambleCustomization sc) {
		raIndex = index;
		raSize = new JSpinner(new SpinnerNumberModel(sc.getRASize(index), 0, null, 1));
		raSize.setToolTipText(StringAccessor.getString("ScrambleCustomizationListModel.specifylength")); //$NON-NLS-1$
		((JSpinner.DefaultEditor) raSize.getEditor()).getTextField().setColumns(3);
		JPanel temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
		temp.add(raSize);
		temp.add(trimmed = new JCheckBox(StringAccessor.getString("ScrambleCustomizationListModel.trimmed"), sc.isTrimmed(index)));
		JButton resetRA = new JButton("X");
		resetRA.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.TRUE);
		resetRA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				raSize.setValue(Configuration.getInt(VariableKey.RA_SIZE(index, null), false));
				trimmed.setSelected(Configuration.getBoolean(VariableKey.RA_TRIMMED(index, null), false));
			}
		});
		temp.add(resetRA);
		disabledComponents = new ArrayList<Component>();
		listenToContainer(temp);
		return temp;
	}

	ScrambleCustomization customization;
	ScrambleChooserComboBox scrambleVariations;
	JSpinner scramLength;
	private JTextField customField;
	private String originalFieldText;
	private JPanel getCustomizationPanel(ScrambleCustomization custom) {
		JPanel customPanel = new JPanel();
		customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.LINE_AXIS));
		if(custom.getCustomization() != null) {
			scrambleVariations = new ScrambleChooserComboBox(false, false);
			scrambleVariations.addItem(ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation());
			scrambleVariations.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
			scrambleVariations.setSelectedItem(custom.getScrambleVariation());
			scrambleVariations.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED)
						customization.setScrambleVariation((ScrambleVariation) scrambleVariations.getSelectedItem());
				}
			});
			scrambleVariations.setToolTipText(StringAccessor.getString("ScrambleCustomizationListModel.selectvariation")); //$NON-NLS-1$
			customPanel.add(scrambleVariations);

			originalFieldText = custom.getCustomization();
			customField = new JTextField(originalFieldText, 15);
			customField.setToolTipText(StringAccessor.getString("ScrambleCustomizationListModel.specifycustomization")); //$NON-NLS-1$
			customPanel.add(customField);
		} else {
			customPanel.add(new JLabel("<html><b>" + custom.getScrambleVariation().toString() + "</b></html>")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		disabledComponents = new ArrayList<Component>();
		listenToContainer(customPanel);

		return customPanel;
	}

	private JPanel getLengthPanel(ScrambleCustomization custom) {
		JPanel lengthPanel = new JPanel();
		lengthPanel.setLayout(new BoxLayout(lengthPanel, BoxLayout.LINE_AXIS));
		customization = custom;
		scramLength = new JSpinner(new SpinnerNumberModel(Math.max(custom.getScrambleVariation().getLength(), 0), 0, null, 1));
		scramLength.setToolTipText(StringAccessor.getString("ScrambleCustomizationListModel.specifylength")); //$NON-NLS-1$
		((JSpinner.DefaultEditor) scramLength.getEditor()).getTextField().setColumns(3);
		lengthPanel.add(scramLength);

		JButton resetButton = new JButton("X"); //$NON-NLS-1$
		resetButton.setEnabled(false);
		resetButton.setToolTipText(StringAccessor.getString("ScrambleCustomizationListModel.resetlength")); //$NON-NLS-1$
		resetButton.setFocusable(false);
		resetButton.setFocusPainted(false);
		resetButton.setMargin(new Insets(0, 0, 0, 0));
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scramLength.setValue(customization.getScrambleVariation().getScrambleLength(true));
			}
		});
		resetButton.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.TRUE);
		resetButton.putClientProperty(SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY, new SubstanceConstants.Side[] { SubstanceConstants.Side.LEFT });
		lengthPanel.add(resetButton);
		disabledComponents = new ArrayList<Component>();
		listenToContainer(lengthPanel);
		return lengthPanel;
	}

	private ArrayList<Component> disabledComponents;
	private void listenToContainer(Component c) {
		c.addMouseListener(this);
		c.setEnabled(false);
		disabledComponents.add(c);
		if(c instanceof Container) {
			Container container = (Container) c;
			for(Component c2 : container.getComponents())
				listenToContainer(c2);
		}
	}

	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {
		for(Component c : disabledComponents)
			c.setEnabled(true);
	}

	private CellEditorListener listener;
	public void addCellEditorListener(CellEditorListener l) {
		listener = l;
	}

	public void cancelCellEditing() {
		scramLength = null;
		listener.editingCanceled(null);
	}

	public Object getCellEditorValue() {
		return customization;
	}

	public boolean isCellEditable(EventObject e) {
		if(e instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) e;
			if(me.getClickCount() >= 2)
				return true;
		}
		return false;
	}

	public void removeCellEditorListener(CellEditorListener l) {
		if(listener == l)
			listener = null;
	}

	public boolean shouldSelectCell(EventObject arg0) {
		return true;
	}

	public boolean stopCellEditing() {
		if(editingColumn == 0) { //customization
//		if(customization.getCustomization() != null) {
			String customName = customField.getText();
			String error = null;
			if(customName.isEmpty()) { //$NON-NLS-1$
				error = StringAccessor.getString("ScrambleCustomizationListModel.noemptycustomization"); //$NON-NLS-1$
			} else {
				String fullCustomName = customization.getScrambleVariation().getVariation() + ":" + customName; //$NON-NLS-1$
				for(ScrambleCustomization c : customizations) {
					if(c.toString().equals(fullCustomName) && c != customization) {
						error = StringAccessor.getString("ScrambleCustomizationListModel.noduplicatecustomizations"); //$NON-NLS-1$
						break;
					}
				}
			}
			if(error != null) {
				customField.setBorder(new LineBorder(Color.RED));
				customField.setToolTipText(error);
				Action toolTipAction = customField.getActionMap().get("postTip"); //$NON-NLS-1$
				if(toolTipAction != null) {
					ActionEvent postTip = new ActionEvent(customField, ActionEvent.ACTION_PERFORMED, ""); //$NON-NLS-1$
					toolTipAction.actionPerformed(postTip);
				}
				return false;
			}
			customization.setCustomization(customField.getText());
		} else if(editingColumn == 1) { //length
//		if(scramLength != null) {
			customization.getScrambleVariation().setLength((Integer) scramLength.getValue());
		} else if(editingColumn == 2) { //generator
			
		} else if(editingColumn == 3) { //ra 0
			customization.setRA(0, (Integer) raSize.getValue(), trimmed.isSelected());
		} else if(editingColumn == 4) { //ra 1
			customization.setRA(1, (Integer) raSize.getValue(), trimmed.isSelected());
		}
		listener.editingStopped(null);
		return true;
	}
}
