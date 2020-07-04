import org.apache.tools.ant.taskdefs.Local;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


@ScriptManifest(author = "kvoli", name="cannon_alcher", version = 1.0, description = "", category = Category.MONEYMAKING)


public class Main extends AbstractScript{

    public static final int C_OBJECT_ID = 6;
    public static final int C_INV_ID = 6;
    public static final int C_BALL_ID = 2;
    public long t;
    private long timeBegan;
    private long timeRan;
    public static final int NAT_RUNE = 561;
    public static final int LAW_RUNE = 563;
    public static final int CASH = 995;
    public Set<Integer> nonAlch = new HashSet<>();

    private Area cannonSpot = new Area(2528, 3371,2528,3371);

    @Override
    public void onStart(){
        getSkillTracker().start(Skill.MAGIC);
        getSkillTracker().start(Skill.RANGED);
        this.timeBegan = System.currentTimeMillis();
        nonAlch.add(NAT_RUNE);
        nonAlch.add(LAW_RUNE);
        nonAlch.add(C_BALL_ID);
        nonAlch.add(556);
        nonAlch.add(6);
        nonAlch.add(8);
        nonAlch.add(10);
        nonAlch.add(12);
        nonAlch.add(555);
        checkTabs();
        nonAlch.add(CASH);
        manageCannon();
    }

    @Override
    public void onPaint(Graphics var1) {
        var1.setColor(Color.GREEN);
        var1.drawString("Current Magic: " + getSkills().getRealLevel(Skill.MAGIC) +
                " | Current Ranged: " + getSkills().getRealLevel(Skill.RANGED), 5, 315);
        var1.drawString("XP P/H (Magic): " + getSkillTracker().getGainedExperiencePerHour(Skill.MAGIC)
                + " | XP P/H (Ranged): " + getSkillTracker().getGainedExperiencePerHour(Skill.RANGED), 5, 300);
        var1.drawString("TTL (Magic): " + ft(getSkillTracker().getTimeToLevel(Skill.MAGIC))
                + "| TTL (Ranged): " + ft(getSkillTracker().getTimeToLevel(Skill.RANGED)), 5, 285);
        var1.drawString("Running Time: " + ft(timeRan), 5, 270);
        var1.setColor(Color.GRAY);
    }

    private String ft(long duration)

    {

        String res = "";

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

    private void placeCannon(){
        getWalking().walkOnScreen(cannonSpot.getCenter());
        sleepWhile(() -> getLocalPlayer().isAnimating() && getLocalPlayer().isMoving(), 5000);
        getInventory().get(C_INV_ID).interact("Set-up");

    }

    private boolean checkInv(){
        return getInventory().contains(6,8,10,12);
    }

    private boolean checkSupplies(){
        return getInventory().contains(C_BALL_ID);
    }

    // put balls in cannon
    private void loadCannon(){
        GameObject cannon = getGameObjects().getTopObjectOnTile(cannonSpot.getCenter());
        cannon.interact("Fire");
    }

    private boolean checkSpot(){
        return getGameObjects().getTopObjectOnTile(cannonSpot.getCenter())!= null &&
                getGameObjects().getTopObjectOnTile(cannonSpot.getCenter()).getID() == C_OBJECT_ID;
    }

    private boolean isBroken(GameObject g){
        for (String s: g.getActions()){
            if ("Fire".equals(s))
                return false;
        }
        return true;
    }

    private void fix(){
        getGameObjects().getTopObjectOnTile(cannonSpot.getCenter()).interact("Repair");
    }

    private void pickupCannon(){
        getGameObjects().getTopObjectOnTile(cannonSpot.getCenter()).interact("Pickup");
    }

    private void checkTabs(){
        if (!getTabs().isOpen(Tab.INVENTORY))
            getTabs().open(Tab.INVENTORY);
    }

    private void manageCannon(){
        if (checkSpot() && !isBroken(getGameObjects().getTopObjectOnTile(cannonSpot.getCenter())))
            loadCannon();
        else if (!checkSpot() && checkInv() && checkSupplies()){
            placeCannon();
        } else if (!checkSupplies() && checkSpot()){
            pickupCannon();
            getTabs().logout();
        } else if (checkSpot() && isBroken(getGameObjects().getTopObjectOnTile(cannonSpot.getCenter()))) {
            fix();
        } else {
            getTabs().logout();
        }
        checkTabs();
        sleep(2048,4096);
    }

    private void alch(){
        if (canAlch())
            castHighAlch();
        if (canSelect())
            selectAlch();
    }

    private boolean canAlch(){
        List<Item> items= filterItems(getInventory().all());
        return items.size() > 0 &&
                !getMagic().isSpellSelected() &&
                getTabs().isOpen(Tab.MAGIC) &&
                getInventory().contains(NAT_RUNE);
    }

    private List<Item> filterItems(List<Item> items){
        List<Item> newItems = new ArrayList<>();
        for (Item i: items) {
            if (i != null && !nonAlch.contains(i.getID()))
                newItems.add(i);
        }
        return newItems;
    }

    private void castHighAlch(){
        getMagic().castSpell(Normal.HIGH_LEVEL_ALCHEMY);
    }

    private void selectAlch(){
        List<Item> items = filterItems(getInventory().all());
        items.get(Calculations.random(0, items.size()-1)).interact();
    }

    private boolean canSelect(){
        return  getMagic().isSpellSelected() &&
                checkInventory();
    }

    private boolean checkInventory(){
        if (getTabs().isOpen(Tab.INVENTORY))
            return true;
        getTabs().open(Tab.INVENTORY);
        return false;
    }

    @Override
    public int onLoop() {
        this.timeRan = System.currentTimeMillis() - this.timeBegan;
        if (System.currentTimeMillis() - t > 20000){
            manageCannon();
            t = System.currentTimeMillis();
        getTabs().open(Tab.MAGIC);
        } else {
            alch();
        }
        return Calculations.random(200,800);
    }

}
