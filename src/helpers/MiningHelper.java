package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import models.Building;

public class MiningHelper {
	public static void Init() {
		minerals = new ArrayList<Unit>();
	}
	public static List<Unit> minerals;
	public static Unit GetMineralFromUnit(Unit unit) {
    	for (Unit mineral : minerals) {
			if (mineral.getID() == unit.getID()) {
    			return mineral;	
    		}	
		}	
    	return null;		
    }
}
