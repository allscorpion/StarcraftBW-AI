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

public class WorkersManager {
	
    public static List<Unit> Workers = new ArrayList<Unit>();
    
    public static void onWorkerCreate(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.add(unit);
    	}
    }

    public static void onWorkerDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.remove(unit);
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