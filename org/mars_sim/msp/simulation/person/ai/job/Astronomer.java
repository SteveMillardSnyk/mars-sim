/**
 * Mars Simulation Project
 * Astronomer.java
 * @version 2.87 2009-06-26
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.simulation.structure.building.function.Research;

/** 
 * The Astronomer class represents a job for an astronomer.
 */
public class Astronomer extends Job implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.job.Astronomer";
    
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor
     */
    public Astronomer() {
        // Use Job constructor
        super("Astronomer");
        
        // Add astronomer-related tasks.
        
        // Add astronomer-related missions.
        jobMissionStarts.add(TravelToSettlement.class);
        jobMissionJoins.add(TravelToSettlement.class);  
        jobMissionStarts.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
    }
    
    @Override
    public double getCapability(Person person) {
        double result = 0D;
        
        int astronomySkill = person.getMind().getSkillManager().getSkillLevel(Skill.ASTRONOMY);
        result = astronomySkill;
        
        NaturalAttributeManager attributes = person.getNaturalAttributeManager();
        int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
        result+= result * ((academicAptitude - 50D) / 100D);
        
        if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;
        
        return result;
    }

    @Override
    public double getSettlementNeed(Settlement settlement) {
        double result = 0D;
        
        BuildingManager manager = settlement.getBuildingManager();
        
        // Add (labspace * tech level) for all labs with astronomy specialities.
        Iterator<Building> i = manager.getBuildings(Research.NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            try {
                Research lab = (Research) building.getFunction(Research.NAME);
                if (lab.hasSpeciality(Skill.ASTRONOMY)) 
                    result += lab.getLaboratorySize() * lab.getTechnologyLevel();
            }
            catch (BuildingException e) {
                logger.log(Level.SEVERE,"getSettlementNeeded(): e.getMessage()", e);
            }
        }
        
        // Add astronomical observatories (observer capacity * tech level * 2).
        Iterator<Building> j = manager.getBuildings(AstronomicalObservation.NAME).iterator();
        while (j.hasNext()) {
            Building building = j.next();
            try {
                AstronomicalObservation observatory = (AstronomicalObservation) 
                        building.getFunction(AstronomicalObservation.NAME);
                result += observatory.getObservatoryCapacity() * observatory.getTechnologyLevel() * 2D;
            }
            catch (BuildingException e) {
                logger.log(Level.SEVERE,"getSettlementNeeded(): e.getMessage()", e);
            }
        }
        
        result *= 5D;
        
        return result;  
    }
}