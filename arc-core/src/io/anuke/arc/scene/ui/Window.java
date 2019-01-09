package io.anuke.arc.scene.ui;

import io.anuke.arc.graphics.Camera;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.BitmapFont;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.Scene;
import io.anuke.arc.scene.event.InputEvent;
import io.anuke.arc.scene.event.InputListener;
import io.anuke.arc.scene.event.Touchable;
import io.anuke.arc.scene.style.Drawable;
import io.anuke.arc.scene.style.SkinReader.ReadContext;
import io.anuke.arc.scene.style.Style;
import io.anuke.arc.scene.ui.Label.LabelStyle;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.util.Align;

import static io.anuke.arc.Core.scene;

/**
 * A table that can be dragged and act as a modal window. The top padding is used as the window's title height.
 * <p>
 * The preferred size of a window is the preferred size of the title text and the children as laid out by the table. After adding
 * children to the window, it can be convenient to call {@link #pack()} to size the window to the size of the children.
 * @author Nathan Sweet
 */
public class Window extends Table{
    static private final Vector2 tmpPosition = new Vector2();
    static private final Vector2 tmpSize = new Vector2();
    static private final int MOVE = 1 << 5;
    protected int edge;
    protected boolean dragging;
    boolean isMovable = false, isModal, isResizable, center = true;
    int resizeBorder = 8;
    boolean keepWithinStage = true;
    boolean drawTitleTable;
    private WindowStyle style;

    public final Label title;
    public final Table titleTable;

    public Window(String title){
        this(title, scene.skin.get(WindowStyle.class));
    }

    public Window(String title, String styleName){
        this(title, scene.skin.get(styleName, WindowStyle.class));
    }

