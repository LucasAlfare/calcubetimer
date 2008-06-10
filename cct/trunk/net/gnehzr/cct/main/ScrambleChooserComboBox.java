package net.gnehzr.cct.main;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;

@SuppressWarnings("serial") //$NON-NLS-1$
public class ScrambleChooserComboBox extends LoudComboBox implements TableCellRenderer, ConfigurationChangeListener {
	public ScrambleChooserComboBox(boolean icons, boolean customizations) {
		this.setRenderer(new PuzzleCustomizationCellRenderer(icons));
		DefaultComboBoxModel model;
		if(customizations)
			model = new DefaultComboBoxModel(ScramblePlugin.getScrambleCustomizations(false).toArray(new ScrambleCustomization[0]));
		else
			model = new DefaultComboBoxModel(ScramblePlugin.getScrambleVariations());
		this.setModel(model);
		this.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
		Configuration.addConfigurationChangeListener(this);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}

		// Select the current value
		setSelectedItem(value);
		return this;
	}
	
	public void configurationChanged() {
		this.setModel(new DefaultComboBoxModel(ScramblePlugin.getScrambleCustomizations(false).toArray(new ScrambleCustomization[0])));
		this.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
	}
}
