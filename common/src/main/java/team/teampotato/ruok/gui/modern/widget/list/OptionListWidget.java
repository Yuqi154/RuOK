package team.teampotato.ruok.gui.modern.widget.list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.teampotato.ruok.gui.modern.option.ModernOption;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OptionListWidget extends ContainerObjectSelectionList<OptionListWidget.WidgetEntry> {
    //private WidgetEntry hoveredEntry;
    public int rowLeft;
    public int rowWidth;
    @Nullable
    public OptionListWidget.WidgetEntry hoveredEntry;
    public OptionListWidget(Minecraft minecraftClient, int width, int height, int y, int y2, int itemHeight, int rowLeft) {
        super(minecraftClient, width, height-y, 0, itemHeight);
        this.rowLeft = rowLeft;
        //this.setRenderBackground(false);
        this.setRenderHeader(false,0);
        //this.setRenderHorizontalShadows(false);
    }
    //        this.width = width;
    //        this.height = height;
    //        this.top = top;
    //        this.bottom = bottom;
    //        this.itemHeight = itemHeight;
    //        this.left = 0;
    //        this.right = width;
    public void addOptionEntry(AbstractWidget option) {
        this.addEntry(WidgetEntry.create(option));
    }
    // 批量添加多个选项，可以自定义每个选项的宽高
    public void addAll(List<AbstractWidget> options) {
        for (AbstractWidget option : options) {
            this.addOptionEntry(option);
        }
    }

    public void addOptionEntry(ModernOption<?> option,int width) {
        this.addEntry(WidgetEntry.create(option,width));
    }
    // 批量添加多个选项，可以自定义每个选项的宽高
    public void addAll(List<ModernOption<?>> options, int width) {
        for (ModernOption<?> option : options) {
            this.addOptionEntry(option,width);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        WidgetEntry entry = this.getEntryAtPosition(mouseX, mouseY);
        if (entry != null) {
            if (entry.mouseClicked(mouseX, mouseY, button)) {
                WidgetEntry entry2 = this.getFocused();
                if (entry2 != entry && entry2 != null) {
                    ((ContainerEventHandler) entry2).setFocused(null);
                }

                this.setFocused(entry);
                this.setDragging(true);
                return true;
            }
        }
        return super.mouseClicked(mouseX,mouseY,button);

    }
    @Override
    public boolean mouseScrolled( double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            this.setScrollAmount(this.getScrollAmount() - verticalAmount * (double)this.itemHeight / 2.0); // ✅ 调整滚动速度
            return true;
        }
        return false;
    }



    @Override
    protected int getRowTop(int index) {
        return super.getRowTop(index);
    }

    @Override
    protected int getRowBottom(int index) {
        return super.getRowBottom(index);
    }

    @Override
    public int getRowLeft() {
        return rowLeft;
    }


    public void setRowLeft(int rowLeft) {
        this.rowLeft = rowLeft;
    }

    @Override
    protected void renderItem(GuiGraphics context, int mouseX, int mouseY, float delta, int index, int x, int y, int entryWidth, int entryHeight) {
        WidgetEntry entry = this.getEntry(index);
        entry.renderBack(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.getHovered(), entry), delta);
        entry.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.getHovered(), entry), delta);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return this.isFocused() ? NarrationPriority.FOCUSED : getTypes();
    }

    private @NotNull NarrationPriority getTypes() {
        if (this.isFocused()) {
            return NarrationPriority.FOCUSED;
        } else {
            return this.hoveredEntry != null ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }


    @Nullable
    @Override
    public OptionListWidget.WidgetEntry getHovered() {
        return hoveredEntry;
    }

    @Override
    public int getRowRight() {
        return super.getRowRight();
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int i = this.getScrollbarPosition();
        int j = i + 4;  // ✅ 总体滚动条区域宽度为 4px

        this.hoveredEntry = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

        this.enableScissor(context);
        this.renderListItems(context, mouseX, mouseY, delta);
        context.disableScissor();

        int m = this.getMaxScroll();
        if (m > 0) {
            int n = (int) ((float) ((this.getBottom() - this.getY()) * (this.getBottom() - this.getY())) / (float) this.getMaxPosition());
            n = Mth.clamp(n, 32, this.getBottom() - this.getY() - 8);

            // ✅ 整体向下偏移 2px（包括边框 & 滚动条）
            int yOffset = 2;
            int o = (int) this.getScrollAmount() * (this.getBottom() - this.getY() - n) / m + this.getY() + yOffset;

            // ✅ 防止滚动条超出底部边框
            int maxScrollY = this.getBottom() - n - yOffset;
            if (o > maxScrollY) {
                o = maxScrollY;
            }

            // ✅ 滚动条边框（淡灰色竖线）
            int borderColor = 0xA0AAAAAA; // 浅灰色
            context.fill(i - 1, this.getY() + yOffset, i, this.getBottom() + yOffset - 2, borderColor); // 左侧竖线
            context.fill(j, this.getY() + yOffset, j + 1, this.getBottom() + yOffset - 2, borderColor); // 右侧竖线

            // ✅ 添加上下横线，使边框更完整（底部边框上移 2px 以对齐滚动条）
            context.fill(i - 1, this.getY() + yOffset, j + 1, this.getY() + 1 + yOffset, borderColor); // 顶部横线
            context.fill(i - 1, this.getBottom() - 3 + yOffset, j + 1, this.getBottom() - 1 + yOffset, borderColor); // 底部横线（上移 2px）

            // ✅ **滚动条变成 2px 宽的竖线（黄绿色，半透明）**
            int trackX = i + 1; // 让竖线居中
            context.fill(trackX, o+2, trackX + 2, o + n, 0xFF88FF88); // 2px 亮绿色滚动条
        }

        this.renderDecorations(context, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRight() - 10;  // 让滚动条紧贴右侧
    }

    protected void enableScissor(GuiGraphics context) {
        context.enableScissor(this.getRowLeft(), this.getY(), this.getRowRight(), this.getBottom());//x1,y1,x2,y2
    }
    public void setX1(int x1) {
        this.setX(x1);
    }
    public void setY1(int y1) {
        this.setY(y1);
    }
    public void setX2(int x2) {
        this.setRowWidth(x2-getX());
    }
    public void setY2(int y2) {
//        this.getBottom() = y2;
    }

    @Override
    protected int getMaxPosition() {
        return super.getMaxPosition();
    }

    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    public void setRowWidth(int rowWidth) {
        this.rowWidth = rowWidth;
    }

    protected static class WidgetEntry extends ContainerObjectSelectionList.Entry<WidgetEntry> {
        final List<AbstractWidget> widgets;
        public WidgetEntry(Map<ModernOption<?>, AbstractWidget> widgetMap) {
            this.widgets = ImmutableList.copyOf(widgetMap.values());
        }
        public WidgetEntry(List<AbstractWidget> widgets) {
            this.widgets = widgets;
        }

        public static WidgetEntry create(ModernOption<?> option,int width) {
            AbstractWidget widget = option.createWidget(10,10,width);
            return new WidgetEntry(ImmutableMap.of(option,widget));
        }
        public static WidgetEntry create(AbstractWidget widget) {
            return new WidgetEntry(List.of(widget));
        }


        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int yaw = y;
            for (AbstractWidget widget : widgets) {
                widget.setY(yaw);
                widget.setX(x);
                //widget.setWidth(entryWidth);
                widget.render(context, mouseX, mouseY, tickDelta);
                yaw += widget.getHeight() + 15;
            }

        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.widgets;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.widgets;
        }
    }
}
