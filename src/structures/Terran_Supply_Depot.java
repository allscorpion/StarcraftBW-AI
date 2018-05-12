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
		return BuildingsManager.amountOfBuildingTypeReserved(UnitType.Terran_Supply_Depot) == 0
		&& ResourcesManager.isDepoRequired();
	}

	@Override
	public boolean Greedy_RequirementsMetToBuild() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean Military_RequirementsMetToBuild() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean Balanced_RequirementsMetToBuild() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void OnSuccess(Worker worker) {
		
	}

}
