/**
 * Mars Simulation Project
 * Unit.java
 * @version 3.1.0 2017-03-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.location.LocationTag;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

/**
 * The Unit class is the abstract parent class to all units in the Simulation.
 * Units include people, vehicles and settlements. This class provides data
 * members and methods common to all units.
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
//@JsonSubTypes({ @Type(value = Person.class, name = "person"), 
//				@Type(value = Structure.class, name = "structure"),
//				@Type(value = Vehicle.class, name = "vehicle"),
//				@Type(value = Equipment.class, name = "equipment"),})
public abstract class Unit implements Serializable, UnitIdentifer, Comparable<Unit> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Unit.class.getName());

	// private static String sourceName =  logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());
	
	public static final int MARS_SURFACE_ID = 0;
	
	public static final int FIRST_SETTLEMENT_ID = 1;
	
	public static final int FIRST_BUILDING_ID = 20;

	public static final int FIRST_VEHICLE_ID = 2050;

	public static final int FIRST_PERSON_ID = 2500;

	public static final int FIRST_ROBOT_ID = 3550;

	public static final int FIRST_EQUIPMENT_ID = 4050;
	
	public static final int UNKNOWN_ID = -1;
	
	
	// Data members
	// The unit's location code
//	private int locationCode = 110_000;
	/** The unit containing this unit. */
	private int containerID = MARS_SURFACE_ID;
	
	/** The mass of the unit without inventory. */
	private double baseMass;
	
	/** TODO Unit name needs to be internationalized. */
	private String name;
	/** TODO Unit description needs to be internationalized. */
	private String description;

	/** The unit's location tag. */
	private LocationTag tag;
	/** The unit's inventory. */
	private Inventory inventory;

	
	/** Unit location coordinates. */
	private Coordinates location;

	protected LocationStateType currentStateType;

	/** Unit listeners. */
	private transient List<UnitListener> listeners;// = Collections.synchronizedList(new ArrayList<UnitListener>());

	protected static Simulation sim = Simulation.instance();
	protected static SimulationConfig simulationConfig = SimulationConfig.instance();
	
	protected static MarsClock marsClock;
	protected static EarthClock earthClock;
	protected static MasterClock masterClock;
	
	protected static MarsSurface marsSurface;
	
	protected static UnitManager unitManager;
	protected static MissionManager missionManager;
	
	protected static Mars mars;
	protected static Weather weather;
	protected static SurfaceFeatures surface;
	
	protected static PersonConfig personConfig = simulationConfig.getPersonConfig();
	protected static VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
	protected static RobotConfig robotConfig = simulationConfig.getRobotConfiguration();
	
	public int getIdentifier() {

		if (this instanceof Settlement)
			return ((Settlement)this).getIdentifier();
		
		if (this instanceof Equipment)
			return ((Equipment)this).getIdentifier();
		
		if (this instanceof Person)
			return ((Person)this).getIdentifier();
		
		if (this instanceof Robot)
			return ((Robot)this).getIdentifier();
		
		if (this instanceof Vehicle)
			return ((Vehicle)this).getIdentifier();

//		if (this instanceof Building) // StackOverflowError
//			return ((Building)this).getIdentifier();
		
		if (this instanceof MarsSurface)
			return MARS_SURFACE_ID;

		return UNKNOWN_ID;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name     {@link String} the name of the unit
	 * @param location {@link Coordinates} the unit's location
	 */
	public Unit(String name, Coordinates location) {
		unitManager = sim.getUnitManager();
		
		listeners = Collections.synchronizedList(new ArrayList<UnitListener>()); // Unit listeners.

		// Creates a new location tag instance for each unit
		tag = new LocationTag(this);

		// Initialize data members from parameters
		this.name = name;
		this.description = name;
		this.baseMass = 0;//Double.MAX_VALUE;

		this.inventory = new Inventory(this);
		this.location = new Coordinates(0D, 0D);

		if (location != null) {
			// Set the unit's location coordinates
			this.location.setCoords(location);
			// Set the unit's inventory location coordinates
			this.inventory.setCoordinates(location);
		}
		
		// Define the default LocationStateType of an unit at the start of the sim
		if (this instanceof Robot) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
		}
		else if (this instanceof Equipment) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
		}
		else if (this instanceof Person) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
		}
		else if (this instanceof Building) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
		}
		else if (this instanceof Vehicle) {
			currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;//.INSIDE_SETTLEMENT;
			containerID = MARS_SURFACE_ID;
		}
		else if (this instanceof Settlement) {
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
			containerID = MARS_SURFACE_ID;
		}
		else {
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
			containerID = MARS_SURFACE_ID;
			logger.info(this + " is " + currentStateType);
		}
	}


	/**
	 * Change the unit's name
	 * 
	 * @param newName new name
	 */
	public final void changeName(String newName) {
		String oldName = this.name;
		Unit unit = this;
		if (unit instanceof Settlement) {
			((Settlement)unit).setName(newName);
			simulationConfig.getSettlementConfiguration().changeSettlementName(oldName, newName);
		}
		this.name = newName;
	}

	/**
	 * Gets the unit's name
	 * 
	 * @return the unit's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the unit's nickname
	 * 
	 * @return the unit's nickname
	 */
	public String getNickName() {
		return name;
	}

	/**
	 * Gets the unit's shortened name
	 * 
	 * @return the unit's shortened name
	 */
	public String getShortenedName() {
		name = name.trim();
		int num = name.length();

		boolean hasSpace = name.matches("^\\s*$");

		if (hasSpace) {
			int space = name.indexOf(" ");

			String oldFirst = name.substring(0, space);
			String oldLast = name.substring(space + 1, num);
			String newFirst = oldFirst;
			String newLast = oldLast;
			String newName = name;

			if (num > 20) {

				if (oldFirst.length() > 10) {
					newFirst = oldFirst.substring(0, 10);
				} else if (oldLast.length() > 10) {
					newLast = oldLast.substring(0, 10);
				}
				newName = newFirst + " " + newLast;

			}

			return newName;
		}

		else
			return name;
	}

	/**
	 * Sets the unit's name
	 * 
	 * @param name new name
	 */
	public void setName(String name) {
		this.name = name;
		fireUnitUpdate(UnitEventType.NAME_EVENT, name);
	}

	/**
	 * Gets the unit's description
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the unit's description.
	 * 
	 * @param description new description.
	 */
	protected void setDescription(String description) {
		this.description = description;
		fireUnitUpdate(UnitEventType.DESCRIPTION_EVENT, description);
	}

	/**
	 * Gets the unit's location
	 * 
	 * @return the unit's location
	 */
	public Coordinates getCoordinates() {
		return location; //getContainerUnit().getCoordinates();
	}

	/**
	 * Sets unit's location coordinates
	 * 
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		location.setCoords(newLocation);
		inventory.setCoordinates(newLocation);
		fireUnitUpdate(UnitEventType.LOCATION_EVENT, newLocation);
	}

	/**
	 * Time passing for unit. Unit should take action or be modified by time as
	 * appropriate.
	 * 
	 * @param time the amount of time passing (in millisols)
	 */
	public void timePassing(double time) {
	}
	
	/**
	 * Gets the unit's inventory
	 * 
	 * @return the unit's inventory object
	 */
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Gets the unit's container unit. Returns null if unit has no container unit.
	 * 
	 * @return the unit's container unit
	 */
	public Unit getContainerUnit() {
//		if (unitManager == null) // for maven test
//			return null;
		return unitManager.getUnitByID(containerID);
	}

	public int getContainerID() {
		return containerID;
	}
	
	public void setContainerID(int ID) {
		containerID = ID;
	}
	
	/**
	 * Gets the topmost container unit that owns this unit (Settlement, Vehicle, Person or Robot) 
	 * If it's on the surface of Mars, then the topmost container is MarsSurface. 
	 * 
	 * @return the unit's topmost container unit
	 */
	public Unit getTopContainerUnit() {
		Unit topUnit = getContainerUnit();
		if (!(topUnit instanceof MarsSurface)) {
			while (topUnit.getContainerUnit() != null && !(topUnit.getContainerUnit() instanceof MarsSurface)) {
				topUnit = topUnit.getContainerUnit();
			}
		}

		return topUnit;
				
		// Scenarios: 
		// 1. If a person is having an EVA Suit, then his container is EVASuit
		//    and his top container is EVA Suit
		// 2. If a person is having an EVA Suit inside a vehicle, then his container is EVASuit
		//    and his top container is that vehicle.

		// WARNING : Under no circumstances should his top container be "MarsSurface".

//		int topID = getContainerUnit().getContainerID();
//		if (topID != 0) {
//			while (topID != 0) {
//				topID = getContainerUnit().getContainerID();
//			}
//		}
//
//		return unitManager.getUnitByID(topID);
	}
	
	/**
	 * Gets the topmost container unit that owns this unit (Settlement, Vehicle, Person or Robot) 
	 * If it's on the surface of Mars, then the topmost container is MarsSurface. 
	 * 
	 * @return the unit's topmost container unit
	 */
	public int getTopContainerID() {
				
		int topID = getContainerUnit().getContainerID();
		if (topID != 0) {
			while (topID != 0) {
				topID = getContainerUnit().getContainerID();
			}
		}

		return topID;
	}
	

