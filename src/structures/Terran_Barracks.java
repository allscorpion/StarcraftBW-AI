package structures;

import bwapi.UnitType;
import helpers.BaseManager;
import helpers.BuildingsManager;
import helpers.ConstructionManager;
import helpers.ResourcesManager;
import interfaces.IStructure;
import models.Worker;

public class Terran_Barracks implements IStructure {

	@Override
	public boolean RequirementsMetToBuild() {
		if (BaseManager.GetTotalAmountOfCommandCenters() < 2) {
			return false;
		}
		if (!BuildingsManager.areAllMilitaryBuildingsProducing()) {
			return false;
		}
		if (BuildingsManager.amountOfBuildingTypeReserved(UnitType.Terran_Barracks) > 2) {
			return false;
		}
		if (BaseManager.GetTotalAmountOfCommandCenters() < 3 && BuildingsManager.BarracksCount < BaseManager.GetTotalAmountOfCommandCenters() * 2.5) {
			return true;
		}else if (BaseManager.GetTotalAmountOfCommandCenters() >= 3) {
			return true;
		}
		return false;
	}

	@Override
	public void OnSuccess(Worker worker) {
		BuildingsManager.BarracksCount++;
		ResourcesManager.MilitaryMineralUnitCost += 50;
	}

}
