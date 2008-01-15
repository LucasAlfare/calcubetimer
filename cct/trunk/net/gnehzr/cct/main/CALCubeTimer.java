package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationDialog;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.help.AboutScrollFrame;
import net.gnehzr.cct.miscUtils.DynamicButton;
import net.gnehzr.cct.miscUtils.DynamicCheckBox;
import net.gnehzr.cct.miscUtils.DynamicCheckBoxMenuItem;
import net.gnehzr.cct.miscUtils.DynamicLabel;
import net.gnehzr.cct.miscUtils.DynamicMenu;
import net.gnehzr.cct.miscUtils.DynamicMenuItem;
import net.gnehzr.cct.miscUtils.DynamicSelectableLabel;
import net.gnehzr.cct.miscUtils.DynamicString;
import net.gnehzr.cct.miscUtils.DynamicStringSettable;
import net.gnehzr.cct.miscUtils.JListMutable;
import net.gnehzr.cct.miscUtils.MyCellRenderer;
import net.gnehzr.cct.miscUtils.PuzzleTypeCellRenderer;
import net.gnehzr.cct.miscUtils.Utils;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScrambleType;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;
import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.umts.client.CCTClient;

import org.jvnet.lafwidget.LafWidget;
import org.jvnet.lafwidget.utils.LafConstants;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants;
import org.jvnet.substance.watermark.SubstanceImageWatermark;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("serial")
public class CALCubeTimer extends JFrame implements ActionListener, ListDataListener, ChangeListener, ConfigurationChangeListener, ItemListener {
	public static final String CCT_VERSION = "0.3 beta";
	private JScrollPane timesScroller = null;
	private TimerLabel timeLabel = null;
	private JLabel onLabel = null;
	private JListMutable<SolveTime> timesList = null;
	private TimerPanel startStopPanel = null;
	private JFrame fullscreenFrame = null;
	private TimerLabel bigTimersDisplay = null;
	private ScrambleArea scrambleText = null;
	private ScrambleFrame scramblePopup = null;
	private String puzzleChoice = null;
	private JComboBox scrambleChooser = null;
	private JPanel scrambleAttributes = null;
	private JSpinner scrambleNumber, scrambleLength = null;
	private ScrambleList scrambles = null;
	private JComboBox profiles = null;
	private Statistics stats = null;
	private StackmatInterpreter stackmatTimer = null;
	private TimerHandler timeListener = null;
	private CCTClient client;
	private ConfigurationDialog configurationDialog;
	
	public static final ImageIcon cube = new ImageIcon(CALCubeTimer.class.getResource("cube.png"));
	
	public CALCubeTimer() {
		stackmatTimer = new StackmatInterpreter();
		Configuration.addConfigurationChangeListener(stackmatTimer);
		stats = new Statistics();
		stats.addListDataListener(this);
		timeListener = new TimerHandler();
		stackmatTimer.addPropertyChangeListener(timeListener);

		this.setUndecorated(true);
		createActions();
		initializeGUIComponents();
	}
	
	public void setVisible(boolean b) {
		stackmatTimer.execute();
		super.setVisible(b);
	}

	private HashMap<String, AbstractAction> actionMap;
	private StatisticsAction currentAverageAction;
	private StatisticsAction rollingAverageAction;
	private StatisticsAction sessionAverageAction;
	private AddTimeAction addTimeAction;
	private ImportScramblesAction importScramblesAction;
	private ExportScramblesAction exportScramblesAction;
	private ExitAction exitAction;
	private AboutAction aboutAction;
	private DocumentationAction documentationAction;
	private ShowConfigurationDialogAction showConfigurationDialogAction;
	private ConnectToServerAction connectToServerAction;
	private FlipFullScreenAction flipFullScreenAction;
	private KeyboardTimingAction keyboardTimingAction;
	private SpacebarOptionAction spacebarOptionAction;
	private FullScreenTimingAction fullScreenTimingAction;
	private IntegratedTimerAction integratedTimerAction;
	private HideScramblesAction hideScramblesAction;
	private AnnoyingDisplayAction annoyingDisplayAction;
	private LessAnnoyingDisplayAction lessAnnoyingDisplayAction;
	private ResetAction resetAction;
	private void createActions(){
		actionMap = new HashMap<String, AbstractAction>();

		keyboardTimingAction = new KeyboardTimingAction(this);
		keyboardTimingAction.putValue(Action.NAME, "Use keyboard timer");
		keyboardTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		keyboardTimingAction.putValue(Action.SHORT_DESCRIPTION, "NOTE: will disable Stackmat!");
		actionMap.put("keyboardtiming", keyboardTimingAction);

		addTimeAction = new AddTimeAction(this);
		addTimeAction.putValue(Action.NAME, "Add time");
		addTimeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		actionMap.put("addtime", addTimeAction);

		resetAction = new ResetAction(this);
		resetAction.putValue(Action.NAME, "Reset");
		resetAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		actionMap.put("reset", resetAction);

		currentAverageAction = new StatisticsAction(this, stats, Statistics.averageType.CURRENT);
		actionMap.put("currentaverage", currentAverageAction);
		rollingAverageAction = new StatisticsAction(this, stats, Statistics.averageType.RA);
		actionMap.put("bestaverage", rollingAverageAction);
		sessionAverageAction = new StatisticsAction(this, stats, Statistics.averageType.SESSION);
		actionMap.put("sessionaverage", sessionAverageAction);

		flipFullScreenAction = new FlipFullScreenAction(this);
		flipFullScreenAction.putValue(Action.NAME, "+");
		flipFullScreenAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Fullscreen");
		actionMap.put("togglefullscreen", flipFullScreenAction);

		importScramblesAction = new ImportScramblesAction(this);
		importScramblesAction.putValue(Action.NAME, "Import scrambles");
		importScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		importScramblesAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		actionMap.put("importscrambles", importScramblesAction);

		exportScramblesAction = new ExportScramblesAction(this);
		exportScramblesAction.putValue(Action.NAME, "Export scrambles");
		exportScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		exportScramblesAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		actionMap.put("exportscrambles", exportScramblesAction);

		connectToServerAction = new ConnectToServerAction(this);
		connectToServerAction.putValue(Action.NAME, "Connect to server");
		connectToServerAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		connectToServerAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		actionMap.put("connecttoserver", connectToServerAction);

		showConfigurationDialogAction = new ShowConfigurationDialogAction(this);
		showConfigurationDialogAction.putValue(Action.NAME, "Configuration");
		showConfigurationDialogAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		showConfigurationDialogAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		actionMap.put("showconfiguration", showConfigurationDialogAction);

		exitAction = new ExitAction(this);
		exitAction.putValue(Action.NAME, "Exit");
		exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		exitAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		actionMap.put("exit", exitAction);

		integratedTimerAction = new IntegratedTimerAction(this);
		integratedTimerAction.putValue(Action.NAME, "Integrate timer and display");
		integratedTimerAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		actionMap.put("toggleintegratedtimer", integratedTimerAction);

		annoyingDisplayAction = new AnnoyingDisplayAction(this);
		annoyingDisplayAction.putValue(Action.NAME, "Use annoying status light");
		annoyingDisplayAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		actionMap.put("toggleannoyingdisplay", annoyingDisplayAction);

		lessAnnoyingDisplayAction = new LessAnnoyingDisplayAction(this);
		lessAnnoyingDisplayAction.putValue(Action.NAME, "Use less-annoying status light");
		lessAnnoyingDisplayAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		actionMap.put("togglelessannoyingdisplay", lessAnnoyingDisplayAction);

		hideScramblesAction = new HideScramblesAction(this);
		hideScramblesAction.putValue(Action.NAME, "Hide scrambles when timer not focused");
		hideScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
		actionMap.put("togglehidescrambles", hideScramblesAction);

		spacebarOptionAction = new SpacebarOptionAction();
		spacebarOptionAction.putValue(Action.NAME, "Only spacebar starts timer");
		spacebarOptionAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		actionMap.put("togglespacebarstartstimer", spacebarOptionAction);

		fullScreenTimingAction = new FullScreenTimingAction();
		fullScreenTimingAction.putValue(Action.NAME, "Fullscreen while timing");
		fullScreenTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		actionMap.put("togglefullscreentiming", fullScreenTimingAction);

		//TODO - possibly switch to anonymous inner classes?
//		AbstractAction act = new AbstractAction() {
//			public void actionPerformed(ActionEvent e) {
//				Configuration.setFullScreenWhileTiming(((AbstractButton)e.getSource()).isSelected());
//			}
//		};
//		act.putValue(Action.SELECTED_KEY, Configuration.isFullScreenWhileTiming());
//		act.putValue(Action.NAME, "Fullscreen while timing");
//		act.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
//		actionMap.put("togglefullscreentiming", act);

		documentationAction = new DocumentationAction(this);
		documentationAction.putValue(Action.NAME, "View Documentation");
		actionMap.put("showdocumentation", documentationAction);

		aboutAction = new AboutAction(new AboutScrollFrame("About CCT " + CCT_VERSION,
						CALCubeTimer.class.getResource("about.html"),
						cube.getImage()));
		aboutAction.putValue(Action.NAME, "About");
		actionMap.put("showabout", aboutAction);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(e.getActionCommand().equals(SCRAMBLE_ATTRIBUTE_CHANGED)) {
			ArrayList<String> attrs = new ArrayList<String>();
			for(JCheckBox attr : attributes) {
				if(attr.isSelected())
					attrs.add(attr.getText());
			}
			String[] attributes = new String[attrs.size()];
			attributes = attrs.toArray(attributes);
			Configuration.setPuzzleAttributes(Configuration.getScrambleType(puzzleChoice), attributes);
			scrambles.getCurrent().setAttributes(attributes);
			updateScramble();
		} else if(e.getActionCommand().equals(GUI_LAYOUT_CHANGED)) {
			String layout = ((JRadioButtonMenuItem) source).getText();
			parseXML_GUI(Configuration.getXMLFile(layout));
			Configuration.setString(VariableKey.XML_LAYOUT, layout);
			this.pack();
		}
	}
	