//	public void setTopContainerUnit(Unit u) {
////		Unit topUnit = getContainerUnit();
////		if (!(topUnit instanceof MarsSurface)) {
////			while (topUnit.getContainerUnit() != null && !(topUnit.getContainerUnit() instanceof MarsSurface)) {
////				topUnit = topUnit.getContainerUnit();
////			}
////		}
//
//		int topID = getContainerUnit().getContainerID();
//		if (topID != 0) {
//			while (topID != 0) {
//				topID = getContainerUnit().getContainerID();
//			}
//		}
//		
//		Unit topUnit = unitManager.getUnitByID(topID);
//		
//		topUnit.setContainerUnit(u);
//		
//		topUnit.setContainerID(u.getIdentifier());
//	}
	
	/**
	 * Sets the unit's container unit.
	 * 
	 * @param newContainer the unit to contain this unit.
	 */
	public void setContainerUnit(Unit newContainer) {

		if (newContainer == null)
			// Set back to its previous container unit's coordinates
			setCoordinates(getContainerUnit().getCoordinates());
		else
			// Slave the coordinates to that of the newContainer
			setCoordinates(newContainer.getCoordinates());
		
		if (this instanceof Person || this instanceof Robot) {
			// NOTE: consider a robot in this case 
			updatePersonRobotState(newContainer);
		}
		else if (this instanceof Equipment) {
			updateEquipmentState(newContainer);
		}
		else if (this instanceof Vehicle) {
			updateVehicleState(newContainer);
		}
		else if (this instanceof Building) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		}
		else if (this instanceof Settlement) {
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		}
		else if (this instanceof MarsSurface) {
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
			// NOTE: this is needed since the surface of Mars is everywhere
			// e.g. EVASuit 001 is the container unit of John Wayne and it's located
			// at right outside the New Plymouth at (0, 0).
			// Set back to its previous container unit's coordinates
			setCoordinates(getContainerUnit().getCoordinates());
		}			
			
		// Set containerID
		if (newContainer == null || newContainer.getIdentifier() == Unit.UNKNOWN_ID) {
			containerID = Unit.UNKNOWN_ID;
		}
		
		else if (newContainer != null || newContainer.getIdentifier() != Unit.UNKNOWN_ID)
			containerID = newContainer.getIdentifier();
		
		fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
	}

	
	/**
	 * Map the new container unit to its location state type
	 * 
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	public LocationStateType map2NewState(Unit newContainer) {
		
		if (newContainer instanceof EVASuit)
			return LocationStateType.INSIDE_EVASUIT;

		else if (newContainer instanceof Settlement)
			return LocationStateType.INSIDE_SETTLEMENT;	
		
		else if (newContainer instanceof Vehicle)
			return LocationStateType.INSIDE_VEHICLE;

		else if (newContainer instanceof Person)
			return LocationStateType.ON_A_PERSON_OR_ROBOT;
		
//		else if (newContainer instanceof MarsSurface)
//			return LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
		
		return null;
	}
	
	/**
	 * Updates the location state type of a person or robot
	 * 
	 * @param newContainer
	 */
	public void updatePersonRobotState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN; 
			//logger.severe("updatePersonRobotState(): " + getName() + " has an null newContainer");
			return;
		}
		
		currentStateType = map2NewState(newContainer);
		
		if (currentStateType != null) {
			return;
		}
		// Case 4 : a person gets buried outside the settlement
		else if (newContainer instanceof MarsSurface) {
			if (tag.isInSettlementVicinity())
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;		
			else
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		}
		
		else
			currentStateType = newContainer.getLocationStateType();	
		
