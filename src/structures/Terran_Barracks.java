package structures;

import bwapi.UnitType;
import enums.PlayStyles;
import helpers.BaseManager;
import helpers.BuildingsManager;
import helpers.Commander;
import helpers.ConstructionManager;
import helpers.ResourcesManager;
import helpers.StarCraftInstance;
import interfaces.IStructure;
import models.Worker;

public class Terran_Barracks implements IStructure {

	@Override
	public boolean RequirementsMetToBuild() {
		if (!BuildingsManager.areAllMilitaryBuildingsProducing()) {
			return false;
		}
		if (BuildingsManager.amountOfBuildingTypeReserved(UnitType.Terran_Barracks) > 2) {
			return false;
		}
		return true;
	}

	@Override
	public boolean Greedy_RequirementsMetToBuild() {
		if (BaseManager.GetTotalAmountOfCommandCenters() < 2) {
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
	public boolean Military_RequirementsMetToBuild() {
		// TODO Auto-generated method stub
		if (BuildingsManager.BarracksCount < 3) {
			return true;
		}
		if (BuildingsManager.Academy != null && BuildingsManager.BarracksCount < 5) {
			return true;
		}	
		return false;
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
