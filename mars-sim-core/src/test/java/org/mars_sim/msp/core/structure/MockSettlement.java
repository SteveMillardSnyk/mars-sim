package org.mars_sim.msp.core.structure;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;

public class MockSettlement extends Settlement {

	/* default logger. */
	private static Logger logger = Logger.getLogger(MockSettlement.class.getName());

	private Simulation sim = Simulation.instance();
	/**
	 * Constructor
	 */
	public MockSettlement()  {
		// Use Settlement constructor.
		super("Mock Settlement", 0, new Coordinates(0, 0));
//      Settlement settlement = Settlement.createMockSettlement("Mock Settlement", 0, new Coordinates(0, 0));
		
		if (sim == null)
			logger.severe("sim is null");
		
		if (sim.getUnitManager() == null)
			logger.severe("unitManager is null");
			
		sim.getUnitManager().addUnit(this);
		
        // Set inventory total mass capacity.
		getInventory().addGeneralCapacity(Double.MAX_VALUE);

        // Initialize building manager
        buildingManager = new BuildingManager(this, true);
//        Building b = new MockBuilding(buildingManager);
//        buildingManager.addMockBuilding(b);

        // Initialize building connector manager.
        buildingConnectorManager = new BuildingConnectorManager(this,
                new ArrayList<BuildingTemplate>());

        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);

        // Initialize power grid
        powerGrid = new PowerGrid(this);

	}
}