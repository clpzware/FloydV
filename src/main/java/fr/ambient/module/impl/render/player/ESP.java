package fr.ambient.module.impl.render.player;

import fr.ambient.Ambient;
import fr.ambient.event.EventStage;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.ModelAnglesEvent;
import fr.ambient.event.impl.render.ModelRenderEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.AntiBot;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.MultiProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.model.ESPUtil;
import fr.ambient.util.render.shader.Shader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjglx.opengl.Display;
import org.lwjglx.util.glu.GLU;
import org.lwjglx.util.vector.Vector4f;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static fr.ambient.util.render.RenderUtil.drawRect;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

@SuppressWarnings("unused")
public class ESP extends Module {
    private final FloatBuffer windowPosition = GLAllocation.createDirectFloatBuffer(4);
    private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private final FloatBuffer modelMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer projectionMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final List<BlockPos> blocks = new CopyOnWriteArrayList<>();

    private final Map<EntityPlayer, float[][]> playerMap = new WeakHashMap<>();

    // Settings
    public final BooleanProperty esp2d = BooleanProperty.newInstance("2D ESP", true);
    private final MultiProperty components = MultiProperty.newInstance("Components", new String[]{"Box", "Healthbar", "Corners"}, Set.of("Box", "Healthbar"), esp2d::getValue);

    public final BooleanProperty outlines = BooleanProperty.newInstance("Outlines", true);
    public final NumberProperty thickness = NumberProperty.newInstance("Outline Thickness", 1f, 3f, 4f, 1f, outlines::getValue);
    public final NumberProperty fillAlpha = NumberProperty.newInstance("Fill Alpha", 0.0f, 0.33f, 0.5f, 0.01f, outlines::getValue);
    private final BooleanProperty teamColors = BooleanProperty.newInstance("Use Team Color", false, outlines::getValue);
    private final BooleanProperty glow = BooleanProperty.newInstance("Glow", false, outlines::getValue);
    private final BooleanProperty penisESP = BooleanProperty.newInstance("Penis ESP", false);
    private final NumberProperty penisSize = NumberProperty.newInstance("Penis Size", 0f, 0.5f, 1f, 0.1f, penisESP::getValue);
    private final BooleanProperty skeletons = BooleanProperty.newInstance("Skeletons", false);
    private final BooleanProperty bedESP = BooleanProperty.newInstance("Bed ESP", true);
    private final BooleanProperty bedOutline = BooleanProperty.newInstance("Bed Outline", true, bedESP::getValue);

    private final BooleanProperty chestesp = BooleanProperty.newInstance("Chest ESP", false);
    private final BooleanProperty enderChest = BooleanProperty.newInstance("Ender Chest", false,chestesp::getValue);

    private ModeProperty itemEsp = ModeProperty.newInstance("Item ESP", new String[]{"None", "Theme", "Item"}, "None");

    private final TimeUtil timeUtil = new TimeUtil();

    public Framebuffer entityFramebuffer;
    public Framebuffer outlineFramebuffer;

    public static boolean renderGlint = true;


    private final Shader fillShader = new Shader("fill.frag");
    private final Shader outlineShader = new Shader("outline.frag");

    public ESP() {
        super(50, "Highlights or outlines players and entities through walls.", ModuleCategory.RENDER);
        this.registerProperties(esp2d, components, outlines, thickness, fillAlpha, teamColors, glow, skeletons, bedESP, bedOutline, chestesp, enderChest,itemEsp);
    }

    @Override
    protected void onDisable() {
        this.blocks.clear();
        this.cachedBeds.clear();
    }

