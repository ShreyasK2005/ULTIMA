import java.awt.Font;
import java.awt.Color;
import java.util.*;
/**
 * The class that describes a monster in the Ultima game
 * 
 * @author Keith Vertanen
 * @author Michele Van Dyne - added commenting
 * @author YOUR NAME HERE!! - added monster movement logic
 */
public class Monster implements Runnable {

   public enum MonsterType {INVALID, SKELETON, ZOMBIE, BAT, GORK, TORNADO, NINJA};
   public enum MoveType {RANDOM, STILL, AGGRO, N};  

   private MonsterType type;                       // type of monster
   private MoveType    moveType;                   // how does this monster move right now
   private int         x;                          // x location of monster
   private int         y;                          // y location of monster
   private int         sleepMs;                    // delay between times monster moves
   private int         hp;                         // hit points - damage sustained
   private int         attackDamage;               // damage monster causes
   private int         aggroRadius;                // how far the enemy can sense the avatar
   private World       world;                      // the world the monster moves about in
   private Stats       timer;                      // elapsed time for showing damage;
   
   /**
    * Construct a new monster
    * @param world     - the world the monster moves about in
    * @param code      - the string code that distinguishes types of monsters
    * @param x         - the x position of the monster
    * @param y         - the y position of the monster
    * @param hp        - hit points - damage sustained by the monster
    * @param damage    - damage the monster causes
    * @param sleepMs   - delay between time monster moves
    */
   public Monster(World world, String code, int x, int y, int hp, int attackDamage, int sleepMs, String moveCode, int aggroRadius) {
      this.world        = world;
      this.x            = x;
      this.y            = y;
      this.hp           = hp;
      this.attackDamage = attackDamage;
      this.sleepMs      = sleepMs;
      this.aggroRadius  = aggroRadius;
   
      if      (code.toUpperCase().equals("SK")) type = MonsterType.SKELETON;
      else if (code.toUpperCase().equals("ZB")) type = MonsterType.ZOMBIE;
      else if (code.toUpperCase().equals("BT")) type = MonsterType.BAT;
      else if (code.toUpperCase().equals("GK")) type = MonsterType.GORK;
      else if (code.toUpperCase().equals("TO")) type = MonsterType.TORNADO;
      else if (code.toUpperCase().equals("NJ")) type = MonsterType.NINJA;
      else                                      type = MonsterType.INVALID;
      
      if      (moveCode.toUpperCase().equals("AGGRO"))  moveType = MoveType.AGGRO;
      else if (moveCode.toUpperCase().equals("RANDOM")) moveType = MoveType.RANDOM;
      else if (moveCode.toUpperCase().equals("STILL"))  moveType = MoveType.STILL;
      else if (moveCode.toUpperCase().equals("N"))  moveType = MoveType.N;
      else                                              moveType = MoveType.STILL;         
   }

   /**
    * The avatar has attacked a monster!
    * @param points    - number of hit points to be subtracted from monster
    */
   public void incurDamage(int points) {
      hp -= points;
      if (timer == null) timer = new Stats();
      timer.reset();
   }

   /**
    * Draw this monster at its current location
    */
   public void draw() {
   Tile ty = null;
      double drawX = (x + 0.5 - World.offSetX) * Tile.SIZE;
      double drawY = (y + 0.5 - World.offSetY) * Tile.SIZE;
      switch (type) {
         case SKELETON: StdDraw.picture(drawX, drawY, "img-skeleton.png", Tile.SIZE, Tile.SIZE); 
            break;
         case ZOMBIE:   StdDraw.picture(drawX, drawY, "img-zombie.png",   Tile.SIZE, Tile.SIZE); 
            break;
         case BAT:      StdDraw.picture(drawX, drawY, "img-bat.png",      Tile.SIZE, Tile.SIZE); 
            break;
         case GORK:     StdDraw.picture(drawX, drawY, "img-gork.png",     Tile.SIZE, Tile.SIZE); 
            break;
         case TORNADO:  StdDraw.picture(drawX, drawY, "img-tornado.png",  Tile.SIZE, Tile.SIZE); 
            break;
         //case NINJA:
         //StdDraw.picture(drawX, drawY, "RF.png",  Tile.SIZE, Tile.SIZE);
         //break;
         default:       StdDraw.picture(drawX, drawY, "img-blank.gif",    Tile.SIZE, Tile.SIZE);
         
         if(world.getNumMonsters() == 3)
         {
           StdDraw.picture(drawX, drawY, "RF.png",  Tile.SIZE, Tile.SIZE);
         } 
      }
   
      //Show health for a small amount of time after taking damage
      if ((timer != null) && (timer.elapsedTime() < World.DISPLAY_DAMAGE_SEC)) {
         String healthString = "" + hp;
         //Draw background box
         StdDraw.setPenColor(new Color(0, 0, 0, 150)); //black with alpha
         StdDraw.filledRectangle(drawX, drawY - Tile.SIZE/2 + 8, (int)(healthString.length()*4.5)+4, 8);                
         //With font size 14, each digit is 4 pixels wide and 8 pixels tall                       
         //Draw health text
         StdDraw.setPenColor(StdDraw.RED);
         StdDraw.setFont(new Font("SansSerif", Font.BOLD, 14));
         StdDraw.text(drawX, drawY - Tile.SIZE/2 + 8, healthString);
      }      
   }

   /**
    * Get the number of hit points the monster has remaining
    * @return the number of hit points
    */
   public int getHitPoints() {
      return hp;
   }

