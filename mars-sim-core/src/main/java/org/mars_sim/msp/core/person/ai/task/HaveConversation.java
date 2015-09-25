/**
 * Mars Simulation Project
 * HaveConversation.java
 * @version 3.08 2015-09-24
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Preference;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.meta.TaskProbabilityUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Communication;

/**
 * The HaveConversation class is the task of having a casual conversation with another person
 */
public class HaveConversation
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.haveConversation"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase HAVING_CONVERSATION = new TaskPhase(Msg.getString(
            "Task.phase.havingConversation")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.1D;

    //private int randomTime;

    /**
     * Constructor. This is an effort-driven task.
     * @param person the person performing the task.
     */
    public HaveConversation(Person person) {
        // Use Task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER, true, 5D);

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check if there is a local dining building.
            Building diningBuilding = EatMeal.getAvailableDiningBuilding(person);
            if (diningBuilding != null)
            	// Walk to that building.
            	walkToActivitySpotInBuilding(diningBuilding, BuildingFunction.DINING, true);

        }
        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

            int time = person.getPreference().getPreferenceScore(Preference.convertTask2MetaTask(this));
            super.setDuration(5 + time);

	        // set the boolean to true so that it won't be done again today
        	//person.getPreference().setTaskStatus(this, false);

        }
        else {
            endTask();
        }

        // Initialize phase
        addPhase(HAVING_CONVERSATION);
        setPhase(HAVING_CONVERSATION);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (HAVING_CONVERSATION.equals(getPhase())) {
            return havingConversation(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs reading phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double havingConversation(double time) {
        return 0D;
    }

    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void endTask() {
        super.endTask();
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

    }
}