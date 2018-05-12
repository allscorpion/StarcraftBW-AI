package helpers;

import bwapi.Position;

public class CameraHelper {
	public static void init() {
		cameraEnabled = true;
		lastFrameMoved = 0;
	}
	private static boolean cameraEnabled;
	private static int lastFrameMoved;
	public static void moveCamera(Position p) {
		if (!cameraEnabled) return;
		if (lastFrameMoved + 50 > StarCraftInstance.game.getFrameCount()) return;
		lastFrameMoved = StarCraftInstance.game.getFrameCount();
		StarCraftInstance.game.setScreenPosition(new Position(p.getX() - 350, p.getY() - 200));
	}
}
