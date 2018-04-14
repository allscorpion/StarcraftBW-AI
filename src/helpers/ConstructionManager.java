package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.UnitType;
import interfaces.IStructure;
import models.Building;
import models.Worker;
import structures.CacheStructure;

public class ConstructionManager {
	public static void ConstructBuilding(UnitType buildingType) {
		try {
			CacheStructure cs = GetCacheStructure(buildingType);
			if (cs != null) {
				CheckStructure(cs.structureClass, buildingType);
				return;
			}
			Class<?> structureClass = Class.forName("structures." + buildingType.toString());
			IStructure structure = (IStructure)structureClass.newInstance();
			if (structure != null) {
				cachedStructures.add(new CacheStructure(buildingType,structure));
				CheckStructure(structure, buildingType);
			}
			
		} catch (ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException e) {
			StarCraftInstance.game.printf(e.toString());
		}
	}
	
	private static void CheckStructure(IStructure structure, UnitType buildingType) {
		if (structure.RequirementsMetToBuild()) {
			Worker worker = WorkersManager.GetWorker();	
			BuildingsManager.BuildingsUnderConstruction.add(new Building(worker, buildingType));	
			structure.OnSuccess();
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
	
	private static List<CacheStructure> cachedStructures = new ArrayList<CacheStructure>();
}
