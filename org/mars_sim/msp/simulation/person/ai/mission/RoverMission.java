/**
 * Mars Simulation Project
 * RoverMission.java
 * @version 2.80 2006-09-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.Map;

import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.simulation.person.ai.task.LoadVehicle;
import org.mars_sim.msp.simulation.person.ai.task.OperateVehicle;
import org.mars_sim.msp.simulation.person.ai.task.UnloadVehicle;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.simulation.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * A mission that involves driving a rover vehicle along a series of navpoints.
 */
public abstract class RoverMission extends VehicleMission {

	// Mission event types
	public static final String STARTING_SETTLEMENT_EVENT = "starting settlement";
	
	// Static members
	protected static final int MIN_PEOPLE = 2;
	
	// Data members
	private Settlement startingSettlement;

	/**
	 * Constructor
	 * @param name the name of the mission.
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error constructing mission.
	 */
	protected RoverMission(String name, Person startingPerson) throws MissionException {
		// Use VehicleMission constructor.
		super(name, startingPerson, MIN_PEOPLE);
	}
	
	/**
	 * Constructor with min people
	 * @param name the name of the mission.
	 * @param startingPerson the person starting the mission.
	 * @param minPeople the minimum number of people required for mission.
	 * @throws MissionException if error constructing mission.
	 */
	protected RoverMission(String name, Person startingPerson, int minPeople) throws MissionException { 
		// Use VehicleMission constructor.
		super(name, startingPerson, minPeople);
	}
	
	/**
	 * Constructor with min people and rover.
	 * @param name the name of the mission.
	 * @param startingPerson the person starting the mission.
	 * @param minPeople the minimum number of people required for mission.
	 * @param rover the rover to use on the mission.
	 * @throws MissionException if error constructing mission.
	 */
	protected RoverMission(String name, Person startingPerson, int minPeople, 
			Rover rover) throws MissionException {
		// Use VehicleMission constructor.
		super(name, startingPerson, minPeople, rover);
	}
	
	/**
	 * Gets the mission's rover if there is one.
	 * @return vehicle or null if none.
	 */
	public final Rover getRover() {
		return (Rover) getVehicle();
	}
	
	/**
	 * Sets the starting settlement.
	 * @param startingSettlement the new starting settlement
	 */
    protected final void setStartingSettlement(Settlement startingSettlement) {
    	this.startingSettlement = startingSettlement;
    	fireMissionUpdate(STARTING_SETTLEMENT_EVENT);
    }
    
    /**
     * Gets the starting settlement.
     * @return starting settlement
     */
    public final Settlement getStartingSettlement() {
    	return startingSettlement;
    }
	
    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     * @throws MissionException if problem performing the phase.
     */
    protected void performPhase(Person person) throws MissionException {
    	// if (hasEmergency()) setEmergencyDestination(true);
    	super.performPhase(person);
    }
    
	/**
	 * Gets the available vehicle at the settlement with the greatest range.
	 * @param settlement the settlement to check.
	 * @return vehicle or null if none available.
	 * @throws Exception if error finding vehicles.
	 */
	protected final static Vehicle getVehicleWithGreatestRange(Settlement settlement) throws Exception {
		Vehicle result = null;

		VehicleIterator i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			
			boolean usable = true;
			if (vehicle.isReserved()) usable = false;
			if (!vehicle.getStatus().equals(Vehicle.PARKED)) usable = false;
			if (vehicle.getInventory().getTotalInventoryMass() > 0D) usable = false;
			if (!(vehicle instanceof Rover)) usable = false;
			
			if (usable) {
				if (result == null) result = vehicle;
				else if (vehicle.getRange() > result.getRange()) result = vehicle;
			}
		}
		
