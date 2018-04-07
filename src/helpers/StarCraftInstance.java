package helpers;

import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;

public class StarCraftInstance {
	public static void Start(Mirror _mirror) {
		mirror = _mirror;
		game = mirror.getGame();
        self = game.self();
        ResourcesManager.Start();
	}
	public static Mirror mirror;

	public static Game game;

	public static Player self;
}
