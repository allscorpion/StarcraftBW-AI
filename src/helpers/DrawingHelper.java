package helpers;

import bwapi.Position;
import bwapi.Unit;

public class DrawingHelper {
	private static int xTextPos = 10;
    private static int yTextPos = 10;
    
    public static void resetTextPos() {
    	xTextPos = 10;
    	yTextPos = 10;
    }
    
    public static void drawTextOnUnit(Unit myUnit, String text) {
    	if (myUnit != null) {
    		StarCraftInstance.game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), text);	
    	}
    }
    
    public static void drawTextOnScreen(String text) {
    	StarCraftInstance.game.drawTextScreen(xTextPos, yTextPos, text);
    	yTextPos += 20;
    }
    
    public static void writeTextMessage(String text) {
    	StarCraftInstance.game.sendText(text);
    }
    
    public static void drawTextAt(int x, int y, int text) {
    	drawTextAt(x, y, String.valueOf(text));
    }
    
    public static void drawTextAt(Position p, int text) {
    	drawTextAt(p.getX(), p.getY(), String.valueOf(text));
    }
    
    public static void drawTextAt(Position p, double text) {
    	drawTextAt(p.getX(), p.getY(), String.valueOf(text));
    }
    
    public static void drawTextAt(Position p, String text) {
    	drawTextAt(p.getX(), p.getY(), text);
    }
    
    public static void drawTextAt(int x, int y, String text) {
    	StarCraftInstance.game.drawTextMap(x, y, text);
    }
}