	private Timer tickTock;
	private JTextField tf;
	private JButton maximize;
	private static final String GUI_LAYOUT_CHANGED = "GUI Layout Changed";
	private JMenu customGUIMenu;
	private void initializeGUIComponents() {
		addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e){
				timeLabel.refreshFocus();
			}
			public void windowLostFocus(WindowEvent e){
				timeLabel.refreshFocus();
			}
		});
		tickTock = new Timer(0, null);
		configurationDialog = new ConfigurationDialog(this, true, stackmatTimer, tickTock);

		scrambleChooser = new JComboBox();
		scrambleChooser.setRenderer(new PuzzleTypeCellRenderer());
		scrambleChooser.addItemListener(this);

		SpinnerNumberModel model = new SpinnerNumberModel(1, //initial value
				1,					//min
				1,	//max
				1);					//step
		scrambleNumber = new JSpinner(model);
		scrambleNumber.setToolTipText("Select nth scramble");
		((JSpinner.DefaultEditor) scrambleNumber.getEditor()).getTextField().setColumns(3);
		scrambleNumber.addChangeListener(this);

		model = new SpinnerNumberModel(1, //initial value
				1,					//min
				null,				//max
				1);					//step
		scrambleLength = new JSpinner(model);
		scrambleLength.setToolTipText("Set length of scramble");
		((JSpinner.DefaultEditor) scrambleLength.getEditor()).getTextField().setColumns(3);
		scrambleLength.addChangeListener(this);

		scrambleAttributes = new JPanel();
		createScrambleAttributes();

		scramblePopup = new ScrambleFrame(this, "Scramble View");
		scramblePopup.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		scramblePopup.setIconImage(cube.getImage());
		scramblePopup.setFocusableWindowState(false);

		onLabel = new JLabel("Timer is OFF");
		onLabel.setFont(onLabel.getFont().deriveFont(AffineTransform.getScaleInstance(2, 2)));
		
		tf = new JTextField();
        tf.setBorder(BorderFactory.createLineBorder(Color.black));
		timesList = new JListMutable<SolveTime>(stats, tf, false, "Type new time here", "Add time...");
		timesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		timesList.setLayoutOrientation(JList.VERTICAL);
		timesList.setCellRenderer(new MyCellRenderer(stats));
		timesScroller = new JScrollPane(timesList);

		scrambleText = new ScrambleArea();
		scrambleText.setAlignmentX(.5f);
		timeLabel = new TimerLabel(timeListener, scrambleText);

		startStopPanel = new TimerPanel(timeListener, scrambleText, timeLabel);
		startStopPanel.setKeyboard(true);

		timeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		timeLabel.setMinimumSize(new Dimension(0, 150));
		timeLabel.setPreferredSize(new Dimension(0, 150));
		timeLabel.setAlignmentX(.5f);

		JFrame.setDefaultLookAndFeelDecorated(false);
		fullscreenFrame = new JFrame();
		fullscreenFrame.setUndecorated(true);
		fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());
		fullscreenFrame.setContentPane(panel);
		bigTimersDisplay = new TimerLabel(timeListener, null);
		bigTimersDisplay.setBackground(Color.WHITE);
		bigTimersDisplay.setEnabledTiming(true);

		panel.add(bigTimersDisplay, BorderLayout.CENTER);
		JButton fullScreenButton = new JButton(flipFullScreenAction);
		panel.add(fullScreenButton, BorderLayout.PAGE_END);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		fullscreenFrame.setResizable(false);
		fullscreenFrame.setSize(screenSize.width, screenSize.height);
		fullscreenFrame.validate();

		customGUIMenu = new JMenu("Load custom GUI");

		ButtonGroup group = new ButtonGroup();
		for(File file : Configuration.getXMLLayoutsAvailable()) {
			JRadioButtonMenuItem temp = new JRadioButtonMenuItem(file.getName());
			temp.setSelected(file.equals(Configuration.getXMLGUILayout()));
			temp.setActionCommand(GUI_LAYOUT_CHANGED);
			temp.addActionListener(this);
			group.add(temp);
			customGUIMenu.add(temp);
		}

		maximize = new JButton(flipFullScreenAction);
		maximize.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.TRUE);


		profiles = new JComboBox();
		profiles.addItemListener(this);