    @SubscribeEvent
    private void onRenderPlayer(ModelRenderEvent e) {
        if (e.getStage() == EventStage.POST && entityFramebuffer != null && outlines.getValue()) {
            if (isValid(e.getEntity())) {
                entityFramebuffer.bindFramebuffer(false);

                fillShader.start();
                fillShader.setUniformInteger("textureIn", 0);
                Color color = teamColors.getValue() ? PlayerUtil.getTeamColor(e.getEntity()).darker() : Ambient.getInstance().getHud().getCurrentTheme().color2.darker();
                fillShader.setUniformFloat("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f);

                GlStateManager.color(1, 1, 1, 1);
                renderGlint = false;
                e.drawEntityModel();
                e.drawEntityLayers();

                renderGlint = true;
                fillShader.stop();

                mc.getFramebuffer().bindFramebuffer(false);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }

    @SubscribeEvent
    private void onUpdateAngles(ModelAnglesEvent e) {
        final ModelPlayer model = e.getModel();

        playerMap.put(e.getPlayer(), getModelRots(model));
    }

    private Vector3d project(final int factor, final double x, final double y, final double z) {
        boolean success = GLU.gluProject(
                (float) x, (float) y, (float) z,
                ActiveRenderInfo.MODELVIEW,
                ActiveRenderInfo.PROJECTION,
                ActiveRenderInfo.VIEWPORT,
                ActiveRenderInfo.OBJECTCOORDS
        );

        if (!success) {
            return null;
        }

        double projectedX = ActiveRenderInfo.OBJECTCOORDS.get(0) / factor;
        double projectedY = (Display.getHeight() - ActiveRenderInfo.OBJECTCOORDS.get(1)) / factor;
        double projectedZ = ActiveRenderInfo.OBJECTCOORDS.get(2);

        return new Vector3d(projectedX, projectedY, projectedZ);
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent e) {
        entityFramebuffer = RenderUtil.createFrameBuffer(entityFramebuffer, false);
        outlineFramebuffer = RenderUtil.createFrameBuffer(outlineFramebuffer, false);

        if (outlines.getValue() && entityFramebuffer != null && outlineFramebuffer != null) {
            int originalFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

            mc.getFramebuffer().bindFramebuffer(false);

            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL_GREATER, 0);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            outlineShader.start();
            outlineShader.setUniformInteger("textureIn", 0);
            outlineShader.setUniformFloat("alpha", fillAlpha.getValue());
            outlineShader.setUniformFloat("texelSize",
                    thickness.getValue() / mc.displayWidth,
                    thickness.getValue() / mc.displayHeight);
            outlineShader.setUniformFloat("edgeThreshold", 1.0f);

            GlStateManager.setActiveTexture(GL_TEXTURE0);
            GlStateManager.bindTexture(entityFramebuffer.framebufferTexture);

            outlineShader.drawQuads();
            outlineShader.stop();

            GlStateManager.bindTexture(0);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();

            entityFramebuffer.framebufferClear();

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, originalFramebuffer);
        }

        if (esp2d.getValue() && components.getValues().length != 0) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!isValid(player) || mc.thePlayer.getDistanceToEntity(player) < 1.0f) continue;

                List<Vector3d> boxCorners = ESPUtil.getVectors(player);

                Vector4f pos = null;
                for (Vector3d corner : boxCorners) {
                    Vector3d projected = project(scaledResolution().getScaleFactor(), corner.x, corner.y, corner.z);

                    if (projected != null && projected.z >= 0.0 && projected.z < 1.0) {
                        if (pos == null) {
                            pos = new Vector4f((float) projected.x, (float) projected.y, (float) projected.x, (float) projected.y);
                        }
                        pos.x = (float) Math.min(projected.x, pos.x);
                        pos.y = (float) Math.min(projected.y, pos.y);
                        pos.z = (float) Math.max(projected.x, pos.z);
                        pos.w = (float) Math.max(projected.y, pos.w);
                    }
                }

                if (pos == null) continue;

                float x = pos.x;
                float y = pos.y;
                float x2 = pos.z;
                float y2 = pos.w;
                float width = x2 - x;
                float height = y2 - y;

                if (components.isSelected("Box")) {
                    drawRect(x - 0.5f, y + 1f, 1.5f, height - 1.5f, Color.BLACK);
                    drawRect(x - 0.5f, y - 0.5f, width + 1.5f, 1.5f, Color.BLACK);
                    drawRect(x2 - 0.5f, y + 1, 1.5f, height, Color.BLACK);
                    drawRect(x - 0.5f, y2 - 0.5f, width, 1.5f, Color.BLACK);

                    drawRect(x, y + 0.5f, 0.5f, height - 0.5f, Ambient.getInstance().getHud().getCurrentTheme().color2);
                    drawRect(x, y, width, 0.5f, Ambient.getInstance().getHud().getCurrentTheme().color2);
                    drawRect(x2, y, 0.5f, height, Ambient.getInstance().getHud().getCurrentTheme().color2);
                    drawRect(x, y2, width + 0.5f, 0.5f, Ambient.getInstance().getHud().getCurrentTheme().color2);
                }

                if (components.isSelected("Corners") && !components.isSelected("Box")) {
                    float cornerSize = (width + 2.5f) / 3;
                    float thickness = 0.5f;

                    drawRect(x - 0.5f, y - 0.5f, thickness + 1, cornerSize, Color.BLACK);
                    drawRect(x - 0.5f, y - 0.5f, cornerSize, thickness + 1, Color.BLACK);

                    drawRect(x - 0.5f, y + height - cornerSize, thickness + 1, cornerSize, Color.BLACK);
                    drawRect(x - 0.5f, y + height - thickness, cornerSize, thickness + 1, Color.BLACK);

                    drawRect(x + width - thickness - 0.5f, y - 0.5f, thickness + 1, cornerSize, Color.BLACK);
                    drawRect(x + width - cornerSize, y - 0.5f, cornerSize, thickness + 1, Color.BLACK);

                    drawRect(x + width - thickness - 0.5f, y + height - cornerSize, thickness + 1, cornerSize, Color.BLACK);
                    drawRect(x + width - cornerSize, y + height - thickness, cornerSize + 0.5f, thickness + 1, Color.BLACK);

                    Color themeColor = Ambient.getInstance().getHud().getCurrentTheme().color2;

                    drawRect(x, y, thickness, cornerSize - 1, themeColor);
                    drawRect(x, y, cornerSize - 1, thickness, themeColor);

                    drawRect(x + width - thickness, y, thickness, cornerSize - 1, themeColor);
                    drawRect(x + width - cornerSize + 0.5f, y, cornerSize - 1, thickness, themeColor);

                    drawRect(x, y + height - cornerSize + 0.5f, thickness, cornerSize - 0.5f, themeColor);
                    drawRect(x, y + height - thickness + 0.5f, cornerSize - 1, thickness, themeColor);

                    drawRect(x + width - thickness, y + height - cornerSize + 0.5f, thickness, cornerSize, themeColor);
                    drawRect(x + width - cornerSize + 0.5f, y + height - thickness + 0.5f, cornerSize - 1, thickness, themeColor);
                }

                if (components.isSelected("Healthbar")) {
                    float healthPercentage = MathHelper.clamp_float((player.getHealth() + player.getAbsorptionAmount()) / (player.getMaxHealth() + player.getAbsorptionAmount()), 0.0f, 1.0f);
                    float health = (y2 - y - 2) * (1.0f - healthPercentage);
                    drawRect(x - 2.5f, y - 0.5f, 1.5f, height + 1.5f, Color.BLACK);
                    drawRect(x - 2f, y + health, 0.5f, y2 - y - health + 0.5f, Color.GREEN);
                }
            }
        }
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent e) {
        AntiBot antiBotModule = Ambient.getInstance().getModuleManager().getModule(AntiBot.class);
        final float partialTicks = e.getPartialTicks();

        if(!itemEsp.is("None")){
            for(Entity entity : mc.theWorld.getLoadedEntityList()){
                if(entity instanceof EntityItem entityItem){
                    Color color = Ambient.getInstance().getHud().getCurrentTheme().getColor1();
                    if(itemEsp.is("Item")){
                        ItemStack stack = entityItem.getEntityItem();

                        if(stack.getItem() == Items.iron_ingot){
                            color = new Color(152, 152, 152);
                        }if(stack.getItem() == Items.gold_ingot){
                            color = new Color(204, 160, 0);
                        }if(stack.getItem() == Items.emerald){
                            color = new Color(0, 180, 19);
                        }if(stack.getItem() == Items.diamond){
                            color = new Color(0, 203, 210);
                        }
                    }
                    ESPUtil.filledInterpolatedESP(entity, color, 0.2f, 0.1f);


                }
            }
        }


        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if (!isValid(player)) {
                continue;
            }

            if (antiBotModule.isEnabled() && antiBotModule.isBot(player)) {
                continue;
            }

            if (skeletons.getValue()) {
                Render<?> renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player);

                if (renderer instanceof RenderPlayer renderPlayer && playerMap.containsKey(player)) {
                    float[][] modelRots = playerMap.get(player);

                    ESPUtil.renderSkeleton(player, modelRots, partialTicks);
                }
            }
        }

        if (chestesp.getValue()) {
            mc.theWorld.loadedTileEntityList.forEach(t -> {
                Color color = Ambient.getInstance().getHud().getCurrentTheme().getColor(8, 0);
                if (t instanceof TileEntityChest || (t instanceof TileEntityEnderChest && enderChest.getValue())) {
                    ESPUtil.filledBlockESP(t.getPos(), color, 0.2F);
                }
            });
        }
    }

    @NotNull
    private static float[][] getModelRots(ModelPlayer model) {
        return new float[][]{
                {model.bipedHead.rotateAngleX, model.bipedHead.rotateAngleY, model.bipedHead.rotateAngleZ},
                {model.bipedRightArm.rotateAngleX, model.bipedRightArm.rotateAngleY, -model.bipedRightArm.rotateAngleZ},
                {model.bipedLeftArm.rotateAngleX, model.bipedLeftArm.rotateAngleY, -model.bipedLeftArm.rotateAngleZ},
                {model.bipedRightLeg.rotateAngleX, model.bipedRightLeg.rotateAngleY, model.bipedRightLeg.rotateAngleZ},
                {model.bipedLeftLeg.rotateAngleX, model.bipedLeftLeg.rotateAngleY, model.bipedLeftLeg.rotateAngleZ}
        };
    }

    private final Set<BlockPos> cachedBeds = new HashSet<>();

    @SubscribeEvent
    public void onPlayerTick(UpdateEvent event) {
        new Thread(() -> {
            List<BlockPos> newBeds = new ArrayList<>();

            int distance = 10;
            for (int x = -distance; x < distance; x++) {
                for (int z = -distance; z < distance; z++) {
                    for (int y = -distance; y < distance; y++) {
                        BlockPos temp = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);

                        if (mc.theWorld.getBlockState(temp).getBlock() instanceof BlockBed) {
                            newBeds.add(temp);
                        }
                    }
                }
            }

            synchronized (blocks) {
                cachedBeds.removeIf(pos -> !(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed));
                cachedBeds.addAll(newBeds);

                blocks.clear();
                blocks.addAll(cachedBeds);
            }

            timeUtil.reset();
        }, "Block Finder").start();
    }

    @SubscribeEvent
    public void onRender3DEvent (Render3DEvent event) {
        if (bedESP.getValue()) {
            synchronized (blocks) {
                Set<BlockPos> renderedBlocks = new HashSet<>();

                for (BlockPos position : this.blocks) {
                    if (isBlockOrNeighborRendered(position, renderedBlocks))
                        continue;

                    AxisAlignedBB boundingBox = getMergedBoundingBox(position);

                    if (boundingBox != null) {
                        ESPUtil.renderFilledBB(boundingBox, Ambient.getInstance().getHud().getCurrentTheme().getColor(8, 0), 0.2F, bedOutline.getValue());
                        markBlocksAsRendered(renderedBlocks, position);
                    }
                }
            }
        }
    }

    private void markBlocksAsRendered(Set<BlockPos> renderedBlocks, BlockPos pos) {
        renderedBlocks.add(pos);

        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos neighborPos = pos.offset(direction);
            if (blocks.contains(neighborPos)) {
                renderedBlocks.add(neighborPos);
            }
        }
    }

    private boolean isBlockOrNeighborRendered(BlockPos pos, Set<BlockPos> renderedBlocks) {
        if (renderedBlocks.contains(pos)) {
            return true;
        }

        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos neighborPos = pos.offset(direction);
            if (blocks.contains(neighborPos) && renderedBlocks.contains(neighborPos)) {
                return true;
            }
        }

        return false;
    }

    public AxisAlignedBB getMergedBoundingBox(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        AxisAlignedBB boundingBox = block.getSelectedBoundingBox(mc.theWorld, pos);

        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos neighborPos = pos.offset(direction);
            if (blocks.contains(neighborPos)) {
                Block neighborBlock = mc.theWorld.getBlockState(neighborPos).getBlock();
                AxisAlignedBB neighborBoundingBox = neighborBlock.getSelectedBoundingBox(mc.theWorld, neighborPos);

                boundingBox = boundingBox.union(neighborBoundingBox);
            }
        }

        return boundingBox;
    }

    private boolean isValid(final Entity entity) {
        if (entity instanceof EntityPlayer player) {
            if (!entity.isEntityAlive()) {
                return false;
            }

            if (entity instanceof EntityPlayerSP) {
                return false;
            }

            AntiBot antibot = Ambient.getInstance().getModuleManager().getModule(AntiBot.class);
            if (antibot.isEnabled() && antibot.isBot(player)) {
                return false;
            }

            return RenderUtil.isBBInFrustum(entity.getEntityBoundingBox());
        }

        return false;
    }
}
