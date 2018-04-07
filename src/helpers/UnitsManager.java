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

public class UnitsManager{
	
    public static List<Unit> MilitaryUnits = new ArrayList<Unit>();
    
    public static void onUnitDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.remove(unit);
    	}
    }
    
    public static void onUnitComplete(Unit unit) {
    	if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.add(unit);
    		//unit.attack(new Position(GetClosestEmptyBase(unit).getPosition().getX(), GetClosestEmptyBase(unit).getPosition().getY() + 20));
    	}
    }
    
    public static void attackUnits() {
    	if (StarCraftInstance.game.enemy().getUnits().size() > 0) {
			for (Unit attackUnit : MilitaryUnits) {
				if (attackUnit.isIdle()) {
					for (Unit u : StarCraftInstance.game.enemy().getUnits()) {
						attackUnit.attack(u);	
			    	}
				}
			}
		} else {
			if (StarCraftInstance.self.allUnitCount(UnitType.Terran_Marine) < 50) {
//				for (Unit attackUnit : MilitaryUnits) {
//					Position rallyLocation = new Position(GetClosestEmptyBase().getPosition().getX(), GetClosestEmptyBase().getPosition().getY() + 20);
//					if (attackUnit.getPosition().getDistance(rallyLocation) > 10) {
//						attackUnit.attack(rallyLocation);
//					}
//				} 
			} else {
	    		for (Unit attackUnit : MilitaryUnits) {
	    			if (attackUnit.isIdle()) {
	    				for (BaseLocation b : BWTA.getBaseLocations()) {
	            			// If this is a possible start location,
	            			if (b.isStartLocation() && b.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
	            				// do something. For example send some unit to attack that position:
	            				attackUnit.attack(b.getPosition());
	            			}
	            		}	
	    			}
				}
			}
	    	
		}
    }
}