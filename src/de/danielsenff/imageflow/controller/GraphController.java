/**
 * Copyright (C) 2008-2010 Daniel Senff
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package de.danielsenff.imageflow.controller;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.UndoableEditListener;

import visualap.Node;
import de.danielsenff.imageflow.ImageFlow;
import de.danielsenff.imageflow.imagej.MacroFlowRunner;
import de.danielsenff.imageflow.models.SelectionList;
import de.danielsenff.imageflow.models.connection.Connection;
import de.danielsenff.imageflow.models.connection.ConnectionList;
import de.danielsenff.imageflow.models.connection.Input;
import de.danielsenff.imageflow.models.connection.Output;
import de.danielsenff.imageflow.models.connection.ProxyInput;
import de.danielsenff.imageflow.models.connection.ProxyOutput;
import de.danielsenff.imageflow.models.datatype.DataTypeFactory;
import de.danielsenff.imageflow.models.datatype.DataTypeFactory.Image;
import de.danielsenff.imageflow.models.unit.CommentNode;
import de.danielsenff.imageflow.models.unit.GroupUnitElement;
import de.danielsenff.imageflow.models.unit.UnitElement;
import de.danielsenff.imageflow.models.unit.UnitFactory;
import de.danielsenff.imageflow.models.unit.UnitList;




/**
 * Controller for Workflows. 
 * @author Daniel Senff
 *
 */
public class GraphController{

	private UnitList nodes;
	/**
	 * List which stores copied Nodes.
	 */
	protected ArrayList<Node> copyNodesList;

	/**
	 * List of selected units
	 */
	private SelectionList selections;

	
	private UndoableEditListener listener;
	
	/**
	 * 
	 */
	public GraphController() {
		this.nodes = new UnitList();
		this.copyNodesList = new ArrayList<Node>();
		this.selections = new SelectionList();
	}

	/**
	 * @return the unitElements
	 */
	public UnitList getUnitElements() {
		return this.nodes;
	}


	/**
	 * Generates the executable Macro based on the current graph.
	 * @param extendedMacro determines if callback functions are put into macro code 
	 * @return
	 */
	public String generateMacro(boolean extendedMacro) {
		final MacroFlowRunner macroFlowRunner = new MacroFlowRunner(this.nodes);
		return macroFlowRunner.generateMacro(extendedMacro);
	}

	/**
	 * Returns current the {@link ConnectionList}
	 * @return
	 */
	public ConnectionList getConnections() {
		return this.nodes.getConnections();
	}


	/**
	 * Selections
	 * @return
	 */
	public SelectionList getSelections() {
		return this.selections;
	}
	
	/**
	 * Get the List of copied {@link Node};
	 * @return
	 */
	public ArrayList<Node> getCopyNodesList() {
		return copyNodesList;
	}

	/**
	 * Set the UndoableEditListener.
	 * @param l
	 */
	public void addUndoableEditListener(UndoableEditListener l) {
		listener = l; // Should ideally throw an exception if listener != null
	}

	/**
	 * Remove the UndoableEditListener.
	 * @param l
	 */
	public void removeUndoableEditListener(UndoableEditListener l) {
		listener = null;
	}
	
	/**
	 * Removes the {@link UnitElement} from the unitList and its Connections.
	 * @param node 
	 * @return
	 */
	public boolean removeNode(final Node node) {
		return nodes.remove(node);
	}
	
	/**
	 * Ungroup the contents of a GroupUnit
	 * @param group
	 */
	public void ungroup(final GroupUnitElement group) {
		ungroup(group, getUnitElements());
	}
	
	/**
	 * @param group
	 * @param units
	 */
	public static void ungroup(final GroupUnitElement group, final UnitList units) {
		
		int deltaX = group.getOrigin().x - 25;
		int deltaY = group.getOrigin().y - 25;
		for (Node node : group.getNodes()) {
			int x = node.getOrigin().x, y = node.getOrigin().y;
			node.getOrigin().setLocation(x+deltaX, y+deltaY);
			
			for (Input input : ((UnitElement)node).getInputs()) {
				input.setLocked(false);
			}
			for (Output output : ((UnitElement)node).getOutputs()) {
				output.setLocked(false);
			}
		}
		
		
		
		units.addAll(group.getNodes());
		ConnectionList connections = units.getConnections();
		
		/*
		 * reconnect inputs
		 */
		for (Input input : group.getInputs()) {
			if(input instanceof ProxyInput) {
				ProxyInput pInput = (ProxyInput)input;
				if(pInput.isConnected()) {
					Output connectedOutput = pInput.getFromOutput();
					Input originalInput = pInput.getEmbeddedInput();
					
					Connection connection = new Connection(connectedOutput, originalInput);
					connections.add(connection);
				}
			}
		}
		
		/*
		 *  reconnect outputs
		 */
		Collection<Connection> tmpConn = new Vector<Connection>();
		for (Output output : group.getOutputs()) {
			if(output instanceof ProxyOutput) {
				ProxyOutput pOutput = (ProxyOutput)output;
				if(pOutput.isConnected()) {
					Output originalOutput = pOutput.getEmbeddedOutput();
					if(originalOutput.getDataType() instanceof DataTypeFactory.Image) {
						Image imageDataType = (DataTypeFactory.Image)originalOutput.getDataType();
						imageDataType.setParentUnitElement(originalOutput.getParent());
						imageDataType.setParentPin(originalOutput);
					}
					
					for (Connection	connection : pOutput.getConnections()) {
						Connection newConn = new Connection(originalOutput, connection.getInput());
						tmpConn.add(newConn);
					}
				}
			}
		}
		// write connections into actual connectionlist
		for (Connection connection : tmpConn) {
			connections.add(connection);	
		}
		
		/*
		 * reconnect connection within the group
		 */
		
		for (Connection connection : group.getInternalConnections()) {
			connections.add(connection);
		}
		
		units.remove(group);
	}

