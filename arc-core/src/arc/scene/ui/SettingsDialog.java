package arc.scene.ui;

import arc.struct.Array;
import arc.func.Boolc;
import arc.func.Cons;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;

import static arc.Core.bundle;
import static arc.Core.settings;

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
        protected Cons<SettingsTable> rebuilt;

        public SettingsTable(){
            left();
        }

        public SettingsTable(Cons<SettingsTable> rebuilt){
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

        public SliderSetting sliderPref(String name, String title, int def, int min, int max, StringProcessor s){
            return sliderPref(name, title, def, min, max, 1, s);
        }

        public SliderSetting sliderPref(String name, String title, int def, int min, int max, int step, StringProcessor s){
            SliderSetting res;
            list.add(res = new SliderSetting(name, title, def, min, max, step, s));
            settings.defaults(name, def);
            rebuild();
            return res;
        }

        public SliderSetting sliderPref(String name, int def, int min, int max, StringProcessor s){
            return sliderPref(name, def, min, max, 1, s);
        }

        public SliderSetting sliderPref(String name, int def, int min, int max, int step, StringProcessor s){
            SliderSetting res;
            list.add(res = new SliderSetting(name, bundle.get("setting." + name + ".name"), def, min, max, step, s));
            settings.defaults(name, def);
            rebuild();
            return res;
        }

        public void checkPref(String name, String title, boolean def){
            list.add(new CheckSetting(name, title, def, null));
            settings.defaults(name, def);
            rebuild();
        }

        public void checkPref(String name, String title, boolean def, Boolc changed){
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
        public void checkPref(String name, boolean def, Boolc changed){
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

            if(rebuilt != null) rebuilt.get(this);
        }

        public abstract static class Setting{
            public String name;
            public String title;

            public abstract void add(SettingsTable table);
        }

        public class CheckSetting extends Setting{
            boolean def;
            Boolc changed;

            CheckSetting(String name, String title, boolean def, Boolc changed){
                this.name = name;
                this.title = title;
                this.def = def;
                this.changed = changed;
            }

            @Override
            public void add(SettingsTable table){
                CheckBox box = new CheckBox(title);

                box.update(() -> box.setChecked(settings.getBool(name)));

                box.changed(() -> {
                    settings.put(name, box.isChecked);
                    settings.save();
                    if(changed != null){
                        changed.get(box.isChecked);
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
            float[] values = null;

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
                if(values != null){
                    slider.setSnapToValues(values, 1f);
                }

                Label label = new Label(title);
                slider.changed(() -> {
                    settings.put(name, (int)slider.getValue());
                    settings.save();
                    label.setText(title + ": " + sp.get((int)slider.getValue()));
                });

                slider.change();

                table.table(t -> {
                    t.left().defaults().left();
                    t.add(label).minWidth(label.getPrefWidth() / Scl.scl(1f) + 50);
                    t.add(slider).width(180);
                }).left().padTop(3);

                table.row();
            }
        }

    }
}