//		Unit oldContainer = getContainerUnit();
//		
//		// Case 0 : a person is inside a settlement airlock
//		if (oldContainer instanceof Settlement && newContainer instanceof Settlement)
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;	
//		
//		
//		// Case 1a : a person dons an EVA suit inside a settlement airlock
//		if (oldContainer instanceof Settlement && newContainer instanceof EVASuit)
//			currentStateType = LocationStateType.INSIDE_EVASUIT;
//
//		// Case 1b : a person takes off an EVA suit inside a settlement airlock
//		else if (oldContainer instanceof EVASuit && newContainer instanceof Settlement)
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;		
//
//		
//		// Case 2a a person dons an EVA suit inside a vehicle airlock
//		else if (oldContainer instanceof Vehicle && newContainer instanceof EVASuit)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.INSIDE_EVASUIT;
//		
//		// Case 2b a person takes off an EVA suit inside a vehicle airlock
//		else if (oldContainer instanceof EVASuit && newContainer instanceof Vehicle)
//			currentStateType = LocationStateType.INSIDE_VEHICLE;
//
//		
//		// Case 3a a person steps out of a vehicle parked inside a garage of a settlement
//		else if (oldContainer instanceof Vehicle && newContainer instanceof Settlement)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//
//		// Case 3b a person steps into a vehicle parked inside a garage of a settlement
//		else if (oldContainer instanceof Settlement && newContainer instanceof Vehicle)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.INSIDE_VEHICLE;
//		
//		// Case 4 : a person gets buried outside the settlement
//		else if (newContainer instanceof MarsSurface)
//			currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
	}

	/**
	 * Updates the location state type of an equipment
	 * 
	 * @param newContainer
	 */
	public void updateEquipmentState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN; 
