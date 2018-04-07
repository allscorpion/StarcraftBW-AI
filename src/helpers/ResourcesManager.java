package helpers;

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;

public class ResourcesManager {

	public ResourcesManager(Game _game, Player _self) {
        game = _game;
        self = _self;
        PotentialSupply = self.supplyTotal();
    }
	
    private Game game;
    private Player self;
    public int MineralsInReserve = 0;
    public int MilitaryMineralUnitCost = 0;
    public int PotentialSupply = 0;
    
    public int getCurrentMinerals() {
    	int total = self.minerals() - MineralsInReserve;
    	return total;
    }
    
    // so that we don't build any additional buildings without producing out of our current structures
    public int getCurrentMineralsIncludingMilitary() {
    	int total = self.minerals() - MineralsInReserve - MilitaryMineralUnitCost;
    	return total;
    }
    
    public boolean isDepoRequired() {
    	if (self.supplyTotal() / 2 == 200) return false;
    	int amountOfFreeSupply = PotentialSupply - self.supplyUsed();
    	int amountOfSupplyQueued = 0;
    	
    	for (Unit myUnit : self.getUnits()) {
    		if (myUnit.getType().isBuilding() && myUnit.isTraining()) {
    			amountOfSupplyQueued += myUnit.getTrainingQueue().get(0).supplyRequired();
    		}
    	} 
//    	drawTextOnScreen("amountOfFreeSupply " + String.valueOf(amountOfFreeSupply / 2));
//    	drawTextOnScreen("amountOfSupplyQueued " + String.valueOf(amountOfSupplyQueued / 2));
    	if (amountOfSupplyQueued > 0) {
    		int productionCyclesLeft = (amountOfFreeSupply - amountOfSupplyQueued) / amountOfSupplyQueued;
        	//drawTextOnScreen("productionCyclesLeft " + String.valueOf(productionCyclesLeft));
    		return productionCyclesLeft < 2 || amountOfFreeSupply <= 0;
    	}
		return amountOfFreeSupply <= 0;
    }
}