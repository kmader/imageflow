package models;

import ij.plugin.filter.PlugInFilter;
import imageflow.models.Connection;
import imageflow.models.ConnectionList;
import imageflow.models.Input;
import imageflow.models.Output;
import imageflow.models.unit.UnitElement;
import imageflow.models.unit.UnitFactory;

import java.awt.Dimension;

import junit.framework.TestCase;

public class OutputTests extends TestCase {

	
	public void testImageTitle() {
		
		// image title of style "Unit_1_Output_1"
		// test output-only
		UnitElement sourceUnit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactory.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactory.createAddNoiseUnit();
		
		Output sourceOutput = sourceUnit.getOutput(0);
		Input filter1Input = filterUnit1.getInput(0);
		Output filter1Output = filterUnit1.getOutput(0);

		Connection conn1 = new Connection(sourceUnit, 1, filterUnit1, 1);
		Connection conn2 = new Connection(sourceUnit, 1, filterUnit2, 1);
		conn1.connect();
		assertTrue(conn1.isConnected());
		conn2.connect();
		assertTrue(conn2.isConnected());
		
		// the imagetitle is constructed from the unit and pin the 
		// connection comes from and the 
		assertEquals("imagetitle for output 1 at unit 1", 
				"Unit_"+sourceUnit.getUnitID()+"_Output_1", sourceOutput.getImageTitle());
		assertEquals("imagetitle for input 1 at unit 2", 
				"Unit_"+sourceUnit.getUnitID()+"_Output_1", filter1Input.getImageTitle());
		
		assertNotNull("imagetitle for output 1 at unit 2", filter1Output.getImageTitle());
	}
	

	public void testIsConnected() {
		
		// test output-only
		UnitElement sourceUnit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactory.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactory.createAddNoiseUnit();
		
		Output sourceOutput = sourceUnit.getOutput(0);
		
		// test beforehand
		assertFalse("output not connected", sourceOutput.isConnected());
		
		Connection conn = new Connection(sourceUnit, 1, filterUnit1, 1);
		
		ConnectionList connList = new ConnectionList();
		connList.add(conn);
		
		// test after connecting
		assertTrue("output connected", sourceOutput.isConnected());	
		assertFalse("output connected", filterUnit2.getOutput(0).isConnected());	
	}
	
	public void testIsConnectedWith() {
		// test output-only
		UnitElement source1Unit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		Output source1Output = source1Unit.getOutput(0);
		UnitElement source2Unit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		Output source2Output = source2Unit.getOutput(0);
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactory.createAddNoiseUnit();
		Input filter1Input = filterUnit1.getInput(0);
		UnitElement filterUnit2 = UnitFactory.createAddNoiseUnit();
		Input filter2Input = filterUnit2.getInput(0);
		
		// test beforehand
		assertFalse("output not connected with filter1Input", 
				source1Output.isConnectedWith(filter1Input));
		assertFalse("output not connected with filter2Input", 
				source1Output.isConnectedWith(filter2Input));
		
		Connection conn = new Connection(source1Unit, 1, filterUnit1, 1);
//		ConnectionList connList = new ConnectionList();
//		connList.add(conn);
		conn.connect();
		
		
		// test after connecting
		assertTrue("Connection connected correctly", conn.isConnected());
		assertTrue("output connected with filter1Input", 
				source1Output.isConnectedWith(filter1Input));
		assertFalse("output not connected with filter2Input", 
				source1Output.isConnectedWith(filter2Input));
	}
	
	public void testIsDisconnected() {
		
		// test output-only
		UnitElement sourceUnit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactory.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactory.createAddNoiseUnit();
		
		Output sourceOutput = sourceUnit.getOutput(0);
		
		
		ConnectionList connList = new ConnectionList();
		
		// test after connecting
		assertTrue("connection added to list", connList.add(sourceUnit, 1, filterUnit1, 1));
		assertTrue("output connected", sourceOutput.isConnected());	
		assertFalse("output connected", filterUnit2.getOutput(0).isConnected());
		
		connList.remove(0);
		
		//test after disconnecting
		assertFalse("output disconnected", sourceOutput.isConnected());
		assertFalse("output connected", filterUnit2.getOutput(0).isConnected());
	}
	
