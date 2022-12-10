package byow.Core;

import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.*;
import java.util.*;

import java.util.List;


import static java.awt.Color.black;
import static java.awt.Color.blue;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Engine {
    public TERenderer ter;
    public Random random;
    public Avatar avatar;
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;
    public TETile[][] world = new TETile[WIDTH][HEIGHT];
    public HashMap<Integer, List<ArrayList<Integer>>> rooms;
    public HashSet<ArrayList<Integer>> canMove = new HashSet<>();
    public HashMap<Integer, Boolean> roomConnected;
    public int numberRooms;
    public StringBuilder actions;
    public boolean playing;
    public boolean won;
    public List<tilePoint>healthFlowers = new ArrayList<>();
    public boolean gameOverWall = false;
    public boolean name = false;
    public static String playerName;
    public Monster mon;
    public boolean pathOn = false;
    public List<tilePoint> path;


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public Engine() {
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
    }


    public void initialize() {
        random = new Random();
        rooms = new HashMap<>();
        canMove = new HashSet<>();
        playing = false;
        won = false;
    }

    public void interactWithKeyboard() {
        drawInitial();
        while (true) {
            if (playing) {
                drawMouse();
            }
            if (StdDraw.hasNextKeyTyped()) {
                char action = StdDraw.nextKeyTyped();
                actionHandler(action);
                if (playing) {
                    showMap();
                }
            }
        }
    }

    public void showMap() {
        won = false;
        playing = true;
        ter.renderFrame(world);
        StdDraw.show();
    }

    public void drawInitial() {
        drawTitle();
        Font subtitle = new Font("Monaco", Font.BOLD, 25);
        StdDraw.setFont(subtitle);
        if (!name) {
            StdDraw.text(WIDTH/2, HEIGHT/2 + 4, "Add Avatar's Name (P)");
        }
        StdDraw.text(WIDTH/2, HEIGHT/2, "New Game (N)");
        StdDraw.text(WIDTH/2, HEIGHT/2 - 4, "Load Game (L)");
        StdDraw.text(WIDTH/2, HEIGHT/2 - 8, "Quit (Q)");
        if (won) {
            StdDraw.clear(black);
            StdDraw.setPenColor(Color.PINK);
            Font title = new Font("Monaco", Font.BOLD, 32);
            StdDraw.setFont(title);
            StdDraw.text(WIDTH / 2, HEIGHT / 4 * 3, "YOU COLLECTED ALL FLOWER POTS! GAME WON.");
        }
        if(gameOverWall) {
            displayWallgameOver();
        }

        StdDraw.show();

        initialize();
    }

    private void drawTitle() {
        StdDraw.clear(black);
        StdDraw.setPenColor(Color.white);
        Font title = new Font("Monaco", Font.BOLD, 32);
        StdDraw.setFont(title);
        StdDraw.text(WIDTH / 2, HEIGHT / 4 * 3, "Mare and Abby's League of Legends");
    }

    //UI describing the tile the mouse is hovering.
    public void drawMouse() {
        StdDraw.setPenColor(Color.black);
        StdDraw.filledRectangle(0, HEIGHT-4, WIDTH/2+10, 2);
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        if (x>=0 && x<WIDTH && y>=0 && y<HEIGHT) {
            TETile target = world[x][y];
            if(target == Tileset.WALL) {
                StdDraw.setPenColor(Color.white);
                StdDraw.textLeft(0 , HEIGHT-4, "It's a " +
                        target.description() +", this is inaccessible.");
            }
            if(target == Tileset.FLOOR) {
                StdDraw.setPenColor(Color.white);
                StdDraw.textLeft(0 , HEIGHT-4, "It's a " +
                        target.description() +", you can go here.");
            }
            if(target == Tileset.NOTHING) {
                StdDraw.setPenColor(Color.white);
                Font font = new Font("Arial", Font.BOLD, 30);
                StdDraw.setFont(font);
                StdDraw.textLeft(0 , HEIGHT-4, "Nothing here.");
            }
        }
        Font font = new Font("Arial", Font.BOLD, 30);
        StdDraw.setFont(font);
        if(name) {
            StdDraw.textLeft(45, HEIGHT-4, playerName + "'s Game");
        }
        StdDraw.textLeft(65, HEIGHT-4, "Health: " + avatar.health);
        StdDraw.setPenColor(blue);
        StdDraw.text(44,HEIGHT-6.5, "Toggle 'm': Update/Turn Off Monster Path.");
        StdDraw.show();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, running both of these:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        //new variable to track input String
        StringInputDevice inputs = new StringInputDevice(input);
        actions = new StringBuilder();
        char current = inputs.getNextKey();
        //sees where the seed input is about to begin (after char "N")
        if (current == 'L') {
            actionHandler(current);
            while (inputs.possibleNextInput()) {
                current = inputs.getNextKey();
                actionHandler(current);
            }
        } else {
            while (inputs.possibleNextInput() && current != 'n' && current != 'N') {
                current = inputs.getNextKey();
            }
            if (current != 'n' && current != 'N') {
                throw new IllegalArgumentException();
            }
            actions.append('n');
            current = inputs.getNextKey();
            StringBuilder temp = new StringBuilder();

            //sees where the seed number ends (at char 'S')

            while (inputs.possibleNextInput() && current != 's' && current != 'S') {
                temp.append(current);
                current = inputs.getNextKey();
            }
            if (current != 's' && current != 'S') {
                throw new IllegalArgumentException();
            }
            int randomSeed = Integer.parseInt(temp.toString());
            actions.append(randomSeed);
            actions.append('s');

            createFrame();
            buildMap(randomSeed);
            playing = true;
            while (inputs.possibleNextInput()) {
                current = inputs.getNextKey();
                actionHandler(current);
            }
        }
        ter.renderFrame(world);
        return world;
    }
    public String collectName() {
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);
        Font title = new Font("Monaco", Font.BOLD, 32);
        StdDraw.setFont(title);
        StdDraw.pause(100);
        StdDraw.show();
        StdDraw.text(WIDTH/2, HEIGHT/2 + 4, "Please Enter Name. Press z when done.");
        StdDraw.show();
        String playerName = "";
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char next = StdDraw.nextKeyTyped();
                if (next == 'z') {
                    if (playerName == "") {
                        StdDraw.clear(Color.black);
                        StdDraw.setPenColor(Color.white);
                        StdDraw.setFont(title);
                        StdDraw.text(WIDTH/2, HEIGHT/2, "Invalid Entry. Try Again");
                        StdDraw.show();
                        StdDraw.pause(800);
                        return collectName();
                    }
                    name = true;
                    return playerName;
                } else {
                    playerName += next;
                    StdDraw.clear(Color.black);
                    StdDraw.setPenColor(Color.white);
                    StdDraw.setFont(title);
                    StdDraw.text(WIDTH/2, HEIGHT/2 + 4, "Please Enter Name. Press z when done.");
                    StdDraw.text(WIDTH/2, HEIGHT/2, playerName);
                    StdDraw.show();
                }
            }
        }

    }

    //builds floor, halls and walls
    public void buildMap(int seed) {
        random = new Random(seed);
        numberRooms = random.nextInt(WIDTH / 3);
        buildRooms(numberRooms);
        int numberHalls = random.nextInt(numberRooms, (int) (numberRooms * 2.25));
        buildHallways(numberHalls, numberRooms);
        buildWalls();
        addHealthFlowers();
        mon = createMonster();
        //create avatar, spawn in first room in HashMap "rooms"
        avatar = new Avatar(rooms.get(0).get(0).get(0), rooms.get(0).get(0).get(1));
        world[avatar.x][avatar.y] = Tileset.AVATAR;
    }

    //handles avatar movement with AWSD input and beginning screen actions (new world, load, quit)
    public void actionHandler(char action) {
        if (actions != null) {
            actions.append(action);
        } else {
            actions = new StringBuilder();
            actions.append(action);
        }
        if (playing) {
            switch (action) {
                case ('W'):
                case ('w'):
                    if (canMove.contains(new ArrayList<>(Arrays.asList(avatar.x, avatar.y + 1)))) {
                        world[avatar.x][avatar.y] = Tileset.FLOOR;
                        avatar.y += 1;
                        world[avatar.x][avatar.y] = Tileset.AVATAR;
                        tilePoint point = new tilePoint(avatar.x,avatar.y);
                        if (healthFlowers.contains(point)) {
                            avatar.health += 1;
                            healthFlowers.remove(point);
                        }
                        moveMonster(avatar, mon);
                    } else {
                        gameOverWall = true;
                        drawInitial();
                    }
                    break;
                case ('S'):
                case ('s'):
                    if (canMove.contains(new ArrayList<>(Arrays.asList(avatar.x, avatar.y - 1)))) {
                        world[avatar.x][avatar.y] = Tileset.FLOOR;
                        avatar.y -= 1;
                        world[avatar.x][avatar.y] = Tileset.AVATAR;
                        tilePoint point = new tilePoint(avatar.x, avatar.y);
                        if (healthFlowers.contains(point)) {
                            avatar.health += 1;
                            healthFlowers.remove(point);
                        }
                        moveMonster(avatar, mon);
                    } else {
                        gameOverWall = true;
                        drawInitial();
                    }
                    break;
                case ('A'):
                case ('a'):
                    if (canMove.contains(new ArrayList<>(Arrays.asList(avatar.x - 1, avatar.y)))) {
                        world[avatar.x][avatar.y] = Tileset.FLOOR;
                        avatar.x -= 1;
                        world[avatar.x][avatar.y] = Tileset.AVATAR;
                        tilePoint point = new tilePoint(avatar.x, avatar.y);
                        if (healthFlowers.contains(point)) {
                            avatar.health += 1;
                            healthFlowers.remove(point);
                        }
                        moveMonster(avatar, mon);
                    } else {
                        gameOverWall = true;
                        drawInitial();
                    }
                    break;
                case ('D'):
                case ('d'):
                    if (canMove.contains(new ArrayList<>(Arrays.asList(avatar.x + 1, avatar.y)))) {
                        world[avatar.x][avatar.y] = Tileset.FLOOR;
                        avatar.x += 1;
                        world[avatar.x][avatar.y] = Tileset.AVATAR;
                        tilePoint point = new tilePoint(avatar.x, avatar.y);
                        if (healthFlowers.contains(point)) {
                            avatar.health += 1;
                            healthFlowers.remove(point);
                        }
                        moveMonster(avatar, mon);
                    } else {
                        gameOverWall = true;
                        drawInitial();
                    }
                    break;
                case ('Q'):
                case ('q'):
                    char last = actions.charAt(actions.length() - 2);
                    if (last == ':') {
                        save();
                        System.exit(0);
                    }
                    break;
                case ('M'):
                case ('m'):
                    showMonsterPath(path(new tilePoint(mon.x, mon.y), new tilePoint(avatar.x, avatar.y)));
                    break;
            }
                if (healthFlowers.isEmpty()) {
                    won = true;
                    drawInitial();
                }
        } else {
            switch (action) {
                case ('N'):
                case ('n'):
                    int seed = getSeed();
                    createFrame();
                    buildMap(seed);
                    actions.append(seed);
                    actions.append('s');
                    showMap();
                    break;
                case ('L'):
                case ('l'):
                    String actions = load();
                    System.out.println(actions);
                    if (actions.length() > 0) {
                        if(actions.charAt(0)=='&') {
                            playerName = actions.split("&")[1];
                            Font font = new Font("Arial", Font.BOLD, 30);
                            StdDraw.setFont(font);
                            StdDraw.textLeft(45, HEIGHT-4, playerName + "'s Game");
                            StdDraw.show();
                            interactWithInputString(actions.substring(playerName.length()+2));
                        } else {
                            interactWithInputString(actions);
                        }
                        showMap();
                    } else {
                        System.exit(0);
                    }
                    break;
                case ('Q'):
                case ('q'):
                    System.exit(0);
                case('p'):
                case('P'):
                    if (!name) {
                        playerName = collectName();
                    }
                    interactWithKeyboard();
                    break;
            }
        }
    }

    //saves game
    private void save() {
        File f = new File("./game.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            String save = "&"+playerName+"&"+actions.substring(0, actions.length() - 2);
            os.writeObject(save);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //loads game
    private String load() {
        File f = new File("./game.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (String) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println(e);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        return "";
    }

    public void displayWallgameOver() {
        StdDraw.clear(black);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "GAME OVER: YOU RAN INTO A WALL!");
    }

    public int getSeed() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;
        drawSeedInterface();
        StdDraw.show();

        int seed = 0;
        boolean done = false;
        while (!done) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = StdDraw.nextKeyTyped();
                if (input == 'Q' || input == 'q') {
                    System.exit(0);
                } else if (input == 'S' || input == 's') {
                    done = true;
                } else if (input >= '0' && input <= '9') {
                    if (seed >= 100000) {
                        StdDraw.text(midWidth, midHeight - 1.2, seed
                                + "(The seed cannot exceed 1000000.)");
                        StdDraw.show();
                    }
                    seed = seed * 10 + input - '0';
                    StdDraw.clear(Color.BLACK);
                    StdDraw.text(midWidth, midHeight - 1.2, "SEED: " + seed);
                    StdDraw.show();
                }
            }
        }
        return seed;
    }

    public void drawSeedInterface() {
        StdDraw.clear(Color.BLACK);
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;
        drawTitle();
        Font context = new Font("Monaco", Font.BOLD, 36);
        StdDraw.setFont(context);
        StdDraw.text(midWidth, midHeight, "Type a number [0-1000000] followed by an 's' to start:");
        StdDraw.text(midWidth, midHeight - 4, "Start (S)");
        StdDraw.text(midWidth, midHeight - 8, "Quit (Q)");
    }

    public void createFrame() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void buildRooms(int numRooms) {
        rooms = new HashMap<>();
        int tracker = 0;
        for (int i = 0; i < numRooms; i++) {
            List<ArrayList<Integer>> newList = new ArrayList<>();
            rooms.put(tracker, newList);
            int randX = random.nextInt(1, WIDTH - 1);
            int randY = random.nextInt(1, HEIGHT - 15); //change the HEIGHT-15 as needed.
            int width = random.nextInt(2, HEIGHT / 3);
            int height = random.nextInt(2, HEIGHT / 3);
            for (int x = randX; x < randX + width; x++) {
                for (int y = randY; y < randY + height; y++) {
                    if (x < WIDTH - 1 && y < HEIGHT - 10 && x > 0 && y > 0) {
                        world[x][y] = Tileset.FLOOR;
                        ArrayList<Integer> temp = new ArrayList<>();
                        temp.add(x);
                        temp.add(y);
                        rooms.get(tracker).add(temp);
                        ArrayList<Integer> points = new ArrayList<>();
                        points.add(x);
                        points.add(y);
                        canMove.add(points);
                    }
                }
            }
            tracker += 1;
        }
    }

    public void buildHallways(int numHallways, int numRooms) {
        roomConnected = new HashMap<>();
        int extraHallways = numHallways - numRooms;
        for (int i = 0; i < rooms.size() - 1; i += 1) {
            List<Integer> point = rooms.get(i).get(random.nextInt(rooms.get(i).size()));
            int x1 = point.get(0);
            int y1 = point.get(1);
            roomConnected.put(i, true);
            List<Integer> point2 = null;
            int x2 = 0;
            int y2 = 0;
            if (rooms.get(i + 1).size() != 0) {
                point2 = rooms.get(i + 1).get(random.nextInt(rooms.get(i + 1).size()));
                x2 = point2.get(0);
                y2 = point2.get(1);
                roomConnected.put(i + 1, true);
            }
            int leaveOff = 0;
            boolean isx1 = false;
            for (int j = min(x1, x2); j < max(x1, x2); j += 1) {
                if (j == x1) {
                    isx1 = true;
                } else if (j == x2) {
                    isx1 = false;
                }
                if (isx1) {
                    world[j][y1] = Tileset.FLOOR;
                    ArrayList<Integer> points = new ArrayList<>();
                    points.add(j);
                    points.add(y1);
                    canMove.add(points);
                } else {
                    world[j][y2] = Tileset.FLOOR;
                    ArrayList<Integer> points = new ArrayList<>();
                    points.add(j);
                    points.add(y2);
                    canMove.add(points);
                }
                leaveOff = j;
            }
            for (int k = min(y1, y2); k < max(y1, y2); k += 1) {
                if (leaveOff != 0 && leaveOff != WIDTH - 1) {
                    world[leaveOff][k] = Tileset.FLOOR;
                    ArrayList<Integer> points = new ArrayList<>();
                    points.add(leaveOff);
                    points.add(k);
                    canMove.add(points);
                }
            }
        }
        for (int i = 0; i < extraHallways; i++) {
            int randRoom = random.nextInt(numRooms);
            List<Integer> point1 = rooms.get(randRoom).get(random.nextInt(rooms.get(randRoom).size()));
            int x1rand = point1.get(0);
            int y1rand = point1.get(1);
            int randRoom2 = random.nextInt(numRooms);
            List<Integer> point2 = rooms.get(randRoom2).get(random.nextInt(rooms.get(randRoom2).size()));
            int x2rand = point2.get(0);
            int y2rand = point2.get(1);
            for (int j = min(x1rand, x2rand); j < max(x1rand, x2rand); j += 1) {
                world[j][y1rand] = Tileset.FLOOR;
                ArrayList<Integer> points = new ArrayList<>();
                points.add(j);
                points.add(y1rand);
                canMove.add(points);
            }
            for (int k = min(y1rand, y2rand); k < max(y1rand, y2rand); k += 1) {
                world[x1rand][k] = Tileset.FLOOR;
                ArrayList<Integer> points = new ArrayList<>();
                points.add(x1rand);
                points.add(k);
                canMove.add(points);
            }
        }
    }

    public void buildWalls() {
        for (int i = 0; i <= WIDTH - 1; i++) {
            for (int j = 0; j <= HEIGHT - 1; j++) {
                if (i == 0 && world[i + 1][j] == Tileset.FLOOR || i == WIDTH - 1 && world[i - 1][j] == Tileset.FLOOR || j == HEIGHT - 1 && world[i][j - 1] == Tileset.FLOOR || j == 0 && world[i][j + 1] == Tileset.FLOOR) {
                    world[i][j] = Tileset.WALL;
                } else if (i != 0 && j != 0 && i != WIDTH - 1 && j != HEIGHT - 1) {
                    if (world[i][j] != Tileset.FLOOR) {
                        if ((world[i + 1][j] == Tileset.FLOOR) || (world[i - 1][j] == Tileset.FLOOR) || (world[i][j - 1] == Tileset.FLOOR) || (world[i][j + 1] == Tileset.FLOOR)) {
                            world[i][j] = Tileset.WALL;
                        }
                    }
                }
            }
        }
    }
    public void addHealthFlowers() {
        int numApples = random.nextInt(3,6);
        for (int start = 0; start < numApples; start += 1) {
            int pickRoom = random.nextInt(0,numberRooms);
            List points = rooms.get(pickRoom).get(random.nextInt(0, rooms.get(pickRoom).size()));
            tilePoint pickpoint = new tilePoint((int)points.get(0),(int)points.get(1));
            world[pickpoint.x][pickpoint.y] = Tileset.FLOWER;
            healthFlowers.add(pickpoint);
        }
    }
    public Monster createMonster() {
        int pickRoom = random.nextInt(0,numberRooms);
        List points = rooms.get(pickRoom).get(random.nextInt(0, rooms.get(pickRoom).size()));
        tilePoint pickpoint = new tilePoint((int)points.get(0),(int)points.get(1));
        Monster monster = new Monster(pickpoint.x, pickpoint.y);
        world[monster.x][monster.y] = Tileset.MOUNTAIN;
        return monster;
    }
    public void showMonsterPath(List<tilePoint> monsterPath) {
        if (!pathOn) {
            path = monsterPath;
            for (tilePoint tile : monsterPath) {
                world[tile.x][tile.y] = Tileset.WATER;
            }
        } else {
            for (tilePoint tile : path) {
                world[tile.x][tile.y] = Tileset.FLOOR;
            }
        }
        world[avatar.x][avatar.y] = Tileset.AVATAR;
        world[mon.x][mon.y] = Tileset.MOUNTAIN;
        ter.renderFrame(world);
        pathOn = !pathOn;
    }

    public void moveMonster(Avatar avatar, Monster monster) {
        List<tilePoint> monsterPath = path(new tilePoint(monster.x, monster.y), new tilePoint(avatar.x, avatar.y));
        world[monster.x][monster.y] = Tileset.FLOOR;
        if(monsterPath.size()!=0) {
            monster.x = monsterPath.get(monsterPath.size()-1).x;
            monster.y = monsterPath.get(monsterPath.size()-1).y;
            world[monster.x][monster.y] = Tileset.MOUNTAIN;
            }
        ter.renderFrame(world);
        if (avatar.x == monster.x && avatar.y == monster.y) {
            monsterGameOver();
        }
    }

    public void monsterGameOver() {
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.red);
        Font title = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(title);
        StdDraw.text(WIDTH / 2,HEIGHT / 2 + 5, "GAME OVER");
        StdDraw.text(WIDTH / 2,HEIGHT / 2, "You've Been Eaten By a Monster");
        StdDraw.show();
        playing = false;
    }
    Comparator<DistPoints> comp = new Comparator<>() {
        @Override
        public int compare(DistPoints a, DistPoints b) {
            return a.distTo - b.distTo;
        }
    };

    public PriorityQueue<DistPoints> data = new PriorityQueue<>(byow.Core.Engine.HEIGHT * byow.Core.Engine.WIDTH, comp);
    public void visit(DistPoints curr) {
        //check left
        PriorityQueue<DistPoints> temp = data;
        data = new PriorityQueue<>(byow.Core.Engine.HEIGHT * byow.Core.Engine.WIDTH, comp);
        while (!temp.isEmpty()) {
            tilePoint left = new tilePoint(curr.location.x-1, curr.location.y);
            tilePoint right = new tilePoint(curr.location.x+1, curr.location.y);
            tilePoint up = new tilePoint(curr.location.x, curr.location.y+1);
            tilePoint down = new tilePoint(curr.location.x, curr.location.y-1);
            DistPoints track = temp.poll();
            if (track.location.equals(left) && curr.distTo + 1 < track.distTo) {
                track.distTo = curr.distTo + 1;
                track.edgeTo = curr;
            }
            if (track.location.equals(right) && curr.distTo + 1 < track.distTo) {
                track.distTo = curr.distTo + 1;
                track.edgeTo = curr;
            }
            if (track.location.equals(up) && curr.distTo + 1 < track.distTo) {
                track.distTo = curr.distTo + 1;
                track.edgeTo = curr;
            }
            if (track.location.equals(down) && curr.distTo + 1 < track.distTo) {
                track.distTo = curr.distTo + 1;
                track.edgeTo = curr;
            }
            data.add(track);
        }
    }
    public List<tilePoint> path(tilePoint start, tilePoint goal) {
        //this will map points to their closest distance to start
        DistPoints starting = new DistPoints(start, null, 0);
        data.add(starting);
        for (List thing : canMove) {
            tilePoint temp =  new tilePoint((int)thing.get(0),(int)thing.get(1));
            data.add(new DistPoints(temp, null, 100000));
        }
        DistPoints current = data.remove();
        while (!current.location.equals(goal)) {
            visit(current);
            current = data.remove();
        }
        return finalHelp(current);
    }
    public List<tilePoint> finalHelp(DistPoints point) {
        List<tilePoint> result = new ArrayList<>();
        while (point.edgeTo != null) {
            result.add(point.location);
            point = point.edgeTo;
        }
        return result;
    }
}