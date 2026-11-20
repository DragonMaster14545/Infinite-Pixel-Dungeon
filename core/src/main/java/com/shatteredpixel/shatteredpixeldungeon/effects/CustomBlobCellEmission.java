package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.noosa.particles.Emitter;

public interface CustomBlobCellEmission {
    void emit(BlobEmitter emitter, Emitter.Factory factory, int index, int cellX, int cellY, int cell, float tileSize);
}
