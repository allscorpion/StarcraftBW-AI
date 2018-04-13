package helpers;

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;

public class ResourcesManager {

	public static void Start() {
        PotentialSupply = StarCraftInstance.self.supplyTotal();
    }
	
    public static int MineralsInReserve = 0;
    public static int MilitaryMineralUnitCost = 0;
    public static int PotentialSupply = 0;
    
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