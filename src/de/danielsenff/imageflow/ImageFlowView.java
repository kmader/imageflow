package de.danielsenff.imageflow;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task.BlockingScope;

import visualap.Node;
import visualap.Selection;
import de.danielsenff.imageflow.controller.DelegatesController;
import de.danielsenff.imageflow.controller.GraphController;
import de.danielsenff.imageflow.controller.MacroFlowRunner;
import de.danielsenff.imageflow.controller.MacroGenerator;
import de.danielsenff.imageflow.gui.DelegatesPanel;
import de.danielsenff.imageflow.gui.DelegatesTreeListener;
import de.danielsenff.imageflow.gui.GPanelPopup;
import de.danielsenff.imageflow.gui.GraphPanel;
import de.danielsenff.imageflow.gui.InsertUnitMenu;
import de.danielsenff.imageflow.gui.MacOSAboutHandler;
import de.danielsenff.imageflow.gui.StatusBar;
import de.danielsenff.imageflow.models.Delegate;
import de.danielsenff.imageflow.models.Model;
import de.danielsenff.imageflow.models.ModelListener;
import de.danielsenff.imageflow.models.NodeListener;
import de.danielsenff.imageflow.models.Selectable;
import de.danielsenff.imageflow.models.SelectionList;
import de.danielsenff.imageflow.models.SelectionListener;
import de.danielsenff.imageflow.models.connection.Connection;
import de.danielsenff.imageflow.models.connection.ConnectionList;
import de.danielsenff.imageflow.models.connection.Input;
import de.danielsenff.imageflow.models.connection.Output;
import de.danielsenff.imageflow.models.datatype.DataTypeFactory;
import de.danielsenff.imageflow.models.parameter.Parameter;
import de.danielsenff.imageflow.models.unit.GroupUnitElement;
import de.danielsenff.imageflow.models.unit.UnitDelegate;
import de.danielsenff.imageflow.models.unit.UnitElement;
import de.danielsenff.imageflow.models.unit.UnitFactory;
import de.danielsenff.imageflow.models.unit.UnitList;
import de.danielsenff.imageflow.models.unit.UnitModelComponent.Size;
import de.danielsenff.imageflow.tasks.ExportMacroTask;
import de.danielsenff.imageflow.tasks.GenerateMacroTask;
import de.danielsenff.imageflow.tasks.ImportGraphTask;
import de.danielsenff.imageflow.tasks.LoadFlowGraphTask;
import de.danielsenff.imageflow.tasks.RunMacroTask;
import de.danielsenff.imageflow.tasks.SaveFlowGraphTask;
import de.danielsenff.imageflow.utils.FileDrop;
import de.danielsenff.imageflow.utils.FileDropListener;



/**
 * Controller of one workspace. Contains all necessary data of an opened 
 * graph.
 * @author danielsenff
 *
 */
public class ImageFlowView extends FrameView {

//	private static final Logger logger = Logger.getLogger(DocumentEditorView.class.getName());
	private JDialog aboutBox;
	private CodePreviewDialog codePreviewBox;
	
	private GraphController graphController;
	private HashMap<TreeNode,Delegate> delegates;
	private File file;

	private JPanel mainPanel;
	protected GraphPanel graphPanel;
	private static JProgressBar progressBar;	
	private StatusBar statusBar = null;
	
	

	private boolean modified = false;
	private boolean selected = false;
	private boolean paste = false;
	private boolean showCode = false;
	private boolean closeAll = false;

	private JCheckBoxMenuItem chkBoxDisplayUnit;
	private JCheckBoxMenuItem chkBoxCollapseIcon;
	
	
	
	
	/**
	 * @param app
	 */
	public ImageFlowView(final Application app) {
		super(app);
		
		this.delegates = DelegatesController.getInstance().getDelegates();
		this.graphController = new GraphController(); 
		
		if (IJ.isMacOSX()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true"); 
			System.setProperty("apple.awt.brushMetalRounded", "true");
		}
		
		initAppIcon();
		
		initComponents();
		
		setFile(new File("new document"));
	}



