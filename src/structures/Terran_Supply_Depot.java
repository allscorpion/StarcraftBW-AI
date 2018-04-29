package structures;

import bwapi.UnitType;
import helpers.BuildingsManager;
import helpers.ConstructionManager;
import helpers.ResourcesManager;
import interfaces.IStructure;
import models.Worker;

public class Terran_Supply_Depot implements IStructure {

	@Override
	public boolean RequirementsMetToBuild() {
		return !BuildingsManager.isBuildingTypeReserved(UnitType.Terran_Supply_Depot)
		&& ResourcesManager.isDepoRequired();
	}

	@Override
	public void OnSuccess(Worker worker) {
		
	}

}
