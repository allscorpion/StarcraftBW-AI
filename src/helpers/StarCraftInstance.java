package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Region;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import enums.PlayStyles;
import models.CustomBaseLocation;

public class StarCraftInstance {
	public static void Start(Mirror _mirror) {
		mirror = _mirror;
		game = mirror.getGame();
        self = game.self();
	}
	
	public static Mirror mirror;

	public static Game game;

	public static Player self;
}