	private void initAppIcon() {
		try {
			this.getFrame().setIconImage(
					ImageIO.read(this.getClass().getResourceAsStream(
							getResourceString("mainFrame.icon"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private void initComponents() {
		addComponents();
		
		setMenuBar(addMenu());
		
		// register listeners
		registerModelListeners();
		getApplication().addExitListener(new ConfirmExit());
	}
	

	/**
	 * Register the ModelListeners.
	 */
	private void registerModelListeners() {
		for (Node node : getNodes()) {
			UnitFactory.registerModelListener(node);
		}
		
		getNodes().addModelListener(new ModelListener() {
			public void modelChanged(Model model) {
				graphPanel.repaint();
				setModified(true);
			}
		});
		
		getConnections().addModelListener(new ModelListener() {
			public void modelChanged(Model model) {
				graphPanel.repaint();
				setModified(true);
			}
		});
		
		getSelections().addSelectionListener(new SelectionListener() {
			public void selectionChanged(Selectable selections) {
				setSelected(selections.isSelected());
			}
		});
		
		setModified(false);
	}




	
	/**
	 * Adds all components of
	 */
	private JMenuBar addMenu() {
		JMenuBar menuBar = new JMenuBar();
		
		
		JMenu fileMenu = new JMenu(getResourceString("file.menu"));
		fileMenu.add(getAction("newDocument"));
		fileMenu.add(getAction("open"));
		
		fileMenu.add(getAction("save"));
		fileMenu.add(getAction("saveAs"));
		fileMenu.add(new JSeparator());
		fileMenu.add(getAction("importGraph"));
		fileMenu.add(getAction("export"));
		fileMenu.add(new JSeparator());
		fileMenu.add(getAction("generateMacro"));
		fileMenu.add(getAction("runMacro"));
		if(!IJ.isMacintosh()) {
			fileMenu.add(new JSeparator());
			fileMenu.add(getAction("quit"));
		} else {
			/*MRJApplicationUtils.registerQuitHandler(new MRJQuitHandler()
			   {
			      public void handleQuit()
			      {
			         SwingUtilities.invokeLater(new Runnable()
			         {
			            public void run()
			            {
			               if(promptTheUser())
			                  System.exit(0);
			            }
			         });
			         throw new IllegalStateException("Stop Pending User Confirmation");
			      }
			   });*/
		}
		
		
		JMenu editMenu = new JMenu(getResourceString("edit.menu"));
		editMenu.add(getAction("cut"));
		editMenu.add(getAction("copy"));
		editMenu.add(getAction("paste"));
		editMenu.add(getAction("unbind"));
		editMenu.add(getAction("delete"));
//		editMenu.add(getAction("clear"));
		editMenu.add(getAction("selectAll"));
		
		editMenu.add(new JSeparator());

		this.chkBoxDisplayUnit = new JCheckBoxMenuItem(getAction("setDisplayUnit")); 
		editMenu.add(chkBoxDisplayUnit);

		this.chkBoxCollapseIcon = new JCheckBoxMenuItem(getAction("setUnitComponentSize"));
		editMenu.add(chkBoxCollapseIcon);

		editMenu.add(getAction("showUnitParameters"));
		editMenu.add(getAction("group"));
		
		
		
		JMenu viewMenu = new JMenu(getResourceString("view.menu"));
		viewMenu.add(new JCheckBoxMenuItem(getAction("alignElements")));
		viewMenu.add(new JCheckBoxMenuItem(getAction("setDrawGrid")));
		
		JMenu debugMenu = new JMenu(getResourceString("debug.menu"));
		debugMenu.add(getAction("debugPrintNodes"));
		debugMenu.add(getAction("debugPrintNodeDetails"));
		debugMenu.add(getAction("debugPrintEdges"));
		debugMenu.add(getAction("debugDrawClonedWorkflow"));
		debugMenu.add(new JSeparator());
		debugMenu.add(getAction("exampleFlow1"));
		debugMenu.add(getAction("exampleFlow2"));
		debugMenu.add(getAction("exampleFlow3"));
		
		JMenu insertMenu = new InsertUnitMenu(graphPanel, getDelegates().values());
		
		/*JMenu windowMenu = new JMenu(getResourceString("window.menu"));
		windowMenu.add(getAction("minimize"));*/
		JMenu helpMenu = new JMenu(getResourceString("help.menu"));
		helpMenu.add(getAction("openDevblogURL"));
		helpMenu.add(getAction("openImageJURL"));
		if(!IJ.isMacintosh()) {
			helpMenu.add(new JSeparator());
	        helpMenu.add(getAction("showAboutBox"));	
		}else {                                               
            new MacOSAboutHandler(getFrame());            
	     }  
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(insertMenu);
		menuBar.add(debugMenu);
//		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		
		menuBar.setVisible(true);
		return menuBar;
	}

	private void updateMenu() {
		if(!getSelections().isEmpty() 
				&& getSelections().size() == 1 
				&& getSelections().get(0) instanceof UnitElement) {
			boolean isCollapsedIcon = 
				((UnitElement)getSelections().get(0)).getCompontentSize() == Size.SMALL 
				? true : false;
			this.chkBoxCollapseIcon.setSelected(isCollapsedIcon);
			
			boolean isDisplayUnit = ((UnitElement)getSelections().get(0)).isDisplay();
			this.chkBoxDisplayUnit.setSelected(isDisplayUnit);	
		} else {
			this.chkBoxCollapseIcon.setSelected(false);
			this.chkBoxDisplayUnit.setSelected(false);
		}
		
	}

	
	
	/**
	 * Adds all components to the JFrame
	 */
	private void addComponents() {
		
		ResourceMap resourceMap = getResourceMap();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		//working area aka graphpanel
		ArrayList<Delegate> delegatesArrayList = new ArrayList<Delegate>();
		delegatesArrayList.addAll(getDelegates().values());
		GPanelPopup popup = new GPanelPopup(getGraphController());
		
		graphPanel = new GraphPanel(delegatesArrayList, popup);
		resourceMap.injectComponent(graphPanel);
		popup.setActivePanel(graphPanel);
		graphPanel.setGraphController(getGraphController());
		graphPanel.setSelections(getSelections());
		if(IJ.isMacOSX())
			graphPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		
		ScrollPane graphScrollpane = new ScrollPane();
		graphScrollpane.add(graphPanel);
		graphScrollpane.setPreferredSize(new Dimension(400, 300));
		new FileDrop( null, graphPanel,  new FileDropListener(graphPanel, getInstance())); // end FileDrop.Listener
		
//		graphScrollpane.add(workspacePanel);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		
		JButton buttonRun = new JButton(getAction("runMacro"));
		buttonPanel.add(buttonRun);
		
		JCheckBox chkShowCode = new JCheckBox(getResourceString("showLog"));
		resourceMap.injectComponent(chkShowCode);
		chkShowCode.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				showCode = ((JCheckBox)e.getSource()).isSelected();
			}});
		buttonPanel.add(chkShowCode);
		
		JCheckBox chkCloseAll = new JCheckBox(getResourceString("closeAll.text"));
		chkCloseAll.setToolTipText(getResourceString("closeAll.shortDescription"));
		resourceMap.injectComponent(chkCloseAll);
		chkCloseAll.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				closeAll = ((JCheckBox)e.getSource()).isSelected();
			}});
		buttonPanel.add(chkCloseAll);
		bottomPanel.add(buttonPanel, BorderLayout.LINE_START);
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		
		statusBar = new StatusBar(getApplication(), getContext().getTaskMonitor());
        
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSize(150, 20);
		progressBar.setVisible(false);
		
		progressPanel.add(statusBar);
		
		bottomPanel.add(progressPanel, BorderLayout.LINE_END);
		
		JPanel sidePane = new JPanel();
		sidePane.setLayout(new BorderLayout());
		DelegatesPanel delegatesPanel = new DelegatesPanel(this.getNodes());
		DelegatesTreeListener delegatesTreeListener = new DelegatesTreeListener(getInstance());
		delegatesPanel.getDelegatesTree().addMouseListener(delegatesTreeListener);
		delegatesPanel.getDelegatesTree().addKeyListener(delegatesTreeListener);
		
		sidePane.add(delegatesPanel, BorderLayout.CENTER);
		
		// setting of MinimumSize is required for drag-ability of JSplitPane
		sidePane.setMinimumSize(new Dimension(150, 100));
		graphScrollpane.setMinimumSize(new Dimension(100, 100));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePane, graphScrollpane);
		splitPane.setEnabled(true);
		splitPane.setOneTouchExpandable(true);
		// enables continuous redrawing while moving the JSplitPane-Divider
		splitPane.setContinuousLayout(true);
		
