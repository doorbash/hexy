/*
 * **************************************************************************
 * Copyright 2017 See AUTHORS file.
 *
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *************************************************************************
 *
 */

package com.crowni.gdx.rtllang.support;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.CharArray;

import java.awt.event.KeyEvent;

import static com.crowni.gdx.rtllang.support.ArUtils.getIndividualChar;

/**
 * Created by Crowni on 10/5/2017.
 **/
public class ArFont {
    private Array<ArGlyph> glyphs = new Array<ArGlyph>();

    public String typing(char c) {
        if (c == KeyEvent.VK_BACK_SPACE)
            popChar();
        else
            addChar(new ArGlyph(c, !ArUtils.isLTR(c)));
        return getText();
    }

    public String getText(String given) {
        char[] chars = given.toCharArray();
        for (char c : chars)
            addChar(new ArGlyph(c, !ArUtils.isLTR(c)));
        String text = getText();
        this.glyphs.clear();
        return text;
    }

    private void addChar(ArGlyph glyph) {
        glyphs.add(glyph);
        for (int i = 1; i <= 2; i += 1)
            filterLastChars(i);

        /** CONSOLE **/
//        System.out.println("==================================");
    }

    private void popChar() {
        if (glyphs.size > 0) {
            ArGlyph aChar = glyphs.pop();
            if (aChar instanceof ArGlyphComplex) {
                Array<ArGlyph> glyphComplex = ((ArGlyphComplex) aChar).getSimpleChars();
                for (int i = glyphComplex.size - 1; i >= 0; i--)
                    glyphs.add(glyphComplex.get(i));
                glyphs.pop();
            }
            filterLastChars(1);
        }
    }

    private String getText() {
        String text = "";

        boolean inserting = true;
        String subtext = "";
        for (int i = glyphs.size - 1; i >= 0; i--) {
            if (glyphs.get(i).isRTL() || glyphs.get(i).isSpace()) {
                if (!inserting) {
                    inserting = true;
                    text += subtext;
                    subtext = "";
                }

                text += glyphs.get(i).getChar();
            } else {
                inserting = false;
                subtext = glyphs.get(i).getOriginalChar() + subtext;
            }
        }

        text += subtext;

        return text;
    }

    /**
     * CONSOLE
     **/
    private int CASE = 0;

    private boolean printCase(int i) {
        this.CASE = i;
        return true;
    }

    /**
     * @param glyph
     * @return ArGlyph after filtering process.
     */
    private ArGlyph filter(ArGlyph glyph) {
        if (!glyph.isRTL()) {
            return glyph;
        }

        ArGlyph before = getPositionGlyph(glyph, -1);
        ArGlyph after = getPositionGlyph(glyph, +1);


        /** CASE 1 **/
        if (before == null && after == null && /** CONSOLE **/printCase(1))
            glyph.setChar(getIndividualChar(glyph.getOriginalChar()));


        /** CASE 2 **/
        if (before == null && after != null && /** CONSOLE **/printCase(2))
            glyph.setChar(ArUtils.getStartChar(glyph.getOriginalChar()));


        /** CASE 3 **/
        if (before != null && after == null && /** CONSOLE **/printCase(3))
            if (ArUtils.isALFChar(glyph.getOriginalChar()) && ArUtils.isLAMChar(before.getOriginalChar())) {
                addComplexChars(glyph);
            } else {
                if (before.getType() == ArCharCode.X2)
                    glyph.setChar(ArUtils.getIndividualChar(glyph.getOriginalChar()));
                else
                    glyph.setChar(ArUtils.getEndChar(glyph.getOriginalChar()));
            }


        /** CASE 4 **/
        if (before != null && after != null && /** TEST **/printCase(4))
            if (glyph.getType() == ArCharCode.X4) {
                if (before.getType() == ArCharCode.X2)
                    glyph.setChar(ArUtils.getStartChar(glyph.getOriginalChar()));
                else
                    glyph.setChar(ArUtils.getCenterChar(glyph.getOriginalChar()));
            } else {
                if (before.getType() == ArCharCode.X2)
                    glyph.setChar(ArUtils.getIndividualChar(glyph.getOriginalChar()));
                else
                    glyph.setChar(ArUtils.getEndChar(glyph.getOriginalChar()));
            }

        return glyph;
    }

    /**
     * @param arGlyph current glyph.
     * @param pos     value always between [-1,1] : -1 is before arGlyph or +1 is after arGlyph.
     * @return correct position of glyph.
     */
    private ArGlyph getPositionGlyph(ArGlyph arGlyph, int pos) {
        int i = glyphs.lastIndexOf(arGlyph, false) + (pos = MathUtils.clamp(pos, -1, 1));
        ArGlyph glyph = (pos > 0 ? i < glyphs.size : i > -1) ? glyphs.get(i) : null;
        return glyph != null ? ArUtils.isInvalidChar(glyph.getOriginalChar()) ? null : glyph : null;
    }

    private void addComplexChars(ArGlyph arGlyph) {
        ArGlyphComplex glyph = new ArGlyphComplex(ArUtils.getLAM_ALF(arGlyph.getOriginalChar()));
        glyph.setSimpleGlyphs(arGlyph, getPositionGlyph(arGlyph, -1));
        for (int i = 0; i < glyph.getSimpleChars().size; i++) glyphs.pop();
        addChar(glyph);
    }

    private void filterLastChars(int i) {
        ArGlyph arGlyph = null;
        if (glyphs.size - i > -1)
            arGlyph = filter(glyphs.get(glyphs.size - i));

        /** CONSOLE **/
        if (arGlyph != null)
            System.out.println("CASE " + CASE + ": " + arGlyph.getOriginalChar() + " To : " + arGlyph.getChar());
    }
}

