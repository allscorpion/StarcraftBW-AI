package helpers;

import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.WalkPosition;
import models.CustomBaseLocation;

public class MapAnalyser {
	public static void ScanMap() {
		for (CustomBaseLocation cbl : BaseManager.baseLocations) {
//			for (int i = 0; i < cbl.baseLocation.getRegion().getPolygon().getPoints().size(); i++) {
//				if (i != cbl.baseLocation.getRegion().getPolygon().getPoints().size() - 1) {
//					StarCraftInstance.game.drawLineMap(cbl.baseLocation.getRegion().getPolygon().getPoints().get(i), cbl.baseLocation.getRegion().getPolygon().getPoints().get(i + 1), Color.Grey);
//				}else {
//					StarCraftInstance.game.drawLineMap(cbl.baseLocation.getRegion().getPolygon().getPoints().get(i), cbl.baseLocation.getRegion().getPolygon().getPoints().get(0), Color.Grey);
//				}
//			}
//    		for(Position position : cbl.baseLocation.getRegion().getPolygon().getPoints()){
//        		StarCraftInstance.game.drawBoxMap(position, new Position(position.getX() + 10, position.getY() + 10), Color.Grey);
//	    	}
    	}
//		for (int x = 0; x < StarCraftInstance.game.mapWidth(); x++) {
//			for (int y = 0; y < StarCraftInstance.game.mapHeight(); y++) {
//				TilePosition tpTopLeft = new TilePosition(x, y);
//				TilePosition tpBottomRight = new TilePosition(x + 1, y + 1);
//				// new WalkPosition(tpTopLeft.getX(), tpTopLeft.getY())
//				if (!StarCraftInstance.game.isBuildable(tpTopLeft)) {
//					StarCraftInstance.game.drawBoxMap(tpTopLeft.toPosition(), tpBottomRight.toPosition(), Color.Red, false);
//				}
//				//StarCraftInstance.game.drawBoxMap(new TilePosition(x, y).toPosition(), new TilePosition(x + 1, y + 1).toPosition(), Color.Grey, false);	
//			}
//		}
	}
}
