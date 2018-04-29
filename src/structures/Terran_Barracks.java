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
		return  BaseManager.GetTotalAmountOfCommandCenters() > 1 && 
				ConstructionManager.CheckIfWeHaveResourcesToBuildIncludingTravelTime(UnitType.Terran_Barracks) && 
				BuildingsManager.BarracksCount < BaseManager.GetTotalAmountOfCommandCenters() * 2.5;
	}

	@Override
	public void OnSuccess(Worker worker) {
		BuildingsManager.BarracksCount++;
		ResourcesManager.MilitaryMineralUnitCost += 50;
	}

}
