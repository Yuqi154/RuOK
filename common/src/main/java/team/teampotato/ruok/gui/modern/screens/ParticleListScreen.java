package team.teampotato.ruok.gui.modern.screens;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.gui.modern.widget.ButtonWidget;
import team.teampotato.ruok.util.render.ParticleRender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParticleListScreen extends Screen {
    private ParticleListWidget list;
    private EditBox textField;
    private final List<String> lists;
    public Screen parent;

    public ParticleListScreen(Component title, Screen parent, List<String> list) {
        super(title);
        this.font = Minecraft.getInstance().font;
        this.lists = list;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new ParticleListWidget(this,lists);
        this.addRenderableWidget(this.list);
        this.addRenderableWidget(ButtonWidget.builder(CommonComponents.GUI_DONE, (v) -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
        }).dimensions(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2, this.height - 29, 150, 20).build());

        this.textField = getTextField();
    }


    private EditBox getTextField() {
        return this.addRenderableWidget(
                new EditBox(
                        this.font,
                        this.width / 2 - 155,
                        this.height - 29,
                        150,
                        20,
                        Component.empty()
                )
        );
    }


    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
       // this.renderBackground(context);
        this.list.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        super.render(context, mouseX, mouseY, delta);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        String fieldText = this.textField.getValue().toLowerCase(); // 获取输入的文本，并转换为小写
        this.list.children().clear(); // 先清空现有列表中的所有条目
        List<ParticleType<?>> addedEntries = new ArrayList<>(); // 用于记录已经添加的条目
        for (ParticleType<?> type : BuiltInRegistries.PARTICLE_TYPE) {
            String translation = Objects.requireNonNull(Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getKey(type)).toLanguageKey());
            String trID = Component.translatable(translation).getString();
            String id = Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getId(type)).toString();
            if ((fieldText.isEmpty() || trID.contains(fieldText)) && !addedEntries.contains(type)) {
                ParticleListWidget.TypeEntry entry = this.list.createEntry(type);
                this.list.add(entry); // 将匹配的条目重新添加到列表中
                addedEntries.add(type); // 记录该条目已经添加
            }
            // 如果输入框为空，或者 id 或 trID 包含输入文本并且没有被添加过，则加入列表
            if ((fieldText.isEmpty() || id.contains(fieldText)) && !addedEntries.contains(type)) {
                ParticleListWidget.TypeEntry entry = this.list.createEntry(type);
                this.list.add(entry); // 将匹配的条目重新添加到列表中
                addedEntries.add(type); // 记录该条目已经添加
            }

        }
    }
    static class ParticleListWidget extends ContainerObjectSelectionList<ParticleListWidget.TypeEntry> {

        public static Minecraft client = Minecraft.getInstance();
        private final List<String> list;

        public ParticleListWidget(@NotNull ParticleListScreen bls, List<String> list) {
            super(client,bls.width , bls.height-64,  32, 20);
            this.list = list;
        }


        public TypeEntry createEntry(ParticleType<?> type){
            return new TypeEntry(type,list);
        }

        public void add(TypeEntry typeEntry){
            this.addEntry(typeEntry);

        }
        //TODO:!!!
        public static class TypeEntry extends Entry<TypeEntry> {
            public Minecraft client;
            public ParticleType<?> type;
            public AbstractWidget widget;
            public List<String> list;

            public TypeEntry(ParticleType<?> type,List<String> list) {
                this.type = type;
                this.client = Minecraft.getInstance();
                this.list = list;

                boolean isList = this.list.contains(Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getKey(type)).toString());
                this.widget = OptionInstance.createBoolean(
                        isList ? "ruok.options.gui.enable" : "ruok.options.gui.disable",
                        isList
                ).createButton(this.client.options, 60, 20, 60, this::onRenderButtonClick);

                this.refreshEntry();
            }

            private void onRenderButtonClick(boolean isEnabled) {
                String register = Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getKey(type)).toString();
                if (isEnabled) {
                    this.list.add(register);
                } else {
                    this.list.remove(register);
                }
                RuOK.save();
                ParticleRender.reloadList();
                this.refreshEntry();
            }

            public void refreshEntry() {
                this.widget.setMessage(this.list.contains(Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getKey(type)).toString())
                        ? Component.translatable("ruok.options.gui.enable")
                        : Component.translatable("ruok.options.gui.disable"));
            }
            @Override
            public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int textYPosition = y + entryHeight / 2 - 9 / 2;
                ResourceLocation text = Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getKey(type));
                context.drawString(client.font, Component.translatable(text.toLanguageKey()),x-20, textYPosition, 16777215, false);
                this.widget.setX(x + 160);
                this.widget.setY(y);
                this.widget.render(context, mouseX, mouseY, tickDelta);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return ImmutableList.of(this.widget);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return ImmutableList.of(this.widget);
            }
        }

    }


}