//			logger.severe("updateEquipmentState(): " + getName() + " has an null newContainer");
			return;
		}
		
		currentStateType = map2NewState(newContainer);
		
		if (currentStateType != null) {
			return;
		}
		// Case 4 : a person gets buried outside the settlement
		else {
		
			if (tag.isInSettlementVicinity())
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;		
			else
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
			
//			Unit oldContainer = getContainerUnit();
//			
//			// Case 5a: an EVA suit leaves a settlement airlock and enters the surface of Mars
//			if (oldContainer instanceof Settlement && newContainer instanceof MarsSurface)
//				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
//			
//			// Case 6a: an EVA suit leaves a vehicle airlock and enters the surface of Mars
//			else if (oldContainer instanceof Vehicle && newContainer instanceof MarsSurface)
//				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
//				// TODO: could be OUTSIDE_SETTLEMENT_VICINITY
//			
//			else
//				currentStateType = newContainer.getLocationStateType();	
		}
			
//		
//		Unit oldContainer = getContainerUnit();
//		
//		// Case 1a : a person or robot picks it up inside a settlement
//		if (oldContainer instanceof Settlement 
//				&& (newContainer instanceof Robot || newContainer instanceof Person))
//			currentStateType = LocationStateType.ON_A_PERSON_OR_ROBOT;
//
//		// Case 1b : a person or robot drops it off inside a settlement
//		else if ((oldContainer instanceof Robot || newContainer instanceof Person)
//				&& newContainer instanceof Settlement)
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//
//		
//		// Case 2a : a person or robot picks it up inside a vehicle
//		else if (oldContainer instanceof Vehicle 
//				&& (newContainer instanceof Robot || newContainer instanceof Person))
//			currentStateType = LocationStateType.ON_A_PERSON_OR_ROBOT;
//
//		// Case 2b : a person or robot drops it off inside a vehicle
//		else if ((oldContainer instanceof Robot || newContainer instanceof Person)
//				&& newContainer instanceof Vehicle)
//			currentStateType = LocationStateType.INSIDE_VEHICLE;
//
//		
//		// Case 3a : a person dons an EVA suit inside a settlement
//		else if ((oldContainer instanceof Robot || newContainer instanceof Person)
//				&& newContainer instanceof Settlement)
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//		
//		// Case 3b : a person takes off an EVA suit inside a settlement
//		else if (oldContainer instanceof Settlement
//				&& (newContainer instanceof Robot || newContainer instanceof Person))
//			currentStateType = LocationStateType.ON_A_PERSON_OR_ROBOT;
//		
//		
//		// Case 4a : a person dons an EVA suit inside a vehicle
//		else if ((oldContainer instanceof Robot || newContainer instanceof Person)
//				&& newContainer instanceof Vehicle)
//			currentStateType = LocationStateType.INSIDE_VEHICLE;
//		
//		// Case 4b : a person takes off an EVA suit inside a vehicle
//		else if (oldContainer instanceof Vehicle
//				&& (newContainer instanceof Robot || newContainer instanceof Person))
//			currentStateType = LocationStateType.ON_A_PERSON_OR_ROBOT;
//		
//		
//		// Case 5a: an EVA suit leaves a settlement airlock and enters the surface of Mars
//		else if (oldContainer instanceof Settlement && newContainer instanceof MarsSurface)
//			currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
//		
//		// Case 5b: an EVA suit returns from the surface of Mars and enters a settlement airlock  
//		else if (oldContainer instanceof MarsSurface && newContainer instanceof Settlement)
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//		
//		
//		// Case 6a: an EVA suit leaves a vehicle airlock and enters the surface of Mars
//		else if (oldContainer instanceof Vehicle && newContainer instanceof MarsSurface)
//			currentStateType = LocationStateType.OUTSIDE_ON_MARS; // or OUTSIDE_SETTLEMENT_VICINITY;
//		
//		// Case 6b: an EVA suit returns from the surface of Mars and enters a vehicle airlock  
//		else if (oldContainer instanceof MarsSurface && newContainer instanceof Vehicle)
//			currentStateType = LocationStateType.INSIDE_VEHICLE;
	}

	/**
	 * Updates the location state type of a vehicle.
	 * 
	 * @apiNote (1) : OUTSIDE_SETTLEMENT_VICINITY is the intermediate state between being INSIDE_SETTLEMENT (in a garage) and being OUTSIDE_ON_MARS.
	 *
	 * @apiNote (2) : OUTSIDE_SETTLEMENT_VICINITY can be used by a person or a vehicle.
	 *
	 * @apiNote (3) : If a vehicle may be in a garage inside a building, this vehicle is INSIDE_SETTLEMENT.
	 *                If a vehicle is parked right outside a settlement, this vehicle is OUTSIDE_SETTLEMENT_VICINITY.
	 * 
	 * @param newContainer
	 */
	public void updateVehicleState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
