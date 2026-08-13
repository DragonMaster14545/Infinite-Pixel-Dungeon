/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.watabou.noosa.Camera;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class SaveScene extends PixelScene {

    private static final int GAP = 6;

    private static final String[] TOP_LEVEL_EXPORT_FILES = {
            "battlepass_new.dat",
            "journal.dat",
            "badges.dat",
            "rankings.dat"
    };

    private static final String EXPORT_BLOB_FILE = "save_export.txt";

    @Override
    public void create() {
        super.create();

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize( w, h );
        add( archs );

        IconTitle title = new IconTitle( com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(
                com.shatteredpixel.shatteredpixeldungeon.ui.Icons.INFO ), Messages.get( this, "title" ) );
        title.setSize(200, 0);
        title.setPos( (w - title.reqWidth()) / 2f, 6 );
        align(title);
        add(title);

        RenderedTextBlock notice = renderTextBlock( Messages.get( this, "notice" ), 9 );
        notice.hardlight( 0xCACFC2 );
        notice.setPos( (w - notice.width()) / 2f, title.bottom() + 6 );
        align(notice);
        add(notice);

        float btnW = 140;
        float btnTop = notice.bottom() + 20;

        RedButton btnExport = new RedButton( Messages.get( this, "export" ) ){
            @Override
            public void onClick(){
                doExport();
            }
        };
        btnExport.setRect( (w - btnW) / 2f, btnTop, btnW, 18 );
        add( btnExport );

        RedButton btnImport = new RedButton( Messages.get( this, "import" ) ){
            @Override
            public void onClick(){
                doImport();
            }
        };
        btnImport.setRect( (w - btnW) / 2f, btnExport.bottom() + GAP, btnW, 18 );
        add( btnImport );

        float nextTop = btnImport.bottom() + GAP;
        if (DeviceCompat.supportsFilePicker()){
            RedButton btnExportTo = new RedButton( Messages.get( this, "export_to_device" ) ){
                @Override
                public void onClick(){
                    doExportToDevice();
                }
            };
            btnExportTo.setRect( (w - btnW) / 2f, nextTop, btnW, 18 );
            add( btnExportTo );
            nextTop = btnExportTo.bottom() + GAP;

            RedButton btnImportFrom = new RedButton( Messages.get( this, "import_from_device" ) ){
                @Override
                public void onClick(){
                    doImportFromDevice();
                }
            };
            btnImportFrom.setRect( (w - btnW) / 2f, nextTop, btnW, 18 );
            add( btnImportFrom );
        }

        ExitButton btnExit = new ExitButton();
        btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
        add( btnExit );

        fadeIn();
    }

    @Override
    protected void onBackPressed(){
        ShatteredPixelDungeon.switchNoFade( TitleScene.class );
    }

    private void doExport(){
        String blob;
        try {
            blob = encodeAll();
        } catch (Exception e){
            ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( this, "export_failed" ) ) );
            return;
        }

        try {
            FileUtils.getFileHandle( EXPORT_BLOB_FILE ).writeBytes( blob.getBytes(StandardCharsets.UTF_8), false );
        } catch (Exception e){
            //the clipboard copy and the text box below are still there as a fallback
        }

        try {
            Gdx.app.getClipboard().setContents( blob );
            ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( this, "export_copied" )) );
        } catch (Exception e){
            //clipboard access can fail on some platforms, fall back to just showing it
        }

        ShatteredPixelDungeon.scene().addToFront( new WndTextInput(
                Messages.get( this, "export" ),
                Messages.get( this, "export_prompt" ),
                blob,
                Integer.MAX_VALUE,
                true,
                Messages.get( this, "close" ),
                Messages.get( this, "close" ) ){
            @Override
            public void onSelect(boolean positive, String text) { /* read-only view, nothing to do */ }
        } );
    }

    private String encodeAll() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ArrayList<String> names = new ArrayList<>( Arrays.asList( TOP_LEVEL_EXPORT_FILES ) );

        for (int slot = 1; slot <= GamesInProgress.MAX_SLOTS; slot++){
            if (!GamesInProgress.gameExists( slot )) continue;
            collectSlotFiles( slot, names );
        }

        for (String name : names){
            if (!FileUtils.fileExists( name )) continue;

            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            byte[] content = FileUtils.getFileHandle( name ).readBytes();

            writeInt( out, nameBytes.length );
            out.write( nameBytes );
            writeInt( out, content.length );
            out.write( content );
        }

        return String.valueOf( Base64Coder.encode( out.toByteArray() ) );
    }

    private void collectSlotFiles( int slot, ArrayList<String> names ){
        String folder = GamesInProgress.gameFolder( slot );
        FileHandle dir = FileUtils.getFileHandle( folder );
        if (!dir.exists() || !dir.isDirectory()) return;

        for (FileHandle child : dir.list()){
            if (child.isDirectory()) continue;
            names.add( folder + "/" + child.name() );
        }
    }

    private void doImport(){
        String initialText = "";

        if (FileUtils.fileExists( EXPORT_BLOB_FILE )){
            try {
                byte[] data = FileUtils.getFileHandle( EXPORT_BLOB_FILE ).readBytes();
                initialText = new String( data, StandardCharsets.UTF_8 );
            } catch (Exception e){
                //ignore
            }
        }

        if (initialText.isEmpty()){
            try {
                String clipboardText = Gdx.app.getClipboard().getContents();
                if (clipboardText != null) initialText = clipboardText;
            } catch (Exception e){
                //ignore
            }
        }

        ShatteredPixelDungeon.scene().addToFront( new WndTextInput(
                Messages.get( this, "import" ),
                Messages.get( this, "import_prompt" ),
                initialText,
                Integer.MAX_VALUE,
                true,
                Messages.get( this, "confirm" ),
                Messages.get( this, "cancel" ) ){
            @Override
            public void onSelect(boolean positive, String text) {
                if (positive && text != null && !text.trim().isEmpty()){
                    confirmImport( text.trim() );
                }
            }
        } );
    }

    private void confirmImport( final String blob ){
        ShatteredPixelDungeon.scene().addToFront( new WndOptions(
                com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(
                        com.shatteredpixel.shatteredpixeldungeon.ui.Icons.WARNING ),
                Messages.get( this, "import_confirm_title" ),
                Messages.get( this, "import_confirm_body" ),
                Messages.get( this, "import_confirm_yes" ),
                Messages.get( this, "import_confirm_no" ) ){
            @Override
            protected void onSelect(int index) {
                if (index == 0){
                    try {
                        decodeAndWrite( blob );
                        invalidateSlotCache();
                        ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( SaveScene.this, "import_success" ) ) );
                    } catch (Exception e){
                        ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( SaveScene.this, "import_failed" ) ) );
                    }
                }
            }
        } );
    }

    private void decodeAndWrite( String blob ) throws IOException {
        byte[] all = Base64Coder.decode( blob );

        ArrayList<String> names = new ArrayList<>();
        ArrayList<byte[]> contents = new ArrayList<>();

        int pos = 0;
        while (pos < all.length){
            int nameLen = readInt( all, pos ); pos += 4;
            String name = new String( all, pos, nameLen, StandardCharsets.UTF_8 ); pos += nameLen;
            int contentLen = readInt( all, pos ); pos += 4;
            byte[] content = new byte[contentLen];
            System.arraycopy( all, pos, content, 0, contentLen ); pos += contentLen;

            names.add( name );
            contents.add( content );
        }

        for (int i = 0; i < names.size(); i++){
            FileUtils.getFileHandle( names.get(i) ).writeBytes( contents.get(i), false );
        }
    }

    private void invalidateSlotCache(){
        for (int slot = 1; slot <= GamesInProgress.MAX_SLOTS; slot++){
            GamesInProgress.setUnknown( slot );
        }
    }

    private void doExportToDevice(){
        final String blob;
        try {
            blob = encodeAll();
        } catch (Exception e){
            ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( this, "export_failed" ) ) );
            return;
        }

        DeviceCompat.saveFileWithPicker( EXPORT_BLOB_FILE, blob.getBytes(StandardCharsets.UTF_8),
                new DeviceCompat.FilePickerCallback(){
                    @Override
                    public void onComplete( boolean success ){
                        if (success){
                            ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( SaveScene.this, "export_copied" ) ) );
                        } else {
                            ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( SaveScene.this, "export_failed" ) ) );
                        }
                    }
                } );
    }

    private void doImportFromDevice(){
        DeviceCompat.openFileWithPicker( new DeviceCompat.FileOpenCallback(){
            @Override
            public void onFileSelected( byte[] data ){
                if (data == null){
                    return;
                }
                String text = new String( data, StandardCharsets.UTF_8 );
                if (!text.trim().isEmpty()){
                    confirmImport( text.trim() );
                } else {
                    ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( SaveScene.class, "import_failed" ) ) );
                }
            }
        } );
    }

    private static void writeInt( ByteArrayOutputStream out, int value ){
        out.write( (value >>> 24) & 0xFF );
        out.write( (value >>> 16) & 0xFF );
        out.write( (value >>> 8)  & 0xFF );
        out.write( value & 0xFF );
    }

    private static int readInt( byte[] data, int offset ){
        return ((data[offset]   & 0xFF) << 24)
                | ((data[offset+1] & 0xFF) << 16)
                | ((data[offset+2] & 0xFF) << 8)
                |  (data[offset+3] & 0xFF);
    }
}