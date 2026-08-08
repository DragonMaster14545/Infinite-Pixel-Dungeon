package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.JournalScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournalItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GlobalCustomNoteButton extends IconButton {

    public GlobalCustomNoteButton () {
        super(Icons.PLUS.get());

        width = 11;
        height = 11;
    }

   private static void show(Window w){
        if (ShatteredPixelDungeon.scene() instanceof GameScene){
            GameScene.show(w);
        } else {
            ShatteredPixelDungeon.scene().addToFront(w);
        }
    }

   private static void refreshHost(){
        if (ShatteredPixelDungeon.scene() instanceof JournalScene) {
            ShatteredPixelDungeon.seamlessResetScene();
        } else {
            WndJournal.refreshGlobalNotes();
        }
    }

    @Override
    public void onClick() {
        super.onClick();

        if (Notes.globalCustomRecords().size() >= Notes.customRecordLimit()){
            show(new WndTitledMessage(Icons.INFO.get(),
                    Messages.get(this, "limit_title"),
                    Messages.get(this, "limit_text")));
            return;
        }

        show(new WndNoteTypeSelect());
    }

    @Override
    protected String hoverText() {
        return Messages.get(this, "title");
    }

    private static WndNoteTypeSelect NOTE_SELECT_INSTANCE;

    private class WndNoteTypeSelect extends WndOptions {

        public WndNoteTypeSelect(){
            super(Icons.SCROLL_COLOR.get(),
                    Messages.get(GlobalCustomNoteButton.class, "title"),
                    Messages.get(GlobalCustomNoteButton.class, "desc"),
                    Messages.get(GlobalCustomNoteButton.class, "new_text"),
                    Messages.get(GlobalCustomNoteButton.class, "new_floor"),
                    Messages.get(GlobalCustomNoteButton.class, "new_type"));
            NOTE_SELECT_INSTANCE = this;
        }

        @Override
        protected void onSelect(int index) {
            if (index == 0){
                Notes.CustomRecord custom = new Notes.CustomRecord("", "");
                addNote(null, custom,
                        Messages.get(GlobalCustomNoteButton.class, "new_text"),
                        Messages.get(GlobalCustomNoteButton.class, "new_text_title"));
            } else if (index == 1){
                show(new WndDepthSelect());
            } else {
                show(new WndItemtypeSelect());
            }
        }

        @Override
        public void hide() {
            //do nothing, prevents window closing when user steps back in note creation process
        }

        @Override
        public void onBackPressed() {
            super.hide();
            NOTE_SELECT_INSTANCE = null;
        }
    }

    private class WndDepthSelect extends WndTitledMessage {

        public WndDepthSelect(){
            super(Icons.STAIRS.get(),
                    Messages.get(GlobalCustomNoteButton.class, "new_floor"),
                    Messages.get(GlobalCustomNoteButton.class, "new_floor_prompt"));

            int top = height+2;
            int left = 0;

            for (int i = Math.max(Statistics.deepestFloor, 1); i > 0; i --){
                if (i % 5 == 0 && left > 0){
                    left = 0;
                    top += 17;
                }
                int finalI = i;
                RedButton btnDepth = new RedButton(Integer.toString(finalI)){
                    @Override
                    public void onClick() {
                        addNote(WndDepthSelect.this, new Notes.CustomRecord(finalI, "", ""),
                                Messages.get(GlobalCustomNoteButton.class, "new_floor"),
                                Messages.get(GlobalCustomNoteButton.class, "new_floor_title", finalI));
                    }
                };
                btnDepth.setRect(left, top, 23, 16);
                left += 24;
                add(btnDepth);
            }

            resize(width, top + (left == 0 ? 0 : 16));
        }
    }

    private static class WndItemtypeSelect extends WndTitledMessage {

        public WndItemtypeSelect() {
            super(Icons.SCROLL_COLOR.get(),
                    Messages.get(GlobalCustomNoteButton.class, "new_type"),
                    Messages.get(GlobalCustomNoteButton.class, "new_type_prompt"));

            int top = height + 2;
            int left = 0;

            ArrayList<Item> items = new ArrayList<>();
            for (Class<?> potionCls : Generator.Category.POTION.classes) {
                items.add((Item) Reflection.newInstance(potionCls));
            }
            for (Class<?> potionCls : Generator.Category.SCROLL.classes) {
                items.add((Item) Reflection.newInstance(potionCls));
            }
            for (Class<?> potionCls : Generator.Category.RING.classes) {
                items.add((Item) Reflection.newInstance(potionCls));
            }
            Collections.sort(items, itemVisualcomparator);
            for (Item item : items) {
                ItemButton itemButton = new ItemButton(){
                    @Override
                    protected void onClick() {
                        addNote(WndItemtypeSelect.this, new Notes.CustomRecord(item, "", ""),
                                Messages.get(GlobalCustomNoteButton.class, "new_type"),
                                Messages.get(GlobalCustomNoteButton.class, "new_item_title", Messages.titleCase(item.name())));
                    }
                };
                itemButton.item(item);
                itemButton.setRect(left, top, 19, 19);
                add(itemButton);

                left += 20;
                if (left >= width - 19){
                    top += 20;
                    left = 0;
                }
            }
            if (left > 0){
                top += 20;
                left = 0;
            }

            resize(width, top);
        }
    }

    private static Comparator<Item> itemVisualcomparator = new Comparator<Item>() {
        @Override
        public int compare(Item i1, Item i2) {
            int i1Idx = i1.image();
            int i2Idx = i2.image();

            if (i1 instanceof Scroll)   i1Idx += 1000;
            if (i1 instanceof Ring)     i1Idx += 2000;

            if (i2 instanceof Scroll)   i2Idx += 1000;
            if (i2 instanceof Ring)     i2Idx += 2000;

            return i1Idx - i2Idx;
        }
    };

    public static class GlobalCustomNoteWindow extends WndJournalItem {

        public GlobalCustomNoteWindow(Notes.CustomRecord rec) {
            super(rec.icon(), rec.title(), rec.desc());

            RedButton title = new RedButton( Messages.get(GlobalCustomNoteWindow.class, "edit_title") ){
                @Override
                public void onClick() {
                    show(new WndTextInput(Messages.get(GlobalCustomNoteWindow.class, "edit_title"),
                            "",
                            rec.title(),
                            50,
                            false,
                            Messages.get(GlobalCustomNoteWindow.class, "confirm"),
                            Messages.get(GlobalCustomNoteWindow.class, "cancel")){
                        @Override
                        public void onSelect(boolean positive, String text) {
                            if (positive && !text.isEmpty()){
                                rec.editText(text, rec.desc());
                                GlobalCustomNoteWindow.this.hide();

                                if (ShatteredPixelDungeon.scene() instanceof JournalScene) {
                                    refreshHost();
                                } else {
                                    show(new GlobalCustomNoteWindow(rec));
                                    refreshHost();
                                }
                            }
                        }
                    });
                }
            };
            add(title);
            title.setRect(0, Math.min(height+2, PixelScene.uiCamera.height-50), width/2-1, 16);

            String editBodyText = rec.desc().isEmpty() ? Messages.get(GlobalCustomNoteWindow.class, "add_text") : Messages.get(GlobalCustomNoteWindow.class, "edit_text");
            RedButton body = new RedButton(editBodyText){
                @Override
                public void onClick() {
                    show(new WndTextInput(editBodyText,
                            "",
                            rec.desc(),
                            500,
                            true,
                            Messages.get(GlobalCustomNoteWindow.class, "confirm"),
                            Messages.get(GlobalCustomNoteWindow.class, "cancel")){
                        @Override
                        public void onSelect(boolean positive, String text) {
                            if (positive){
                                rec.editText(rec.title(), text);
                                GlobalCustomNoteWindow.this.hide();

                                if (ShatteredPixelDungeon.scene() instanceof JournalScene) {
                                    refreshHost();
                                } else {
                                    show(new GlobalCustomNoteWindow(rec));
                                    refreshHost();
                                }
                            }
                        }
                    });
                }
            };
            add(body);
            body.setRect(title.right()+2, title.top(), width/2-1, 16);

            RedButton delete = new RedButton( Messages.get(GlobalCustomNoteWindow.class, "delete") ){
                @Override
                public void onClick() {
                    show(new WndOptions(Icons.WARNING.get(),
                            Messages.get(GlobalCustomNoteWindow.class, "delete"),
                            Messages.get(GlobalCustomNoteWindow.class, "delete_warn"),
                            Messages.get(GlobalCustomNoteWindow.class, "confirm"),
                            Messages.get(GlobalCustomNoteWindow.class, "cancel")){
                        @Override
                        protected void onSelect(int index) {
                            if (index == 0){
                                Notes.removeGlobalCustom(rec);
                                GlobalCustomNoteWindow.this.hide();
                                refreshHost();
                            }
                        }
                    });
                }
            };
            add(delete);
            delete.setRect(0, title.bottom()+1, width, 16);

            resize(width, (int)delete.bottom());
        }

        @Override
        protected boolean useHighlighting() {
            return false;
        }
    }

    private static void addNote(Window parentWindow, Notes.CustomRecord note, String promptTitle, String prompttext){
        show(new WndTextInput(promptTitle,
                prompttext,
                "",
                50,
                false,
                Messages.get(GlobalCustomNoteWindow.class, "confirm"),
                Messages.get(GlobalCustomNoteWindow.class, "cancel")){
            @Override
            public void onSelect(boolean positive, String text) {
                if (positive && !text.isEmpty()){
                    note.assignID();
                    note.editText(text, "");
                    Notes.addGlobalCustom(note);

                    if (parentWindow != null) {
                        parentWindow.hide();
                    }
                    if (NOTE_SELECT_INSTANCE != null){
                        NOTE_SELECT_INSTANCE.onBackPressed();
                    }
                    hide();

                    if (ShatteredPixelDungeon.scene() instanceof JournalScene) {
                       refreshHost();
                    } else {
                        show(new GlobalCustomNoteWindow(note));
                        refreshHost();
                    }
                }
            }
        });
    }
}