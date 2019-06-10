package io.anuke.arc.scene.ui;

import io.anuke.arc.collection.Array;
import io.anuke.arc.function.BooleanConsumer;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.scene.ui.layout.Unit;

import static io.anuke.arc.Core.bundle;
import static io.anuke.arc.Core.settings;

public class SettingsDialog extends Dialog{
    public SettingsTable main;

    public SettingsDialog(){
        super(bundle.get("settings", "Settings"));
        addCloseButton();

        main = new SettingsTable();

        cont.add(main);
    }

    public interface StringProcessor{
        String get(int i);
    }

    public static class SettingsTable extends Table{
        protected Array<Setting> list = new Array<>();
        protected Consumer<SettingsTable> rebuilt;

        public SettingsTable(){
            left();
        }

        public SettingsTable(Consumer<SettingsTable> rebuilt){
            this.rebuilt = rebuilt;
            left();
        }

        public Array<Setting> getSettings(){
            return list;
        }

        public void pref(Setting setting){
            list.add(setting);
            rebuild();
        }

        public void screenshakePref(){
            sliderPref("screenshake", bundle.get("setting.screenshake.name", "Screen Shake"), 4, 0, 8, i -> (i / 4f) + "x");
        }

        //TODO implement volume preferences
        /*
        public void volumePrefs(){

            sliderPref("musicvol", bundle.get("setting.musicvol.name", "Music Volume"), 10, 0, 10, 1, i -> {
                Musics.updateVolume();
                return i * 10 + "%";
            });
            checkPref("mutemusic", bundle.get("setting.mutemusic.name", "Mute Music"), false, Musics::setMuted);

            sliderPref("sfxvol", bundle.get("setting.sfxvol.name", "SFX Volume"), 10, 0, 10, 1, i -> i * 10 + "%");
            checkPref("mutesound", bundle.get("setting.mutesound.name", "Mute Sound"), false, Sounds::setMuted);

            Musics.setMuted(settings.getBool("mutemusic"));
            Sounds.setMuted(settings.getBool("mutesound"));
        }*/

        public void sliderPref(String name, String title, int def, int min, int max, StringProcessor s){
            sliderPref(name, title, def, min, max, 1, s);
        }

        public void sliderPref(String name, String title, int def, int min, int max, int step, StringProcessor s){
            list.add(new SliderSetting(name, title, def, min, max, step, s));
            settings.defaults(name, def);
            rebuild();
        }

        public void sliderPref(String name, int def, int min, int max, StringProcessor s){
            sliderPref(name, def, min, max, 1, s);
        }

        public void sliderPref(String name, int def, int min, int max, int step, StringProcessor s){
            list.add(new SliderSetting(name, bundle.get("setting." + name + ".name"), def, min, max, step, s));
            settings.defaults(name, def);
            rebuild();
        }

        public void checkPref(String name, String title, boolean def){
            list.add(new CheckSetting(name, title, def, null));
            settings.defaults(name, def);
            rebuild();
        }

        public void checkPref(String name, String title, boolean def, BooleanConsumer changed){
            list.add(new CheckSetting(name, title, def, changed));
            settings.defaults(name, def);
            rebuild();
        }

        /** Localized title. */
        public void checkPref(String name, boolean def){
            list.add(new CheckSetting(name, bundle.get("setting." + name + ".name"), def, null));
            settings.defaults(name, def);
            rebuild();
        }

        /** Localized title. */
        public void checkPref(String name, boolean def, BooleanConsumer changed){
            list.add(new CheckSetting(name, bundle.get("setting." + name + ".name"), def, changed));
            settings.defaults(name, def);
            rebuild();
        }

        void rebuild(){
            clearChildren();

            for(Setting setting : list){
                setting.add(this);
            }

            addButton(bundle.get("settings.reset", "Reset to Defaults"), () -> {
                for(SettingsTable.Setting setting : list){
                    if(setting.name == null || setting.title == null) continue;
                    settings.put(setting.name, settings.getDefault(setting.name));
                    settings.save();
                }
                rebuild();
            }).margin(14).width(240f).pad(6);

            if(rebuilt != null) rebuilt.accept(this);
        }

        public abstract static class Setting{
            public String name;
            public String title;

            public abstract void add(SettingsTable table);
        }

        public class CheckSetting extends Setting{
            boolean def;
            BooleanConsumer changed;

            CheckSetting(String name, String title, boolean def, BooleanConsumer changed){
                this.name = name;
                this.title = title;
                this.def = def;
                this.changed = changed;
            }

            @Override
            public void add(SettingsTable table){
                CheckBox box = new CheckBox(title);

                box.setChecked(settings.getBool(name));

                box.changed(() -> {
                    settings.put(name, box.isChecked);
                    settings.save();
                    if(changed != null){
                        changed.accept(box.isChecked);
                    }
                });

                box.left();
                table.add(box).left().padTop(3f);
                table.row();
            }
        }

        public class SliderSetting extends Setting{
            int def;
            int min;
            int max;
            int step;
            StringProcessor sp;

            SliderSetting(String name, String title, int def, int min, int max, int step, StringProcessor s){
                this.name = name;
                this.title = title;
                this.def = def;
                this.min = min;
                this.max = max;
                this.step = step;
                this.sp = s;
            }

            @Override
            public void add(SettingsTable table){
                Slider slider = new Slider(min, max, step, false);

                slider.setValue(settings.getInt(name));

                Label label = new Label(title);
                slider.changed(() -> {
                    settings.put(name, (int)slider.getValue());
                    settings.save();
                    label.setText(title + ": " + sp.get((int)slider.getValue()));
                });

                slider.change();

                table.table(t -> {
                    t.left().defaults().left();
                    t.add(label).minWidth(label.getPrefWidth() / Unit.dp.scl(1f) + 50);
                    t.add(slider).width(180);
                }).left().padTop(3);

                table.row();
            }
        }

    }
}
