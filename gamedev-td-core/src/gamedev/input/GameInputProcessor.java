package gamedev.input;

import gamedev.entity.GameState;
import gamedev.entity.Tower;
import gamedev.entity.TowerFactory;
import gamedev.entity.TowerFactory.TowerType;
import gamedev.entity.tower.ArrowTower;
import gamedev.entity.tower.DirtTower;
import gamedev.screen.GameScreen;
import gamedev.screen.GameUserInterface;
import gamedev.td.Config;
import gamedev.td.GDSprite;
import gamedev.td.TowerDefense;
import gamedev.td.helper.MathHelper;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;

public class GameInputProcessor extends GDInputProcessor {

	private GameScreen gameScreen;
	private Tower towerToBuild, selectedTower;

	private GDSprite selectedSprite;
	private GameUserInterface userInterface;

	public GameInputProcessor(TowerDefense towerDefense) {
		super(towerDefense);
		userInterface = new GameUserInterface();
		towerToBuild = null;
		selectedSprite = null;
		selectedTower = null;
	}

	@Override
	public boolean keyDown(int keycode) {
		
		if(keycode == 27){
			System.out.println("d");
			towerDefense.switchScreen(towerDefense.getPauseScreen());
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		if(character == 'p'){
			towerDefense.switchScreen(towerDefense.getPauseScreen());
		}
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {

		if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
			// Step 1
			selectTowerToBuild(x, y, pointer, button);

			// Step 2
			buildSelectedTower(x, y, pointer, button);

			// Step 1
			selectTowerToUpgrade(x, y, pointer, button);
			
			
			//Modify(2017.02.04 23:39 By JangMinWoo)
			//Pressed Skip Button
			skipRound(x, y, pointer, button);
			
			
		} else if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
			resetInteractions();
		}

		return false;
	}

	private void resetInteractions() {
		//Modify(2017.02.02 19:33 By Youngin Cho)
		//Deselect Tower
		towerToBuild = null;
		userInterface.setTowerRange(null);
		userInterface.setGhostTower(null);
		System.out.println("[Inform] Nothing selected");

	}

	private void selectTowerToBuild(int x, int y, int pointer, int button) {
		List<GDSprite> availableTowers = userInterface.getBuildTowerButtons();

		for (int type = 0; type < availableTowers.size(); type++) {
			GDSprite sprite = availableTowers.get(type);
			if (sprite.contains(x, y)) {
				TowerType towerType = TowerFactory.interpretType(type);
				System.out.println("[Input] User is trying to build a " + towerType);
				towerToBuild = TowerFactory.createTower(towerType);
				userInterface.setTowerToBuild(towerToBuild, towerType);
				userInterface.setGhostTower(towerType);
				userInterface.setTowerRange(towerToBuild);
			}
		}
	}

	private void buildSelectedTower(int x, int y, int pointer, int button) {
		if (towerToBuild == null)
			return; // Do nothing if there is no tower to build yet.

		GameState state = GameState.getInstance();
		Point point = getGridCoordinate(x, y);
		if(point != null) {
			towerToBuild.getPosition().set(MathHelper.PointToVector2(point));
		}
		
		
		//Modify(2017.02.02 20:00 By Minseok Koo)
		//Tower overlap problem solving
		List<Tower> deployedTowers = state.getDeployedTowers();

		for (int i = 0; i < deployedTowers.size(); i++) {
			Tower tower = deployedTowers.get(i);
			GDSprite sprite = tower.getSprite();
	
			if (sprite.contains(x, y)) {
				System.out.println("[Inform] The tower already exists.");
				return ;
			}
		}
		
		if (point != null && state.isTowerPlaceable(point)) {
			if (state.canBuyTower(towerToBuild)){
				System.out.println("[Input] User built a " + towerToBuild.getTowerName() + " on " + point);
				state.buildTower(towerToBuild.clone(), point);			
			}else{
				System.out.println("[Input] User cannot build a " + towerToBuild.getTowerName() + " because he/she does not have money.");
				
				//Modify(2017.02.02 19:33 By Youngin Cho)
				userInterface.reset();
			}	
			//towerToBuild = null;
			//userInterface.setTowerRange(null);
			//userInterface.setGhostTower(null);
			
			//Modify(2017.02.02 19:33 By Youngin Cho)
			//userInterface.reset();
		}
	}
	