//			logger.warning("updateVehicleState(): " + getName() + " has an null newContainer");
			return;
		}
		
		currentStateType = map2NewState(newContainer);
		
		if (currentStateType != null) {
			return;
		}
		else {
			if (tag.isInSettlementVicinity())
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;		
			else
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
			
//			Unit oldContainer = getContainerUnit();
//			
//			// Case 2a : a LUV is brought out of a vehicle onto the surface of Mars 
//			if (oldContainer instanceof Vehicle && newContainer instanceof MarsSurface)
//				// only if the vehicle is inside a garage can this happen
//				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
//			// TODO: could be on a construction mission and is OUTSIDE_SETTLEMENT_VICINITY
//			
//			// Case 3a : a vehicle leaves a settlement and embark on a mission outside on the surface of Mars 
//			else if (oldContainer instanceof Settlement && newContainer instanceof MarsSurface)
//				// only if the vehicle is inside a garage can this happen
//				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
//			
//			else
//				currentStateType = newContainer.getLocationStateType();	
		}
		
		
//		Unit oldContainer = getContainerUnit();
//		
//		// Case 1a : a LUV is brought out of a vehicle parked inside a garage of a settlement
//		if (oldContainer instanceof Vehicle && newContainer instanceof Settlement)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//
//		// Case 1b : a LUV is attached to a vehicle parked inside a garage of a settlement
//		else if (oldContainer instanceof Settlement && newContainer instanceof Vehicle)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.INSIDE_VEHICLE;
//		
//		
//		// Case 2a : a LUV is brought out of a vehicle onto the surface of Mars 
//		else if (oldContainer instanceof Vehicle && newContainer instanceof MarsSurface)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
//
//		// Case 2b : a LUV moves from the surface of Mars back to be attached to a vehicle 
//		else if (oldContainer instanceof MarsSurface && newContainer instanceof Vehicle)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.INSIDE_VEHICLE;
//
//
//		// Case 3a : a vehicle leaves a settlement and embark on a mission outside on the surface of Mars 
//		else if (oldContainer instanceof Settlement && newContainer instanceof MarsSurface)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
//
//		// Case 3b : a vehicle dismbark from a mission outside on the surface of Mars and enter a settlement 
//		else if (oldContainer instanceof MarsSurface && newContainer instanceof Settlement)
//			// only if the vehicle is inside a garage can this happen
//			currentStateType = LocationStateType.INSIDE_SETTLEMENT;

	}

	/**
	 * Gets the unit's mass including inventory mass.
	 * 
	 * @return mass of unit and inventory
	 * @throws Exception if error getting the mass.
	 */
	public double getMass() {
		return baseMass + inventory.getTotalInventoryMass(false);
	}

	/**
	 * Sets the unit's base mass.
	 * 
	 * @param baseMass mass (kg)
	 */
	protected final void setBaseMass(double baseMass) {
		this.baseMass = baseMass;
		fireUnitUpdate(UnitEventType.MASS_EVENT);
	}

	/**
	 * Gets the base mass of the unit.
	 * 
	 * @return base mass (kg).
	 */
	public double getBaseMass() {
		return baseMass;
	}

	/**
	 * String representation of this Unit.
	 * 
	 * @return The units name.
	 */
	@Override
	public String toString() {
		// return name + " (" + identifier + ")";
		return name;
	}

    public String getCode() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
    
	public synchronized boolean hasUnitListener(UnitListener listener) {
		if (listeners == null)
			return false;
		return listeners.contains(listener);
	}

	/**
	 * Adds a unit listener
	 * 
	 * @param newListener the listener to add.
	 */
	public synchronized final void addUnitListener(UnitListener newListener) {
		if (newListener == null)
			throw new IllegalArgumentException();
		if (listeners == null)
			listeners = Collections.synchronizedList(new ArrayList<UnitListener>());

		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
//		} else {
//			try {
//				throw new IllegalStateException(Msg.getString("Unit.log.alreadyContainsListener", //$NON-NLS-1$
//						newListener.getClass().getName(), newListener.toString()));
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.log(Level.SEVERE, Msg.getString("Unit.log.addingListenerDupe"), e); //$NON-NLS-1$
//			}
		}
	}

	/**
	 * Removes a unit listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public synchronized final void removeUnitListener(UnitListener oldListener) {
		if (oldListener == null)
			throw new IllegalArgumentException();

		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
		}
		if (listeners.size() < 1)
			return;
		listeners.remove(oldListener);
	}

	/**
	 * Fire a unit update event.
	 * 
	 * @param updateType the update type.
	 */
	public final void fireUnitUpdate(UnitEventType updateType) {
		fireUnitUpdate(updateType, null);
	}

	/**
	 * Fire a unit update event.
	 * 
	 * @param updateType the update type.
	 * @param target     the event target object or null if none.
	 */
	public final void fireUnitUpdate(UnitEventType updateType, Object target) {
		// logger.info("Unit's fireUnitUpdate() is on " +
		// Thread.currentThread().getName() + " Thread");

		if (listeners == null || listeners.size() < 1) {
			// listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
			// we don't do anything if there's no listeners attached
			return;
		}
		final UnitEvent ue = new UnitEvent(this, updateType, target);
		synchronized (listeners) {
			 Iterator<UnitListener> i = listeners.iterator();
				 while (i.hasNext()) {
				 i.next().unitUpdate(ue);
			 }
//			for (UnitListener u : listeners) {
//				u.unitUpdate(ue);
//			}
		}
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Unit o) {
		return name.compareToIgnoreCase(o.name);
	}

	
	// TODO: determine why equals() causes testInventoryFindAllUnitsGood() and 
	// testInventoryFindNumUnitsGood() in TestInventory to fail. 
	// TODO: how to tweak Inventory's storeUnit() to allow 
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (obj == null) return false;
//		if (this.getClass() != obj.getClass()) return false;
//		Unit u = (Unit) obj;
//		return this.name.equals(u.getName())
//				&& Math.abs(this.baseMass - u.getBaseMass()) < Double.MIN_NORMAL ;
//	}
	