	public void testImageBitDepthCompatible() {

		UnitElement unit1 = new UnitElement("unit1", "some syntax");
		unit1.addOutput("output1", "o", PlugInFilter.DOES_32, false);
		unit1.addOutput("output2", "o", PlugInFilter.DOES_ALL, false);
		unit1.addOutput("output3", "o", -1, false);
		Output output1 = unit1.getOutput(0);
		Output output2 = unit1.getOutput(1);
		Output output3 = unit1.getOutput(2);
		
		UnitElement unit2 = new UnitElement("unit2", "some syntax");
		unit2.addInput("input1", "i", PlugInFilter.DOES_32, false);
		unit2.addInput("input2", "i", PlugInFilter.DOES_16, false);
		unit2.addInput("input3", "i", PlugInFilter.DOES_ALL, false);
		Input input1 = unit2.getInput(0);
		Input input2 = unit2.getInput(1);
		Input input3 = unit2.getInput(2);

		/*assertTrue("both do 32", input1.isImageBitDepthCompatible(output1.getImageBitDepth()));
		assertFalse("32 to 16", input2.isImageBitDepthCompatible(output1.getImageBitDepth()));
		assertTrue("all to 32", input1.isImageBitDepthCompatible(output2.getImageBitDepth()));
		assertTrue("32 to all", input3.isImageBitDepthCompatible(output1.getImageBitDepth()));*/
		
		assertTrue("both do 32", output1.isImageBitDepthCompatible(input1.getImageBitDepth()));
		// ok, technically ALL to 32 does work, but since the input tests for a concrete type, 
		// and not for a could-be, this is false
		assertFalse("All to 32", output2.isImageBitDepthCompatible(input1.getImageBitDepth()));
		assertFalse("32 to 16", output1.isImageBitDepthCompatible(input2.getImageBitDepth()));
		assertFalse("-1 to 16", output3.isImageBitDepthCompatible(input2.getImageBitDepth()));
		assertFalse("32 to 16", output1.isImageBitDepthCompatible(input2.getImageBitDepth()));

	}
	
	public String verboseBitDepth(int imagetype) {
		switch(imagetype) {
		case PlugInFilter.DOES_16:
			return "DOES_16";
		case PlugInFilter.DOES_32:
			return "DOES_32";
		case PlugInFilter.DOES_8G:
			return "DOES_8G";
		case PlugInFilter.DOES_8C:
			return "DOES_8C";
		case PlugInFilter.DOES_RGB:
			return "DOES_RGB";
		case PlugInFilter.DOES_ALL:
			return "DOES_ALL";
		case PlugInFilter.DOES_STACKS:
			return "DOES_STACKS";
		case -1:
			return "predecessor type";
		}
		return "unknown";
	}
	
	public void traverseImageBitDepth(int unit1Obitdepth, 
			int unit2Ibitdepth, int unit2Obitdepth, 
			int unit3Ibitdepth,
			boolean expFirstConn, boolean expScndConn) {
		UnitElement unit1 = new UnitElement("unit1", "some syntax");
		unit1.addOutput("output1", "o", unit1Obitdepth, false);
		
		UnitElement unit2 = new UnitElement("unit2", "some syntax");
		unit2.addInput("input1", "i", unit2Ibitdepth, false);
		unit2.addOutput("output1", "o", unit2Obitdepth, false);

		UnitElement unit3 = new UnitElement("unit3", "some syntax");
		unit3.addInput("input1", "i", unit3Ibitdepth, false);

		Connection conn1 = new Connection(unit1, 1, unit2, 1); // 32 to 32
		conn1.connect();
		assertEquals("Conn1: "+verboseBitDepth(unit1Obitdepth)+" to "+ verboseBitDepth(unit2Ibitdepth), 
				expFirstConn, conn1.areImageBitDepthCompatible());
		Connection conn2 = new Connection(unit2, 1, unit3, 1); // 32 to 32
		conn2.connect();
		assertEquals("Conn2: "+verboseBitDepth(unit2Obitdepth)+" to "+ verboseBitDepth(unit3Ibitdepth), 
				expScndConn, conn2.areImageBitDepthCompatible());
	}
	
