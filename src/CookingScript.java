import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.interact.Interactable;
import org.dreambot.api.wrappers.widgets.message.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

@ScriptManifest(name = "Bullets Cooker", description = "Cooks fish", author = "Bulletmagnet",
        version = 1.0, category = Category.COOKING, image = "")
public class CookingScript extends AbstractScript implements ChatListener, ActionListener {


    final Area CookingRange = new Area(3212, 3216, 3210, 3213, 0);
    String RawAnchovies = "Raw anchovies";
    String RawShrimps = "Raw shrimps";
    String CoookedFish = "Anchovies";
    private Timer timeRan;
    int fishCooked = 0;
    int beginningXP;
    int currentXp;
    int xpGained;
    private final Color color1 = new Color(51, 51, 51, 147);
    private final Color color2 = new Color(138, 54, 15);
    private final Color color3 = new Color(255, 255, 255);
    private final BasicStroke stroke1 = new BasicStroke(5);
    private DrawMouseUtil drawMouseUtil = new DrawMouseUtil();

    public String getFish() {
        return fish;
    }

    public void setFish(String fish) {
        this.fish = fish;
    }

    public String fish;

    public boolean isStart() {
        return Start;
    }

    public void setStart(boolean start) {
        Start = start;
    }

    public boolean Start;

    public void onStart() {
        GUI();
        drawMouseUtil.setRandomColor();
        SkillTracker.start(Skill.COOKING);
        beginningXP = Skills.getExperience(Skill.COOKING);
        Timer TTLV = new Timer();
        timeRan = new Timer();
        log("Waiting for gui to say start");
        if (Start) {
            if (Inventory.isEmpty()) {
                bank();
            } else if (Inventory.contains(fish)) {
                cook();
            }
            log("Starting Bullets Cooker");
        }
    }


    @Override
    public int onLoop() {
        if (Start) {
            if (Inventory.isEmpty()) {
                bank();
            } else {
                cook();
            }
        }
        return 0;
    }

    public void onGameMessage(Message message) {
        if (message.getMessage().contains("You successfully cook some anchovies")) {
            fishCooked++;
        }
    }

    public void cook() {
        if (!CookingRange.contains(getLocalPlayer()) && Inventory.contains(getFish())) {
            Walking.walk(CookingRange);
            sleep(1000, 4500);
        } else if (CookingRange.contains(getLocalPlayer()) && Inventory.contains(getFish())) {
            sleep(1000, 3000);
            GameObjects.closest("Cooking range").interact("Cook");
            sleep(1000, 3000);
            Keyboard.type(1);
            sleep(1000, 5000);
            Mouse.moveMouseOutsideScreen();
            sleepUntil(() -> getLocalPlayer().getAnimation() == -1, 56000);
            sleep(3000);
        } else {
            bank();
        }
    }

    public void bank() {
        if (Inventory.isEmpty() || Inventory.contains(CoookedFish)) {
            Walking.walk(BankLocation.LUMBRIDGE.getCenter());
            sleep(1000, 2500);
            Bank.openClosest();
            sleep(500, 1200);
            Bank.depositAllItems();
            sleep(500, 1000);
            Bank.withdraw(getFish(), 28);
            sleep(500, 1000);
            Bank.close();
        } else {
            cook();
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        drawMouseUtil.drawRandomMouse(g);
        drawMouseUtil.drawRandomMouseTrail(g);
        long ttl = SkillTracker.getTimeToLevel(Skill.COOKING);
        long timeTNL = ttl;
        g.setColor(Color.CYAN);
        g.drawRect(10, 250, 300, 400);


        Timer.formatTime(ttl);
        Polygon tile = Map.getPolygon(getLocalPlayer().getTile());

        g.drawPolygon(tile);
        currentXp = Skills.getExperience(Skill.COOKING);
        xpGained = currentXp - beginningXP;
        SkillTracker.getTimeToLevel(Skill.COOKING);
        g.setColor(color1);
        g.fillRect(10, 250, 300, 350);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.setColor(color3);
        g.drawString("Bullets fish cooker", 110, 280);
        g.drawString("current xp: " + currentXp, 200, 450);
        g.drawString("Things Cooked Successfully: " + fishCooked, 130, 425);
        g.drawString("Time Ran: " + timeRan.formatTime(), 200, 400);
        g.drawString("XP GAINED: " + xpGained, 200, 375);
        g.drawString("Current level: " + Skills.getRealLevel(Skill.COOKING), 200, 350);
        g.drawString("Time tell level: " + ft(timeTNL), 20, 300);



    }

    private String ft(long duration) {
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

    public void GUI (){
        JFrame frame = new JFrame();
        frame.setTitle("FishSettings");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(180, 160);
        frame.setVisible(true);
        JButton start = new JButton();
        start.addActionListener(this);
        start.setText("START");
        JButton shrimp = new JButton();
        JButton anchovies = new JButton();
        shrimp.setText("Shrimp");
        anchovies.setText("Anchovies");
        start.setBounds(0,10, 50, 50);
        shrimp.setBounds(50, 10, 50, 50);
        anchovies.setBounds(100, 10, 60, 50);
        shrimp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SetShrimps();
            }
        });
        anchovies.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SetAnchovies();
            }
        });


        frame.add(start);
        frame.add(shrimp);
        frame.add(anchovies);
        frame.getContentPane().setLayout(null);


    }
    public void SetShrimps(){
        setFish(RawShrimps);
        log("Set fish to cook to Raw Shrimps");
    }
    public void SetAnchovies(){
        setFish(RawAnchovies);
        log("Set fish to cook to Raw Anchovies");


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setStart(true);


    }
}

