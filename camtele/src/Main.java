import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@ScriptManifest(author = "kvoli", name="tele_alcher", version = 2.0, description = "", category = Category.MONEYMAKING)


public class Main extends AbstractScript{

    private long timeBegan;
    private long timeRan;
    public static final int NAT_RUNE = 561;
    public static final int LAW_RUNE = 563;
    public static final int CASH = 995;
    public Set<Integer> nonAlch = new HashSet<>();
    private int stage;


    @Override
    public void onStart(){
        getSkillTracker().start(Skill.MAGIC);
        this.timeBegan = System.currentTimeMillis();
        nonAlch.add(NAT_RUNE);
        nonAlch.add(LAW_RUNE);
        checkTab();
        nonAlch.add(CASH);
        this.stage = 0;
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
        var1.drawString("Current XP: " + getSkills().getExperience(Skill.MAGIC), 5, 330);
        var1.drawString("Current Level: " + getSkills().getRealLevel(Skill.MAGIC), 5, 315);
        var1.drawString("XP Till Level: " + getSkills().getExperienceToLevel(Skill.MAGIC), 5, 300);
        var1.drawString("XP Gained: " + getSkillTracker().getGainedExperience(Skill.MAGIC), 5, 270);
        var1.setColor(Color.GREEN);
        var1.drawString("XP P/H: " + getSkillTracker().getGainedExperiencePerHour(Skill.MAGIC), 5, 255);
        var1.drawString("Time to Next Level: " + ft(getSkillTracker().getTimeToLevel(Skill.MAGIC)), 5, 285);
        var1.drawString("Running Time: " + ft(timeRan), 5, 240);
        var1.setColor(Color.RED);
        var1.drawString("STAGE: " + stage, 5, 225);
        var1.setColor(Color.GRAY);
    }

    private void checkTab(){
        if (!getTabs().isOpen(Tab.MAGIC))
            getTabs().open(Tab.MAGIC);
    }

    private boolean canAlch(){
        List<Item> items= filterItems(getInventory().all());
        return items.size() > 0 && stage == 1 && !getMagic().isSpellSelected() && getTabs().isOpen(Tab.MAGIC);
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
        stage = 2;
        sleep(750);
    }

    private void selectAlch(){
        List<Item> items = filterItems(getInventory().all());
        sleep(0,25);
        items.get(Calculations.random(0, items.size()-1)).interact();
        stage = 0;
        sleep(0,25);
    }

    private boolean canCast(){
        return getInventory().contains(563) && stage == 0 && !getMagic().isSpellSelected() && getTabs().isOpen(Tab.MAGIC);
    }

    private void castTele(){
        getMagic().castSpell(Normal.CAMELOT_TELEPORT);
        stage = 1;
    }

    private boolean canSelect(){
        return stage == 2 && !getLocalPlayer().isAnimating() && getMagic().isSpellSelected() && checkInventory();
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
        if (canCast())
            castTele();
        if (canAlch())
            castHighAlch();
        if (canSelect())
            selectAlch();
        return Calculations.random(25);
    }

}
