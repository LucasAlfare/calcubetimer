package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.JSpinnerWithText;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

public class ScrambleExportDialog extends JDialog implements ActionListener {
	private JTextField urlField;
	private JButton browse;
	private ScrambleChooserComboBox scrambleChooser;
	private JSpinnerWithText scrambleLength, numberOfScrambles;
	private JButton htmlExportButton, exportButton, cancelButton;
	public ScrambleExportDialog(JFrame owner, ScrambleVariation selected) {
		super(owner, StringAccessor.getString("ScrambleExportDialog.exportscrambles"), true); //$NON-NLS-1$
		urlField = new JTextField(40);
		urlField.setToolTipText(StringAccessor.getString("ScrambleExportDialog.choosefile")); //$NON-NLS-1$
		browse = new JButton(StringAccessor.getString("ScrambleExportDialog.browse")); //$NON-NLS-1$
		browse.addActionListener(this);

		scrambleChooser = new ScrambleChooserComboBox(false, false);
		scrambleChooser.setSelectedItem(selected);
		scrambleChooser.addActionListener(this);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));

		JPanel sideBySide = new JPanel();
		sideBySide.setLayout(new BoxLayout(sideBySide, BoxLayout.X_AXIS));
		sideBySide.add(urlField);
		sideBySide.add(browse);
		
		subPanel.add(sideBySide);
		subPanel.add(scrambleChooser);

		scrambleLength = new JSpinnerWithText(selected.getLength(), 1, StringAccessor.getString("ScrambleExportDialog.lengthscrambles")); //$NON-NLS-1$
		numberOfScrambles = new JSpinnerWithText(Configuration.getInt(VariableKey.RA_SIZE0, false), 1, StringAccessor.getString("ScrambleExportDialog.numberscrambles")); //$NON-NLS-1$
		subPanel.add(scrambleLength);
		subPanel.add(numberOfScrambles);
		
		exportButton = new JButton(StringAccessor.getString("ScrambleExportDialog.export")); //$NON-NLS-1$
		exportButton.addActionListener(this);
		htmlExportButton = new JButton(StringAccessor.getString("ScrambleExportDialog.htmlexport")); //$NON-NLS-1$
		htmlExportButton.addActionListener(this);
		cancelButton = new JButton(StringAccessor.getString("ScrambleExportDialog.cancel")); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		sideBySide = new JPanel();
		sideBySide.add(exportButton);
		sideBySide.add(htmlExportButton);
		sideBySide.add(cancelButton);
		subPanel.add(sideBySide);
		
		add(subPanel);
		
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == browse) {
			JFileChooser fc = new JFileChooser("."); //$NON-NLS-1$
			if(fc.showDialog(this, StringAccessor.getString("ScrambleExportDialog.save")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
				File selectedFile = fc.getSelectedFile();
				urlField.setText(selectedFile.toURI().toString());
			}
		} else if(source == scrambleChooser && scrambleLength != null) {
			ScrambleVariation curr = (ScrambleVariation) scrambleChooser.getSelectedItem();
			scrambleLength.setValue(curr.getLength());
		} else if(source == htmlExportButton) {
			URL file = null;
			try {
				file = new URI(urlField.getText()).toURL();
			} catch (Exception e1) {
				Utils.showErrorDialog(this, e1.getMessage() + "\n" + StringAccessor.getString("ScrambleExportDialog.badfilename"));
				return;
			}
			if(exportScramblesToHTML(file, getNumberOfScrambles(), getVariation()))
				setVisible(false);
		} else if(source == exportButton) {
			URL file = null;
			try {
				file = new URI(urlField.getText()).toURL();
			} catch (Exception e1) {
				Utils.showErrorDialog(this, e1.getMessage() + "\n" + StringAccessor.getString("ScrambleExportDialog.badfilename"));
				return;
			}
			if(exportScrambles(file, getNumberOfScrambles(), getVariation()))
				setVisible(false);
		} else if(source == cancelButton) {
			setVisible(false);
		}
	}
	
	private int getNumberOfScrambles() {
		return numberOfScrambles.getSpinnerValue();
	}

	private ScrambleVariation getVariation() {
		ScrambleVariation var = (ScrambleVariation) scrambleChooser.getSelectedItem();
		if(scrambleLength != null)
			var.setLength(scrambleLength.getSpinnerValue());
		return var;
	}

	private boolean exportScrambles(URL outputFile, int numberOfScrambles, ScrambleVariation scrambleVariation) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputFile.toURI())));
			ScrambleList generatedScrambles = new ScrambleList();
			generatedScrambles.setScrambleCustomization(new ScrambleCustomization(scrambleVariation, null));
			for(int ch = 0; ch < numberOfScrambles; ch++, generatedScrambles.getNext()) {
				out.println(generatedScrambles.getCurrent().toString());
			}
			out.close();
			Utils.showConfirmDialog(this, StringAccessor.getString("ScrambleExportDialog.successmessage") + "\n" + outputFile.getPath());
		} catch(Exception e) {
			Utils.showErrorDialog(this, e.toString());
			return false;
		}
		return true;
	}
	
	private boolean exportScramblesToHTML(URL outputFile, int numberOfScrambles, ScrambleVariation scrambleVariation) {
		File htmlFile = null;
		try {
			htmlFile = new File(outputFile.toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			Utils.showErrorDialog(this, e1.toString());
			return false;
		}
		File imageDir = new File(htmlFile.getParentFile(), htmlFile.getName() + ".files");
		if(imageDir.isFile()){
			Utils.showErrorDialog(this, StringAccessor.getString("ScrambleExportDialog.directoryexists") + "\n" + imageDir);
			return false;
		}
		//need to check isDirectory() because mkdir() returns false if the directory exists
		if(!imageDir.isDirectory() && !imageDir.mkdir()) {
			Utils.showErrorDialog(this, StringAccessor.getString("ScrambleExportDialog.mkdirfail") + "\n" + imageDir);
			return false;
		}

		ScramblePlugin sp = scrambleVariation.getScramblePlugin();

		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputFile.toURI())));
			out.println("<html><head><title>Exported Scrambles</title></head><body><table>");
			ScrambleList generatedScrambles = new ScrambleList();
			generatedScrambles.setScrambleCustomization(new ScrambleCustomization(scrambleVariation, null));

			for(int ch = 0; ch < numberOfScrambles; ch++, generatedScrambles.getNext()) {
				Scramble s = scrambleVariation.generateScramble(generatedScrambles.getCurrent().getScramble());
				String str = s.toString();
				BufferedImage image = sp.getScrambleImage(s, Configuration.getInt(VariableKey.POPUP_GAP, false).intValue(), sp.getDefaultUnitSize(), sp.getColorScheme(false));

				File file = new File(imageDir, "scramble" + ch + ".png");
				ImageIO.write(image, "png", file);
				out.println("<tr><td>" + (ch+1) + "</td><td width='100%'>" + str + "</td><td><img src='" + imageDir.getName() + File.separator + file.getName() + "'></td></tr>");
			}
			out.println("</table></body></html>");
			out.close();
			Utils.showConfirmDialog(this, StringAccessor.getString("ScrambleExportDialog.successmessage") + "\n" + outputFile.getPath());
		} catch(Exception e) {
			e.printStackTrace();
			Utils.showErrorDialog(this, e.toString());
			return false;
		}
		return true;
	}
}
