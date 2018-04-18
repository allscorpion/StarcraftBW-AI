package structures;

import bwapi.UnitType;
import helpers.BaseManager;
import helpers.BuildingsManager;
import helpers.ConstructionManager;
import helpers.ResourcesManager;
import interfaces.IStructure;

public class Terran_Barracks implements IStructure {

	@Override
	public boolean RequirementsMetToBuild() {
		return //BaseManager.GetTotalAmountOfCommandCenters() > 1 && 
				ConstructionManager.CheckIfWeHaveResourcesToBuild(UnitType.Terran_Barracks) && 
				BuildingsManager.BarracksCount < BaseManager.GetTotalAmountOfCommandCenters() * 5;
	}

	@Override
	public void OnSuccess() {
		BuildingsManager.BarracksCount++;
		ResourcesManager.MilitaryMineralUnitCost += 50;
	}

}
