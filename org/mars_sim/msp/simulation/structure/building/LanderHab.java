/**
 * Mars Simulation Project
 * LanderHab.java
 * @version 2.75 2003-06-19
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.medical.HealthProblem;
import org.mars_sim.msp.simulation.structure.building.function.Communication;
import org.mars_sim.msp.simulation.structure.building.function.Dining;
import org.mars_sim.msp.simulation.structure.building.function.EVA;
import org.mars_sim.msp.simulation.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
import org.mars_sim.msp.simulation.structure.building.function.PowerGeneration;
import org.mars_sim.msp.simulation.structure.building.function.Recreation;
import org.mars_sim.msp.simulation.structure.building.function.Research;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.simulation.structure.building.function.Storage;
import org.mars_sim.msp.simulation.structure.building.function.impl.SolarPowerGeneration;
import org.mars_sim.msp.simulation.structure.building.function.impl.StandardLivingAccommodations;
import org.mars_sim.msp.simulation.structure.building.function.impl.StandardMedicalCare;
import org.mars_sim.msp.simulation.structure.building.function.impl.StandardResearch;
import org.mars_sim.msp.simulation.structure.building.function.impl.StandardResourceProcessing;

/**
 * The LanderHab class represents a lander habitat building from a Mars Direct mission.
 * It has water recycling and carbon scrubbing processes.
 * It also has a 5Kw solar cell.
 */