	public void group() throws Exception {
		if(!getSelections().isEmpty()) {
			GroupUnitElement group = new GroupUnitElement(new Point(34, 250), "Group");
			group.putUnits(getSelections(), getUnitElements());
			getUnitElements().add(group);
			selections.clear();
			selections.add(group);	
		}
		/*if (listener != null) {
			listener.undoableEditHappened(new UndoableEditEvent(this, new UndoableEdit() {

				public boolean addEdit(UndoableEdit anEdit) {
					// TODO Auto-generated method stub
					return false;
				}

				public boolean canRedo() {	return true; }

				public boolean canUndo() {	return true; }

				public void die() {}

				public String getPresentationName() {
					return null;
				}

				public String getRedoPresentationName() {
					return "Regroup";
				}

				public String getUndoPresentationName() {
					return "Ungroup";
				}

				public boolean isSignificant() { return true; }

				public void redo() throws CannotRedoException {
					// TODO Auto-generated method stub
					
				}

				public boolean replaceEdit(UndoableEdit anEdit) {
					// TODO Auto-generated method stub
					return false;
				}

				public void undo() throws CannotUndoException {
					ungroup(group);
				}
				
			}));
		}*/
	}
	
	/**
	 * Reads the contents of a flow-XML-document.
	 * @param url The document to load.
	 */
	public void read(URL url) {
		WorkflowXMLBuilder workflowbuilder = new WorkflowXMLBuilder(nodes);
		System.out.println(url);
		workflowbuilder.read(url);
	}
	
	/**
	 * Write the workflow to a XML-file
	 * @param file
	 * @throws IOException
	 */
	public void write(final File file) throws IOException {
		WorkflowXMLBuilder workflowbuilder = new WorkflowXMLBuilder(nodes);
		workflowbuilder.write(file);
	}
	
	/** 
	 * TODO remove this, update unit tests
	 */
	public void setupExample1() {
		////////////////////////////////////////////////////////
		// setup of units
		////////////////////////////////////////////////////////
		DelegatesController delegatesController = DelegatesController.getInstance();

		final UnitElement sourceUnit = delegatesController.getDelegate("Image Source").createUnit(new Point(30, 100));
		final UnitElement blurUnit = delegatesController.getDelegate("Gaussian Blur").createUnit(new Point(180, 50));
		final UnitElement mergeUnit = delegatesController.getDelegate("Image Calculator").createUnit(new Point(320, 100));
		final UnitElement noiseUnit = delegatesController.getDelegate("Add Noise").createUnit(new Point(450, 100));
		noiseUnit.setDisplay(true);
		
		CommentNode comment = UnitFactory.createComment("my usual example", new Point(30, 40));

		// some mixing, so they are not in order
		nodes.add(noiseUnit);
		nodes.add(blurUnit);
		nodes.add(sourceUnit);
		nodes.add(mergeUnit);
		nodes.add(comment);


		////////////////////////////////////////////////////////
		// setup the connections
		////////////////////////////////////////////////////////

		// add six connections
		// the conn is established on adding
		// fromUnit, fromOutputNumber, toUnit, toInputNumber
		Connection con;
		con = new Connection(sourceUnit,1,blurUnit,1);
		nodes.addConnection(con);
		con = new Connection(blurUnit,1,mergeUnit,1);
		nodes.addConnection(con);
		con = new Connection(sourceUnit,1,mergeUnit,2);
		nodes.addConnection(con);
		con = new Connection(mergeUnit,1,noiseUnit,1);
		nodes.addConnection(con);
	}

	private void showExampleLoadError(Exception e) {
		final int type = JOptionPane.ERROR_MESSAGE;
		JOptionPane.showMessageDialog(
			ImageFlow.getApplication().getMainFrame(),
			"An error occured while loading the example!", "Could not load example", type);
	}
}