	public void testImageBitDepthTraversing() {
		
		traverseImageBitDepth(PlugInFilter.DOES_32, //unit 1 output
				PlugInFilter.DOES_32, PlugInFilter.DOES_32,  //unit2 input, output
				PlugInFilter.DOES_32, true, true); // unit 3, expectations
		
		traverseImageBitDepth(PlugInFilter.DOES_32, //unit 1 output
				PlugInFilter.DOES_ALL, PlugInFilter.DOES_32,  //unit2 input, output
				PlugInFilter.DOES_ALL, true, true); // unit 3, expectations
		
		
		
		traverseImageBitDepth(PlugInFilter.DOES_32, //unit 1 output
				PlugInFilter.DOES_ALL, PlugInFilter.DOES_32,  //unit2 input, output
				PlugInFilter.DOES_16, true, false); // unit 3, expectations
		traverseImageBitDepth(PlugInFilter.DOES_32, //unit 1 output
				PlugInFilter.DOES_16, PlugInFilter.DOES_32,  //unit2 input, output
				PlugInFilter.DOES_32, false, true); // unit 3, expectations
		
		
		traverseImageBitDepth(PlugInFilter.DOES_32, //unit 1 output
				PlugInFilter.DOES_ALL, -1,  //unit2 input, output
				PlugInFilter.DOES_32, true, true); // unit 3, expectations
		
		traverseImageBitDepth(PlugInFilter.DOES_16, //unit 1 output
				PlugInFilter.DOES_ALL, -1,  //unit2 input, output
				PlugInFilter.DOES_32, true, false); // unit 3, expectations

		traverseImageBitDepth(PlugInFilter.DOES_16, //unit 1 output
				PlugInFilter.DOES_ALL, -1,  //unit2 input, output
				PlugInFilter.DOES_ALL, true, true); // unit 3, expectations	
	}
	
	
	public void testTraversingImageBitDepth() {
		UnitElement unit1 = new UnitElement("unit1", "some syntax");
		unit1.addOutput("output1", "o", PlugInFilter.DOES_32, false);
		Output output1 = unit1.getOutput(0);
		
		UnitElement unit2 = new UnitElement("unit2", "some syntax");
		unit2.addInput("input1", "i", PlugInFilter.DOES_ALL, false);
		Input input1 = unit2.getInput(0);
		unit2.addOutput("output1", "o", -1, false);
		Output output2 = unit2.getOutput(0);
		
//		UnitElement unit3 = new UnitElement("unit3", "some syntax");
//		unit3.addInput("input1", "i", unit3Ibitdepth, false);

		Connection conn1 = new Connection(unit1, 1, unit2, 1);
		conn1.connect();
		
		assertTrue("travers from DOES_32", 
				output1.isImageBitDepthCompatible(PlugInFilter.DOES_32));
		assertFalse("travers from DOES_32", 
				output1.isImageBitDepthCompatible(PlugInFilter.DOES_16));
		
		assertTrue("travers from DOES_32 via DOES_ALL to -1", 
				output2.isImageBitDepthCompatible(PlugInFilter.DOES_32));
		
		assertFalse("travers from DOES_32 via DOES_ALL to -1", 
				output2.isImageBitDepthCompatible(PlugInFilter.DOES_16));

	}
	
	
	public void testLoopScenario1() {
		
		UnitElement unit1 = UnitFactory.createFindEdgesUnit();
		UnitElement unit2 = UnitFactory.createImageCalculatorUnit();
		UnitElement unit3 = UnitFactory.createAddNoiseUnit();
		UnitElement unit4 = UnitFactory.createGaussianBlurUnit();
		
		Connection conn1 = new Connection(unit1, 1, unit2, 1);
		conn1.connect();
		assertTrue(conn1.isConnected());
		Connection conn2 = new Connection(unit3, 1, unit2, 2);
		conn2.connect();
		assertTrue(conn2.isConnected());
		
		Output outputU2 = unit2.getOutput(0);
		Input inputU4 = unit4.getInput(0);
		
		assertFalse(outputU2.existsInInputSubgraph(unit4));
		assertFalse(inputU4.isConnectedInInputBranch(unit2));
		
		// now we create a loop
		
		Connection conn3 = new Connection(unit4, 1, unit1, 1);
		conn3.connect();
		assertTrue(conn3.isConnected());
		
		assertTrue(outputU2.existsInInputSubgraph(unit4));
		assertTrue(inputU4.isConnectedInInputBranch(unit2));
	}
	

