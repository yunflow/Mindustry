package mindustry.logic;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.core.GameState.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LExecutor.*;
import mindustry.logic.LStatements.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.*;
import static mindustry.logic.LCanvas.*;

public class LogicDialog extends BaseDialog{
    public static final float PAD_MARGIN = 10f;
    public static final float DEFAULT_SIZE_WIDTH = 280f;
    public static final float DEFAULT_SIZE_HEIGHT = 60f;
    public static final float MARGIN_LEFT = 12f;
    public static final float BUTTON_DEFAULT_WIDTH = 160f;
    public static final float BUTTON_DEFAULT_HEIGHT = 64f;
    public static final float PAD_RIGHT = 16f;
    public static final float TABLE_HEIGHT = 45f;
    public static final float CAT_WIDTH = 130f;
    public static final float CAT_HEIGHT = 50f;
    public static final float GRAPHICS_OFFSET = 0.8f;
    public static final int CAT_ROW = 3;
    public static final float IMAGE_PAD = 10f;
    public static final float PERIOD = 15f;
    public static final int PAD_LEFT = 4;
    public static final float WIDTH = 140f;
    public static final double DOUBLE_MIN = 0.00001;
    public static final float DURATION = 0.2f;
    public static final float IMAGE_OFFSET = 5f;
    public static final float STUB = 8f;
    public static final float MUL = 0.5f;
    public static final int COLSPAN = 6;
    public static final int HEIGHT = 4;
    public LCanvas canvas;
    Cons<String> consumer = s -> {};
    boolean privileged;
    @Nullable LExecutor executor;

    public LogicDialog(){
        super("logic");

        clearChildren();

        canvas = new LCanvas();
        shouldPause = true;

        addCloseListener();

        shown(this::setup);
        hidden(() -> consumer.get(canvas.save()));
        onResize(() -> {
            setup();
            canvas.rebuild();
        });

        add(canvas).grow().name("canvas");

        row();

        add(buttons).growX().name("canvas");
    }

    private Color typeColor(Var s, Color color){
        return color.set(
            !s.isobj ? Pal.place :
            s.objval == null ? Color.darkGray :
            s.objval instanceof String ? Pal.ammo :
            s.objval instanceof Content ? Pal.logicOperations :
            s.objval instanceof Building ? Pal.logicBlocks :
            s.objval instanceof Unit ? Pal.logicUnits :
            s.objval instanceof Team ? Pal.logicUnits :
            s.objval instanceof Enum<?> ? Pal.logicIo :
            Color.white
        );
    }

    private String typeName(Var s){
        return
            !s.isobj ? "number" :
            s.objval == null ? "null" :
            s.objval instanceof String ? "string" :
            s.objval instanceof Content ? "content" :
            s.objval instanceof Building ? "building" :
            s.objval instanceof Team ? "team" :
            s.objval instanceof Unit ? "unit" :
            s.objval instanceof Enum<?> ? "enum" :
            "unknown";
    }

    private void setup(){
        buttons.clearChildren();
        buttons.defaults().size(BUTTON_DEFAULT_WIDTH, BUTTON_DEFAULT_HEIGHT);
        buttons.button("@back", Icon.left, this::hide).name("back");

        buttons.button("@edit", Icon.edit, this::buttonEdit).name("edit");

        if(Core.graphics.isPortrait()) buttons.row();

        Runnable buttonExecute = this::buttonExecute;
        Boolf<TextButton> textButtonBoolf = b -> executor == null || executor.vars.length == 0;
        buttons.button("@variables", Icon.menu, buttonExecute).name("variables").disabled(textButtonBoolf);

        Boolf<TextButton> textButtonBool1 = t -> canvas.statements.getChildren().size >= LExecutor.maxInstructions;
        buttons.button("@add", Icon.add, this::buttonAdd).disabled(textButtonBool1);
    }

    private void buttonAdd() {
        BaseDialog dialog = new BaseDialog("@add");
        dialog.cont.table(table -> {
            table.background(Tex.button);
            table.pane(t -> paneList(dialog, t)).grow();
        }).fill().maxHeight(Core.graphics.getHeight() * GRAPHICS_OFFSET);
        dialog.addCloseButton();
        dialog.show();
    }

    private void paneList(BaseDialog dialog, Table t) {
        for(Prov<LStatement> prov : LogicIO.allStatements){
            LStatement example = prov.get();
            boolean exampleValid = example.hidden() || (example.privileged() && !privileged);
            boolean examplePri = exampleValid || (example.nonPrivileged() && privileged);
            if(example instanceof InvalidStatement || examplePri) continue;

            LCategory category = example.category();
            Table cat = t.find(category.name);
            if(cat == null){
                cat = getTable(t, category);
            }

            TextButtonStyle style = new TextButtonStyle(Styles.flatt);
            style.fontColor = category.color;
            style.font = Fonts.outline;

            cat.button(example.name(), style, () -> {
                canvas.add(prov.get());
                dialog.hide();
            }).size(CAT_WIDTH, CAT_HEIGHT).self(c -> tooltip(c, "lst." + example.name())).top().left();

            if(cat.getChildren().size % CAT_ROW == 0) cat.row();
        }
    }

