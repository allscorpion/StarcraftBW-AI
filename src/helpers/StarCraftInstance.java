package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;

public class StarCraftInstance {
	public static void Start(Mirror _mirror) {
		mirror = _mirror;
		game = mirror.getGame();
        self = game.self();
        ResourcesManager.Start();
        allMyUnits = new ArrayList<Unit>();
	}
	
	public static void SetBaseLocations() {
		baseLocations = BWTA.getBaseLocations();
        mySpawn = BWTA.getStartLocation(self);
	}
	
	public static Mirror mirror;

	public static Game game;

	public static Player self;
	
	public static List<Unit> allMyUnits;
	
	public static BaseLocation mySpawn;
	
	public static List<BaseLocation> baseLocations = new ArrayList<BaseLocation>();
}
