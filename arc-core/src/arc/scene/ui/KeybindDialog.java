package arc.scene.ui;

import arc.*;
import arc.Application.*;
import arc.KeyBinds.*;
import arc.struct.*;
import arc.graphics.*;
import arc.input.*;
import arc.input.InputDevice.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;

import static arc.Core.*;

public class KeybindDialog extends Dialog{
    protected KeybindDialogStyle style;

    protected Section section;
    protected KeyBind rebindKey = null;
    protected boolean rebindAxis = false;
    protected boolean rebindMin = true;
    protected KeyCode minKey = null;
    protected Dialog rebindDialog;
    protected ObjectIntMap<Section> sectionControls = new ObjectIntMap<>();

    public KeybindDialog(){
        super(bundle.get("keybind.title", "Rebind Keys"));
        style = scene.getStyle(KeybindDialogStyle.class);
        setup();
        addCloseButton();
    }

    public void setStyle(KeybindDialogStyle style){
        this.style = style;
        setup();
    }

    private void setup(){
        cont.clear();

        Section[] sections = Core.keybinds.getSections();

        Stack stack = new Stack();
        ButtonGroup<TextButton> group = new ButtonGroup<>();
        ScrollPane pane = new ScrollPane(stack);
        pane.setFadeScrollBars(false);
        this.section = sections[0];

        for(Section section : sections){
            if(!sectionControls.containsKey(section))
                sectionControls.put(section, input.getDevices().indexOf(section.device, true));

            if(sectionControls.get(section, 0) >= input.getDevices().size){
                sectionControls.put(section, 0);
                section.device = input.getDevices().get(0);
            }

            if(sections.length != 1){
                //TODO toggle style
                TextButton button = new TextButton(bundle.get("section." + section.name + ".name", Strings.capitalize(section.name))/*, "toggle"*/);
                if(section.equals(this.section))
                    button.toggle();

                button.clicked(() -> this.section = section);

                group.add(button);
                cont.add(button).fill();
            }

            Table table = new Table();

            Label device = new Label("Keyboard");
            //device.setColor(style.controllerColor);
            device.setAlignment(Align.center);

            Array<InputDevice> devices = input.getDevices();

            Table stable = new Table();

            stable.addButton("<", () -> {
                int i = sectionControls.get(section, 0);
                if(i - 1 >= 0){
                    sectionControls.put(section, i - 1);
                    section.device = devices.get(i - 1);
                    settings.save();
                    setup();
                }
            }).disabled(sectionControls.get(section, 0) - 1 < 0).size(40);

            stable.add(device).minWidth(device.getMinWidth() + 60);

            device.setText(input.getDevices().get(sectionControls.get(section, 0)).name());

            stable.addButton(">", () -> {
                int i = sectionControls.get(section, 0);

                if(i + 1 < devices.size){
                    sectionControls.put(section, i + 1);
                    section.device = devices.get(i + 1);
                    settings.save();
                    setup();
                }
            }).disabled(sectionControls.get(section, 0) + 1 >= devices.size).size(40);

            table.add(stable).colspan(4);

            table.row();
            table.add().height(10);
            table.row();
            if(section.device.type() == DeviceType.controller){
                table.table(info -> info.add("Controller Type: [#" + style.controllerColor.toString().toUpperCase() + "]" +
                Strings.capitalize(section.device.name())).left());
            }
            table.row();

            String lastCategory = null;

            for(KeyBind keybind : keybinds.getKeybinds()){
                if(lastCategory != keybind.category() && keybind.category() != null){
                    table.add(bundle.get("category." + keybind.category() + ".name", Strings.capitalize(keybind.category()))).color(Color.gray).colspan(4).pad(10).padBottom(4).row();
                    table.addImage().color(Color.gray).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();
                    lastCategory = keybind.category();
                }

                Axis axis = keybinds.get(section, keybind);

                if(keybind.defaultValue(section.device.type()) instanceof Axis){
                    table.add(bundle.get("keybind." + keybind.name() + ".name", Strings.capitalize(keybind.name())), style.keyNameColor).left().padRight(40).padLeft(8);

                    if(axis.key != null){
                        table.add(axis.key.toString(), style.keyColor).left().minWidth(90).padRight(20);
                    }else{
                        Table axt = new Table();
                        axt.left();
                        axt.labelWrap(axis.min.toString() + " [red]/[] " + axis.max.toString()).color(style.keyColor).width(140f).padRight(5);
                        table.add(axt).left().minWidth(90).padRight(20);
                    }

                    table.addButton(bundle.get("settings.rebind", "Rebind"), () -> {
                        rebindAxis = true;
                        rebindMin = true;
                        openDialog(section, keybind);
                    }).width(130f);
                }else{
                    table.add(bundle.get("keybind." + keybind.name() + ".name", Strings.capitalize(keybind.name())),
                    style.keyNameColor).left().padRight(40).padLeft(8);
                    table.add(keybinds.get(section, keybind).key.toString(),
                    style.keyColor).left().minWidth(90).padRight(20);

                    table.addButton(bundle.get("settings.rebind", "Rebind"), () -> {
                        rebindAxis = false;
                        rebindMin = false;
                        openDialog(section, keybind);
                    }).width(130f);
                }
                table.addButton(bundle.get("settings.resetKey", "Reset"), () -> {
                    keybinds.resetToDefault(section, keybind);
                    settings.save();
                    setup();
                }).width(130f);
                table.row();
            }

            table.visible(() -> this.section.equals(section));

            table.addButton(bundle.get("settings.reset", "Reset to Defaults"), () -> {
                keybinds.resetToDefaults();
                setup();
                settings.save();
            }).colspan(4).padTop(4).fill();

            stack.add(table);
        }

        cont.row();

        cont.add(pane).growX().colspan(sections.length);

    }

