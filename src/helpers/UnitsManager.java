package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import bwta.Chokepoint;
import models.CustomBaseLocation;

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
    
    public static void KiteMeleeUnits(Unit myUnit, Unit enemyUnit) {
    	// myUnit.isAttacking() && (myUnit.isAttackFrame() || myUnit.isStartingAttack())
    	//  || myUnit.getDistance(enemyUnit) < myUnit.getType().groundWeapon().maxRange() * 0.66
    	// myUnit.getGroundWeaponCooldown() > 0
    	//if ((myUnit.isAttackFrame() || myUnit.isStartingAttack())) return;
    	if (myUnit.isMoving()) return;
    	int distanceFromEnemy = myUnit.getDistance(enemyUnit);
    	double requiredDistance = myUnit.getType().groundWeapon().maxRange() * 0.66;
    	if (myUnit.getGroundWeaponCooldown() > 0 && distanceFromEnemy < requiredDistance) {
    		int xDifferential = (int) (requiredDistance - (myUnit.getPosition().getX() - enemyUnit.getPosition().getX()));
    		int newX = xDifferential > 0 ? -xDifferential : xDifferential;
    		int yDifferential = (int) (requiredDistance - (myUnit.getPosition().getY() - enemyUnit.getPosition().getY()));
    		int newY = yDifferential > 0 ? -yDifferential : yDifferential;
    		myUnit.move(
				new Position(
					myUnit.getPosition().getX() + newX, 
					myUnit.getPosition().getY() + newY
				)
			);
    	}
    	else {
			if (
				myUnit.canUseTech(TechType.Stim_Packs) && 
				myUnit.getHitPoints() >= myUnit.getType().maxHitPoints() * 0.6 && 
				myUnit.isInWeaponRange(enemyUnit) && 
				!myUnit.isStimmed()
			) {
				myUnit.useTech(TechType.Stim_Packs);
			}
			if (myUnit.isIdle()) {
				myUnit.attack(enemyUnit.getPosition());	
			}
    	}
    }
    
    public static void attackUnits() {
    	List<Unit> closestEnemyUnits = StarCraftInstance.game.enemy().getUnits();
    	if (StarCraftInstance.self.supplyUsed() / 2 >= 200) {
    		for (Unit myUnit : MilitaryUnits) {
    			if (myUnit.isIdle()) {
    				for (CustomBaseLocation cbl : BaseManager.baseLocations) {
            			// If this is a possible start location,
            			if (cbl.baseLocation.isStartLocation() && cbl.baseLocation.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
            				// do something. For example send some unit to attack that position:
            				myUnit.attack(cbl.baseLocation.getPosition());
            			}
            		}
    			}
    		}
    	}
    	if (closestEnemyUnits.size() == 0) return;
		for (final Unit myUnit : MilitaryUnits) {
			//if (myUnit.isIdle()) {
//					Collections.sort(closestEnemyUnits, new Comparator<Unit>() {
//			            @Override
//			            public int compare(Unit u1, Unit u2) {
//							return myUnit.getPosition().getDistance(u1.getPosition()) < myUnit.getPosition().getDistance(u2.getPosition())
//			                        ? -1 : 1;
//			            }
//			        });
				Unit closestEnemy = null;
				int distanceFromClosestEnemy = Integer.MAX_VALUE;
				for (Unit enemyUnit : closestEnemyUnits) {
					int distance = enemyUnit.getDistance(myUnit);
					if(distance < distanceFromClosestEnemy) {
						distanceFromClosestEnemy = distance;
						closestEnemy = enemyUnit;
					}
					
					if (enemyUnit.isCloaked() && myUnit.isInWeaponRange(enemyUnit)) {
						for (CustomBaseLocation cbl : BaseManager.baseLocations) {
							if (cbl.commandCenter != null && cbl.commandCenter.unit.getAddon() != null) {
								cbl.commandCenter.unit.getAddon().useTech(TechType.Scanner_Sweep, enemyUnit.getPosition());
								break;
							}
						}
					}
		    	}
				if(closestEnemy == null) return;
				if (closestEnemy.getType() == UnitType.Protoss_Zealot) {
					KiteMeleeUnits(myUnit, closestEnemy);	
				}else if (closestEnemy.isVisible()) {
					KiteMeleeUnits(myUnit, closestEnemy);
					//myUnit.attack(enemyUnit.getPosition());
				}
			//}
		}
		
    	// else {
//			if (StarCraftInstance.self.allUnitCount(UnitType.Terran_Marine) < 50) {
////				for (Unit attackUnit : MilitaryUnits) {
////					Position rallyLocation = new Position(GetClosestEmptyBase().getPosition().getX(), GetClosestEmptyBase().getPosition().getY() + 20);
////					if (attackUnit.getPosition().getDistance(rallyLocation) > 10) {
////						attackUnit.attack(rallyLocation);
////					}
////				} 
//			} else {
//	    		for (Unit myUnit : MilitaryUnits) {
//	    			if (myUnit.isIdle()) {
//	    				for (BaseLocation b : StarCraftInstance.baseLocations) {
//	            			// If this is a possible start location,
//	            			if (b.isStartLocation() && b.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
//	            				// do something. For example send some unit to attack that position:
//	            				myUnit.attack(b.getPosition());
//	            			}
//	            		}	
//	    			}
//				}
//			}
//	    	
//		}
    }
}