   /**
    * Get the amount of damage a monster causes
    * @return amount of damage monster causes
    */
   public int getAttackDamage() {
      return attackDamage;
   }

   /**
    * Get the x position of the monster
    * @return x position
    */
   public int getX() {
      return x;
   }

   /**
    * Get the y position of the monster
    * @return y position
    */
   public int getY() {
      return y;
   }

   /**
    * Set the new location of the monster
    * @param x the new x location
    * @param y the new y location
    */
   public void setLocation(int x, int y) {
      this.x = x;
      this.y = y;
   }

        
   /**
    * Thread that runs on loop moving the monster 
    * around as long as it is alive
    */
   public void run() {
      while (hp > 0)
      {         
         Tile nextLocation = getNextLocation();  
         if (nextLocation != null)          
               world.monsterMove(nextLocation.getX(), nextLocation.getY(), this);                             
         
         // ***** Thread sleeps for moment until next move *****
         try { Thread.sleep(sleepMs); }
         catch (InterruptedException e) { System.out.println(e); }            
      }
   }
   
   private Tile getNextLocation() {
   double drawX = (x + 0.5 - World.offSetX) * Tile.SIZE;
      double drawY = (y + 0.5 - World.offSetY) * Tile.SIZE;
      // Depending on the MoveType of this monster, either getRandomMove or getBFSMove (See below)
      // Include code that switches RANDOM enemies into AGGRO when appropriate
      // Return the Tile object indicating the next location to move into 
         Tile ty = null;
         if(moveType == moveType.AGGRO)
         {
           ty =  getBFSMove();
         }
         if(moveType == moveType.RANDOM)
         {
           ty =  getRandomMove();
         }
         if(world.getNumMonsters() == 3)
         {
          if(moveType == moveType.N)
          {
            ty =  getBFSMove();
          }
         } 
         if(ty != null && (Math.sqrt(Math.pow((x - ty.getX()), 2) + Math.pow((y - ty.getY()), 2))) >= aggroRadius && moveType == moveType.AGGRO)
         {
           moveType = moveType.AGGRO;
           ty = getBFSMove();
         }
        if(world.getNumMonsters() == 3)
        { 
         if(ty != null && (Math.sqrt(Math.pow((x - ty.getX()), 2) + Math.pow((y - ty.getY()), 2))) >= aggroRadius && moveType == moveType.N)
         {
           moveType = moveType.N;
           ty = getBFSMove();
         }
        } 
       
       
         return ty;         
   }
   
   private Tile getRandomMove() {
      Tile[][] tiles = world.getTileMatrx();
      Tile[] chosenMoves = new Tile[4];
         
      // ***** <YOUR CODE GOES HERE> *****
      // Return a Tile object indicating the next location for this monster who moves randomly
       double a = Math.random() * 4;
       int m = (int)a;
         switch((int)m)
         {
           case 1:
           if(y-1 >= 0)
           {
             if(tiles[x][y-1].isPassable())
             {
               chosenMoves[m] = tiles[x][y-1];
             }
           }
           break;
           case 2:
           if(y+1 < tiles.length)
           {
             if(tiles[x][y+1].isPassable())
             {
               chosenMoves[m] = tiles[x][y+1];
             }
           }
           break;
           case 3:
           if(x-1 >= 0)
           {
             if(tiles[x-1][y].isPassable())
             {
               chosenMoves[m] = tiles[x-1][y];
             }
           }
           break;
           case 4:
           if(x+1 < tiles.length)
           {
             if(tiles[x+1][y].isPassable())
             {
               chosenMoves[m] = tiles[x+1][y];
             }
           }
           break;
         }
                  
      return chosenMoves[m];
   }
   
   private Tile getBFSMove() {
    Tile[][] tiles = world.getTileMatrx();
      Tile avatar = world.getAvatarTile();
      Tile monster = tiles[x][y];
      Tile chosenMove = null;

        Map<Tile, Tile> prev  = new HashMap<>();
        Queue<Tile> placesToVisit = new LinkedList<>();

        placesToVisit.offer(monster);
        prev.put(monster, null);
        outer:
        while (! placesToVisit.isEmpty()) {   
            Tile current = placesToVisit.poll();                               
            for (Tile adj : getNeighbors(current)) {
                if (!prev.containsKey(adj)) {
                    placesToVisit.offer(adj);
                    prev.put(adj, current);
                    if (adj.equals(avatar))
                        break outer;                                       
                }
            }
        }

        //work backwards from goal to start to get path and number of steps
        Tile move = avatar;
        while ( prev.get(move) != monster ) {
            move = prev.get(move);
        }
        return move;
  }
  
  
  public List<Tile> getNeighbors(Tile current) {
      Tile[][] tiles = world.getTileMatrx();
      int X = current.getX();
      int Y = current.getY();
      List<Tile> n = new ArrayList<>();
      if (X < tiles.length-1 && tiles[X+1][Y].isPassable())
         n.add(tiles[X+1][Y]);
      if (X > 0 && tiles[X-1][Y].isPassable())
         n.add(tiles[X-1][Y]);
      if (Y < tiles.length-1 && tiles[X][Y+1].isPassable())
         n.add(tiles[X][Y+1]);
      if (Y > 0 && tiles[X][Y-1].isPassable())
         n.add(tiles[X][Y-1]);
   
        //randomize the list so option up isn't prioritized.
      for (int i = 0; i < n.size(); i++){
         int rand = (int)(Math.random()*n.size());
         Tile temp = n.get(i);
         n.set(i, n.get(rand));
         n.set(rand, temp);                    
      }
      return n;
   }
     
}