    private void rebind(Section section, KeyBind bind, KeyCode newKey){
        if(rebindKey == null) return;
        rebindDialog.hide();
        boolean isAxis = bind.defaultValue(section.device.type()) instanceof Axis;

        if(isAxis){
            if(newKey.axis || !rebindMin){
                section.binds.getOr(section.device.type(), OrderedMap::new).put(rebindKey, newKey.axis ? new Axis(newKey) : new Axis(minKey, newKey));
            }
        }else{
            section.binds.getOr(section.device.type(), OrderedMap::new).put(rebindKey, new Axis(newKey));
        }

        if(rebindAxis && isAxis && rebindMin && !newKey.axis){
            rebindMin = false;
            minKey = newKey;
            openDialog(section, rebindKey);
        }else{
            settings.save();
            rebindKey = null;
            rebindAxis = false;
            setup();
        }
    }

    private void openDialog(Section section, KeyBind name){
        rebindDialog = new Dialog(rebindAxis ? bundle.get("keybind.press.axis", "Press an axis or key...") : bundle.get("keybind.press", "Press a key..."));

        rebindKey = name;

        rebindDialog.titleTable.getCells().first().pad(4);

        if(section.device.type() == DeviceType.keyboard){
            rebindDialog.keyDown(i -> setup());

            rebindDialog.addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(Core.app.getType() == ApplicationType.Android) return false;
                    rebind(section, name, button);
                    return false;
                }

                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode){
                    rebindDialog.hide();
                    if(keycode == KeyCode.ESCAPE) return false;
                    rebind(section, name, keycode);
                    return false;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                    if(!rebindAxis) return false;
                    rebindDialog.hide();
                    rebind(section, name, KeyCode.SCROLL);
                    return false;
                }
            });
        }

        rebindDialog.show();
        Time.runTask(1f, () -> getScene().setScrollFocus(rebindDialog));
    }

    public static class KeybindDialogStyle extends Style{
        public Color keyColor = Color.white;
        public Color keyNameColor = Color.white;
        public Color controllerColor = Color.white;
    }
}
