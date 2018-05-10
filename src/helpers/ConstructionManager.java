package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import interfaces.IStructure;
import models.Building;
import models.CustomBaseLocation;
import models.Worker;
import structures.CacheStructure;

public class ConstructionManager {
	public static void Init() {
		cachedStructures = new ArrayList<CacheStructure>();
	}
	
	private static List<CacheStructure> cachedStructures;
	
	public static void StartConstructionQueue() {
		ConstructBuilding(UnitType.Terran_Supply_Depot);
		if (!ResourcesManager.isDepoRequired()) {
			BuildWorker();
			BuildGas();
			ConstructBuilding(UnitType.Terran_Academy);
			BuildBarracksUnit();
			//BuildBase();
			ConstructBuilding(UnitType.Terran_Barracks);	
		}
	}
	
	public static boolean CheckIfWeHaveResourcesToBuild(UnitType unitType) {
		return ResourcesManager.getCurrentMinerals() >= unitType.mineralPrice() && StarCraftInstance.self.gas() >= unitType.gasPrice();
	}
	
	public static boolean CheckIfWeHaveResourcesToBuildIncludingTravelTime(UnitType unitType) {
		return ResourcesManager.getCurrentMinerals() >= unitType.mineralPrice() && StarCraftInstance.self.gas() >= unitType.gasPrice();
	}
	
	private static void ConstructBuilding(UnitType buildingType) {
		try {
			CacheStructure cs = GetCacheStructure(buildingType);
			if (cs != null) {
				CheckStructure(cs.structureClass, buildingType);
			}
			Class<?> structureClass = Class.forName("structures." + buildingType.toString());
			IStructure structure = (IStructure)structureClass.newInstance();
			if (structure != null) {
				CacheStructure newCs = new CacheStructure(buildingType,structure);
				cachedStructures.add(newCs);
				CheckStructure(structure, buildingType);
			}
			
		} catch (ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException e) {
			StarCraftInstance.game.printf(e.toString());
		}
	}
	
	private static void BuildWorker() {
		for (CustomBaseLocation cbl : BaseManager.baseLocations) {
    		if (cbl.commandCenter != null) {
    			if (BuildingsManager.Academy != null && cbl.commandCenter.unit.getAddon() == null && StarCraftInstance.game.canMake(UnitType.Terran_Comsat_Station)) {
    				cbl.commandCenter.unit.buildAddon(UnitType.Terran_Comsat_Station);
    			}
    			// keep constant scv production if we can afford it
    			else if (CheckIfWeHaveResourcesToBuild(UnitType.Terran_SCV)) {
    				if (cbl.commandCenter.unit.getTrainingQueue().size() < 2 && WorkersManager.Workers.size() < BaseManager.TotalWorkersAllCommandCenters() - BaseManager.GetTotalAmountOfCommandCenters()) {
    					cbl.commandCenter.unit.train(UnitType.Terran_SCV);
    				}
                }
    		}
    	}
	}
	
	private static void BuildGas() {
    	for (CustomBaseLocation cbl : BaseManager.baseLocations) {
			if (cbl.commandCenter != null) {
				if (BuildingsManager.BarracksCount > 1 && cbl.baseLocation.getGeysers().size() > 0 && (BaseManager.GetAmountOfWorkersAssignedToCommandCenter(cbl) >= BaseManager.GetCommandCenterMaxWorkers(cbl) / 2) && !cbl.commandCenter.hasGasStructure && CheckIfWeHaveResourcesToBuild(UnitType.Terran_Refinery)) {
					cbl.commandCenter.hasGasStructure = true;
					TilePosition gasPosition = cbl.baseLocation.getGeysers().get(0).getTilePosition();
					BuildingsManager.BuildingsUnderConstruction.add(new Building(WorkersManager.GetWorker(gasPosition), UnitType.Terran_Refinery, gasPosition));
				}
			}
		}
	}
	
	private static void BuildBase() {
		boolean allowBaseCreation = false;
		if (BaseManager.GetTotalAmountOfCommandCenters() == 1) {
			if (ResourcesManager.PotentialSupply / 2 > 10 && BuildingsManager.amountOfBuildingTypeReserved(UnitType.Terran_Command_Center) == 0) {
				allowBaseCreation = true;
			}
		}else if (BaseManager.GetTotalAmountOfCommandCenters() == 2) {
			if (BuildingsManager.BarracksCount >= 5 && BuildingsManager.amountOfBuildingTypeReserved(UnitType.Terran_Command_Center) == 0) {
				allowBaseCreation = true;
			}
		} else if (BaseManager.GetTotalAmountOfCommandCenters() == 3) {
			if (BuildingsManager.BarracksCount >= 7.5 && BuildingsManager.amountOfBuildingTypeReserved(UnitType.Terran_Command_Center) == 0) {
				allowBaseCreation = true;
			}
		}
		if (allowBaseCreation) {
			BaseLocation bl = BuildingsManager.GetClosestEmptyBase();
			Worker worker = WorkersManager.GetWorker(bl.getTilePosition());
			if (bl != null && !BuildingsManager.isTileReserved(bl.getTilePosition(), UnitType.Terran_Command_Center)) {
				BuildingsManager.BuildingsUnderConstruction.add(new Building(worker, UnitType.Terran_Command_Center, bl.getTilePosition()));
				worker.miningFrom = null;
			}
		}
	}
	
	private static void BuildBarracksUnit() {
		int totalMarines = StarCraftInstance.self.allUnitCount(UnitType.Terran_Marine);
		int totalMedics = StarCraftInstance.self.allUnitCount(UnitType.Terran_Medic);
		for (Unit myUnit : BuildingsManager.MilitaryBuildings) {
			if (myUnit.getType() == UnitType.Terran_Barracks) {
				if ((totalMarines % 4 == 0 && totalMarines / 4 > totalMedics) && myUnit.canTrain(UnitType.Terran_Medic) && myUnit.getTrainingQueue().size() < 1 && CheckIfWeHaveResourcesToBuild(UnitType.Terran_Medic)) {
		            myUnit.train(UnitType.Terran_Medic);
		            totalMedics++;
		        }
				else if (myUnit.getTrainingQueue().size() < 1 && CheckIfWeHaveResourcesToBuild(UnitType.Terran_Marine)) {
		            myUnit.train(UnitType.Terran_Marine);
		            totalMarines++;
		        }
			}
		}
	}
	
	private static void CheckStructure(IStructure structure, UnitType buildingType) {	
		if (structure.RequirementsMetToBuild()) {
			Worker worker = WorkersManager.GetWorker();	
			if (worker != null) {
				BuildingsManager.BuildingsUnderConstruction.add(new Building(worker, buildingType));	
				structure.OnSuccess(worker);	
			}
		}
	}
	
	private static CacheStructure GetCacheStructure(UnitType buildingType) {
		for (CacheStructure cs : cachedStructures) {
			if (cs.buildingType == buildingType) {
				return cs;
			}
		}
			
		return null;
	}
}