    public Window(String title, WindowStyle style){
        if(title == null) throw new IllegalArgumentException("title cannot be null.");
        touchable(Touchable.enabled);
        setClip(true);

        this.title = new Label(title, new LabelStyle(style.titleFont, style.titleFontColor));
        this.title.setEllipsis(true);

        titleTable = new Table(){
            @Override
            public void draw(){
                if(drawTitleTable) super.draw();
            }
        };
        titleTable.add(this.title).expandX().fillX().minWidth(0);
        addChild(titleTable);

        setStyle(style);
        setWidth(150);
        setHeight(150);

        addCaptureListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                toFront();
                return false;
            }
        });
        addListener(new InputListener(){
            float startX, startY, lastX, lastY;

            private void updateEdge(float x, float y){
                float border = resizeBorder / 2f;
                float width = getWidth(), height = getHeight();
                float padTop = getMarginTop(), padRight = getMarginRight();
                float right = width - padRight;
                edge = 0;
                if(isResizable && x >= getMarginLeft() - border && x <= right + border && y >= getMarginBottom() - border){
                    if(x < getMarginLeft() + border) edge |= Align.left;
                    if(x > right - border) edge |= Align.right;
                    if(y < getMarginBottom() + border) edge |= Align.bottom;
                    if(edge != 0) border += 25;
                    if(x < getMarginLeft() + border) edge |= Align.left;
                    if(x > right - border) edge |= Align.right;
                    if(y < getMarginBottom() + border) edge |= Align.bottom;
                }
                if(isMovable && edge == 0 && y <= height && y >= height - padTop && x >= getMarginLeft() && x <= right)
                    edge = MOVE;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(button == KeyCode.MOUSE_LEFT){
                    updateEdge(x, y);
                    dragging = edge != 0;
                    startX = x;
                    startY = y;
                    lastX = x - getWidth();
                    lastY = y - getHeight();
                }
                return edge != 0 || isModal;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                dragging = false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                if(!dragging) return;
                float width = getWidth(), height = getHeight();
                float windowX = getX(), windowY = getY();

                float minWidth = getMinWidth();
                float minHeight = getMinHeight();
                Scene stage = getScene();
                boolean clampPosition = keepWithinStage && getParent() == stage.root;

                if((edge & MOVE) != 0){
                    float amountX = x - startX, amountY = y - startY;
                    windowX += amountX;
                    windowY += amountY;
                }
                if((edge & Align.left) != 0){
                    float amountX = x - startX;
                    if(width - amountX < minWidth) amountX = -(minWidth - width);
                    if(clampPosition && windowX + amountX < 0) amountX = -windowX;
                    width -= amountX;
                    windowX += amountX;
                }
                if((edge & Align.bottom) != 0){
                    float amountY = y - startY;
                    if(height - amountY < minHeight) amountY = -(minHeight - height);
                    if(clampPosition && windowY + amountY < 0) amountY = -windowY;
                    height -= amountY;
                    windowY += amountY;
                }
                if((edge & Align.right) != 0){
                    float amountX = x - lastX - width;
                    if(width + amountX < minWidth) amountX = minWidth - width;
                    if(clampPosition && windowX + width + amountX > stage.getWidth())
                        amountX = stage.getWidth() - windowX - width;
                    width += amountX;
                }
                if((edge & Align.top) != 0){
                    float amountY = y - lastY - height;
                    if(height + amountY < minHeight) amountY = minHeight - height;
                    if(clampPosition && windowY + height + amountY > stage.getHeight())
                        amountY = stage.getHeight() - windowY - height;
                    height += amountY;
                }
                setBounds(Math.round(windowX), Math.round(windowY), Math.round(width), Math.round(height));
            }

            public boolean mouseMoved(InputEvent event, float x, float y){
                updateEdge(x, y);
                return isModal;
            }

            public boolean scrolled(InputEvent event, float x, float y, int amount){
                return isModal;
            }

            public boolean keyDown(InputEvent event, int keycode){
                return isModal;
            }

            public boolean keyUp(InputEvent event, int keycode){
                return isModal;
            }

            public boolean keyTyped(InputEvent event, char character){
                return isModal;
            }
        });
    }

    /**
     * Returns the window's style. Modifying the returned style may not have an effect until {@link #setStyle(WindowStyle)} is
     * called.
     */
    public WindowStyle getStyle(){
        return style;
    }

    public void setStyle(WindowStyle style){
        if(style == null) throw new IllegalArgumentException("style cannot be null.");
        this.style = style;
        setBackground(style.background);
        //title.setStyle(new LabelStyle(style.titleFont, style.titleFontColor));
        invalidateHierarchy();
    }

    void keepWithinStage(){
        if(!keepWithinStage) return;
        Scene stage = getScene();
        Camera camera = stage.getCamera();
        float parentWidth = stage.getWidth();
        float parentHeight = stage.getHeight();
        if(getX(Align.right) - camera.position.x > parentWidth / 2)
            setPosition(camera.position.x + parentWidth / 2, getY(Align.right), Align.right);
        if(getX(Align.left) - camera.position.x < -parentWidth / 2)
            setPosition(camera.position.x - parentWidth / 2, getY(Align.left), Align.left);
        if(getY(Align.top) - camera.position.y > parentHeight / 2)
            setPosition(getX(Align.top), camera.position.y + parentHeight / 2, Align.top);
        if(getY(Align.bottom) - camera.position.y < -parentHeight / 2)
            setPosition(getX(Align.bottom), camera.position.y - parentHeight / 2, Align.bottom);
    }

    @Override
    public void draw(){
        Scene stage = getScene();
        if(stage.getKeyboardFocus() == null) stage.setKeyboardFocus(this);

        keepWithinStage();
        if(center && !isMovable && this.getActions().size == 0)
            centerWindow();

        if(style.stageBackground != null){
            stageToLocalCoordinates(tmpPosition.set(0, 0));
            stageToLocalCoordinates(tmpSize.set(stage.getWidth(), stage.getHeight()));
            drawStageBackground(getX() + tmpPosition.x, getY() + tmpPosition.y, getX() + tmpSize.x,
            getY() + tmpSize.y);
        }

        super.draw();
    }

    protected void drawStageBackground(float x, float y, float width, float height){
        Color color = getColor();
        Draw.color(color.r, color.g, color.b, color.a * parentAlpha);
        style.stageBackground.draw(x, y, width, height);
    }

    protected void drawDefaultBackground(float x, float y){
        super.drawBackground(x, y);
    }

    protected void drawBackground(float x, float y){
        super.drawBackground(x, y);

        // Manually draw the title table before clipping is done.
        titleTable.getColor().a = getColor().a;
        float padTop = getMarginTop(), padLeft = getMarginLeft();
        titleTable.setSize(getWidth() - padLeft - getMarginRight(), padTop);
        titleTable.setPosition(padLeft, getHeight() - padTop);
        drawTitleTable = true;
        titleTable.draw();
        drawTitleTable = false; // Avoid drawing the title table again in drawChildren.
    }

    public Element hit(float x, float y, boolean touchable){
        Element hit = super.hit(x, y, touchable);
        if(hit == null && isModal && (!touchable || getTouchable() == Touchable.enabled)) return this;
        float height = getHeight();
        if(hit == null || hit == this) return hit;
        if(y <= height && y >= height - getMarginTop() && x >= 0 && x <= getWidth()){
            // Hit the title bar, don't use the hit child if it is in the Window's table.
            Element current = hit;
            while(current.getParent() != this)
                current = current.getParent();
            if(getCell(current) != null) return this;
        }
        return hit;
    }

    /** Centers the dialog in the scene. */
    public void centerWindow(){
        Scene stage = getScene();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
    }

    public boolean isMovable(){
        return isMovable;
    }

    public void setMovable(boolean isMovable){
        this.isMovable = isMovable;
    }

    public boolean isModal(){
        return isModal;
    }

    public void setModal(boolean isModal){
        this.isModal = isModal;
    }

    public void setKeepWithinStage(boolean keepWithinStage){
        this.keepWithinStage = keepWithinStage;
    }

    public boolean isCentered(){
        return center;
    }

    public void setCentered(boolean center){
        this.center = center;
    }

    public boolean isResizable(){
        return isResizable;
    }

    public void setResizable(boolean isResizable){
        this.isResizable = isResizable;
    }

    public void setResizeBorder(int resizeBorder){
        this.resizeBorder = resizeBorder;
    }

    public boolean isDragging(){
        return dragging;
    }

    public float getPrefWidth(){
        return Math.max(super.getPrefWidth(), titleTable.getPrefWidth() + getMarginLeft() + getMarginRight());
    }

    /**
     * The style for a window, see {@link Window}.
     * @author Nathan Sweet
     */
    public static class WindowStyle extends Style{
        /** Optional. */
        public Drawable background;
        public BitmapFont titleFont;
        /** Optional. */
        public Color titleFontColor = new Color(1, 1, 1, 1);
        /** Optional. */
        public Drawable stageBackground;

        public WindowStyle(){
        }

        @Override
        public void read(ReadContext read){
            background = read.draw("background");
            titleFont = read.rfont("titleFont");
            titleFontColor = read.color("titleFontColor");
            stageBackground = read.draw("stageBackground");
        }
    }
}
