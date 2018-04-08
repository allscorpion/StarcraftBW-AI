package helpers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import models.Building;

public class WorkersManager {
	
    public static List<Unit> Workers = new ArrayList<Unit>();
    public static List<Unit> Builders = new ArrayList<Unit>();
    
    public static void onWorkerCreate(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.add(unit);
    	}
    }

    public static void onWorkerDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.remove(unit);
    		if (Builders.size() > 0 && Builders.contains(unit)) {
    			Building workerBuilding = BuildingsManager.GetBuildingFromWorker(unit);
        		if (workerBuilding != null) {
        			workerBuilding._builder = null;	
        		}else {
        			DrawingHelper.writeTextMessage("Unable to find building from dead worker");
        		}
    		}
    	}
    }
   
    public static Unit GetWorker() {
    	for (Unit myUnit : Workers) {
			if (myUnit.canBuild() && !myUnit.isConstructing()) {
				return myUnit;
			}
		}	
    	return null;
    }
    
    public static void SendIdleWorkersToMinerals() {
    	for (Unit myUnit : Workers) {
    		if (myUnit.isIdle()) {
                Unit closestMineral = null;

                //find the closest mineral
                for (Unit neutralUnit : StarCraftInstance.game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                        if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                            closestMineral = neutralUnit;
                        }
                    }
                }

                //if a mineral patch was found, send the worker to gather it
                if (closestMineral != null) {
                    myUnit.gather(closestMineral, false);
                }
            }
    	}
    }
}