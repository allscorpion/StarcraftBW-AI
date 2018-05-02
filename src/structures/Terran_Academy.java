package structures;

import bwapi.Unit;
import bwapi.UnitType;
import helpers.BaseManager;
import helpers.BuildingsManager;
import helpers.ConstructionManager;
import helpers.StarCraftInstance;
import interfaces.IStructure;
import models.Worker;

public class Terran_Academy implements IStructure {

	@Override
	public boolean RequirementsMetToBuild() {
		return  BaseManager.GetTotalAmountOfCommandCenters() > 1 && 
				BuildingsManager.amountOfBuildingTypeReserved(UnitType.Terran_Academy) == 0 &&
				StarCraftInstance.game.canMake(UnitType.Terran_Academy) &&
				BuildingsManager.Academy == null;
	}

	@Override
	public void OnSuccess(Worker worker) {
		BuildingsManager.Academy = worker.unit;
	}

}
