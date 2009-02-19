package imageflow.models;

import imageflow.models.unit.UnitElement;
import graph.Edge;
import graph.Node;
import graph.Pin;

/**
 * Connection between two {@link Pin}s, {@link Input} and {@link Output}.
 * @author danielsenff
 *
 */
public class Connection extends Edge {
	public int id;		// the id of this connection
	
	/** 
	 * From which {@link Output}
	 */
//	protected int fromOutputNumber;	
	/**
	 * From which {@link UnitElement}-number
	 */
//	protected int fromUnitNumber;
	/**
	 * From which {@link UnitElement} connected.
	 */
	protected UnitElement fromUnit;
	
	/**
	 * Connected to this Input.
	 */
//	protected int toInputNumber;
	/**
	 * Connected to this {@link UnitElement}-number.
	 */
//	protected int toUnitNumber;	
	/**
	 * Connected to this {@link UnitElement}.
	 */
	protected UnitElement toUnit;
	
	/**
	 * Connection-Status
	 *
	 */
	public enum Status {OK, MISSING_TO_UNIT, MISSING_FROM_UNIT, MISSING_BOTH }
	
	/**
	 * @param fromUnit
	 * @param fromOutputNumber index starting with 1
	 * @param toUnit
	 * @param toInputNumber index starting with 1
	 */
	public Connection(final UnitElement fromUnit, 
			final int fromOutputNumber, 
			final UnitElement toUnit, 
			final int toInputNumber) {
		this(fromUnit, fromUnit.getOutput(fromOutputNumber-1), 
				toUnit, toUnit.getInput(toInputNumber-1));
		
		
		
		/*this.fromUnit = fromUnit;
//		this.fromUnitNumber = fromUnit.getUnitID();
//		this.fromOutputNumber = fromOutputNumber;
		this.toUnit = toUnit;
		this.toUnitNumber = toUnit.getUnitID();
//		this.toInputNumber = toInputNumber;
		
		
		
		id = getID(fromUnitNumber, fromOutputNumber, toUnitNumber, toInputNumber);*/
//		connect();
	}
	
	/**
	 * @param fromUnit 
	 * @param fromOutput 
	 * @param toUnit 
	 * @param toInput 
	 * 
	 */
	public Connection(final UnitElement fromUnit, 
			final Pin fromOutput, 
			final UnitElement toUnit, 
			final Pin toInput) {
		super(fromOutput, toInput);
		this.fromUnit = fromUnit;
//		this.fromUnitNumber = fromUnit.getUnitID();
		this.toUnit = toUnit;
//		this.toUnitNumber = toUnit.getUnitID();
		
		id = getID(fromUnit.getUnitID(), fromOutput.getIndex(), 
				toUnit.getUnitID(), toInput.getIndex());
	}



	/**
	 * connect the inputs and outputs of the units
	 * @param unitElements
	 */
	public void connect() {
		((Output)this.from).connectTo(toUnit, to);
		((Input)this.to).connectTo(fromUnit, from);
		
//		this.toUnit.getInput(toInputNumber-1).connectTo(fromUnit, fromOutputNumber);
//		this.fromUnit.getOutput(fromOutputNumber-1).connectTo(toUnit, toInputNumber);
//		super.to = toUnit.getInput(toInputNumber-1);
//		((Input) super.to).setConnection(toUnit, fromOutputNumber);	
	}
	
	/**
	 * creates a unique id for each connection
	 * @param fromUnitNumber
	 * @param fromOutputNumber
	 * @param toUnitNumber
	 * @param toInputNumber
	 * @return
	 */
	public static int getID(final int fromUnitNumber, 
			final int fromOutputNumber, 
			final int toUnitNumber, 
			final int toInputNumber) {
		
		final int id = (fromUnitNumber<<20) 
			| (fromOutputNumber<<16) | (toUnitNumber<<4) | toInputNumber;   
		return id;
	}
	
	/**
	 * Returns the status of the connection, whether all ends are connected
	 * @return 
	 */
	public Status checkConnection() {
		if(this.fromUnit != null && this.toUnit != null) {
			return Status.OK;
		} else if (this.fromUnit == null || this.toUnit != null) {
			return Status.MISSING_FROM_UNIT;
		} else if (this.fromUnit != null || this.toUnit == null) {
			return Status.MISSING_TO_UNIT;
		}
		return Status.MISSING_BOTH;
	}
	
	/**
	 * Returns the {@link UnitElement} from which this connection comes.
	 * @return 
	 */
	public UnitElement getFromUnit() {
		return this.fromUnit;
	}

	/**
	 * Returns the {@link UnitElement} to which this connections leads.
	 * @return
	 */
	public UnitElement getToUnit() {
		return this.toUnit;
	}
	
	/**
	 * Get the {@link Input}-Pin.
	 * @return
	 */
	public Input getInput() {
		return (Input)super.to;
	}
	
	/**
	 * Get the {@link Output}-pin
	 * @return
	 */
	public Output getOutput() {
		return (Output)super.from;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final String string = super.toString() + " fromUnit: "+ this.fromUnit +" toUnit:" + toUnit;
		return string;
	}
	
	

	/**
	 * Checks a unit, if it's inputs have already been registered in the algorithm.
	 * @param unit
	 * @return
	 */
	public boolean hasInputMarked() {
		boolean hasMarked = true;
		
		if(toUnit.getInputsCount() > 0) {
			// check each input, if it's parent has been registered
			int mark = fromUnit.getOutput(0).getMark();
			// if mark is not set
			if(mark == 0) {
				// this connected ouput hasn't been registered and 
				// is missing a mark, 
				// so the whole unit isn't ready set. 
				hasMarked = false;
			} 
			// else mark is already set, so this output is fine
		}
		
		return hasMarked;
	}


	/**
	 * Checks if this connection is attached to the {@link Pin}
	 * @param pin
	 * @return
	 */
	public boolean isConnected(Pin pin) {
		return (this.from.equals(pin)) || this.to.equals(pin);
	}


	/**
	 * Gets whether this Connection is connected with this {@link UnitElement}.
	 * @param unit
	 * @return
	 */
	public boolean isConnectedToUnit(final UnitElement unit) {
		return (this.fromUnit.equals(unit)) || (this.toUnit.equals(unit));
	}
	
	/**
	 * Is true, when the ImageBitDepth given by the {@link Output} is 
	 * supported by this Input.
	 * @return
	 */
	public boolean areImageBitDepthCompatible() {
		Input input = ((Input)this.to);
		Output output = ((Output)this.from);
		
//		boolean areCompatible = (input.getImageBitDepth()&output.getImageBitDepth()) != 0;
//		boolean areCompatible = input.isImageBitDepthCompatible(output.getImageBitDepth());
		boolean areCompatible = output.isImageBitDepthCompatible(input.getImageBitDepth());
		return areCompatible;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if(((Input)this.to).isConnected() 
				&& ((Output)this.from).isConnected()) return true;
		return false;
	}
	
	public boolean causesLoop() {
		if(((Input)this.to).knows(this.fromUnit)) return true;
		if(((Output)this.from).knows(this.toUnit)) return true;
		return false;
	}
}