		mainPanel.add(splitPane, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		setMainPanel(mainPanel);
	}




	
	
	
	
	
	/*
	 * getter and setters
	 */
	
	



	/**
	 * @return
	 */
	public GraphController getGraphController() {
		return graphController;
	}

	private String getResourceString(final String key) {
		return getResourceMap().getString(key);
	}
	
	/**
	 * @param graphController
	 */
	public void setGraphController(final GraphController graphController) {
		GraphController oldValue = this.graphController;
		this.graphController = graphController;
		graphPanel.setGraphController(this.graphController);
		registerModelListeners();
		graphPanel.repaint();
		firePropertyChange("graphController", oldValue, graphController);
	}
	

	protected HashMap<TreeNode, Delegate> getDelegates() {
		return delegates;
	}
	
	/**
	 * Returns a Singleton-Instance of a {@link CodePreviewDialog}.
	 * @return
	 */
	public CodePreviewDialog getCodePreviewBox() {
		if(codePreviewBox == null) {
			this.codePreviewBox  = new CodePreviewDialog(ImageFlow.getApplication().getMainFrame(), "generated Macro");
		}
		return this.codePreviewBox;
	}
	
	/**
	 * Jdialog for displaying macro code.
	 * @author danielsenff
	 *
	 */
	public class CodePreviewDialog extends JDialog {
		private final JTextArea ta;
		
		public CodePreviewDialog(final JFrame frame, final String title) {
			setPreferredSize(new Dimension(350,150));
			ta = new JTextArea();
			setLayout(new BorderLayout());

			final JScrollPane scrollPane = new JScrollPane(ta);
			add(scrollPane, BorderLayout.CENTER);
			pack();
			setVisible(true);
		}
		
		public void setMacroCode(final String code) {
			this.ta.setText(code);
		}
		
		public String getMacroCode() {
			return this.ta.getText();
		}
	}
	
	
	/**
	 * Returns the current selection in the {@link GraphPanel}.
	 * @return
	 */
	public final SelectionList getSelections() {
		return graphController.getSelections();
	}

	private ImageFlowView getInstance() {
		return this;
	}
	
	/**
	 * Convenience method for getting the UnitList of the current GraphController
	 * @return the nodes
	 */
	public UnitList getNodes() {
		return graphController.getUnitElements();
	}