	private void selectTowerToUpgrade(int x, int y, int pointer, int button) {
		GameState state = GameState.getInstance();

		List<Tower> deployedTowers = state.getDeployedTowers();

		for (int i = 0; i < deployedTowers.size(); i++) {
			Tower tower = deployedTowers.get(i);
			GDSprite sprite = tower.getSprite();

			if (sprite.contains(x, y)) {
				selectedTower = tower;
				
				/////////////////////
				GameUserInterface userinterace = new GameUserInterface();
				userinterace.setSelectedDeploedTower(tower);
				userInterface.setTowerToUpgrade(tower);
				System.out.println("[Input] User selected a tower, " + selectedTower.getTowerName() + " found at " + selectedTower.getPosition());
			}
		}
	}

	
	
	//Modify(2017.02.04 23:39 By JangMinWoo)
	//Pressed Skip Button
	private void skipRound(int x ,int y, int position, int button){
			
		GDSprite sprite = userInterface.getSkipButton();
		GameState instance = GameState.getInstance();
		
		if(sprite.contains(x,y)){
			System.out.println("[Inform] User use skip button");
			instance.pressedCheckSkip(0);	
			instance.pressedCheckSkip(1);	
		}	
			
	}
	
	
	//TODO: rework this method to follow the design
	private void upgradeButtonIntesersect(int screenX, int screenY) {
		if(selectedTower != null) {
	//		TowerInformation towerInfo = gameScreen.getUiInformation();
			if(selectedTower instanceof ArrowTower) {
				// TODO: Check if mouse intersects with upgradeToFireArrow or upgradeToIceArrow sprite in TowerInformation
			}
			else if(selectedTower instanceof DirtTower) {
				
			}
		}
		
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {

		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y) {

		Point point = getGridCoordinate(x, y);
		userInterface.setHighlightedCell(point);
		if (towerToBuild != null){
			userInterface.setGhostTowerLocation(point);
			towerToBuild.getPosition().set(MathHelper.PointToVector2(point));
		}
	
		
// TODO: Don't know what this code is for
//		GameState state = GameState.getInstance();
//		if (selectedTower == null) {
//			if (state.isTowerPlaceable(point) == true) {
//				gameScreen.getTowerRangeRender().setColor(Color.WHITE);
//				//gameScreen.getTowerRangeRenderer().setColor(red);
//			} else
//				gameScreen.getTowerRangeRender().setColor(Color.RED);
//		} else
//			gameScreen.getTowerRangeRender().setColor(Color.WHITE);


		List<GDSprite> towerSprites = userInterface.getBuildTowerButtons();
		userInterface.getTowerBtnHighlight().setPosition(-50, -50);
		for (int i = 0; i < towerSprites.size(); i++) {
			GDSprite sprite = towerSprites.get(i);
			if (sprite.contains(x, y)) {
				userInterface.getTowerBtnHighlight().setPosition(sprite.getX(), sprite.getY());
				
//				boolean showTooltip = true;
//				Point spritePoint = sprite.getPosition();

				
				//modify(2017.02.08 03:31 By JangMinWoo)
				//remove under 3lines for selecting towerToBuild when mouse.LEFT clicked  
				//TowerType towerType = TowerFactory.interpretType(i);
				//towerToBuild = TowerFactory.createTower(towerType);
				
				//userInterface.setTowerToBuild(towerToBuild, towerType);
			} else {
				userInterface.reset();
			}
		}
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	private Point getGridCoordinate(int screenX, int screenY) {
		if (screenX < 0 || screenY < 0)
			return null;

		// TODO: Check if screen exceeds map, if so, return null.
		// if (screenX > )
		
		int truncateX = screenX / Config.tileSize;
		int truncateY = screenY / Config.tileSize;
		return new Point(truncateX * Config.tileSize, truncateY * Config.tileSize);
	}

	public GameUserInterface getUserInterface() {
		return userInterface;
	}
}
