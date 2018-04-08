package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import models.Building;
import models.Mineral;

public class MineralsHelper {
	public static List<Mineral> minerals = new ArrayList<Mineral>();
	public static Mineral GetMineralFromUnit(Unit unit) {
    	for (Mineral mineral : minerals) {
			if (mineral.node.getID() == unit.getID()) {
    			return mineral;	
    		}	
		}	
    	return null;		
    }
}
