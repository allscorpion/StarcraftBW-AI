package helpers;

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import models.Worker;

public class ResourcesManager {

	public static void Init() {
		MineralsInReserve = 0;
	    MilitaryMineralUnitCost = 0;
        PotentialSupply = StarCraftInstance.self.supplyTotal();
        MineralsPerMinute = 0;
        GasPerFrame = 0;
    }
	
    public static int MineralsInReserve;
    public static int MilitaryMineralUnitCost;
    public static int PotentialSupply;
    public static int MineralsPerMinute;
    public static int GasPerFrame;
    
    public static void calcIncome() {
    	if (StarCraftInstance.game.getFPS() == 0) return;
    	if (StarCraftInstance.game.getFrameCount() % StarCraftInstance.game.getFPS() != 0) return;
    	int currentMineralsPerMinute = 0;
    	for (Worker w : WorkersManager.Workers) {
    		if (w.unit.isGatheringMinerals() || w.unit.isCarryingMinerals()) {
    			currentMineralsPerMinute += 50;
    		}
    	}
    	MineralsPerMinute = currentMineralsPerMinute;
    }
    
    public static int getCurrentMinerals() {
    	int total = StarCraftInstance.self.minerals() - MineralsInReserve;
    	return total;
    }
    
    // so that we don't build any additional buildings without producing out of our current structures
    public static int getCurrentMineralsIncludingMilitary() {
    	int total = StarCraftInstance.self.minerals() - MineralsInReserve - MilitaryMineralUnitCost;
    	return total;
    }
    
    public static boolean isDepoRequired() {
    	if (StarCraftInstance.self.supplyTotal() / 2 == 200) return false;
    	int amountOfFreeSupply = PotentialSupply - StarCraftInstance.self.supplyUsed();
    	int amountOfSupplyQueued = 0;
    	
    	for (Unit myUnit : StarCraftInstance.self.getUnits()) {
    		if (myUnit.getType().isBuilding() && myUnit.isTraining() && myUnit.getTrainingQueue().size() > 0) {
    			amountOfSupplyQueued += myUnit.getTrainingQueue().get(0).supplyRequired();
    		}
    	} 
    	//DrawingHelper.drawTextOnScreen("amountOfFreeSupply " + String.valueOf(amountOfFreeSupply / 2));
    	//DrawingHelper.drawTextOnScreen("amountOfSupplyQueued " + String.valueOf(amountOfSupplyQueued / 2));
    	if (amountOfSupplyQueued > 0) {
    		int productionCyclesLeft = (amountOfFreeSupply - amountOfSupplyQueued) / amountOfSupplyQueued;
    		//DrawingHelper.drawTextOnScreen("productionCyclesLeft " + String.valueOf(productionCyclesLeft));
    		return productionCyclesLeft < 2 || amountOfFreeSupply <= 0;
    	}
		return amountOfFreeSupply <= 0;
    }
}