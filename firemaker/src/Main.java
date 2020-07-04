import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@ScriptManifest(author = "kvoli", name="fire_maker", version = 1.0, description = "", category = Category.MONEYMAKING)


public class Main extends AbstractScript{


    public static final int BANK = 26707;
    public static final int BANK_NPC = 3194;
    public static final int BANK_TILE_Y = 4649;
    public static final int BANK_TILE_X = 3039;
    public static final int FIRE_TILE_X = 3063;
    public static final int FIRE_TILE_Y = 4967;
    public static final int TINDER_BOX = 590;
    private int currentLog;
    private long timeBegan;
    private long timeRan;
    private int hopCounter;

    private Area fireArea = new Area(3064, 4969, FIRE_TILE_X, FIRE_TILE_Y);
    private Area fireLine = new Area(3064, 4966, 3040, 4967);

    private int stage = 0;

    @Override
    public void onStart(){
        setLog();
        getSkillTracker().start(Skill.FIREMAKING);
        this.timeBegan = System.currentTimeMillis();
        fireArea.setZ(1);
        fireLine.setZ(1);

    }

    private String ft(long duration)

    {

        String res;

        long days = TimeUnit.MILLISECONDS.toDays(duration);

        long hours = TimeUnit.MILLISECONDS.toHours(duration)

                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));

        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)

                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS

                .toHours(duration));

        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)

                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS

                .toMinutes(duration));

        if (days == 0) {

            res = (hours + ":" + minutes + ":" + seconds);

        } else {

            res = (days + ":" + hours + ":" + minutes + ":" + seconds);

        }

        return res;

    }

    @Override
    public void onPaint(Graphics var1) {
        var1.setColor(Color.GRAY);
        var1.drawString("Current XP: " + getSkills().getExperience(Skill.FIREMAKING), 5, 330);
        var1.drawString("Current Level: " + getSkills().getRealLevel(Skill.FIREMAKING), 5, 315);
        var1.drawString("XP Till Level: " + getSkills().getExperienceToLevel(Skill.FIREMAKING), 5, 300);
        var1.drawString("XP Gained: " + getSkillTracker().getGainedExperience(Skill.FIREMAKING), 5, 270);
        var1.setColor(Color.GREEN);
        var1.drawString("XP P/H: " + getSkillTracker().getGainedExperiencePerHour(Skill.FIREMAKING), 5, 255);
        var1.drawString("Time to Next Level: " + ft(getSkillTracker().getTimeToLevel(Skill.FIREMAKING)), 5, 285);
        var1.drawString("Running Time: " + ft(timeRan), 5, 240);
        var1.setColor(Color.RED);
        var1.drawString("STAGE: " + stage, 5, 225);
        var1.drawString("Current Log " + currentLog, 5, 210);
        var1.drawString("Nearby Players: " + nearbyPlayers().toString(), 5, 195);
        var1.setColor(Color.BLUE);
        fillTile(closestFire(), var1, Color.GREEN);
        drawTiles(var1, fireTiles(), Color.GREEN);
        var1.drawPolygon(getLocalPlayer().getTile().getPolygon());
        var1.setColor(Color.GRAY);
    }

    private void drawRect(Rectangle r, Graphics ctx){
        ctx.drawRect((int)getLocalPlayer().getBoundingBox().getX(), (int)getLocalPlayer().getBoundingBox().getY(), (int)getLocalPlayer().getBoundingBox().getWidth(), (int)getLocalPlayer().getBoundingBox().getHeight());
    }

    private void drawTiles(Graphics ctx, List<Tile> tiles, Color c){
        tiles.forEach(e -> drawTile(e, ctx, c));
    }

    private void drawTile(Tile t, Graphics ctx, Color c){
        ctx.setColor(c);
        ctx.drawPolygon(t.getPolygon());
    }

    private void fillTile(Tile t, Graphics ctx, Color c){
        ctx.setColor(c);
        ctx.fillPolygon(t.getPolygon());
    }

    private void setLog(){
        int currentLevel = getSkills().getRealLevel(Skill.FIREMAKING);
        if (currentLevel < 15)
            currentLog = 1511;
        else if (currentLevel < 30)
            currentLog = 1521;
        else if (currentLevel < 35)
            currentLog = 1519;
        else if (currentLevel < 45)
            currentLog = 6333;
    }


    //goto bank
    private void travelBank(){
        stage = 1;
        getWalking().walk(BankLocation.ROGUES_DEN.getCenter());
        randSleep();
    }

    private boolean checkBankClosest(){
        return getNpcs().closest(BANK_NPC) != null && getNpcs().closest(BANK_NPC).isOnScreen() && stage == 1;
    }

    // withdraw bank
    private void transact(){
        randSleep();
        setLog();
        getNpcs().closest(BANK_NPC).interact("Bank");
        randSleep();
        getBank().withdrawAll(currentLog);
        randSleep();
        getBank().close();
        stage = 0;
    }

    // goto firemaking spot
    private void travelFire(){
        log("walking to fire area");
        getWalking().walk(fireArea.getRandomTile());
        randSleep();
    }

    private boolean shouldTravel(){
        return !fireArea.contains(getLocalPlayer()) && stage == 0;
    }

    private boolean checkTravel(){
        if (!shouldTravel() && stage == 0){
            log("Lighting Good");
            return true;
        }
        return false;
    }

    private List<World> validWorlds(){
        return getWorlds().all(e ->
                e.isMembers()
                        && !e.isHighRisk()
                        && !e.isLastManStanding()
                        && !e.isDeadmanMode()
                        && !e.isTwistedLeague()
                        && e.getMinimumLevel() < getSkills().getTotalLevel()
                        && !e.isTournamentWorld()
                        && !e.isPVP());
    }

    private World getRandomValidWorld(){
        return validWorlds().get(Calculations.random(0, validWorlds().size()-1));
    }

    private void hopRandom(){
        randSleep();
        getWorldHopper().openWorldHopper();
        randSleep();
        getWorldHopper().hopWorld(getRandomValidWorld());
        sleep(4048,8048);
        getWorldHopper().closeWorldHopper();
        randSleep();
        getTabs().open(Tab.INVENTORY);
        stage = 0;
    }

    //check inv

    private boolean checkInventory(){
        return getInventory().contains(TINDER_BOX) && checkStock();
    }

    private boolean checkTile(Tile tile){
        return getGameObjects().getTopObjectOnTile(tile) == null ||
                checkObjects(getGameObjects().getObjectsOnTile(tile));
    }

    private boolean checkObjects(GameObject[] g){
        for (GameObject gameObject: g)
            if (gameObject.getID() == 26185 || gameObject.getID() == 5881)
                return false;
        return true;
    }

    private boolean checkTiles(List<Tile> tiles){
        for (Tile t : tiles)
            if (!checkTile(t))
                return false;
        return true;
    }

    private boolean checkLight(){
        return stage == 2 && checkInventory() && !getLocalPlayer().isAnimating() ;
    }

    private boolean checkStock(){
        return getInventory().count(currentLog) > 0;
    }

    // light fires
    private void lightFire(){
        checkTabs();
        if (checkTile(getLocalPlayer().getTile()))
            getInventory().get(TINDER_BOX).useOn(currentLog);
        else {
            log("Tile obstructed: " + getLocalPlayer().getTile().toString() + ", closest tile is: " + closestFire().toString());
            getWalking().walkOnScreen(closestFire());
        }
        randSleep();
    }

    private void randSleep(){
        sleep(1000,2000);
    }

    private Tile closestTile(List<Tile> tiles){
        Tile best = tiles.get(0);
        for (Tile t: tiles){
            best = Calculations.distance(getLocalPlayer().getTile(), t) <
                    Calculations.distance(getLocalPlayer().getTile(), best)  ? t : best;
        }
        return best;
    }

    private List<Tile> fireTiles(){
        List<Tile> goodTiles = new ArrayList<>();
        for (Tile t :fireLine.getTiles()){
            if (checkTile(t))
                goodTiles.add(t);
        }
        return goodTiles;
    }

    private Tile closestFire(){
        return closestTile(fireTiles());
    }

    private void checkTabs(){
        if (!getTabs().isOpen(Tab.INVENTORY))
            getTabs().open(Tab.INVENTORY);
    }

    private boolean nearbyPlayer(){
        return getPlayers().all().size() > 1;
    }

    private List<String> nearbyPlayers(){
        return getPlayers().all().stream().map(Player::getName).collect(Collectors.toList());
    }

    @Override
    public int onLoop() {
        this.timeRan = System.currentTimeMillis() - this.timeBegan;

        if (getLocalPlayer().isAnimating() || getLocalPlayer().isMoving())
            return 0;
        else if (nearbyPlayer()){
            hopRandom();
        }
        else if (checkBankClosest())
            transact();
        else if (!checkStock())
            travelBank();
        else if (shouldTravel())
            travelFire();
        else if (checkTravel())
            stage = 2;
        else if (checkLight())
            lightFire();
        if (stage == 3 && getWorldHopper().isWorldHopperOpen())
            sleep(500);


        return 0;
    }

}