//		profiles.setMaximumSize(new Dimension(1000, 100));
	}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(source == scrambleChooser) {
			scrambleChooserAction();
		} else if(source == profiles) {
			Profile affected = (Profile)e.getItem();
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				saveToConfiguration();
				try {
					Configuration.saveConfigurationToFile(affected.getConfigurationFile());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else if(e.getStateChange() == ItemEvent.SELECTED) {
				Configuration.setSelectedProfile(affected);
				try {
					Configuration.loadConfiguration(affected.getConfigurationFile());
					Configuration.apply();
				} catch (IOException err) {
					err.printStackTrace();
				} catch (URISyntaxException err) {
					err.printStackTrace();
				}
			}
		}
	}
	private Profile getSelectedProfile() {
		return (Profile) profiles.getSelectedItem();
	}

	private void parseXML_GUI(File xmlGUIfile) {
		//this is needed to compute the size of the gui correctly
		scrambleText.resetPreferredSize();
		
		DefaultHandler handler = new GUIParser(this);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmlGUIfile, handler);
		} catch(SAXParseException spe) {
			System.err.println(spe.getSystemId() + ":" + spe.getLineNumber() + ": parse error: " + spe.getMessage());

			Exception x = spe;
			if(spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();
		} catch(SAXException se) {
			Exception x = se;
			if(se.getException() != null)
				x = se.getException();
			x.printStackTrace();
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	//This is a more appropriate way of doing gui's, to prevent weird resizing issues
	public Dimension getMinimumSize() {
		return new Dimension(235, 30);
	}

	private JCheckBox[] attributes;
	private static final String SCRAMBLE_ATTRIBUTE_CHANGED = "Scramble Attribute Changed";
	private void createScrambleAttributes() {
		if(puzzleChoice == null) //this happens when we are initializing cct, before a profile is selected
			return;
		scrambleAttributes.removeAll();
		ScrambleType curr = Configuration.getScrambleType(puzzleChoice);
		String[] attrs = Configuration.getPuzzleAttributes(curr.getPuzzleClass());
		attributes = new JCheckBox[attrs.length];

		for(int ch = 0; ch < attrs.length; ch++) { //create checkbox for each possible attribute
			boolean selected = false;
			for(String attr : Configuration.getPuzzleAttributes(curr)) { //see if attribute is selected
				if(attrs[ch].equals(attr)) {
					selected = true;
					break;
				}
			}
			attributes[ch] = new JCheckBox(attrs[ch], selected);
			attributes[ch].setActionCommand(SCRAMBLE_ATTRIBUTE_CHANGED);
			attributes[ch].addActionListener(this);
			scrambleAttributes.add(attributes[ch]);
		}
	}

	//{{{ GUIParser
	private class GUIParser extends DefaultHandler {
		private int level = -2;
//		private String location;
		private ArrayList<String> strs;
		private ArrayList<JComponent> componentTree;
		private ArrayList<Boolean> needText;
		private ArrayList<String> elementNames;
		private JFrame frame;

		public GUIParser(JFrame frame){
			this.frame = frame;

			componentTree = new ArrayList<JComponent>();
			strs = new ArrayList<String>();
			needText = new ArrayList<Boolean>();
			elementNames = new ArrayList<String>();
		}

		public void setDocumentLocator(Locator l) {
//			location = l.getSystemId();
		}

		//{{{ startElement
		public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
			String temp;
			JComponent com = null;

			level++;
			String elementName = qName.toLowerCase();

			if(level == -1){
				if(!elementName.equals("gui")){
					throw new SAXException("parse error: invalid root tag");
				}
				return;
			}
			else if(level == 0){
				if(!(elementName.equals("menubar") || elementName.equals("panel")))
					throw new SAXException("parse error: level 1 must be menubar or panel");
			}

			//must deal with level < 0 before adding anything
			elementNames.add(elementName);
			needText.add(elementName.equals("label") || elementName.equals("selectablelabel") || elementName.equals("button") || elementName.equals("checkbox") || elementName.equals("menu") || elementName.equals("menuitem") || elementName.equals("checkboxmenuitem"));
			strs.add("");

			if(elementName.equals("label")){
				com = new DynamicLabel();
			}
			else if(elementName.equals("selectablelabel")) {
				com = new DynamicSelectableLabel();
				try{
					if((temp = attrs.getValue("editable")) != null)
						((DynamicSelectableLabel) com).setEditable(Boolean.parseBoolean(temp));
				} catch(Exception e){
					throw new SAXException(e);
				}
			}
			else if(elementName.equals("button")){
				com = new DynamicButton();
			}
			else if(elementName.equals("checkbox")){
				com = new DynamicCheckBox();
			}
			else if(elementName.equals("panel")){
				com = new JPanel();
				int hgap = 0;
				int vgap = 0;
				int align = FlowLayout.CENTER;
				int rows = 0;
				int cols = 0;
				int orientation = BoxLayout.Y_AXIS;

				LayoutManager layout;
				if(attrs == null) layout = new FlowLayout();
				else{
					try{
						if((temp = attrs.getValue("hgap")) != null) hgap = Integer.parseInt(temp);
						if((temp = attrs.getValue("vgap")) != null) vgap = Integer.parseInt(temp);
						if((temp = attrs.getValue("rows")) != null) rows = Integer.parseInt(temp);
						if((temp = attrs.getValue("cols")) != null) cols = Integer.parseInt(temp);
					} catch(Exception e){
						throw new SAXException("integer parse error", e);
					}

					if((temp = attrs.getValue("align")) != null){
						if(temp.equalsIgnoreCase("left")) align = FlowLayout.LEFT;
						else if(temp.equalsIgnoreCase("right")) align = FlowLayout.RIGHT;
						else if(temp.equalsIgnoreCase("center")) align = FlowLayout.CENTER;
						else if(temp.equalsIgnoreCase("leading")) align = FlowLayout.LEADING;
						else if(temp.equalsIgnoreCase("trailing")) align = FlowLayout.TRAILING;
						else throw new SAXException("parse error in align");
					}

					if((temp = attrs.getValue("orientation")) != null){
						if(temp.equalsIgnoreCase("horizontal")) orientation = BoxLayout.X_AXIS;
						else if(temp.equalsIgnoreCase("vertical")) orientation = BoxLayout.Y_AXIS;
						else if(temp.equalsIgnoreCase("page")) orientation = BoxLayout.PAGE_AXIS;
						else if(temp.equalsIgnoreCase("line")) orientation = BoxLayout.LINE_AXIS;
						else throw new SAXException("parse error in orientation");
					}

					if((temp = attrs.getValue("layout")) != null) {
						if(temp.equalsIgnoreCase("border")) layout = new BorderLayout(hgap, vgap);
						else if(temp.equalsIgnoreCase("box")) layout = new BoxLayout(com, orientation);
						else if(temp.equalsIgnoreCase("grid")) layout = new GridLayout(rows, cols, hgap, vgap);
						else if(temp.equalsIgnoreCase("flow")) layout = new FlowLayout(align, hgap, vgap);
						else throw new SAXException("parse error in layout");
					} else 
						layout = new FlowLayout(align, hgap, vgap);
				}

				com.setLayout(layout);
			}
			else if(elementName.equals("component")){
				if(attrs == null || (temp = attrs.getValue("type")) == null)
					throw new SAXException("parse error in component");
				else if(temp.equalsIgnoreCase("scramblechooser")) com = scrambleChooser;
				else if(temp.equalsIgnoreCase("scramblenumber")) com = scrambleNumber;
				else if(temp.equalsIgnoreCase("scramblelength")) com = scrambleLength;
				else if(temp.equalsIgnoreCase("scrambleattributes")) com = scrambleAttributes;
				else if(temp.equalsIgnoreCase("stackmatstatuslabel")) com = onLabel;
				else if(temp.equalsIgnoreCase("scrambletext")) com = scrambleText;
				else if(temp.equalsIgnoreCase("timerdisplay")) com = timeLabel;
				else if(temp.equalsIgnoreCase("startstoppanel")) com = startStopPanel;
				else if(temp.equalsIgnoreCase("timeslist")) com = timesScroller;
				else if(temp.equalsIgnoreCase("customguimenu")) com = customGUIMenu;
				else if(temp.equalsIgnoreCase("maximizebutton")) com = maximize;
				else if(temp.equalsIgnoreCase("profilecombobox")) com = profiles;
			}
			else if(elementName.equals("center") || elementName.equals("east") || elementName.equals("west") || elementName.equals("south") || elementName.equals("north") || elementName.equals("page_start") || elementName.equals("page_end") || elementName.equals("line_start") || elementName.equals("line_end")){
				com = null;
			}
			else if(elementName.equals("menubar")){
				com = new JMenuBar();
			}
			else if(elementName.equals("menu")){
				JMenu menu = new DynamicMenu();
				if((temp = attrs.getValue("mnemonic")) != null)
					menu.setMnemonic(temp.charAt(0));

				com = menu;
			}
			else if(elementName.equals("menuitem")){
				com = new DynamicMenuItem();
			}
			else if(elementName.equals("checkboxmenuitem")){
				com = new DynamicCheckBoxMenuItem();
			}
			else if(elementName.equals("separator")){
				com = new JSeparator();
				//TODO needs orientation
			}
			
			else if(elementName.equals("glue")) {
				Component glue = null;
				if((temp = attrs.getValue("orientation")) != null) {
					if(temp.equalsIgnoreCase("horizontal")) glue = Box.createHorizontalGlue();
					else if(temp.equalsIgnoreCase("vertical")) glue = Box.createVerticalGlue();
					else throw new SAXException("parse error in orientation");
				}
				else glue = Box.createGlue();
				com = new JPanel();
				com.add(glue);
			}			
			else throw new SAXException("invalid tag " + elementName);

			if(com instanceof AbstractButton){
				if(attrs != null){
					if((temp = attrs.getValue("action")) != null){
						AbstractAction a = actionMap.get(temp.toLowerCase());
						if(a != null) ((AbstractButton)com).setAction(a);
						else throw new SAXException("parse error in action");
					}
				}
			}

			if(com != null && attrs != null){
				try{
					if((temp = attrs.getValue("alignmentX")) != null)
						com.setAlignmentX(Float.parseFloat(temp));
					if((temp = attrs.getValue("alignmentY")) != null)
						com.setAlignmentY(Float.parseFloat(temp));
					if((temp = attrs.getValue("border")) != null) {
						String[] titleAttrs = temp.split(";");
						
						Border border = null;
						if(titleAttrs[0].equals(""))
							border = BorderFactory.createEtchedBorder();
						else
							border = BorderFactory.createLineBorder(Utils.stringToColor(new DynamicString(titleAttrs[0], null).toString()));
						if(titleAttrs.length > 1)
							border = BorderFactory.createTitledBorder(border, titleAttrs[1]);
						com.setBorder(border);
					}
					if((temp = attrs.getValue("opaque")) != null)
						com.setOpaque(Boolean.parseBoolean(temp));
					if((temp = attrs.getValue("background")) != null)
						com.setBackground(Utils.stringToColor(temp));
					if((temp = attrs.getValue("foreground")) != null)
						com.setForeground(Utils.stringToColor(temp));
				} catch(Exception e) {
					throw new SAXException(e);
				}
			}

			componentTree.add(com);

			if(level == 0){
				if(elementName.equals("panel")){
					frame.setContentPane((JPanel)componentTree.get(level));
				}
				else if(elementName.equals("menubar")){
					frame.setJMenuBar((JMenuBar)componentTree.get(level));
				}
			}
			else if(com != null){
				temp = null;
				for(int i = level - 1; i >= 0; i--){
					if(componentTree.get(i) != null){
						if(temp == null) componentTree.get(i).add(com);
						else{
							String loc = null;
							if(temp.equals("center")) loc = BorderLayout.CENTER;
							else if(temp.equals("east")) loc = BorderLayout.EAST;
							else if(temp.equals("west")) loc = BorderLayout.WEST;
							else if(temp.equals("south")) loc = BorderLayout.SOUTH;
							else if(temp.equals("north")) loc = BorderLayout.NORTH;
							else if(temp.equals("page_start")) loc = BorderLayout.PAGE_START;
							else if(temp.equals("page_end")) loc = BorderLayout.PAGE_END;
							else if(temp.equals("line_start")) loc = BorderLayout.LINE_START;
							else if(temp.equals("line_end")) loc = BorderLayout.LINE_END;
							componentTree.get(i).add(com, loc);
						}
						break;
					}
					else temp = elementNames.get(i);
				}
			}
		}
		//}}}

		public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
			if(level >= 0){
				if(needText.get(level) && strs.get(level).length() > 0)
					if(componentTree.get(level) instanceof DynamicStringSettable)
						((DynamicStringSettable)componentTree.get(level)).setDynamicString(new DynamicString(strs.get(level), stats));

				componentTree.remove(level);
				elementNames.remove(level);
				strs.remove(level);
				needText.remove(level);
			}
			level--;
		}

		public void characters(char buf[], int offset, int len) throws SAXException {
			if(level >= 0 && needText.get(level)){
				String s = new String(buf, offset, len);
				if(!s.trim().equals("")) strs.set(level, strs.get(level) + s);
			}
		}

		public void error(SAXParseException e) throws SAXParseException {
			throw e;
		}

		public void warning(SAXParseException e) throws SAXParseException {
			System.err.println(e.getSystemId() + ":" + e.getLineNumber() + ": warning: " + e.getMessage());
		}
	} //}}}

	private void repaintTimes() {
		String temp = stats.average(Statistics.averageType.CURRENT);
		sendAverage(temp);
		temp = stats.average(Statistics.averageType.RA);
		sendBestAverage(temp);
		currentAverageAction.setEnabled(stats.isValid(Statistics.averageType.CURRENT));
		rollingAverageAction.setEnabled(stats.isValid(Statistics.averageType.RA));
		sessionAverageAction.setEnabled(stats.isValid(Statistics.averageType.SESSION));
		timesList.ensureIndexIsVisible(stats.getSize());
	}

	public static void main(String[] args) {
		JDialog.setDefaultLookAndFeelDecorated(true);
		JFrame.setDefaultLookAndFeelDecorated(true);

		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			UIManager.setLookAndFeel(new SubstanceModerateLookAndFeel());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		UIManager.put(LafWidget.TEXT_EDIT_CONTEXT_MENU, Boolean.TRUE);
		UIManager.put(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.TRUE);
		UIManager.put(LafWidget.ANIMATION_KIND, LafConstants.AnimationKind.NONE);
//		UIManager.put(SubstanceLookAndFeel.WATERMARK_TO_BLEED, Boolean.TRUE);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String errors = Configuration.getStartupErrors();
				if(!errors.equals("")) {
					JOptionPane.showMessageDialog(
							null,
							errors,
							"Can't start CCT!",
							JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				try {
					Configuration.loadConfiguration(Configuration.getSelectedProfile().getConfigurationFile());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				CALCubeTimer main = new CALCubeTimer();
				Configuration.addConfigurationChangeListener(main);
				main.setTitle("CCT " + CCT_VERSION);
				main.setIconImage(cube.getImage());
				main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				Configuration.apply();
				main.setVisible(true);
			}
		});
	}

	private void exportScrambles(URL outputFile, int numberOfScrambles, ScrambleType scrambleChoice) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputFile.toURI())));
			ScrambleList generatedScrambles = new ScrambleList(scrambleChoice);
			for(int ch = 0; ch < numberOfScrambles; ch++, generatedScrambles.getNext()) {
				out.println(generatedScrambles.getCurrent().toString());
			}
			out.close();
			JOptionPane.showMessageDialog(this,
					"Scrambles successfully saved!",
					outputFile.getPath(),
					JOptionPane.INFORMATION_MESSAGE);
		} catch(Exception e) {
			showErrorMessage("Error!\n" + e.toString(), "Hmmmmm...");
		}
	}

	private void readScramblesFile(URL inputFile, ScrambleType newType) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputFile.openStream()));
			scrambles = ScrambleList.importScrambles(newType, in);
			in.close();

			int newLength = scrambles.getCurrent().getLength();
			puzzleChoice = newType.toString();
			newType.setLength(newLength);
			safeSetValue(scrambleLength, newLength);
			safeSelectItem(scrambleChooser, puzzleChoice);
			updateScramble();
			JOptionPane.showMessageDialog(this,
					"Scrambles successfully loaded!",
					inputFile.getPath(),
					JOptionPane.INFORMATION_MESSAGE);
		} catch(ConnectException e) {
			showErrorMessage("Connection refused!", "Error!");
		} catch(FileNotFoundException e) {
			showErrorMessage(inputFile + "\nURL not found!", "Four-O-Four-ed!");
		} catch(Exception e) {
			showErrorMessage("Error!\n" + e.toString(), "Hmmmmm...");
		}
	}

	private void showErrorMessage(String errorMessage, String title){
		JOptionPane.showMessageDialog(this, errorMessage, title, JOptionPane.ERROR_MESSAGE);
	}

	private void safeSetValue(JSpinner test, Object val) {
		test.removeChangeListener(this);
		test.setValue(val);
		test.addChangeListener(this);
	}
	private void safeSelectItem(JComboBox test, Object item) {
		test.removeItemListener(this);
		test.setSelectedItem(item);
		test.addItemListener(this);
	}
	private void safeSetScrambleNumberMax(int max) {
		scrambleNumber.removeChangeListener(this);
		((SpinnerNumberModel) scrambleNumber.getModel()).setMaximum(max);
		scrambleNumber.addChangeListener(this);
	}
	private void updateScramble() {
		if(scrambleChooser.getSelectedItem() == null) return;

		String newPuzzleChoice = (String)scrambleChooser.getSelectedItem();
		int newLength = (Integer) scrambleLength.getValue();
		
		ScrambleType scrambleChoice = Configuration.getScrambleType(puzzleChoice);
		ScrambleType newScrambleChoice = Configuration.getScrambleType(newPuzzleChoice);
		if(scrambleChoice != newScrambleChoice || scrambleChoice.getLength() != newLength) {
			int choice = JOptionPane.YES_OPTION;
			if(scrambleChoice != null && scrambles.getCurrent().isImported()){
				choice = JOptionPane.showOptionDialog(this,
						"Do you want to discard the imported scrambles?",
						"Discard scrambles?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null);
			} else {
				if(scrambles != null && scrambles.size() > 1)
					choice = JOptionPane.showOptionDialog(this,
							"Do you really wish to switch the type of scramble?\n" +
							"All previous scrambles will be lost. Your times, however, will be saved.",
							"Discard scrambles?",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							null,
							null);
			}
			if(choice == JOptionPane.YES_OPTION) {
				newScrambleChoice.setLength(newLength);
				puzzleChoice = newPuzzleChoice;
				scrambleChoice = newScrambleChoice;
				createScrambleAttributes();
				validate();
				scrambles = new ScrambleList(scrambleChoice);
			}
		} else
			puzzleChoice = newPuzzleChoice;
		safeSelectItem(scrambleChooser, puzzleChoice);
		safeSetValue(scrambleLength, scrambleChoice.getLength());
		//update new number of scrambles
		if((Integer)((SpinnerNumberModel)scrambleNumber.getModel()).getMaximum() != scrambles.size())
			safeSetScrambleNumberMax(scrambles.size());
		//update new scramble number
		if((Integer)scrambleNumber.getValue() != scrambles.getScrambleNumber())
			safeSetValue(scrambleNumber, scrambles.getScrambleNumber());

		scrambleText.setText(scrambles.getCurrent());
		scramblePopup.setScramble(scrambles.getCurrent());
		scramblePopup.pack();
	}

	public void dispose() {
		saveToConfiguration();
		try {
			Configuration.saveConfigurationToFile(getSelectedProfile().getConfigurationFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.dispose();
		System.exit(0);
	}
	private void saveToConfiguration() {
		Configuration.setBoolean(VariableKey.STACKAMT_ENABLED, !(Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY));
		Configuration.setPuzzle(puzzleChoice);
		Configuration.setDimension(VariableKey.SCRAMBLE_VIEW_DIMENSION, scramblePopup.getSize());
		Configuration.setPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, scramblePopup.getLocation());
		Configuration.setDimension(VariableKey.MAIN_FRAME_DIMENSION, this.getSize());
		Configuration.setPoint(VariableKey.MAIN_FRAME_LOCATION, this.getLocation());
	}

	private boolean isFullScreen = false;
	public void setFullScreen(boolean b) {
		isFullScreen = b;
		fullscreenFrame.setVisible(isFullScreen);
		if(isFullScreen) {
			bigTimersDisplay.setText(timeLabel.getText());
			bigTimersDisplay.requestFocusInWindow();
		}
	}

	public void contentsChanged(ListDataEvent e) {
		if(e != null && e.getType() == ListDataEvent.INTERVAL_ADDED) {
			Scramble curr = scrambles.getCurrent();
			if(curr != null){
				stats.get(stats.getSize() - 1).setScramble(scrambles.getCurrent().toString());
				boolean outOfScrambles = curr.isImported(); //This is tricky, think before you change it
				outOfScrambles = !scrambles.getNext().isImported() && outOfScrambles;
				if(outOfScrambles)
					JOptionPane.showMessageDialog(this,
							"All imported scrambles have been used.\n" +
							"Generated scrambles will be used from now on.",
							"All Out of Scrambles!",
							JOptionPane.INFORMATION_MESSAGE, cube);
				updateScramble();
			}
		}
		if(stats != null && stats.getSize() >= 1)
			sendTime(stats.get(-1));
		repaintTimes();
	}
	public void intervalAdded(ListDataEvent e) {}
	public void intervalRemoved(ListDataEvent e) {}

	private void sendCurrentTime(String s){
		if(client != null && client.isConnected()){
			client.sendCurrentTime(s);
		}
	}

	private void sendTime(SolveTime s){
		if(client != null && client.isConnected()){
			client.sendTime(s);
		}
	}

	private void sendAverage(String s){
		if(client != null && client.isConnected()){
			client.sendAverage(s, stats);
		}
	}

	private void sendBestAverage(String s){
		if(client != null && client.isConnected()){
			client.sendBestAverage(s, stats);
		}
	}

	//this is only called by cctclient
//	public void setScramble(String s) {
//		try {
//			ScrambleType scrambleChoice = Configuration.getScrambleType(puzzleChoice);
//			scrambles = new ScrambleList(scrambleChoice, scrambleChoice.generateScramble(s));
//			javax.swing.SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					updateScramble();
//				}
//			});
//		} catch(Exception e) {
//			scrambleText.setText("Error in scramble from server.");
//			e.printStackTrace();
//		}
//	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source == scrambleNumber) {
			if(scrambleNumber.isEnabled())
				scrambles.setScrambleNumber((Integer) scrambleNumber.getValue());
			updateScramble();
		} else if(source == scrambleLength) {
			updateScramble();
		}
	}

	public static void updateWatermark() {
		if(Configuration.getBoolean(VariableKey.WATERMARK_ENABLED, false)) {
			SubstanceLookAndFeel.setImageWatermarkKind(SubstanceConstants.ImageWatermarkKind.APP_CENTER);
			SubstanceLookAndFeel.setImageWatermarkOpacity(Configuration.getFloat(VariableKey.OPACITY, false));
			InputStream in = CALCubeTimer.class.getResourceAsStream(Configuration.getString(VariableKey.WATERMARK_FILE, true));
			try {
				in = new FileInputStream(Configuration.getString(VariableKey.WATERMARK_FILE, false));
			} catch (FileNotFoundException e) {}
			SubstanceLookAndFeel.setCurrentWatermark(new SubstanceImageWatermark(in));
		} else
			SubstanceLookAndFeel.setCurrentWatermark(new org.jvnet.substance.watermark.SubstanceNoneWatermark());

		Window[] frames = JFrame.getWindows();
		for(int ch = 0; ch < frames.length; ch++) {
			frames[ch].repaint();
		}
	}
	
	public void configurationChanged() {
		updateWatermark();
		boolean stackmatEnabled = Configuration.getBoolean(VariableKey.STACKAMT_ENABLED, false);
		keyboardTimingAction.putValue(Action.SELECTED_KEY, !stackmatEnabled);
		integratedTimerAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.INTEGRATED_TIMER_DISPLAY, false));
		annoyingDisplayAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.ANNOYING_DISPLAY, false));
		lessAnnoyingDisplayAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.LESS_ANNOYING_DISPLAY, false));
		hideScramblesAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false));
		spacebarOptionAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false));
		fullScreenTimingAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false));
		scrambleChooser.setModel(new DefaultComboBoxModel(Configuration.getCustomScrambleTypes(false).toArray(new String[0])));
		profiles.setModel(new DefaultComboBoxModel(Configuration.getProfiles().toArray(new Profile[0])));
		safeSelectItem(profiles, Configuration.getSelectedProfile());
		scrambleChooser.setSelectedItem(Configuration.getPuzzle());
		timeLabel.setKeyboard(!stackmatEnabled);
		timeLabel.setEnabledTiming(Configuration.getBoolean(VariableKey.INTEGRATED_TIMER_DISPLAY, false));
		timeLabel.setOpaque(Configuration.getBoolean(VariableKey.ANNOYING_DISPLAY, false));
		timeLabel.setFont(Configuration.getFont(VariableKey.TIMER_FONT, false));
		bigTimersDisplay.setFont(Configuration.getFont(VariableKey.TIMER_FONT, false));
		startStopPanel.setEnabled((Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY));
		startStopPanel.setVisible(!Configuration.getBoolean(VariableKey.INTEGRATED_TIMER_DISPLAY, false));
		bigTimersDisplay.setKeyboard(!stackmatEnabled);
		scrambleChooser.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
		
		updateScramble();
		Component focusedComponent = this.getFocusOwner();
		parseXML_GUI(Configuration.getXMLGUILayout());
		Dimension size = Configuration.getDimension(VariableKey.MAIN_FRAME_DIMENSION, false);
		if(size == null) {
			this.pack();
		} else
			this.setSize(size);
		Point location = Configuration.getPoint(VariableKey.MAIN_FRAME_LOCATION, false);
		if(location == null)
			this.setLocationRelativeTo(null);
		else
			this.setLocation(location);

		scramblePopup.syncColorScheme();
		scramblePopup.pack();
		size = Configuration.getDimension(VariableKey.SCRAMBLE_VIEW_DIMENSION, false);
		if(size != null)
			scramblePopup.setSize(size);
		location = Configuration.getPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, false);
		if(location != null)
			scramblePopup.setLocation(location);
		scramblePopup.setVisible(Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, false));
		
		if(!stackmatEnabled) { //This is to ensure that the keyboard is focused
			timeLabel.requestFocusInWindow();
			startStopPanel.requestFocusInWindow();
		} else if(focusedComponent != null) {
			focusedComponent.requestFocusInWindow();
		} else
			scrambleText.requestFocusInWindow();
		timeLabel.componentResized(null);
	}
	
	// Actions section {{{
	public void addTimeAction() {
		timesList.promptForNewItem();
	}

	public void resetAction(){
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Do you really want to reset?",
				"Warning!",
				JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.YES_OPTION) {
			timeLabel.reset();
			ScrambleType scrambleChoice = Configuration.getScrambleType(puzzleChoice);
			scrambles = new ScrambleList(scrambleChoice);
			updateScramble();
			stats.clear();
		}
	}

	private static String[] okCancel = new String[] {"OK", "Cancel"};
	public void importScramblesAction(){
		ScrambleImportExportDialog scrambleImporter = new ScrambleImportExportDialog(true, Configuration.getScrambleType(puzzleChoice));
		int choice = JOptionPane.showOptionDialog(this,
				scrambleImporter,
				"Import Scrambles",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				cube,
				okCancel,
				okCancel[0]);
		if(choice == JOptionPane.OK_OPTION) {
			URL file = scrambleImporter.getURL();
			if(file != null)
				readScramblesFile(scrambleImporter.getURL(), scrambleImporter.getType());
		}
	}

	public void exportScramblesAction(){
		ScrambleImportExportDialog scrambleExporter = new ScrambleImportExportDialog(false, Configuration.getScrambleType(puzzleChoice));
		int choice = JOptionPane.showOptionDialog(this,
				scrambleExporter,
				"Export Scrambles",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				cube,
				okCancel,
				okCancel[0]);
		if(choice == JOptionPane.OK_OPTION) {
			URL file = scrambleExporter.getURL();
			if(file != null)
				exportScrambles(file, scrambleExporter.getNumberOfScrambles(), scrambleExporter.getType());
		}
	}

	public void showDocumentation(){
		try {
			URI uri = Configuration.documentationFile.toURI();
			Desktop.getDesktop().browse(uri);
		} catch(Exception error) {
			JOptionPane.showMessageDialog(this,
					error.getMessage(),
					"Error!",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	public void showConfigurationDialog(){
		saveToConfiguration();
		configurationDialog.setVisible(true, (Profile) profiles.getSelectedItem());
	}

	public void connectToServer(){
		client = new CCTClient(cube);
		client.enableAndDisable(connectToServerAction);
	}

	public void flipFullScreen(){
		setFullScreen(!isFullScreen);
	}

	public void keyboardTimingAction(){
		boolean selected = (Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY);
		startStopPanel.setEnabled(selected);
		timeLabel.setKeyboard(selected);
		bigTimersDisplay.setKeyboard(selected);
		stackmatTimer.enableStackmat(!selected);
		if(!selected) {
			timeLabel.reset();
		} else {
			timeLabel.requestFocus();
			startStopPanel.requestFocus();
		}
	}

	public void scrambleChooserAction(){
		safeSetValue(scrambleLength, Configuration.getScrambleType((String)scrambleChooser.getSelectedItem()).getLength());
		updateScramble();
	}

	public void lessAnnoyingDisplayAction(){
		Configuration.setBoolean(VariableKey.LESS_ANNOYING_DISPLAY, (Boolean)lessAnnoyingDisplayAction.getValue(Action.SELECTED_KEY));
		timeLabel.repaint();
	}

	public void integratedTimerAction(){
		boolean isIntegrated = (Boolean)integratedTimerAction.getValue(Action.SELECTED_KEY);
		Configuration.setBoolean(VariableKey.INTEGRATED_TIMER_DISPLAY, isIntegrated);
		startStopPanel.setVisible(!isIntegrated);
		timeLabel.setEnabledTiming(isIntegrated);
		if(isIntegrated)
			timeLabel.requestFocusInWindow();
		else
			startStopPanel.requestFocusInWindow();
	}

	public void hideScramblesAction(){
		Configuration.setBoolean(VariableKey.HIDE_SCRAMBLES, (Boolean)hideScramblesAction.getValue(Action.SELECTED_KEY));
		scrambleText.refresh();
	}

	public void annoyingDisplayAction(){
		boolean b = (Boolean)annoyingDisplayAction.getValue(Action.SELECTED_KEY);
		timeLabel.setOpaque(b);
		Configuration.setBoolean(VariableKey.ANNOYING_DISPLAY, b);
		timeLabel.repaint();
	}
	// End actions section }}}

	private void startMetronome() {
		tickTock.setDelay(Configuration.getInt(VariableKey.METRONOME_DELAY, false));
		tickTock.start();
	}
	
	private void stopMetronome() {
		tickTock.stop();
	}
	
	private static String[] options = {"Accept", "+2", "POP"};
	private class TimerHandler implements PropertyChangeListener, ActionListener {
		private StackmatState lastAccepted = new StackmatState();
		private boolean reset = false;
		private ArrayList<SolveTime> splits = new ArrayList<SolveTime>();
		public void propertyChange(PropertyChangeEvent evt) {
			String event = evt.getPropertyName();
			boolean on = !event.equals("Off");
			timeLabel.setStackmatOn(on);
			if(on)
				onLabel.setText("Timer is ON");
			else
				onLabel.setText("Timer is OFF");
			if(!Configuration.getBoolean(VariableKey.STACKAMT_ENABLED, false))
				return;

			if(evt.getNewValue() instanceof StackmatState){
				StackmatState current = (StackmatState) evt.getNewValue();
				timeLabel.setStackmatHands(current.bothHands());
				if(event.equals("TimeChange")) {
					if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false)) setFullScreen(true);
					if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false)) startMetronome();
					reset = false;
					updateTime(current.toString());
				} else if(event.equals("Split")) {
					addSplit((TimerState) current);
				} else if(event.equals("Reset")) {
					updateTime("0:00.00");
					reset = true;
				} else if(event.equals("New Time")) {
					if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false)) setFullScreen(false);
					if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false)) stopMetronome();
					updateTime(current.toString());
					if(addTime(current))
						lastAccepted = current;
				} else if(event.equals("Current Display")) {
					timeLabel.setText(current.toString());
				}
			}
		}

		private void updateTime(String newTime) {
			timeLabel.setText(newTime);
			if(isFullScreen)
				bigTimersDisplay.setText(newTime);
			if(!reset) {
				sendCurrentTime(newTime);
			}
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			TimerState newTime = (TimerState) e.getSource();
			updateTime(newTime.toString());
			if(command.equals("Started")) {
				if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
					setFullScreen(true);
				if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
					startMetronome();
			} else if(command.equals("Stopped")) {
				addTime(newTime);
				if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
					setFullScreen(false);
				if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
					stopMetronome();
			} else if(command.equals("Split"))
				addSplit(newTime);
		}

		private long lastSplit;
		private void addSplit(TimerState state) {
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastSplit) / 1000. > Configuration.getDouble(VariableKey.MIN_SPLIT_DIFFERENCE, false)) {
				String hands = "";
				if(state instanceof StackmatState) {
					hands += ((StackmatState) state).leftHand() ? " Left Hand" : " Right Hand";
				}
				splits.add(state.toSolveTime(hands, null));
				lastSplit = currentTime;
			}
		}

		private boolean addTime(TimerState addMe) {
			SolveTime protect = addMe.toSolveTime(null, splits);
			splits = new ArrayList<SolveTime>();
			boolean sameAsLast = addMe.compareTo(lastAccepted) == 0;
			if(sameAsLast) {
				int choice = JOptionPane.showOptionDialog(null,
						"This is the exact same time as last time! Are you sure you wish to add it?",
						"Confirm Time: " + addMe.toString(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null);
				if(choice != JOptionPane.YES_OPTION)
					return false;
			}
			int choice = JOptionPane.YES_OPTION;
			if(Configuration.getBoolean(VariableKey.PROMPT_FOR_NEW_TIME, false) && !sameAsLast) {
				choice = JOptionPane.showOptionDialog(null,
						"Your time: " + protect.toString() + "\n Hit esc or close dialog to discard.",
						"Confirm Time",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);
			}
			if(choice == JOptionPane.YES_OPTION) {
				stats.add(protect);
			} else if(choice == JOptionPane.NO_OPTION) {
				protect.setPlusTwo(true);
				stats.add(protect);
			} else if(choice == JOptionPane.CANCEL_OPTION) {
				protect.setPop(true);
				stats.add(protect);
			} else {
				return false;
			}
			repaintTimes(); //needed here too
			return true;
		}
	}
}