	/**
	 * @return the connections
	 */
	public ConnectionList getConnections() {
		return graphController.getConnections();
	}

	/**
	 * @return the progressBar
	 */
	public static synchronized final JProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * 
	 * @return
	 */
	public JPanel getMainPanel() {
		return this.mainPanel;
	}
	
	protected void setMainPanel(JPanel mainPanel2) {
		setComponent(mainPanel);
	}


	/**
	 *  Set the bound file property and update the GUI.
	 * @param file 
     */
    public void setFile(final File file) {
    	File oldValue = this.file;
    	this.file = file;
    	String appId = getResourceString("Application.id");
    	getFrame().setTitle(file.getName() + " - " + appId);
    	firePropertyChange("file", oldValue, this.file);	
    }
    

    /**
     * True if the file value has been modified but not saved.  The 
     * default value of this property is false.
     * <p>
     * This is a bound read-only property.  
     * 
     * @return the value of the modified property.
     * @see #isModified
     */
    public boolean isModified() { 
        return this.modified;
    }
    
    /**
     * Returns true if a {@link UnitElement} is selected in the workflow.
     * @return
     */
    public boolean isSelected() { 
        return this.selected;
    }
   
    
    /**
     * Sets the modified flag.
     * @param modified
     */
    public void setModified(final boolean modified) {
        boolean oldValue = this.modified;
        this.modified = modified;
        // on program start, file may not be initialised
        if(file != null){
        	String appId = getResourceString("Application.id");
            String changed = modified ? "*" : "";
        	getFrame().setTitle(file.getName() + changed +" - " + appId);	
        }
        
        
        firePropertyChange("modified", oldValue, this.modified);
    }
	
    /**
     * Returns true if a {@link UnitElement} is selected.
     * @param selected
     */
    public void setSelected(final boolean selected) {
        boolean oldValue = this.selected;
        this.selected = selected;
        updateMenu();
        
        firePropertyChange("selected", oldValue, this.selected);
    }


	/**
	 * @return the paste
	 */
	public boolean isPaste() {
		return paste;
	}

	/**
	 * @param hasPaste the hasPaste to set
	 */
	public void setPaste(boolean hasPaste) {
		boolean oldValue = this.paste;
		this.paste = hasPaste;
		firePropertyChange("paste", oldValue, hasPaste );
	}
	
	/**
	 * @return the showlog
	 */
	public boolean isShowlog() {
		return showCode;
	}



	/**
	 * @param showlog the showlog to set
	 */
	public void setShowlog(boolean showcode) {
		boolean oldValue = showcode;
		this.showCode = showcode;
		firePropertyChange("showcode", oldValue, showcode);
	}
	
    
	/**
	 * Returns the currently loaded workflow-file.
	 * @return
	 */
	public File getFile() {
		return file;
	}
	

	
	
	
	
	
	
	
	/*
	 * Action related stuff
	 * 
	 */
	
	/**
	 * convenient Example workflow
	 * @return 
	 */
	@Action public LoadFlowGraphTask exampleFlow1() {
		String exampleFilename = getResourceString("exampleFlow1.Path");
		return new LoadFlowGraphTask(new File(exampleFilename));
	}
	
	/**
	 * convenient Example workflow
	 * @return 
	 */
	@Action public LoadFlowGraphTask exampleFlow2() {
		String exampleFilename = getResourceString("exampleFlow2.Path");
		return new LoadFlowGraphTask(new File(exampleFilename));
	}
	
	/**
	 * convenient Example workflow
	 * @return 
	 */
	@Action public LoadFlowGraphTask exampleFlow3() {
		String exampleFilename = getResourceString("exampleFlow3.Path");
		return new LoadFlowGraphTask(new File(exampleFilename));
	}

	/**
	 * Converts the current workflow into a macro and executes it in ImageJ.
	 * @return
	 */
    @Action
    (block = BlockingScope.ACTION)
    public RunMacroTask runMacro() {
        return new RunMacroTask(this.getApplication(), graphController, this.showCode, this.closeAll);
    }

    /**
	 * Converts the current workflow into a macro and displays it.
	 * @return
	 */
    @Action public GenerateMacroTask generateMacro() {
        return new GenerateMacroTask(this.getApplication(), graphController);
    }
	