    private static Table getTable(Table t, LCategory category) {
        Table cat;
        t.table(s -> {
            if(category.icon != null){
                s.image(category.icon, Pal.darkishGray).left().size(PERIOD).padRight(IMAGE_PAD);
            }
            s.add(category.localized()).color(Pal.darkishGray).left().tooltip(category.description());
            s.image(Tex.whiteui, Pal.darkishGray).left().height(IMAGE_OFFSET).growX().padLeft(IMAGE_PAD);
        }).growX().pad(IMAGE_OFFSET).padTop(IMAGE_PAD);

        t.row();

        cat = t.table(c -> {
            c.top().left();
        }).name(category.name).top().left().growX().fillY().get();
        t.row();
        return cat;
    }

    private void buttonExecute() {
        BaseDialog dialog = new BaseDialog("@variables");
        dialog.hidden(() -> {
            if(!wasPaused && !net.active()){
                state.set(State.paused);
            }
        });

        dialog.shown(() -> {
            if(!wasPaused && !net.active()){
                state.set(State.playing);
            }
        });

        dialog.cont.pane(p -> {
            p.margin(PAD_MARGIN).marginRight(PAD_RIGHT);
            p.table(Tex.button, t -> {
                t.defaults().fillX().height(TABLE_HEIGHT);
                executorTable(t);
            });
        });

        dialog.addCloseButton();
        dialog.show();
    }

    private void executorTable(Table t) {
        for(var s : executor.vars){
            if(s.constant) continue;

            Color varColor = Pal.gray;
            float stub = STUB, mul = MUL, pad = PAD_LEFT;

            t.add(new Image(Tex.whiteui, varColor.cpy().mul(mul))).width(stub);
            t.stack(new Image(Tex.whiteui, varColor), new Label(" " + s.name + " ", Styles.outlineLabel){{
                setColor(Pal.accent);
            }}).padRight(pad);

            t.add(new Image(Tex.whiteui, Pal.gray.cpy().mul(mul))).width(stub);
            t.table(Tex.pane, out -> tableOut(s, out)).padRight(pad);

            Cons<Image> imageCons = i -> i.setColor(typeColor(s, i.color).mul(mul));
            Image element = new Image(Tex.whiteui, typeColor(s, new Color()).mul(mul));
            t.add(element).update(imageCons).width(stub);

            t.stack(new Image(Tex.whiteui, typeColor(s, new Color())){{
                update(() -> setColor(typeColor(s, color)));
            }}, new Label(() -> " " + typeName(s) + " "){{
                setStyle(Styles.outlineLabel);
            }});

            t.row();

            t.add().growX().colspan(COLSPAN).height(HEIGHT).row();
        }
    }

    private static void tableOut(Var s, Table out) {
        float[] counter = {-1f};
        Label label = out.add("").style(Styles.outlineLabel).padLeft(PAD_LEFT).padRight(PAD_LEFT).width(WIDTH).wrap().get();
        label.update(() -> {
            if(counter[0] < 0 || (counter[0] += Time.delta) >= PERIOD){
                double a = s.numval - (long) s.numval;
                String s1 = Math.abs(a) < DOUBLE_MIN ? (long) s.numval + "" : s.numval + "";
                String text = s.isobj ? PrintI.toString(s.objval) : s1;
                if(!label.textEquals(text)){
                    label.setText(text);
                    if(counter[0] >= 0f){
                        label.actions(Actions.color(Pal.accent), Actions.color(Color.white, DURATION));
                    }
                }
                counter[0] = 0f;
            }
        });
        label.act(1f);
    }

    private void buttonEdit() {
        BaseDialog dialog = new BaseDialog("@editor.export");
        dialog.cont.pane(p -> {
            p.margin(PAD_MARGIN);
            p.table(Tex.button, t -> {
                dialogTable(dialog, t);
            });
        });

        dialog.addCloseButton();
        dialog.show();
    }

    private void dialogTable(BaseDialog dialog, Table t) {
        TextButtonStyle style = Styles.flatt;
        t.defaults().size(DEFAULT_SIZE_WIDTH, DEFAULT_SIZE_HEIGHT).left();

        t.button("@schematic.copy", Icon.copy, style, () -> {
            dialog.hide();
            Core.app.setClipboardText(canvas.save());
        }).marginLeft(MARGIN_LEFT);
        t.row();
        t.button("@schematic.copy.import", Icon.download, style, () -> {
            dialog.hide();
            try{
                canvas.load(Core.app.getClipboardText().replace("\r\n", "\n"));
            }catch(Throwable e){
                ui.showException(e);
            }
        }).marginLeft(MARGIN_LEFT).disabled(b -> Core.app.getClipboardText() == null);
    }

    public void show(String code, LExecutor executor, boolean privileged, Cons<String> modified){
        this.executor = executor;
        this.privileged = privileged;
        canvas.statements.clearChildren();
        canvas.rebuild();
        canvas.privileged = privileged;
        try{
            canvas.load(code);
        }catch(Throwable t){
            Log.err(t);
            canvas.load("");
        }
        this.consumer = result -> {
            if(!result.equals(code)){
                modified.get(result);
            }
        };

        show();
    }
}