@SuppressWarnings("serial")
class StatisticsAction extends AbstractAction{
	private StatsDialogHandler statsHandler;
	public StatisticsAction(CALCubeTimer cct, Statistics stats, Statistics.averageType type){
		statsHandler = new StatsDialogHandler(cct, stats, type);
	}

	public void actionPerformed(ActionEvent e){
		statsHandler.setVisible(true);
	}
}
@SuppressWarnings("serial")
class AddTimeAction extends AbstractAction{
	private CALCubeTimer cct;
	public AddTimeAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.addTimeAction();
	}
}
@SuppressWarnings("serial")
class ResetAction extends AbstractAction{
	private CALCubeTimer cct;
	public ResetAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.resetAction();
	}
}
@SuppressWarnings("serial")
class ImportScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ImportScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.importScramblesAction();
	}
}
@SuppressWarnings("serial")
class ExportScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExportScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.exportScramblesAction();
	}
}
@SuppressWarnings("serial")
class ExitAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExitAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.dispose();
	}
}
@SuppressWarnings("serial")
class AboutAction extends AbstractAction{
	private JFrame makeMeVisible;
	public AboutAction(JFrame makeMeVisible){
		this.makeMeVisible = makeMeVisible;
	}

	public void actionPerformed(ActionEvent e){
		makeMeVisible.setVisible(true);
	}
}
@SuppressWarnings("serial")
class DocumentationAction extends AbstractAction{
	private CALCubeTimer cct;
	public DocumentationAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showDocumentation();
	}
}
@SuppressWarnings("serial")
class ShowConfigurationDialogAction extends AbstractAction{
	private CALCubeTimer cct;
	public ShowConfigurationDialogAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showConfigurationDialog();
	}
}
@SuppressWarnings("serial")
class ConnectToServerAction extends AbstractAction{
	private CALCubeTimer cct;
	public ConnectToServerAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.connectToServer();
	}
}
@SuppressWarnings("serial")
class FlipFullScreenAction extends AbstractAction{
	private CALCubeTimer cct;
	public FlipFullScreenAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.flipFullScreen();
	}
}
@SuppressWarnings("serial")
class KeyboardTimingAction extends AbstractAction{
	private CALCubeTimer cct;
	public KeyboardTimingAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.keyboardTimingAction();
	}
}
@SuppressWarnings("serial")
class SpacebarOptionAction extends AbstractAction{
	public SpacebarOptionAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.SPACEBAR_ONLY, ((AbstractButton)e.getSource()).isSelected());
	}
}
@SuppressWarnings("serial")
class FullScreenTimingAction extends AbstractAction{
	public FullScreenTimingAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.FULLSCREEN_TIMING, ((AbstractButton)e.getSource()).isSelected());
	}
}
@SuppressWarnings("serial")
class IntegratedTimerAction extends AbstractAction{
	private CALCubeTimer cct;
	public IntegratedTimerAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.integratedTimerAction();
	}
}
@SuppressWarnings("serial")
class HideScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public HideScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.hideScramblesAction();
	}
}
@SuppressWarnings("serial")
class AnnoyingDisplayAction extends AbstractAction{
	private CALCubeTimer cct;
	public AnnoyingDisplayAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.annoyingDisplayAction();
	}
}
@SuppressWarnings("serial")
class LessAnnoyingDisplayAction extends AbstractAction{
	private CALCubeTimer cct;
	public LessAnnoyingDisplayAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.lessAnnoyingDisplayAction();
	}
}
