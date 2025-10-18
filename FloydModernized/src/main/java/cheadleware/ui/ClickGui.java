package cheadleware.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cheadleware.Cheadleware;
import cheadleware.module.Module;
import cheadleware.module.modules.AntiFireball;
import cheadleware.module.modules.Combat.*;
import cheadleware.module.modules.Misc.*;
import cheadleware.module.modules.Movement.*;
import cheadleware.module.modules.Player.*;
import cheadleware.module.modules.Render.*;
import cheadleware.ui.components.CategoryComponent;
import cheadleware.util.font.FontManager;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ClickGui extends GuiScreen {
    private static ClickGui instance;
    private final File configFile = new File("./config/Myau/", "clickgui.txt");
    private final ArrayList<CategoryComponent> categoryList;

    public ClickGui() {
        instance = this;

        List<Module> combatModules = new ArrayList<>();
        combatModules.add(Cheadleware.moduleManager.getModule(AimAssist.class));
        combatModules.add(Cheadleware.moduleManager.getModule(AutoClicker.class));
        combatModules.add(Cheadleware.moduleManager.getModule(KillAura.class));
        combatModules.add(Cheadleware.moduleManager.getModule(Wtap.class));
        combatModules.add(Cheadleware.moduleManager.getModule(Velocity.class));
        combatModules.add(Cheadleware.moduleManager.getModule(Reach.class));
        combatModules.add(Cheadleware.moduleManager.getModule(TargetStrafe.class));
        combatModules.add(Cheadleware.moduleManager.getModule(NoHitDelay.class));
        combatModules.add(Cheadleware.moduleManager.getModule(AntiFireball.class));
        combatModules.add(Cheadleware.moduleManager.getModule(LagRange.class));

        List<Module> movementModules = new ArrayList<>();
        movementModules.add(Cheadleware.moduleManager.getModule(Fly.class));
        movementModules.add(Cheadleware.moduleManager.getModule(Speed.class));
        movementModules.add(Cheadleware.moduleManager.getModule(LongJump.class));
        movementModules.add(Cheadleware.moduleManager.getModule(Sprint.class));
        movementModules.add(Cheadleware.moduleManager.getModule(SafeWalk.class));
        movementModules.add(Cheadleware.moduleManager.getModule(Jesus.class));
        movementModules.add(Cheadleware.moduleManager.getModule(Blink.class));
        movementModules.add(Cheadleware.moduleManager.getModule(NoFall.class));
        movementModules.add(Cheadleware.moduleManager.getModule(NoSlow.class));
        movementModules.add(Cheadleware.moduleManager.getModule(KeepSprint.class));
        movementModules.add(Cheadleware.moduleManager.getModule(Eagle.class));
        movementModules.add(Cheadleware.moduleManager.getModule(NoJumpDelay.class));
        movementModules.add(Cheadleware.moduleManager.getModule(AntiVoid.class));

        List<Module> renderModules = new ArrayList<>();
        renderModules.add(Cheadleware.moduleManager.getModule(Ambience.class));
        renderModules.add(Cheadleware.moduleManager.getModule(Animations.class));
        renderModules.add(Cheadleware.moduleManager.getModule(ESP.class));
        renderModules.add(Cheadleware.moduleManager.getModule(Chams.class));
        renderModules.add(Cheadleware.moduleManager.getModule(FullBright.class));
        renderModules.add(Cheadleware.moduleManager.getModule(Tracers.class));
        renderModules.add(Cheadleware.moduleManager.getModule(NameTags.class));
        renderModules.add(Cheadleware.moduleManager.getModule(Xray.class));
        renderModules.add(Cheadleware.moduleManager.getModule(TargetHUD.class));
        renderModules.add(Cheadleware.moduleManager.getModule(Indicators.class));
        renderModules.add(Cheadleware.moduleManager.getModule(BedESP.class));
        renderModules.add(Cheadleware.moduleManager.getModule(ItemESP.class));
        renderModules.add(Cheadleware.moduleManager.getModule(ViewClip.class));
        renderModules.add(Cheadleware.moduleManager.getModule(NoHurtCam.class));
        renderModules.add(Cheadleware.moduleManager.getModule(PostProcessing.class));
        renderModules.add(Cheadleware.moduleManager.getModule(HUD.class));
        renderModules.add(Cheadleware.moduleManager.getModule(GuiModule.class));
        renderModules.add(Cheadleware.moduleManager.getModule(ChestESP.class));
        renderModules.add(Cheadleware.moduleManager.getModule(Trajectories.class));

        List<Module> playerModules = new ArrayList<>();
        playerModules.add(Cheadleware.moduleManager.getModule(AutoHeal.class));
        playerModules.add(Cheadleware.moduleManager.getModule(AutoTool.class));
        playerModules.add(Cheadleware.moduleManager.getModule(ChestStealer.class));
        playerModules.add(Cheadleware.moduleManager.getModule(InvManager.class));
        playerModules.add(Cheadleware.moduleManager.getModule(InvWalk.class));
        playerModules.add(Cheadleware.moduleManager.getModule(Scaffold.class));
        playerModules.add(Cheadleware.moduleManager.getModule(SpeedMine.class));
        playerModules.add(Cheadleware.moduleManager.getModule(FastPlace.class));
        playerModules.add(Cheadleware.moduleManager.getModule(GhostHand.class));
        playerModules.add(Cheadleware.moduleManager.getModule(MCF.class));
        playerModules.add(Cheadleware.moduleManager.getModule(AntiDebuff.class));

        List<Module> miscModules = new ArrayList<>();
        miscModules.add(Cheadleware.moduleManager.getModule(Spammer.class));
        miscModules.add(Cheadleware.moduleManager.getModule(BedNuker.class));
        miscModules.add(Cheadleware.moduleManager.getModule(BedTracker.class));
        miscModules.add(Cheadleware.moduleManager.getModule(LightningTracker.class));
        miscModules.add(Cheadleware.moduleManager.getModule(NoRotate.class));
//        miscModules.add(Cheadleware.moduleManager.getModule(Radio.class));
        miscModules.add(Cheadleware.moduleManager.getModule(NickHider.class));
        miscModules.add(Cheadleware.moduleManager.getModule(AntiObbyTrap.class));
        miscModules.add(Cheadleware.moduleManager.getModule(AntiObfuscate.class));

        Comparator<Module> comparator = Comparator.comparing(m -> m.getName().toLowerCase());
        combatModules.sort(comparator);
        movementModules.sort(comparator);
        renderModules.sort(comparator);
        playerModules.sort(comparator);
        miscModules.sort(comparator);

        Set<Module> registered = new HashSet<>();
        registered.addAll(combatModules);
        registered.addAll(movementModules);
        registered.addAll(renderModules);
        registered.addAll(playerModules);
        registered.addAll(miscModules);

        for (Module module : Cheadleware.moduleManager.modules.values()) {
            if (!registered.contains(module)) {
                throw new RuntimeException(module.getClass().getName() + " is unregistered to click gui.");
            }
        }

        this.categoryList = new ArrayList<>();
        int xOffset = 20;
        int spacing = 135;

        CategoryComponent combat = new CategoryComponent("Combat", combatModules);
        combat.setX(xOffset);
        combat.setY(20);
        categoryList.add(combat);
        xOffset += spacing;

        CategoryComponent movement = new CategoryComponent("Movement", movementModules);
        movement.setX(xOffset);
        movement.setY(20);
        categoryList.add(movement);
        xOffset += spacing;

        CategoryComponent render = new CategoryComponent("Render", renderModules);
        render.setX(xOffset);
        render.setY(20);
        categoryList.add(render);
        xOffset += spacing;

        CategoryComponent player = new CategoryComponent("Player", playerModules);
        player.setX(xOffset);
        player.setY(20);
        categoryList.add(player);
        xOffset += spacing;

        CategoryComponent misc = new CategoryComponent("Misc", miscModules);
        misc.setX(xOffset);
        misc.setY(20);
        categoryList.add(misc);

        loadPositions();
    }

    public static ClickGui getInstance() {
        return instance;
    }

    public void initGui() {
        super.initGui();
    }

    public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();

        for (CategoryComponent category : categoryList) {
            category.render(this.fontRendererObj);
            category.handleDrag(x, y);

            for (Component module : category.getModules()) {
                module.update(x, y);
            }
        }

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            int scrollDir = wheel > 0 ? 1 : -1;
            for (CategoryComponent category : categoryList) {
                category.onScroll(x, y, scrollDir);
            }
        }
    }

    public void mouseClicked(int x, int y, int mouseButton) {
        Iterator<CategoryComponent> btnCat = categoryList.iterator();
        while (true) {
            CategoryComponent category;
            do {
                do {
                    if (!btnCat.hasNext()) {
                        return;
                    }

                    category = btnCat.next();
                    if (category.insideArea(x, y) && !category.isHovered(x, y) && !category.mousePressed(x, y) && mouseButton == 0) {
                        category.mousePressed(true);
                        category.xx = x - category.getX();
                        category.yy = y - category.getY();
                    }

                    if (category.mousePressed(x, y) && mouseButton == 0) {
                        category.setOpened(!category.isOpened());
                    }

                    if (category.isHovered(x, y) && mouseButton == 0) {
                        category.setPin(!category.isPin());
                    }
                } while (!category.isOpened());
            } while (category.getModules().isEmpty());

            for (Component c : category.getModules()) {
                c.mouseDown(x, y, mouseButton);
            }
        }
    }

    public void mouseReleased(int x, int y, int s) {
        if (s == 0) {
            Iterator<CategoryComponent> iterator = categoryList.iterator();

            CategoryComponent categoryComponent;
            while (iterator.hasNext()) {
                categoryComponent = iterator.next();
                categoryComponent.mousePressed(false);
            }

            iterator = categoryList.iterator();

            while (true) {
                do {
                    do {
                        if (!iterator.hasNext()) {
                            return;
                        }

                        categoryComponent = iterator.next();
                    } while (!categoryComponent.isOpened());
                } while (categoryComponent.getModules().isEmpty());

                for (Component component : categoryComponent.getModules()) {
                    component.mouseReleased(x, y, s);
                }
            }
        }
    }

    public void keyTyped(char typedChar, int key) {
        if (key == 1) {
            mc.displayGuiScreen(null);
        } else {
            Iterator<CategoryComponent> btnCat = categoryList.iterator();

            while (true) {
                CategoryComponent cat;
                do {
                    do {
                        if (!btnCat.hasNext()) {
                            return;
                        }

                        cat = btnCat.next();
                    } while (!cat.isOpened());
                } while (cat.getModules().isEmpty());

                for (Component component : cat.getModules()) {
                    component.keyTyped(typedChar, key);
                }
            }
        }
    }

    public void onGuiClosed() {
        savePositions();
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private void savePositions() {
        JsonObject json = new JsonObject();
        for (CategoryComponent cat : categoryList) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", cat.getX());
            pos.addProperty("y", cat.getY());
            pos.addProperty("open", cat.isOpened());
            json.add(cat.getName(), pos);
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPositions() {
        if (!configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            for (CategoryComponent cat : categoryList) {
                if (json.has(cat.getName())) {
                    JsonObject pos = json.getAsJsonObject(cat.getName());
                    cat.setX(pos.get("x").getAsInt());
                    cat.setY(pos.get("y").getAsInt());
                    cat.setOpened(pos.get("open").getAsBoolean());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}