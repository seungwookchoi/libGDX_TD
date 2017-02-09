package gamedev.entity;

import gamedev.entity.Enemy.EnemyType;
import gamedev.entity.Tile.TileType;
import gamedev.level.Level;
import gamedev.level.Map;
import gamedev.td.Config;
import gamedev.td.helper.MathHelper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class GameState {
	private static GameState instance;

	public static final int GRIDX = 17, GRIDY = 12;

	/**
	 * One full round lasts thirty (30) seconds.
	 */
	private static final float ROUND_DURATION = 30;

	private static final float PRE_ROUND_WAIT_DURATION = 5;

	private Level currentLevel;
	private int mapType;

	private int level;
	private int money = 0;
	private int playerLife = 10;
	private int spawnedEnemies;

	private TileType grid[][];
	private float roundTime, spawnDelay;

	private List<Enemy> enemies;
	private List<Integer> enemiesToBeSpawned;
	private List<Tower> deployedTowers;
	private List<Projectile> projectiles;

	private boolean roundHasStarted;
	
	//modify(2017.02.06 16:39 By JangMinWoo)
	//add checkskip field
	private boolean[] checkskip={false,false};  //[0]: Enemy/Tower/Projectile, [1]: RoundTime


	public static GameState getInstance() {
		if (instance == null)
			instance = new GameState();
		return instance;
	}

	/**
	 * Game state cannot be instantiated outside of the class. To get a reference to this object, call the static method getInstance().
	 */
	private GameState() {
		instance = this;
		createMap();
	}

	private void createMap() {
		grid = new TileType[GRIDX][GRIDY];
		for (int i = 0; i < GRIDX; i++) {
			for (int j = 0; j < GRIDY; j++) {
				grid[i][j] = TileType.Used;
			}
		}
	}

	public void initialize() {
		newRoundInitialization();
		level = 1;
		currentLevel = Level.generateLevel(level);	
		money = 100;
		playerLife = 10;
		roundTime = PRE_ROUND_WAIT_DURATION;
		deployedTowers = new ArrayList<Tower>();
		
	}
	
	public void newRoundInitialization(){
		spawnDelay = 0;
		spawnedEnemies = 0;
		enemiesToBeSpawned = new ArrayList<Integer>();
		enemies = new ArrayList<Enemy>();
		projectiles = new ArrayList<Projectile>();
		
	}
	
	public void update(float delta) {
		updateRoundTimer(delta);

		if (roundHasStarted) {
			
			//modify(2017.02.07 04:30 By JangMinWoo)
			//adapt Skipbutton (movement of enemy)
			GameState instance = GameState.getInstance();
			int N;
			if(!instance.getCheckSkip(0)) N=1;
			else N=200;	//about 5sec later
			
			for(int i=0;i<N;i++){	
			
				checkForEnemySpawn(delta);

				for (Enemy enemy : enemies)
					enemy.update(delta);

				for (Tower tower : deployedTowers)
					tower.update(delta);
			
				for(Projectile projectile : projectiles)
					projectile.update(delta);
			
			}
			if(N==200) instance.depressedCheckSkip(0);
			
		}
		
	}

	private void updateRoundTimer(float delta) {
		
		
		//modify(2017.02.07 06:36 By JangMinWoo)
		//adapt Skipbutton (update round time)
		GameState instance = GameState.getInstance();
		
		if(instance.getCheckSkip(1)){		//'5sec' is about skipTime
			roundTime-=5;
			instance.depressedCheckSkip(1);
		}
		
		
		if (roundTime > 0) {
			roundTime -= delta;
		}
		else {
			roundHasStarted = true;
			roundTime = ROUND_DURATION;
			
			prepareLevel(level++);
			spawnedEnemies = 0;
		}
		
		
		
		
		
	}

	public void render(SpriteBatch spriteBatch) {
		displayMap(spriteBatch);
		displayEnemies(spriteBatch);
		displayTowers(spriteBatch); 
		displayProjectiles(spriteBatch);
	}
	
	private void displayProjectiles(SpriteBatch spriteBatch) {
		spriteBatch.begin();
		for(Projectile projectile : projectiles){
			projectile.draw(spriteBatch);
		}
		spriteBatch.end();
		
	}

	private void displayTowers(SpriteBatch spriteBatch) {
		spriteBatch.begin();
		for(Tower tower : deployedTowers) {
			tower.draw(spriteBatch);
		}
		
		spriteBatch.end();
	}

	private void displayEnemies(SpriteBatch spriteBatch){
		spriteBatch.begin();
		for(Enemy enemy : enemies){
			enemy.draw(spriteBatch);
		}
		
		spriteBatch.end();
	}

	private void displayMap(SpriteBatch spriteBatch) {
		spriteBatch.begin();
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				TileType type = grid[i][j];

				Tile newTile = Tile.create(type);
				Vector2 position = new Vector2(i * Config.tileSize, j * Config.tileSize);
				newTile.setPosition(position);
				newTile.draw(spriteBatch);
			}
		}
		spriteBatch.end();
	}

	public boolean checkProjectileCollision() {

		return false;
	}


	/*
	 * TODO prepare enemies gets the list of enemies, instances e.g. { {1,2} , {2,1} , {1,2} } 2 spiders, 1 skeleton, 2 spiders in order
	 */

	public void checkForEnemySpawn(float delta) {
		spawnDelay += delta;

		if (spawnDelay >= .5 && spawnedEnemies < enemiesToBeSpawned.size()) {

			EnemyType type = Enemy.interpretType(enemiesToBeSpawned.get(spawnedEnemies));
			Enemy enemy = Enemy.createEnemy(type);
			enemies.add(enemy);
			spawnDelay = 0;
			++spawnedEnemies;
		}
	}

	/**
	 * This should be called after every round.
	 */
	public void prepareLevel(int lvl) {
		newRoundInitialization();
		currentLevel = Level.generateLevel(lvl);
		if((enemiesToBeSpawned = currentLevel.getEnemiesToBeSpawned()) == null){
			prepareLevel(lvl++);
		}else enemiesToBeSpawned = currentLevel.getEnemiesToBeSpawned();
		
	}


	public void deployTower(Tower tower) {
		if (canBuyTower(tower)) {
			money -= tower.getCost();
			deployedTowers.add(tower);
		}
	}

	public boolean canBuyTower(Tower tower) {
		return money >= tower.getCost();
	}

	public void setWaveSpawnTime(float waveSpawnTime) {
		if (waveSpawnTime < 0)
			this.roundTime = 10;
		else
			this.roundTime = waveSpawnTime;
	}

	public void getDamaged() {
		playerLife--;
	}
	
	public List<Projectile> getProjectiles(){
		return projectiles;
	}

	public Level getCurrentLevel() {
		return currentLevel;
	}

	public int getPlayerLife() {
		return playerLife;
	}

	public List<Enemy> getEnemies() {
		return enemies;
	}
	
	public float getRoundTime() {
		return roundTime;
	}
	
	public int getMoney() {
		return money;
	}
	
	public void addMoney(int bounty){
		this.money += bounty;
	}

	public List<Tower> getDeployedTowers() {
		return deployedTowers;
	}
	
	public List<Point> getWaypoints(){
		return Map.getInstance().getWaypoints(mapType);
	}
	
	public void setMap(int type){
		mapType = type;
		this.grid = Map.generateMap(type);
		
	}
	
	//modify(2017.02.06 05:57 By JangMinWoo)
	//add getcheck,pressed,depressed method  at GameState
	public boolean getCheckSkip(int type){
		return checkskip[type];
	}
	public void pressedCheckSkip(int type){
		System.out.println("Pressed the CheckSkip Button");;
		checkskip[type] = true;
	}
	public void depressedCheckSkip(int type){
		System.out.println("Depressed the CheckSkip Button");;
		checkskip[type]=false;
	}
	
	//modify(2017.02.09 02:53 By JangMinWoo)
	//make it impossible to build Tower at DartDirt,Steve as well as Dirt
	public boolean isTowerPlaceable(Point point) {
		
		//TileType tile_type = grid[point.x /40][point.y /40];
		try {
			//return point.x > 0 && point.y > 0 && tile_type != TileType.Dirt; //error...		
			return point.x > 0 && point.y > 0 && grid[point.x / 40][point.y / 40] != TileType.Dirt && grid[point.x / 40][point.y / 40]!=TileType.Dirt_Dark && grid[point.x / 40][point.y / 40]!=TileType.Steve;			
		}catch (Exception e){
			
		}
		return false;
		
	}

	public void buildTower(Tower towerToBuild, Point point) {
		// grid[point.x / 40][point.y / 40] = TileType.Used;

		Vector2 position = MathHelper.PointToVector2(point);
		towerToBuild.setPosition(position);

		towerToBuild.setCenter((float) point.x + Config.tileSize / 2, (float) point.y + Config.tileSize / 2);
		towerToBuild.getPosition().set(MathHelper.PointToVector2(point));
		deployTower(towerToBuild);
	}
	
}
