/**
 * 
 */
package models;

import java.awt.Dimension;

import models.unit.UnitElement;
import models.unit.UnitFactory;
import junit.framework.TestCase;

/**
 * @author danielsenff
 *
 */
public class InputTests extends TestCase {

	public void testImageTitle() {
	
		// image title of style "Unit_1_Output_1"
		// test output-only
		UnitElement sourceUnit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactory.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactory.createAddNoiseUnit();
		
		Input filter1Input = filterUnit1.getInput(0);
		Input filter2Input = filterUnit1.getInput(0);

		Connection conn1 = new Connection(sourceUnit, 1, filterUnit1, 1);
		Connection conn2 = new Connection(sourceUnit, 1, filterUnit2, 1);
		
		// the imagetitle is constructed from the unit and pin the 
		// connection comes from and the 
		assertEquals("imagetitle for input 1 at unit 2", 
				"Unit_"+sourceUnit.getUnitID()+"_Output_1", filter1Input.getImageTitle());
		
		assertEquals("imagetitle for input 1 at unit 3", 
				"Unit_"+sourceUnit.getUnitID()+"_Output_1", filter1Input.getImageTitle());
	}
	
	public void testIsConnected() {
		
		// test output-only
		UnitElement sourceUnit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactory.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactory.createAddNoiseUnit();
		
		Input filterInput = filterUnit1.getInput(0);
		
		// test beforehand
		assertFalse("input not connected", filterInput.isConnected());
		
		Connection conn = new Connection(sourceUnit, 1, filterUnit1, 1);
		
		ConnectionList connList = new ConnectionList();
		connList.add(conn);
		
		// test after connecting
		assertTrue("input connected", filterInput.isConnected());	
	}
	
	public void testIsConnectedWith() {
		// test output-only
		UnitElement source1Unit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		Output source1Output = source1Unit.getOutput(0);
		UnitElement source2Unit = UnitFactory.createBackgroundUnit(new Dimension(12, 12));
		Output source2Output = source2Unit.getOutput(0);
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactory.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactory.createAddNoiseUnit();
		
		Input filterInput = filterUnit1.getInput(0);
		
		// test beforehand
		assertFalse("input not connected with source1Output", 
				filterInput.isConnectedWith(source1Output));
		assertFalse("input not connected with source2Output", 
				filterInput.isConnectedWith(source2Output));
		
		Connection conn = new Connection(source1Unit, 1, filterUnit1, 1);
		ConnectionList connList = new ConnectionList();
		connList.add(conn);
		
		
		// test after connecting
		assertTrue("input not connected with source1Output", 
				filterInput.isConnectedWith(source1Output));
		assertFalse("input not connected with source2Output", 
				filterInput.isConnectedWith(source2Output));
		
	}
	
	
}
