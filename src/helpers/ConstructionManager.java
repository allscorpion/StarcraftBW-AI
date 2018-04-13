package helpers;

import bwapi.UnitType;
import interfaces.IStructure;
import models.Building;
import models.Worker;

public class ConstructionManager {
	public static void ConstructBuilding(UnitType buildingType) {
		Worker worker = WorkersManager.GetWorker();	
		try {
			Class<?> structureClass = Class.forName("structures." + buildingType.toString());
			IStructure structure = (IStructure)structureClass.newInstance();
			if (structure != null) {
				if (structure.RequirementsMetToBuild()) {
					BuildingsManager.BuildingsUnderConstruction.add(new Building(worker, buildingType));	
					structure.OnSuccess();
				}
			}
			
		} catch (ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			StarCraftInstance.game.printf(e.toString());
		}
	}
}