		return result;
	}
    
	/**
	 * Checks to see if any vehicles are available at a settlement.
	 * @param settlement the settlement to check.
	 * @return true if vehicles are available.
	 */
	protected static boolean areVehiclesAvailable(Settlement settlement) {
		
		boolean result = false;
		
		VehicleIterator i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			
			boolean usable = true;
			if (vehicle.isReserved()) usable = false;
			if (!vehicle.getStatus().equals(Vehicle.PARKED)) usable = false;
			if (!(vehicle instanceof Rover)) usable = false;
			
			try {
				if (vehicle.getInventory().getTotalInventoryMass() > 0D) usable = false;
			}
			catch (InventoryException e) {
				e.printStackTrace(System.err);
			}
			
			if (usable) result = true;    
		}
		
		return result;
	}
	
	/**
	 * Checks if vehicle is usable for this mission.
	 * (This method should be overridden by children)
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws Exception if problem determining if vehicle is usable.
	 */
	protected boolean isUsableVehicle(Vehicle newVehicle) throws Exception {
		boolean usable = super.isUsableVehicle(newVehicle);
		if (!(newVehicle instanceof Rover)) usable = false;
		return usable;
	}
	
    /**
     * Checks that everyone in the mission is aboard the rover.
     * @return true if everyone is aboard
     */
    protected final boolean isEveryoneInRover() {
        boolean result = true;
        PersonIterator i = getPeople().iterator();
        while (i.hasNext()) {
            if (!i.next().getLocationSituation().equals(Person.INVEHICLE)) result = false;
        }
        return result;
    }
    
    /**
     * Checks that no one in the mission is aboard the rover.
     * @return true if no one is aboard
     */
    protected final boolean isNoOneInRover() {
    	boolean result = true;
        PersonIterator i = getPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getLocationSituation().equals(Person.INVEHICLE)) result = false;
        }
        return result;
    }
    
    /**
     * Checks if the rover is currently in a garage or not.
     * @return true if rover is in a garage.
     */
    protected boolean isRoverInAGarage() {
    	return (BuildingManager.getBuilding(getVehicle()) != null);
    }
    
    /** 
     * Performs the embark from settlement phase of the mission.
     * @param person the person currently performing the mission
     * @throws MissionException if error performing phase.
     */ 
    protected void performEmbarkFromSettlementPhase(Person person) throws MissionException {
    	
    	try {
    		Settlement settlement = getVehicle().getSettlement();
    		if (settlement == null) 
    			throw new MissionException(VehicleMission.EMBARKING, "Vehicle is not at a settlement.");
    	
    		// Add the rover to a garage if possible.
    		if (BuildingManager.getBuilding(getVehicle()) != null) {
    			try {
    				BuildingManager.addToRandomBuilding((Rover) getVehicle(), getVehicle().getSettlement());
    			}
    			catch (BuildingException e) {}
    		}
    	
    		// Load vehicle if not fully loaded.
    		if (!loadedFlag) {
    			if (isVehicleLoaded()) loadedFlag = true;
    			else {
    				// Check if vehicle can hold enough supplies for mission.
    				if (isVehicleLoadable()) {
    					// Load rover
    					// Random chance of having person load (this allows person to do other things sometimes)
    					if (RandomUtil.lessThanRandPercent(50)) { 
    						assignTask(person, new LoadVehicle(person, getVehicle(), getResourcesNeededForRemainingMission(true), 
    								getEquipmentNeededForRemainingMission(true)));
    					}
        			}
    				else endMission("Vehicle is not loadable (RoverMission).");
    			}
    		}
    		else {
    			// If person is not aboard the rover, board rover.
    			if (!person.getLocationSituation().equals(Person.INVEHICLE) && !person.getLocationSituation().equals(Person.BURIED)) {
    				if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
    					try {
    						settlement.getInventory().retrieveUnit(person);
    					}
    					catch (InventoryException e) {}
    				}
    				getVehicle().getInventory().storeUnit(person);
            	}
    			
    			// If rover is loaded and everyone is aboard, embark from settlement.
        		if (isEveryoneInRover()) {
        			
        			// Remove from garage if in garage.
        			Building garageBuilding = BuildingManager.getBuilding(getVehicle());
        			if (garageBuilding != null) {
        				VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
        				garage.removeVehicle(getVehicle());
        			}
        			
        			// Embark from settlement
        			settlement.getInventory().retrieveUnit(getVehicle());
        			setPhaseEnded(true);
        		}
    		}
    	}
    	catch (Exception e) {
    		throw new MissionException(VehicleMission.EMBARKING, e);
    	}
    }
    
    /**
     * Performs the disembark to settlement phase of the mission.
     * @param person the person currently performing the mission.
     * @param disembarkSettlement the settlement to be disembarked to.
     * @throws MissionException if error performing phase.
     */
    protected void performDisembarkToSettlementPhase(Person person, Settlement disembarkSettlement) 
    		throws MissionException {
    	
        Building garageBuilding = null;
        VehicleMaintenance garage = null;
    	
    	// If rover is not parked at settlement, park it.
        if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {
    		try {
    			disembarkSettlement.getInventory().storeUnit(getVehicle());
    		}
    		catch (InventoryException e) {
    			throw new MissionException(VehicleMission.DISEMBARKING, e);
    		}
    		
    		// Add vehicle to a garage if available.
    		try {
    			BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(), disembarkSettlement);
                garageBuilding = BuildingManager.getBuilding(getVehicle());
                garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
            }
            catch (BuildingException e) {}
    	}
    	
        // Have person exit rover if necessary.
        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
        	try {
        		getVehicle().getInventory().retrieveUnit(person);
        		disembarkSettlement.getInventory().storeUnit(person);
        	}
        	catch (InventoryException e) {
        		throw new MissionException(VehicleMission.DISEMBARKING, e);
        	}
            
            // Add the person to the rover's garage if it's in one.
            // Otherwise add person to another building in the settlement.
            try {
            	garageBuilding = BuildingManager.getBuilding(getVehicle());
                if (isRoverInAGarage() && garageBuilding.hasFunction(LifeSupport.NAME)) {
                	LifeSupport lifeSupport = (LifeSupport) garageBuilding.getFunction(LifeSupport.NAME);
                	lifeSupport.addPerson(person);
                }
                else BuildingManager.addToRandomBuilding(person, disembarkSettlement);
            }
            catch (BuildingException e) {
            	throw new MissionException(VehicleMission.DISEMBARKING, e);
            } 
        }
        
        // If any people are aboard the rover who aren't mission members, carry them into the settlement.
        Rover rover = (Rover) getVehicle();
        if (rover != null) {
        	if (isNoOneInRover() && (rover.getCrewNum() > 0)) {
        		PersonIterator i = rover.getCrew().iterator();
        		while (i.hasNext()) {
        			Person crewmember = i.next();
        			try {
        				rover.getInventory().retrieveUnit(crewmember);
        				disembarkSettlement.getInventory().storeUnit(crewmember);
        				BuildingManager.addToRandomBuilding(crewmember, disembarkSettlement);
        			}
        			catch (Exception e) {
        				throw new MissionException(VehicleMission.DISEMBARKING, e);
        			}
        		}
        	}
        
        	//	Unload rover if necessary.
        	try {
        		boolean roverUnloaded = UnloadVehicle.isFullyUnloaded(rover);
        		if (!roverUnloaded) {
        			// Random chance of having person unload (this allows person to do other things sometimes)
        			if (RandomUtil.lessThanRandPercent(50)) {
        				assignTask(person, new UnloadVehicle(person, rover));
        				return;
        			}
        		}
        	}
        	catch (Exception e) {
        		throw new MissionException(VehicleMission.DISEMBARKING, e);
        	}
        
        	// If everyone has left the rover, end the phase.
        	if (isNoOneInRover()) {
        	
        		// If the rover is in a garage, put the rover outside.
        		if (isRoverInAGarage()) {
        			try {
        				garageBuilding = BuildingManager.getBuilding(getVehicle());
        				garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
        				garage.removeVehicle(getVehicle());
        			}
        			catch (BuildingException e) {}
        		}
        	
        		// Leave the vehicle.
        		leaveVehicle();
        		setPhaseEnded(true);
        	}
        }
        else {
        	setPhaseEnded(true);
        }
    }
    
    /**
     * Gets a new instance of an OperateVehicle task for the person.
     * @param person the person operating the vehicle.
     * @return an OperateVehicle task for the person.
     * @throws Exception if error creating OperateVehicle task.
     */
    protected OperateVehicle getOperateVehicleTask(Person person, String lastOperateVehicleTaskPhase) 
    		throws Exception {
    	OperateVehicle result = null;
    	if (lastOperateVehicleTaskPhase != null) {
    		result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(), 
    				getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
    	}
    	else {
    		result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
    				getCurrentLegStartingTime(), getCurrentLegDistance());
    	}
    	
    	return result;
    }
    
	/**
	 * Checks to see if at least one inhabitant a settlement is remaining there.
	 * @param settlement the settlement to check.
	 * @param person the person checking
	 * @return true if at least one person left at settlement.
	 */
	protected static boolean atLeastOnePersonRemainingAtSettlement(Settlement settlement, Person person) {
		boolean result = false;
		
		if (settlement != null) {
			PersonIterator i = settlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if ((inhabitant != person) && !inhabitant.getMind().hasActiveMission()) result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Checks to see if at least a minimum number of people are available for a mission at a settlement.
	 * @param settlement the settlement to check.
	 * @param minNum minimum number of people required.
	 * @return true if minimum people available.
	 */
	protected static boolean minAvailablePeopleAtSettlement(Settlement settlement, int minNum) {
		boolean result = false;
		
		if (settlement != null) {
			int numAvailable = 0;
			PersonIterator i = settlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission()) numAvailable++;
			}
			if (numAvailable >= minNum) result = true;
		}
		
		return result;
	}
	
	/**
	 * Checks if there is only one person at the associated settlement and he/she has a serious medical problem.
	 * @return true if serious medical problem
	 */
	protected final boolean hasDangerousMedicalProblemAtAssociatedSettlement() {
		boolean result = false;
		if (getAssociatedSettlement() != null) {
			if (getAssociatedSettlement().getCurrentPopulationNum() == 1) {
				Person person = (Person) getAssociatedSettlement().getInhabitants().get(0);
				if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = true;
			}
		}
		return result;
	}
	
	/**
	 * Checks if the mission has an emergency situation.
	 * @return true if emergency.
	 */
	protected final boolean hasEmergency() {
		boolean result = super.hasEmergency();
		if (hasDangerousMedicalProblemAtAssociatedSettlement()) result = true;
		return result;
	}
    
	/**
	 * Gets a map of all resources needed for the trip.
	 * @param useBuffer should a buffer be used when determining resources?
	 * @param distance the distance of the trip.
	 */
    public Map<Resource, Number> getResourcesNeededForTrip(boolean useBuffer, double distance) throws Exception {
    	Map<Resource, Number> result = super.getResourcesNeededForTrip(useBuffer, distance);
    	
    	// Determine estimate time for trip.
    	double time = getEstimatedTripTime(useBuffer, distance);
    	double timeSols = time / 1000D;
    	
    	int crewNum = getPeopleNumber();
    	
    	// Determine life support supplies needed for trip.
    	double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum;
    	if (useBuffer) oxygenAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	result.put(AmountResource.OXYGEN, new Double(oxygenAmount));
    		
    	double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum;
    	if (useBuffer) waterAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	result.put(AmountResource.WATER, new Double(waterAmount));
    		
    	double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum;
    	if (useBuffer) foodAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	result.put(AmountResource.FOOD, new Double(foodAmount));
    	
    	return result;
    }
    
    /**
     * Gets the number and types of equipment needed for the mission.
     * @param useBuffer use time buffers in estimation if true.
     * @return map of equipment class and Integer number.
     * @throws Exception if error determining needed equipment.
     */
    public abstract Map<Class, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) throws Exception;
}