package helpers;

import bwapi.Unit;

public class DrawingHelper {
	private static int xTextPos = 10;
    private static int yTextPos = 10;
    
    public static void resetTextPos() {
    	xTextPos = 10;
    	yTextPos = 10;
    }
    
    public static void drawTextOnUnit(Unit myUnit, String text) {
    	StarCraftInstance.game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), text);
    }
    
    public static void drawTextOnScreen(String text) {
    	StarCraftInstance.game.drawTextScreen(xTextPos, yTextPos, text);
    	yTextPos += 20;
    }
}