//	public LocationSituation getLocationSituation() {
//		return null;
//	}

	public LocationStateType getLocationStateType() {
		return currentStateType;
	}

	public LocationTag getLocationTag() {
		return tag;
	}

	public Settlement getSettlement() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getSettlement();
		} 
		
		else if (this instanceof Person) {
			return ((Person) this).getSettlement();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getSettlement();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getSettlement();
		} 
		
		else
			return null;
	}

	public Building getBuildingLocation() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getBuildingLocation();
		} 
		
		else if (this instanceof Person) {
			return ((Person) this).getBuildingLocation();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getBuildingLocation();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getBuildingLocation();
		} 
		
		else
			return null;
	}

	public Settlement getAssociatedSettlement() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getAssociatedSettlement();
		} 
		
		else if (this instanceof Person) {
			return ((Person) this).getAssociatedSettlement();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getAssociatedSettlement();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getAssociatedSettlement();
		} 
		
		else
			return null;
	}

	public Vehicle getVehicle() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getVehicle();
		} 
		
		else if (this instanceof Person) {
			return ((Person) this).getVehicle();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getVehicle();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getVehicle();
		} 
		
		else
			return null;
	}

	/**
	 * Is this unit inside an environmentally enclosed breathable living space
	 * such as inside a settlement or a vehicle (NOT including in an EVA Suit)
	 * 
	 * @return true if the unit is inside a breathable environment
	 */
	public boolean isInside() {
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType
				|| LocationStateType.INSIDE_VEHICLE == currentStateType)
			return true;
		
		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
			return getContainerUnit().isInside();
		
		if (LocationStateType.ON_A_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInside();
		
		return false;
	}

	/**
	 * Is this unit outside on the surface of Mars, including wearing an EVA Suit 
	 * and being outside the settlement/building/vehicle
	 * 
	 * @return true if the unit is outside
	 */
	public boolean isOutside() {
		if (LocationStateType.OUTSIDE_ON_MARS == currentStateType
				|| LocationStateType.OUTSIDE_SETTLEMENT_VICINITY == currentStateType)
			return true;
		
		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
			return getContainerUnit().isOutside();
		
		if (LocationStateType.ON_A_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isOutside();
		
		return false;
	}
	
	/**
	 * Is this unit inside a vehicle
	 * 
	 * @return true if the unit is inside a vehicle
	 */
	public boolean isInVehicle() {
		if (LocationStateType.INSIDE_VEHICLE == currentStateType)
			return true;
		
		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
			return getContainerUnit().isInVehicle();
		
		if (LocationStateType.ON_A_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInVehicle();
		
		return false;
	}
	
	/**
	 * Is this unit inside a settlement
	 * 
	 * @return true if the unit is inside a settlement
	 */
	public boolean isInSettlement() {
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;
		
		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
			return getContainerUnit().isInSettlement();
		
		if (LocationStateType.ON_A_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInSettlement();
		
		return false;
	}
	
	/**
	 * Is this unit in a vehicle inside a garage
	 * 
	 * @return true if the unit is in a vehicle inside a garage
	 */
	public boolean isInVehicleInGarage() {
		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
			return getContainerUnit().isInVehicleInGarage();
		
		
//		if (containerID >= FIRST_VEHICLE_ID && containerID < FIRST_PERSON_ID) {
//			int vid = getContainerUnit().getContainerID();
//			
//			if (vid >= FIRST_SETTLEMENT_ID
//					&& vid < FIRST_VEHICLE_ID)
//				return true;
//		}
//		
//		return false;	
		
		if (getContainerUnit() instanceof Vehicle) {
			if (BuildingManager.getBuilding((Vehicle) getContainerUnit()) != null)
				// still inside the garage
				return true;
		}
		return false;
	}
	
//	/**
//	 * Is this unit inside a garage
//	 * 
//	 * @return true if the unit is inside a garage
//	 */
//	public boolean isInGarage() {
//		if (containerID >= FIRST_SETTLEMENT_ID
//				&& containerID < FIRST_VEHICLE_ID)
//			return true;
//		
//		int vid = getContainerUnit().getContainerID();
//		
//		if (vid >= FIRST_SETTLEMENT_ID
//				&& vid < FIRST_VEHICLE_ID)
//			return true;
//		
//		return false;		
//	}
	
	/**
	 * Loads instances
	 * 
	 * @param c0 {@link MasterClock}
	 * @param c1 {@link MarsClock}
	 * @param e {@link EarthClock}
	 * @param s {@link Simulation}
	 * @param m {@link Mars}
	 * @param ms {@link MarsSurface}
	 * @param w {@link Weather}
	 * @param u {@link UnitManager}
	 * @param mm {@link MissionManager}
	 */
	public static void initializeInstances(MasterClock c0, MarsClock c1, EarthClock e, Simulation s, 
			Mars m, MarsSurface ms, Weather w, MissionManager mm) {
		masterClock = c0;
		marsClock = c1;
		earthClock = e;
		sim = s;
		mars = m;
		marsSurface = ms;
		weather = w;
//		unitManager = u;
		missionManager = mm;
		
		surface = mars.getSurfaceFeatures();
	}
	
	public static void setUnitManager(UnitManager u) {
		unitManager = u;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		location = null;
		name = null;
		description = null;
		inventory.destroy();
		inventory = null;
//		containerUnit = null;
		// if (listeners != null) listeners.clear();
		listeners = null;
	}

}