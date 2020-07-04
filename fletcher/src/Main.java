import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.event.KeyboardEvent;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


@ScriptManifest(author = "kvoli", name="fletcher", version = 1.1, description = "", category = Category.MONEYMAKING)


public class Main extends AbstractScript{


    private int normal_logs = 0;
    private int oak_logs = 0;
    private int willow_logs = 0;
    private int maple_logs = 0;
    private int yew_logs = 0;
    private int magic_logs = 0;

    private int normal_u = 0;
    private int oak_u = 0;
    private int willow_u = 0;
    private int maple_u = 0;
    private int yew_u = 0;
    private int magic_u = 0;

    private int bowstring = 0;

    private int current_u = 62;
    private int current_log;
    private int stage = 0;
    private long timeBegan;
    private long timeRan;


    @Override
    public void onStart(){
        getSkillTracker().start(Skill.FLETCHING);
        this.timeBegan = System.currentTimeMillis();
    }


    // check if any string left in inv
    private boolean checkDone(){
        return getInventory().contains(1777) && getInventory().contains(current_u);
    }

    private void setCurrent(){
        int lvl = getSkills().getRealLevel(Skill.FLETCHING);
        log("Fletching level is " + lvl);
        if (lvl >= 85 && magic_u > 1) {
            current_u = 70;
        } else if (lvl >= 70 && yew_u > 1){
            current_u = 66;
        } else if (lvl >= 55 && maple_u > 1){
            current_u = 62;
        } else if (lvl >= 40 && willow_u > 1){
            current_u = 58;
        } else if (lvl >= 25 && oak_u > 1){
            current_u = 56;
        } else if (lvl >= 10 && normal_u > 1){
            current_u = 48;
        }
    }

    private void updateItems(){
        normal_logs = getBank().count(1511);
        oak_logs = getBank().count(1521);
        willow_logs = getBank().count(1519);
        maple_logs = getBank().count(1517);
        yew_logs = getBank().count(1515);
        magic_logs = getBank().count(1513);

        normal_u = getBank().count(70);
        oak_u = getBank().count(66);
        willow_u = getBank().count(58);
        maple_u = getBank().count(62);
        yew_u = getBank().count(56);
        magic_u = getBank().count(48);

        bowstring = getBank().count(1777);

        log("n/o/w/m/y/m " + normal_u + oak_u + willow_u + maple_u + yew_u + magic_u);
        log("Bowstring: " + bowstring);
    }

    private void checkTabs(){
        if (!getTabs().isOpen(Tab.INVENTORY))
            getTabs().open(Tab.INVENTORY);
    }

    private void openBank(){
        getBank().open(BankLocation.GRAND_EXCHANGE);
        updateItems();
        setCurrent();
        sleep(1024,2048);
        if (getInventory().all().size() > 1)
            getBank().depositAllItems();
        getBank().withdraw(1777, 14);
        getBank().withdraw(current_u, 14);
        sleep(1024,2048);
        getBank().close();
        checkTabs();
    }

    // make bow
    private void makeBow(){
        checkTabs();
        getInventory().get(current_u).useOn(1777);
        sleep(500,3000);
        getKeyboard().typeSpecialKey(KeyEvent.VK_SPACE);
        sleep(300,1000);
        getKeyboard().typeSpecialKey(KeyEvent.VK_SPACE);
        sleep(2048,4096);
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

    @Override
    public void onPaint(Graphics var1) {
        var1.setColor(Color.GRAY);
        var1.drawString("Current XP: " + getSkills().getExperience(Skill.FLETCHING), 5, 330);
        var1.drawString("Current Level: " + getSkills().getRealLevel(Skill.FLETCHING), 5, 315);
        var1.drawString("XP Till Level: " + getSkills().getExperienceToLevel(Skill.FLETCHING), 5, 300);
        var1.drawString("XP Gained: " + getSkillTracker().getGainedExperience(Skill.FLETCHING), 5, 270);
        var1.setColor(Color.GREEN);
        var1.drawString("XP P/H: " + getSkillTracker().getGainedExperiencePerHour(Skill.FLETCHING), 5, 255);
        var1.drawString("Time to Next Level: " + ft(getSkillTracker().getTimeToLevel(Skill.FLETCHING)), 5, 285);
        var1.drawString("Running Time: " + ft(timeRan), 5, 240);
        var1.setColor(Color.RED);
        var1.setColor(Color.GRAY);
    }

    @Override
    public int onLoop() {
        this.timeRan = System.currentTimeMillis() - this.timeBegan;
        if (getLocalPlayer().isAnimating()) {
            log("Sleep - animating");
            sleep(1024,2048);
        } else if (checkDone() && stage == 0) {
            log("Can make - do so");
            makeBow();
            stage = 1;
        } else if (!checkDone()){
            log("Out of materials");
            openBank();
            stage = 0;
        }
        return Calculations.random(1024,2048);
    }

}
