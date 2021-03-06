/**
 * 
 */
package models;

import java.awt.Dimension;
import java.awt.Point;

import visualap.Node;
import de.danielsenff.imageflow.models.connection.Connection;
import de.danielsenff.imageflow.models.connection.ConnectionList;
import de.danielsenff.imageflow.models.unit.UnitElement;
import de.danielsenff.imageflow.models.unit.UnitList;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author danielsenff
 *
 */
public class UnitListTests {


	/**
	 * tests wheather all inputs of a all unit are connected
	 */
	@Test
	public void testAreAllInputsConnected() {

		// test output-only
		UnitElement sourceUnit = UnitFactoryExt.createBackgroundUnit(new Dimension(12, 12));
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactoryExt.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactoryExt.createAddNoiseUnit();
		
		Connection conn = new Connection(sourceUnit, 1, filterUnit1, 1);
		ConnectionList connList = new ConnectionList();
		connList.add(conn);
		
		// adding to UnitList
		
		UnitList units = new UnitList();
		units.add(sourceUnit);
		units.add(filterUnit1);
		
		
		//assertion
		assertTrue("only nodes added, which are connected", units.areAllInputsConnected());
		
		// add one more which is not connected
		units.add(filterUnit2);
		
		//assertion
		assertFalse("contains inputs, which are not connected", units.areAllInputsConnected());
		
	}
	
	@Test public void testHasDisplayUnit() {
		// test output-only
		UnitElement sourceUnit = UnitFactoryExt.createBackgroundUnit(new Dimension(12, 12));
		Node comment = UnitFactoryExt.createComment("hello a comment", new Point(13, 13));
		
		// test input/output case
		UnitElement filterUnit1 = UnitFactoryExt.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactoryExt.createAddNoiseUnit();
		
		// adding to UnitList
		
		UnitList units = new UnitList();
		units.add(comment);
		units.add(sourceUnit);
		units.add(filterUnit1);
		units.add(filterUnit2);
		
		assertFalse(units.isEmpty());
		assertFalse("has no displayunits", units.hasUnitAsDisplay());
		
		filterUnit1.setDisplay(true);
		
		assertTrue("has displayunits", units.hasUnitAsDisplay());
		
	}
	
	@Test public void testIsEmpty() {
		UnitList units = new UnitList();
		
		assertTrue(units.isEmpty());
		
		UnitElement filterUnit1 = UnitFactoryExt.createAddNoiseUnit();
		UnitElement filterUnit2 = UnitFactoryExt.createAddNoiseUnit();
		
		units.add(filterUnit1);
		units.add(filterUnit2);
		
		assertFalse(units.isEmpty());
		assertEquals("elements counts",2, units.size());
		
		units.remove(filterUnit1);
		
		assertFalse(units.isEmpty());
		assertEquals("elements counts",1, units.size());
		assertFalse(units.contains(filterUnit1));
		assertTrue(units.contains(filterUnit2));
		
		units.remove(filterUnit2);
		
		assertTrue(units.isEmpty());
		assertEquals("elements counts",0, units.size());
		assertFalse(units.contains(filterUnit2));
		
	}
	
}
