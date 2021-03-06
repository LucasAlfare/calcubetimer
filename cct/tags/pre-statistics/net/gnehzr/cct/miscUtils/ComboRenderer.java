package net.gnehzr.cct.miscUtils;
import java.awt.Component;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class ComboRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;

	public ComboRenderer() {
		setOpaque(true);
		setBorder(new EmptyBorder(1, 1, 1, 1));
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
		if(isSelected){
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		if(!((ComboItem)value).isEnabled()) {
			setBackground(list.getBackground());
			setForeground(UIManager.getColor("Label.disabledForeground"));
		}

		if(((ComboItem)value).isInUse()){
			setForeground(Color.RED);
		}
		setFont(list.getFont());
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