	/**
	 * A number of units are collapsed into one group-unit.
	 */
	@Action(enabledProperty = "selected")
	public void group() {
		
		if(getSelections().size() == 1
				&& getSelections().get(0) instanceof GroupUnitElement) {

			GroupUnitElement group = (GroupUnitElement) getSelections().get(0);
			graphController.ungroup(group);	
		} else {

			for (Node node : getSelections()) {
				if(node instanceof GroupUnitElement) {
					System.out.println("Group disallowed: No conistent connections between units");
					JOptionPane.showMessageDialog(this.getFrame(), 
							"Groups may not be included in groups.",
							"Grouping refused", 
							JOptionPane.INFORMATION_MESSAGE);
					
					return;
				}
			}
			
			try {
				graphController.group();
			} catch (Exception e) {
				System.out.println("Group disallowed: No conistent connections between units");
				JOptionPane.showMessageDialog(this.getFrame(), 
						"This grouping of units is not permitted.  " + '\n' +
						"The connected units need to form a conistent branch.",
						"Grouping refused", 
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	/**
	 * A selected {@link GroupUnitElement} is exploded into it's original contents.
	 */
	@Action(enabledProperty = "selected")
	public void degroup() {
		if(getSelections().size() == 1
				&& getSelections().get(0) instanceof GroupUnitElement) {
			GroupUnitElement group = (GroupUnitElement) getSelections().get(0);
			graphController.ungroup(group);
		}
	}
	
	
	/**
	 * Import workflow from XML. 
	 * The current workflow will remain and the second workflow will be added without replacement
	 * @return
	 */
	@Action public ImportGraphTask importGraph() {
	    final JFileChooser fc = new JFileChooser();
	    final String filesExtension = getResourceString("flowXMLFileExtension");
	    final String filesDesc = getResourceString("flowXMLFileExtensionDescription");
	    fc.setFileFilter(new DescriptiveFileFilter(filesExtension, filesDesc));
	    
	    ImportGraphTask task = null;
	    final int option = fc.showOpenDialog(null);
	    if (option == JFileChooser.APPROVE_OPTION) {
	    	task = new ImportGraphTask(fc.getSelectedFile());
	    }
	    return task;
	}
	
	/**
	 * Action that toggles the display-status of a {@link UnitElement}
	 * TODO use interface displayable
	 */
	@Action(enabledProperty = "selected")
	public void setDisplayUnit() {
		for (Object selectedElement : getSelections()) {
			if(selectedElement instanceof UnitElement) {
				final UnitElement unit = (UnitElement) selectedElement;
				boolean newDisplayStatus = unit.isDisplay() ? false : true;
				unit.setDisplay(newDisplayStatus);	
			}
		}
		graphPanel.repaint();
	}
	
	/**
	 * Changes the icon size of a {@link UnitElement}. 
	 */
	@Action(enabledProperty = "selected")
	public void setUnitComponentSize() {
		for (Object selectedElement : getSelections()) {
			if(selectedElement instanceof UnitElement) {
				final UnitElement unit = (UnitElement) selectedElement;
				
				Size newSize = (unit.getCompontentSize() == Size.BIG) ? Size.SMALL : Size.BIG;
				unit.setCompontentSize(newSize);				
			}
		}
		graphPanel.repaint();
	}
	
	/**
	 * Activates to draw a grid on the workspace.
	 */
	@Action
	public void setDrawGrid() {
		boolean drawGrid = graphPanel.isDrawGrid() ? false : true;
		graphPanel.setDrawGrid(drawGrid);
		graphPanel.repaint();
	}
	
	/**
	 * Activates element alignment on the workspace.
	 */
	@Action
	public void alignElements() {
		boolean align = graphPanel.isAlign() ? false : true;
		graphPanel.setAlign(align);
		graphPanel.repaint();
	}
	
	/**
	 * Removes all connections of the selected {@link UnitElement}
	 */
	@Action(enabledProperty = "selected")
	public void unbind() {
		final Selection<Node> selection = getSelections();
		for (final Node unit : selection) {
			getNodes().unbindUnit((UnitElement)unit);	
		}
		graphPanel.repaint();
	}

	/**
	 * Deletes a selected {@link UnitElement}
	 */
	@Action(enabledProperty = "selected")
	public void delete() {
		final Selection<Node> selection = getSelections();
		for (final Node unit : selection) {
			graphController.removeNode(unit);
		}
		graphPanel.repaint();
	}
	
	/**
	 * Selects all {@link UnitElement}s
	 */
	@Action
	public void selectAll() {
		final UnitList list = graphController.getUnitElements();
		final Selection<Node> selection = getSelections();
		selection.clear();
		for (final Node unit : list) {
			selection.add(unit);
		}
		graphPanel.repaint();
	}
	
	/**
	 * Clears the workflow from all {@link UnitElement}s
	 */
	@Action	public void clear() {
		getNodes().clear();
	}
	
	/**
	 * Cut {@link UnitElement} from the workflow.
	 */
	@Action(enabledProperty = "selected")
	public void cut() {
		final Selection<Node> selectedNodes = getSelections();
		final ArrayList<Node> copyUnitsList = graphController.getCopyNodesList();
		if (selectedNodes.size() > 0) {
			// il problema java.util.ConcurrentModificationException � stato risolto introducendo la lista garbage
			final HashSet<Connection> garbage = new HashSet<Connection>();
			copyUnitsList.clear();
			for (final Node t : selectedNodes) {
				/*for (Edge c : activePanel.getEdgeL())
					if ((c.from.getParent() == t)||(t == c.to.getParent()))
						garbage.add(c);
				copyUnitsList.add(t);
				activePanel.getNodeL().remove(t);*/
				copyUnitsList.add(t);
				graphController.removeNode(t);
			}
			for (final Connection c : garbage) {
				graphController.getConnections().remove(c);
//				activePanel.getEdgeL().remove(c);
			}
			selectedNodes.clear();
			setPaste(true);
		}
		graphPanel.repaint();
	}
	
	/**
	 * Copy {@link UnitElement} from the workflow.
	 */
	@Action(enabledProperty = "selected")
	public void copy() { 
		final Selection<Node> selectedNodes = getSelections();
		final ArrayList<Node> copyUnitsList = graphController.getCopyNodesList();
		if (!selectedNodes.isEmpty()) {
			copyUnitsList.clear();
			for (final Node t : selectedNodes) {
				Node clone;
				try {
					clone = t.clone();
					clone.setLabel(t.getLabel());
					copyUnitsList.add(clone);
					
				} catch (final CloneNotSupportedException e) {
					e.printStackTrace();
				}	
				setPaste(true);
			}
			
		}
	}
	
	/**
	 * Paste {@link UnitElement} into the workflow.
	 */
	@Action(enabledProperty = "paste")
	public void paste() {
		final Selection<Node> selectedNodes = getSelections();
		final ArrayList<Node> copyUnitsList = graphController.getCopyNodesList();
		if (!copyUnitsList.isEmpty()) {
			selectedNodes.clear();
			// this is added here so that the new pasted units are selected
			selectedNodes.addAll(copyUnitsList);
			copyUnitsList.clear();
			for (final Node t : selectedNodes) {
				try {
					t.setSelected(true);
//					UnitElement clone = (UnitElement)t.clone();
					// retain a copy, in case he pastes several times
					final Node clone = t.clone();
					clone.setLabel(t.getLabel());
					getNodes().add(t);
					copyUnitsList.add(clone);
				} catch(final CloneNotSupportedException ex) {
//					ErrorPrinter.printInfo("CloneNotSupportedException");
				}
			}
			graphPanel.repaint();
		}
	}
	
	/**
	 * Opens a dialog with the available {@link Input} for the selected {@link UnitElement}.
	 */
	@Action(enabledProperty = "selected")
	public void showUnitParameters() {
		for (Node node : getSelections()) {
			if(node instanceof UnitElement)
				((UnitElement)node).showProperties();
		}
	}


	/**
	 * Creates a new document and an empty workflow.
	 */
	@Action public void newDocument() {

		if(isModified()) {
			int optionSave = showSaveConfirmation();
			
			if(optionSave == JOptionPane.OK_OPTION) {
				save().run();
			}else if(optionSave == JOptionPane.CANCEL_OPTION) {
				return;
			}
	    }
		getNodes().clear();
	    setFile(new File("new document"));
	    this.setModified(false);
	    graphPanel.repaint();
	}
	
	/**
	 * Open a workflow file from hard drive.
	 * @return
	 */
	@Action public LoadFlowGraphTask open() {
		if(isModified()) {
			final int optionSave = showSaveConfirmation();
			if(optionSave == JOptionPane.OK_OPTION) {
				save().run();
			}else if(optionSave == JOptionPane.CANCEL_OPTION) {
				return null;
			}
		} 
		final JFileChooser fc = new JFileChooser();
		final String fileExtension = getResourceString("flowXMLFileExtension");
        final String filesDesc = getResourceString("flowXMLFileExtensionDescription");
        fc.setFileFilter(new DescriptiveFileFilter("XML", filesDesc + " Version 1.0"));
		fc.setFileFilter(new DescriptiveFileFilter(fileExtension, filesDesc));

		LoadFlowGraphTask task = null;
		final int option = fc.showOpenDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			this.setModified(false);
			task = new LoadFlowGraphTask(fc.getSelectedFile());
		}
		return task;			
	}
	
	private int showSaveConfirmation() {
		return JOptionPane.showConfirmDialog(this.getFrame(), 
				"The workflow has modifications which were not yet saved."
				+'\n'+"Save changes now?",
				"Save changes?", 
				JOptionPane.INFORMATION_MESSAGE);
	}
    
	/**
     * Save the contents of the workspace to the current {@link #getFile file}.
     * <p>
     * The text is written to the file on a worker thread because we don't want to 
     * block the EDT while the file system is accessed.  To do that, this
     * Action method returns a new SaveFlowGraphTask instance.  The task
     * is executed when the "save" Action's actionPerformed method runs.
     * The SaveFlowGraphTask is responsible for updating the GUI after it
     * has successfully completed saving the file.

     * 
     * @see #getFile
     */
    @Action(enabledProperty = "modified")
    public SaveFlowGraphTask save() {
    	if(getFile().exists()) {
    		return new SaveFlowGraphTask(getFile());	
    	} else 
    		return saveAs();
        
    }
    
    /**
     * Save the contents of the workspace to the current file.
     * <p>
     * This action is nearly identical to {@link #open open}.  In
     * this case, if the user chooses a file, a {@code SaveFlowGraphTask}
     * is returned.  Note that the selected file only becomes the
     * value of the {@code file} property if the file is saved
     * successfully.
     * @return 
     */
    @Action
    public SaveFlowGraphTask saveAs() {
        final JFileChooser fc = createFileChooser("saveAsFileChooser");
        getResourceMap().injectComponents(fc);
        final String fileExtension = getResourceString("flowXMLFileExtension");
        final String filesDesc = getResourceString("flowXMLFileExtensionDescription");
		fc.setFileFilter(new DescriptiveFileFilter(fileExtension, filesDesc));
        
        final int option = fc.showSaveDialog(getFrame());
        SaveFlowGraphTask task = null;
        if (JFileChooser.APPROVE_OPTION == option) {
        	File selectedFile = fc.getSelectedFile();
        	if(!selectedFile.getName().toLowerCase().endsWith("."+fileExtension)) {
        		selectedFile = new File(selectedFile.getAbsoluteFile()+"."+fileExtension);
        	}
        	
        	if(selectedFile.exists()) {
				final int response = JOptionPane.showConfirmDialog(getMainPanel(), 
						"This file already exists. Do you want to overwrite it?",
						"Overwrite existing file?", 
						JOptionPane.OK_CANCEL_OPTION);
				if (!(response == JOptionPane.OK_OPTION)) {
					return null;
				}
        	}
        	task = new SaveFlowGraphTask(selectedFile);
            
        }
        return task;
    }
    
    /**
	 * Converts the current workflow into a macro and saves this to file.
	 * @return
	 */
	@Action public ExportMacroTask export() {
		JFileChooser fc = createFileChooser("saveAsFileChooser");
		getResourceMap().injectComponents(fc);
		fc.setFileFilter(new DescriptiveFileFilter(
				getResourceString("imageJMacroTXTFileExtension"), 
				getResourceString("imageJMacroTXTFileExtensionDescription")));
		fc.setFileFilter(new DescriptiveFileFilter(
				getResourceString("imageJMacroFileExtension"), 
				getResourceString("imageJMacroFileExtensionDescription")));

		int option = fc.showSaveDialog(getFrame());
		ExportMacroTask task = null;
		if (JFileChooser.APPROVE_OPTION == option) {
			File selectedFile = fc.getSelectedFile();
			if(selectedFile.exists()) {
				int response = JOptionPane.showConfirmDialog(this.getFrame(), 
						"This file already exists. Do you want to overwrite it?",
						"Overwrite existing file?", 
						JOptionPane.OK_CANCEL_OPTION);
				if (!(response == JOptionPane.OK_OPTION)) {
					return null;
				}
			}
			task = new ExportMacroTask(selectedFile, graphController);

		}

	    return task;
	}
    
    private JFileChooser createFileChooser(String name) {
        JFileChooser fc = new JFileChooser(this.file);
        fc.setDialogTitle(getResourceString(name + ".dialogTitle"));
        return fc;
    }
	
    /**
     * Opens a dialog with the list of {@link UnitElement}s in the workflow.
     */
    @Action public void debugPrintNodes() {
    	final JDialog dialog = new JDialog();

    	final DefaultListModel lm = new DefaultListModel();
    	for (final Node node : getNodes()) {
    		lm.addElement(node);	
    	}
    	final JList list = new JList(lm);
    	
		dialog.add(new ScrollPane().add(list));
		dialog.pack();
		dialog.setVisible(true);
    }
    
    /**
     * Opens a dialog with the list of {@link Connection} in the workflow.
     */
    @Action public void debugPrintEdges() {
    	final JDialog dialog = new JDialog();

    	final DefaultListModel lm = new DefaultListModel();
    	for (final Connection connection : getConnections()) {
    		lm.addElement("Connection between "+ connection.getOutput().getName() + " and " + connection.getInput().getName());	
    	}
    	final JList list = new JList(lm);
    	
		dialog.add(new ScrollPane().add(list));
		dialog.pack();
		dialog.setVisible(true);
    }
    
    /**
     * Simulates the cloning if Workflows.
     * This is convinient for work on the {@link MacroFlowRunner} and
     * the {@link MacroGenerator}, because they use only a cloned 
     * version of the actual workflow. This simulates the cloning to 
     * check if the clone is identical.
     */
    @Action public void debugDrawClonedWorkflow() {
    	final JDialog dialog = new JDialog();
    	
    	final GPanelPopup popup = new GPanelPopup(graphController);
    	final GraphPanel gpanel = new GraphPanel(popup, graphController);
    	popup.setActivePanel(gpanel);
    	final UnitList cloneUnitList = getNodes().clone();
    	gpanel.setNodeL(cloneUnitList);
    	gpanel.setEdgeL(cloneUnitList.getConnections());
    	
    	dialog.add(new ScrollPane().add(gpanel));
    	dialog.setSize(400, 300);
		dialog.setVisible(true);
    }
    
    /**
     * Opens a dialog with debugging information about the selected {@link UnitElement}
     */
    @Action(enabledProperty = "selected")
    public void debugPrintNodeDetails() {
    	for (Node node : getSelections()) {
    		final JDialog dialog = new JDialog();
    		final DefaultListModel lm = new DefaultListModel();

    		dialog.setTitle(node.getLabel());
    		lm.addElement(node.getOrigin());
    		if(node instanceof UnitElement) {
    			final UnitElement unit = (UnitElement)node;

        		// list parameters
            	for (final Parameter parameter : unit.getParameters()) {
            		lm.addElement(parameter);	
            	}
            	for (final Input input : unit.getInputs()) {
            		lm.addElement(input);
            		lm.addElement("name:"+input.getName());
            		lm.addElement("datatype: "+input.getDataType());
            		if(input.getDataType() instanceof DataTypeFactory.Image)
            			lm.addElement("imagetype def:"
            					+((DataTypeFactory.Image)input.getDataType()).getImageBitDepth());
            		lm.addElement("connected to:");
            		lm.addElement(input.getConnection());
            	}
            	for (final Output output : unit.getOutputs()) {
            		lm.addElement(output);
            		lm.addElement("name:"+output.getName());
            		lm.addElement("datatype:"+output.getDataType());
            		if(output.getDataType() instanceof DataTypeFactory.Image)
            			lm.addElement("imagetype:"
            					+((DataTypeFactory.Image)output.getDataType()).getImageBitDepth());
            		lm.addElement("connected to:");
            		for (Connection conn : output.getConnections()) {
            			lm.addElement(conn);
    				}
            		
            	}
    		}
			
        	final JList list = new JList(lm);
        	
    		dialog.add(new ScrollPane().add(list));
    		dialog.pack();
    		dialog.setVisible(true);	
		}
    	
    }
    
    /**
     * Opens a {@link JDialog} with the contents of a selected {@link GroupUnitElement}.
     */
    @Action public void showGroupContents() {
    	if(getSelections().size() == 1 
    			&& getSelections().get(0) instanceof GroupUnitElement) {
    		final JDialog dialog = new JDialog();
    		
    		
    		final GroupUnitElement group = (GroupUnitElement) getSelections().get(0);
//    		group.showGroupWindow();
    		dialog.setTitle(group.getLabel());
    		
    		final GPanelPopup popup = new GPanelPopup( graphController);
        	final GraphPanel gpanel = new GraphPanel(popup);
    		gpanel.setNodeL(group.getNodes());
    		popup.setActivePanel(gpanel);
        	gpanel.setEdgeL(group.getInternalConnections());
        	
        	dialog.add(new ScrollPane().add(gpanel));
        	dialog.setSize(400, 300);
    		dialog.setVisible(true);
    	}
    }
    
    /**
     * Opens the URL to the development blog of the project.
     */
    @Action 
    public void openDevblogURL() {
    	try {
			ij.plugin.BrowserLauncher.openURL(getResourceString("openDevblogURL.Url"));
		} catch (final IOException e) { e.printStackTrace(); }
    }
    
    /**
     * Opens the URL to the ImageJ-website.
     */
    @Action 
    public void openImageJURL() {
    	try {
			ij.plugin.BrowserLauncher.openURL(getResourceString("openImageJURL.Url"));
		} catch (final IOException e) { e.printStackTrace(); }
    }
    
    /**
     * Minimize this frame.
     */
    @Action
    public void minimize() {
    	this.getFrame().setState(Frame.ICONIFIED);
    }
    
    /**
     * Displays the About-dialog
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            final JFrame mainFrame = getFrame();
            aboutBox = new ImageFlowAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ImageFlow.getApplication().show(aboutBox);
//        aboutBox.setVisible(true);
//        ((ImageFlow)getApplication()).addWindow(aboutBox);
    }
    
	private javax.swing.Action getAction(String actionName) {
		ActionMap actionMap = getContext().getActionMap(ImageFlowView.class, this);
	    return actionMap.get(actionName);
	}

	/**
	 * The GraphPanel of this View-instance.
	 * @return
	 */
	public GraphPanel getGraphPanel() {
		return this.graphPanel;
	}
	
    private class ConfirmExit implements Application.ExitListener {
        public boolean canExit(EventObject e) {
            if (isModified()) {
//                String confirmExitText = getResourceString("confirmTextExit", getFile());
//                int option = JOptionPane.showConfirmDialog(getFrame(), confirmExitText);
            	int option = showSaveConfirmation();
                if(option == JOptionPane.YES_OPTION) {
                	save().run();
                } else if (option == JOptionPane.CANCEL_OPTION) {
                	return false;
                }  
            }
            return true;
        }
        public void willExit(EventObject e) { }
        
        
    }


    /** This is a substitute for FileNameExtensionFilter, which is
     * only available on Java SE 6.
     */
    private static class DescriptiveFileFilter extends FileFilter {

        private final String description;
        private final String extension; 

        public DescriptiveFileFilter(String extension, String description) {
        	this.extension = extension;
        	this.description = extension + " - " +description;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String fileName = f.getName();
            int i = fileName.lastIndexOf('.');
            if ((i > 0) && (i < (fileName.length() - 1))) {
                String fileExt = fileName.substring(i + 1);
                if (extension.equalsIgnoreCase(fileExt)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
    
}