public class LanderHab extends InhabitableBuilding 
        implements LivingAccommodations, Research, Communication, EVA, 
        Recreation, Dining, ResourceProcessing, Storage, PowerGeneration, MedicalCare {
    
    // Number of people the hab can accommodate at once.
    private final static int ACCOMMODATION_CAPACITY = 4;
    
    // Power down level for processes.
    private final static double POWER_DOWN_LEVEL = .5D;
    
    // Base power (kWe) generated by the lander hab's solar cell.
    private static final double BASE_POWER_GENERATION = 5D;
    
    // Laboratory info.
    private static final int LAB_TECH_LEVEL = 1;
    private static final String[] LAB_SPECIALITIES = {"Areology"};
    private static final int RESEARCHER_NUM = 4;
    
    // Infirmary info.
    private static final int INFIRMARY_BEDS = 2;
    private static final int INFIRMARY_TREATMENT_LEVEL = 4;
    
    private Map resourceStorageCapacity;
    private Research lab;
    private MedicalCare infirmary;
    private LivingAccommodations quarters;
    private PowerGeneration solarCells;
    private ResourceProcessing processor;
    private Airlock airlock;
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public LanderHab(BuildingManager manager) {
        // Use InhabitableBulding constructor
        super("Lander Hab", manager, ACCOMMODATION_CAPACITY);
        
        // Create lab
        lab = new StandardResearch(this, LAB_TECH_LEVEL, LAB_SPECIALITIES, RESEARCHER_NUM);
        
        // Create infirmary
        infirmary = new StandardMedicalCare(this, INFIRMARY_BEDS, INFIRMARY_TREATMENT_LEVEL);
        
        // Create quarters
        quarters = new StandardLivingAccommodations(this, ACCOMMODATION_CAPACITY);
        
        // Create solar cells
        solarCells = new SolarPowerGeneration(this, BASE_POWER_GENERATION);
        
        // Create airlock
        airlock = new BuildingAirlock(this, 4);
        
        // Create processor
        processor = new StandardResourceProcessing(this, POWER_DOWN_LEVEL);
        ResourceProcessManager processManager = processor.getResourceProcessManager();
        
        Inventory inv = getInventory();
        
        // Create water recycling process
        ResourceProcess waterRecycling = new ResourceProcess("Water Recycling", inv);
        waterRecycling.addMaxInputResourceRate(Resource.WASTE_WATER, .0002D, false);
        waterRecycling.addMaxOutputResourceRate(Resource.WATER, .00017D, false);
        processManager.addResourceProcess(waterRecycling);
        
        // Create carbon scrubbing process
        ResourceProcess carbonScrubbing = new ResourceProcess("Carbon Scrubbing", inv);
        carbonScrubbing.addMaxInputResourceRate(Resource.CARBON_DIOXIDE, .000067D, false);
        carbonScrubbing.addMaxOutputResourceRate(Resource.OXYGEN, .00005D, false);
        processManager.addResourceProcess(carbonScrubbing);
        
        // Set up resource storage capacity map.
        resourceStorageCapacity = new HashMap();
        resourceStorageCapacity.put(Resource.OXYGEN, new Double(1000D));
        resourceStorageCapacity.put(Resource.WATER, new Double(5000D));
        resourceStorageCapacity.put(Resource.WASTE_WATER, new Double(500D));
        resourceStorageCapacity.put(Resource.CARBON_DIOXIDE, new Double(500D));
        resourceStorageCapacity.put(Resource.FOOD, new Double(1000D));
        resourceStorageCapacity.put(Resource.ROCK_SAMPLES, new Double(1000D));
        
        // Add resource storage capacity to settlement inventory.
        Iterator i = resourceStorageCapacity.keySet().iterator();
        while (i.hasNext()) {
            String resourceName = (String) i.next();
            double capacity = ((Double) resourceStorageCapacity.get(resourceName)).doubleValue();
            inv.setResourceCapacity(resourceName, inv.getResourceCapacity(resourceName) + capacity);
        }
        
        // Initial resources in lander hab
        inv.addResource(Resource.WATER, 500D);
        inv.addResource(Resource.OXYGEN, 500D);
        inv.addResource(Resource.FOOD, 500D);
        
        // Add scope string to malfunction manager.
        MalfunctionManager malfunctionManager = getMalfunctionManager();
        malfunctionManager.addScopeString("Living Accommodations");
        malfunctionManager.addScopeString("Research");
        malfunctionManager.addScopeString("Communication");
        malfunctionManager.addScopeString("EVA");
        malfunctionManager.addScopeString("Recreation");
        malfunctionManager.addScopeString("Dining");
        malfunctionManager.addScopeString("Resource Processing");
        malfunctionManager.addScopeString("Storage");
        malfunctionManager.addScopeString("Power Generation");
        malfunctionManager.addScopeString("Inhabitable Building");
        malfunctionManager.addScopeString("Lander Hab");
    }
    
    /**
     * Gets the accommodation capacity of this building.
     *
     * @return number of accomodations.
     */
    public int getAccommodationCapacity() {
        return quarters.getAccommodationCapacity();
    }
    
    /** 
     * Utilizes water for bathing, washing, etc. based on population.
     * @param time amount of time passing (millisols)
     */
    public void waterUsage(double time) {
        quarters.waterUsage(time);   
    }
    
    /**
     * Gets the power this building currently requires for full-power mode.
     * @return power in kW.
     */
    public double getFullPowerRequired() {
        return getLifeSupportPowerRequired() + 10D;
    }
    
    /**
     * Gets the building's resource process manager.
     * @return resource process manager
     */
    public ResourceProcessManager getResourceProcessManager() {
        return processor.getResourceProcessManager();
    }
    
    /**
     * Gets the power down mode resource processing level.
     * @return proportion of max processing rate (0D - 1D)
     */
    public double getPowerDownResourceProcessingLevel() {
        return processor.getPowerDownResourceProcessingLevel();
    }
    
    /** 
     * Gets a map of the resources this building is capable of
     * storing and their amounts in kg.
     * @return Map of resource keys and amount Double values.
     */
    public Map getResourceStorageCapacity() {
        return resourceStorageCapacity;
    }
    
    /**
     * Time passing for building.
     * Child building should override this method for things
     * that happen over time for the building.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        super.timePassing(time);
        
        // Utilize water.
        waterUsage(time);
        
        // Determine resource processing production level.
        double productionLevel = 0D;
        if (getPowerMode().equals(FULL_POWER)) productionLevel = 1D;
        else if (getPowerMode().equals(POWER_DOWN)) productionLevel = POWER_DOWN_LEVEL;
        
        // Process resources
        processor.getResourceProcessManager().processResources(time, productionLevel);
        
        // Add time to airlock
        airlock.timePassing(time);
    } 
    
    /**
     * Gets the amount of electrical power generated.
     * @return power generated in kW
     */
    public double getGeneratedPower() {
        return solarCells.getGeneratedPower();
    }
    
    /** 
     * Gets the laboratory size.
     * This is the number of researchers supportable at any given time. 
     * @return the size of the laboratory (in researchers). 
     */
    public int getLaboratorySize() {
        return lab.getLaboratorySize();
    }

    /** 
     * Gets the technology level of laboratory
     * (units defined later) 
     * @return the technology level of the laboratory 
     * (units defined later)
     */
    public int getTechnologyLevel() {
        return lab.getTechnologyLevel();
    }

    /** 
     * Gets the lab's science specialities as an array of Strings 
     * @return the lab's science specialities as an array of Strings
     */
    public String[] getTechSpecialities() {
        return lab.getTechSpecialities();
    }

    /**
     * Checks to see if the laboratory has a given tech speciality.
     * @return true if lab has tech speciality
     */
    public boolean hasSpeciality(String speciality) {
        return lab.hasSpeciality(speciality);
    }
    
    /**
     * Gets the number of people currently researching in the laboratory.
     * @return number of researchers
     */
    public int getResearcherNum() {
        return lab.getResearcherNum();
    }

    /**
     * Adds a researcher to the laboratory.
     * @throws Exception if person cannot be added.
     */
    public void addResearcher() throws Exception {
        lab.addResearcher();
    }

    /**
     * Removes a researcher from the laboratory.
     * @throws Exception if person cannot be removed.
     */
    public void removeResearcher() throws Exception {
        lab.removeResearcher();
    }
    
    /**
     * Gets the building's airlock.
     * @return airlock
     */
    public Airlock getAirlock() {
        return airlock;
    }
    
    /**
     * Gets the health problems awaiting treatment at the medical station.
     *
     * @return list of health problems
     */
    public List getProblemsAwaitingTreatment() {
        return infirmary.getProblemsAwaitingTreatment();
    }
    
    /**
     * Gets the health problems currently being treated at the medical station.
     *
     * @return list of health problems
     */
    public List getProblemsBeingTreated() {
        return infirmary.getProblemsBeingTreated();
    }
    
    /**
     * Get a list of supported Treatments at this medical aid.
     *
     * @return List of treatments.
     */
    public List getSupportedTreatments() {
        return infirmary.getSupportedTreatments();
    }
    
    /**
     * Checks if a health problem can be treated at this medical aid.
     *
     * @param problem The health problem to check treatment.
     * @return true if problem can be treated.
     */
    public boolean canTreatProblem(HealthProblem problem) {
        return infirmary.canTreatProblem(problem);
    }
    
    /**
     * Add a health problem to the queue of problems awaiting treatment at this
     * medical aid.
     *
     * @param problem The health problem to await treatment.
     * @throws Exception if health problem cannot be treated here.
     */
    public void requestTreatment(HealthProblem problem) throws Exception {
        infirmary.requestTreatment(problem);
    }

    /**
     * Starts the treatment of a health problem in the waiting queue.
     *
     * @param problem the health problem to start treating.
     * @param treatmentDuration the time required to perform the treatment.
     * @throws Exception if treatment cannot be started.
     */
    public void startTreatment(HealthProblem problem, double treatmentDuration) throws Exception {
        infirmary.startTreatment(problem, treatmentDuration);
    }
    
    /**
     * Stop a previously started treatment.
     *
     * @param problem Health problem stopping treatment on.
     * @throws Exception if health problem is not being treated.
     */
    public void stopTreatment(HealthProblem problem) throws Exception {
        infirmary.stopTreatment(problem);
    }
    
    /**
     * Gets the number of sick beds.
     *
     * @return Sick bed count.
     */
    public int getSickBedNum() {
        return infirmary.getSickBedNum();
    }
    
    /**
     * Gets the current number of people being treated here.
     *
     * @return Patient count.
     */
    public int getPatientNum() {
        return infirmary.getPatientNum();
    }
    
    /**
     * Gets the patients at this medical station.
     * @return Collection of People.
     */
    public PersonCollection getPatients() {
        return infirmary.getPatients();
    }
    
    /**
     * Gets the number of people using this medical aid to treat sick people.
     *
     * @return number of people
     */
    public int getPhysicianNum() {
        return infirmary.getPhysicianNum();
    }
}