	public void testLoopScenario2() {
		
		UnitElement unit1 = UnitFactory.createFindEdgesUnit();
		UnitElement unit2 = UnitFactory.createImageCalculatorUnit();
		UnitElement unit4 = UnitFactory.createGaussianBlurUnit();
		
		Connection conn1 = new Connection(unit1, 1, unit2, 1);
		conn1.connect();
		Connection conn2 = new Connection(unit1, 1, unit2, 2);
		conn2.connect();
		assertTrue(conn2.isConnected());
		
		Output outputU2 = unit2.getOutput(0);
		Input inputU4 = unit4.getInput(0);
		
		assertFalse(outputU2.existsInInputSubgraph(unit4));
		assertFalse(inputU4.isConnectedInInputBranch(unit2));
		
		// now we create a loop
		
		Connection conn3 = new Connection(unit4, 1, unit1, 1);
		conn3.connect();
		assertTrue(conn3.isConnected());
		
		assertTrue(outputU2.existsInInputSubgraph(unit4));
		assertTrue(inputU4.isConnectedInInputBranch(unit2));
	}
	
	public void testLoopScenario3() {
		UnitElement unit1 = UnitFactory.createFindEdgesUnit();
		UnitElement unit4 = UnitFactory.createGaussianBlurUnit();
		
		Output outputU1 = unit1.getOutput(0);
		Input inputU4 = unit4.getInput(0);
		
		assertFalse(outputU1.existsInInputSubgraph(unit4));
		assertFalse(inputU4.isConnectedInInputBranch(unit1));
		
		//connecting first time
		
		Connection conn1 = new Connection(unit1, 1, unit4, 1);
		conn1.connect();
		assertTrue(conn1.isConnected());
		
		assertFalse(outputU1.existsInInputSubgraph(unit4));
		assertFalse(inputU4.isConnectedInInputBranch(unit1));
		
		// creating the same connection a second time
		
		Connection conn3 = new Connection(unit4, 1, unit1, 1);
		conn3.connect();
		assertTrue(conn3.isConnected());
//		assertFalse(conn1.isConnected());
	}
	
	
	public void testUnitConnectedInBranch() {
		
		UnitElement unit1 = UnitFactory.createAddNoiseUnit();
		Output output1 = unit1.getOutput(0);
		UnitElement unit2 = UnitFactory.createAddNoiseUnit();
		Output output2 = unit2.getOutput(0);
		UnitElement unit3 = UnitFactory.createAddNoiseUnit();
		
		Connection conn1 = new Connection(unit1, 1, unit2, 1);
		
		ConnectionList connList = new ConnectionList();
		connList.add(conn1);
		
		
		assertTrue("output2 knows unit1", output2.existsInInputSubgraph(unit1));
		assertTrue("output2 knows unit2", output2.existsInInputSubgraph(unit2));
		assertFalse("output2 knows unit3", output2.existsInInputSubgraph(unit3));
		
		Connection conn2 = new Connection(unit2, 1, unit3, 1);
		
		
		assertTrue("output2 knows unit1", output2.existsInInputSubgraph(unit1));
		assertTrue("output2 knows unit2", output2.existsInInputSubgraph(unit2));
		assertFalse("output2 knows unit3", output2.existsInInputSubgraph(unit3));
		
		/*
		 * output 1 is on a source, so it has no input branch
		 */
		
		assertTrue("output1 knows unit1", output1.existsInInputSubgraph(unit1));
		assertFalse("output1 knows unit2", output1.existsInInputSubgraph(unit2));
		assertFalse("output1 knows unit3", output2.existsInInputSubgraph(unit3));
		
		
		connList.add(conn2);
		
		
		assertTrue("output1 knows unit1", output1.existsInInputSubgraph(unit1));
		assertFalse("output1 knows unit2", output1.existsInInputSubgraph(unit2));
		assertFalse("output1 knows unit3", output1.existsInInputSubgraph(unit3));
		
	}
}