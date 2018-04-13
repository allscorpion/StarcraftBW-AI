package structures;

import bwapi.UnitType;
import helpers.BaseManager;
import helpers.BuildingsManager;
import helpers.ResourcesManager;
import interfaces.IStructure;

public class Terran_Barracks implements IStructure {

	@Override
	public boolean RequirementsMetToBuild() {
		return BaseManager.GetTotalAmountOfCommandCenters() > 1 && ResourcesManager.getCurrentMineralsIncludingMilitary() >= UnitType.Terran_Barracks.mineralPrice() && BuildingsManager.BarracksCount < BaseManager.GetTotalAmountOfCommandCenters() * 2.5;
	}

	@Override
	public void OnSuccess() {
		BuildingsManager.BarracksCount++;
		ResourcesManager.MilitaryMineralUnitCost += 50;
	}

}
