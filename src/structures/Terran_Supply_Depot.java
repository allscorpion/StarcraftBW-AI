package structures;

import bwapi.UnitType;
import helpers.ConstructionManager;
import helpers.ResourcesManager;
import interfaces.IStructure;

public class Terran_Supply_Depot implements IStructure {

	@Override
	public boolean RequirementsMetToBuild() {
		return ResourcesManager.isDepoRequired() && ConstructionManager.CheckIfWeHaveResourcesToBuild(UnitType.Terran_Supply_Depot);
	}

	@Override
	public void OnSuccess() {
		
	}

}
