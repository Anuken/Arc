/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <br>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior
 * written permission.
 * <br>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 1.2_01
 */

package io.anuke.arc.typelabel.reg;

import io.anuke.arc.typelabel.reg.ds.IntBitSet;

import java.util.*;

class CharacterClass extends Term implements UnicodeConstants{
    private static final BlockSet DIGIT = new BlockSet();
    private static final BlockSet WORDCHAR = new BlockSet();
    private static final BlockSet SPACE = new BlockSet();
    private static final BlockSet HSPACE = new BlockSet();
    private static final BlockSet VSPACE = new BlockSet();

    private static final BlockSet UDIGIT = new BlockSet();
    private static final BlockSet UWORDCHAR = new BlockSet();
    private static final BlockSet USPACE = new BlockSet();
    private static final BlockSet UHSPACE = new BlockSet();
    private static final BlockSet UVSPACE = new BlockSet();

    private static final BlockSet NONDIGIT = new BlockSet();
    private static final BlockSet NONWORDCHAR = new BlockSet();
    private static final BlockSet NONSPACE = new BlockSet();
    private static final BlockSet NONHSPACE = new BlockSet();
    private static final BlockSet NONVSPACE = new BlockSet();

    private static final BlockSet UNONDIGIT = new BlockSet();
    private static final BlockSet UNONWORDCHAR = new BlockSet();
    private static final BlockSet UNONSPACE = new BlockSet();
    private static final BlockSet UNONHSPACE = new BlockSet();
    private static final BlockSet UNONVSPACE = new BlockSet();

    private static boolean namesInitialized = false;

    private static final HashMap<String, BlockSet> namedClasses = new HashMap<String, BlockSet>();
    private static final ArrayList<String> unicodeBlocks = new ArrayList<String>();
    private static final ArrayList<String> posixClasses = new ArrayList<String>();
    private static final ArrayList<String> unicodeCategories = new ArrayList<String>();

    //modes; used in parseGroup(()
    private final static int ADD = 1;
    private final static int SUBTRACT = 2;
    private final static int INTERSECT = 3;

    private static final int[] blockDataStarts = {0x0000, 0x0080, 0x0100, 0x0180, 0x0250, 0x02B0, 0x0300, 0x0370, 0x0400, 0x0500, 0x0530, 0x0590, 0x0600, 0x0700, 0x0750, 0x0780, 0x07C0, 0x0800, 0x0840, 0x0860, 0x08A0, 0x0900, 0x0980, 0x0A00, 0x0A80, 0x0B00, 0x0B80, 0x0C00, 0x0C80, 0x0D00, 0x0D80, 0x0E00, 0x0E80, 0x0F00, 0x1000, 0x10A0, 0x1100, 0x1200, 0x1380, 0x13A0, 0x1400, 0x1680, 0x16A0, 0x1700, 0x1720, 0x1740, 0x1760, 0x1780, 0x1800, 0x18B0, 0x1900, 0x1950, 0x1980, 0x19E0, 0x1A00, 0x1A20, 0x1AB0, 0x1B00, 0x1B80, 0x1BC0, 0x1C00, 0x1C50, 0x1C80, 0x1CC0, 0x1CD0, 0x1D00, 0x1D80, 0x1DC0, 0x1E00, 0x1F00, 0x2000, 0x2070, 0x20A0, 0x20D0, 0x2100, 0x2150, 0x2190, 0x2200, 0x2300, 0x2400, 0x2440, 0x2460, 0x2500, 0x2580, 0x25A0, 0x2600, 0x2700, 0x27C0, 0x27F0, 0x2800, 0x2900, 0x2980, 0x2A00, 0x2B00, 0x2C00, 0x2C60, 0x2C80, 0x2D00, 0x2D30, 0x2D80, 0x2DE0, 0x2E00, 0x2E80, 0x2F00, 0x2FF0, 0x3000, 0x3040, 0x30A0, 0x3100, 0x3130, 0x3190, 0x31A0, 0x31C0, 0x31F0, 0x3200, 0x3300, 0x3400, 0x4DC0, 0x4E00, 0xA000, 0xA490, 0xA4D0, 0xA500, 0xA640, 0xA6A0, 0xA700, 0xA720, 0xA800, 0xA830, 0xA840, 0xA880, 0xA8E0, 0xA900, 0xA930, 0xA960, 0xA980, 0xA9E0, 0xAA00, 0xAA60, 0xAA80, 0xAAE0, 0xAB00, 0xAB30, 0xAB70, 0xABC0, 0xAC00, 0xD7B0, 0xD800, 0xDB80, 0xDC00, 0xE000, 0xF900, 0xFB00, 0xFB50, 0xFE00, 0xFE10, 0xFE20, 0xFE30, 0xFE50, 0xFE70, 0xFF00, 0xFFF0},
            blockDataEnds = {0x007F, 0x00FF, 0x017F, 0x024F, 0x02AF, 0x02FF, 0x036F, 0x03FF, 0x04FF, 0x052F, 0x058F, 0x05FF, 0x06FF, 0x074F, 0x077F, 0x07BF, 0x07FF, 0x083F, 0x085F, 0x086F, 0x08FF, 0x097F, 0x09FF, 0x0A7F, 0x0AFF, 0x0B7F, 0x0BFF, 0x0C7F, 0x0CFF, 0x0D7F, 0x0DFF, 0x0E7F, 0x0EFF, 0x0FFF, 0x109F, 0x10FF, 0x11FF, 0x137F, 0x139F, 0x13FF, 0x167F, 0x169F, 0x16FF, 0x171F, 0x173F, 0x175F, 0x177F, 0x17FF, 0x18AF, 0x18FF, 0x194F, 0x197F, 0x19DF, 0x19FF, 0x1A1F, 0x1AAF, 0x1AFF, 0x1B7F, 0x1BBF, 0x1BFF, 0x1C4F, 0x1C7F, 0x1C8F, 0x1CCF, 0x1CFF, 0x1D7F, 0x1DBF, 0x1DFF, 0x1EFF, 0x1FFF, 0x206F, 0x209F, 0x20CF, 0x20FF, 0x214F, 0x218F, 0x21FF, 0x22FF, 0x23FF, 0x243F, 0x245F, 0x24FF, 0x257F, 0x259F, 0x25FF, 0x26FF, 0x27BF, 0x27EF, 0x27FF, 0x28FF, 0x297F, 0x29FF, 0x2AFF, 0x2BFF, 0x2C5F, 0x2C7F, 0x2CFF, 0x2D2F, 0x2D7F, 0x2DDF, 0x2DFF, 0x2E7F, 0x2EFF, 0x2FDF, 0x2FFF, 0x303F, 0x309F, 0x30FF, 0x312F, 0x318F, 0x319F, 0x31BF, 0x31EF, 0x31FF, 0x32FF, 0x33FF, 0x4DBF, 0x4DFF, 0x9FFF, 0xA48F, 0xA4CF, 0xA4FF, 0xA63F, 0xA69F, 0xA6FF, 0xA71F, 0xA7FF, 0xA82F, 0xA83F, 0xA87F, 0xA8DF, 0xA8FF, 0xA92F, 0xA95F, 0xA97F, 0xA9DF, 0xA9FF, 0xAA5F, 0xAA7F, 0xAADF, 0xAAFF, 0xAB2F, 0xAB6F, 0xABBF, 0xABFF, 0xD7AF, 0xD7FF, 0xDB7F, 0xDBFF, 0xDFFF, 0xF8FF, 0xFAFF, 0xFB4F, 0xFDFF, 0xFE0F, 0xFE1F, 0xFE2F, 0xFE4F, 0xFE6F, 0xFEFF, 0xFFEF, 0xFFFD};
    private static final String[] blockDataNames = {"BasicLatin", "Latin-1Supplement", "LatinExtended-A", "LatinExtended-B", "IPAExtensions", "SpacingModifierLetters", "CombiningDiacriticalMarks", "Greek", "Cyrillic", "CyrillicSupplement", "Armenian", "Hebrew", "Arabic", "Syriac", "ArabicSupplement", "Thaana", "NKo", "Samaritan", "Mandaic", "SyriacSupplement", "ArabicExtended-A", "Devanagari", "Bengali", "Gurmukhi", "Gujarati", "Oriya", "Tamil", "Telugu", "Kannada", "Malayalam", "Sinhala", "Thai", "Lao", "Tibetan", "Myanmar", "Georgian", "HangulJamo", "Ethiopic", "EthiopicSupplement", "Cherokee", "UnifiedCanadianAboriginalSyllabics", "Ogham", "Runic", "Tagalog", "Hanunoo", "Buhid", "Tagbanwa", "Khmer", "Mongolian", "UnifiedCanadianAboriginalSyllabicsExtended", "Limbu", "TaiLe", "NewTaiLue", "KhmerSymbols", "Buginese", "TaiTham", "CombiningDiacriticalMarksExtended", "Balinese", "Sundanese", "Batak", "Lepcha", "OlChiki", "CyrillicExtended-C", "SundaneseSupplement", "VedicExtensions", "PhoneticExtensions", "PhoneticExtensionsSupplement", "CombiningDiacriticalMarksSupplement", "LatinExtendedAdditional", "GreekExtended", "GeneralPunctuation", "SuperscriptsAndSubscripts", "CurrencySymbols", "CombiningDiacriticalMarksForSymbols", "LetterlikeSymbols", "NumberForms", "Arrows", "MathematicalOperators", "MiscellaneousTechnical", "ControlPictures", "OpticalCharacterRecognition", "EnclosedAlphanumerics", "BoxDrawing", "BlockElements", "GeometricShapes", "MiscellaneousSymbols", "Dingbats", "MiscellaneousMathematicalSymbols-A", "SupplementalArrows-A", "BraillePatterns", "SupplementalArrows-B", "MiscellaneousMathematicalSymbols-B", "SupplementalMathematicalOperators", "MiscellaneousSymbolsAndArrows", "Glagolitic", "LatinExtended-C", "Coptic", "GeorgianSupplement", "Tifinagh", "EthiopicExtended", "CyrillicExtended-A", "SupplementalPunctuation", "CJKRadicalsSupplement", "KangxiRadicals", "IdeographicDescriptionCharacters", "CJKSymbolsAndPunctuation", "Hiragana", "Katakana", "Bopomofo", "HangulCompatibilityJamo", "Kanbun", "BopomofoExtended", "CJKStrokes", "KatakanaPhoneticExtensions", "EnclosedCJKLettersAndMonths", "CJKCompatibility", "CJKUnifiedIdeographsExtensionA", "YijingHexagramSymbols", "CJKUnifiedIdeographs", "YiSyllables", "YiRadicals", "Lisu", "Vai", "CyrillicExtended-B", "Bamum", "ModifierToneLetters", "LatinExtended-D", "SylotiNagri", "CommonIndicNumberForms", "Phags-pa", "Saurashtra", "DevanagariExtended", "KayahLi", "Rejang", "HangulJamoExtended-A", "Javanese", "MyanmarExtended-B", "Cham", "MyanmarExtended-A", "TaiViet", "MeeteiMayekExtensions", "EthiopicExtended-A", "LatinExtended-E", "CherokeeSupplement", "MeeteiMayek", "HangulSyllables", "HangulJamoExtended-B", "HighSurrogates", "HighPrivateUseSurrogates", "LowSurrogates", "PrivateUseArea", "CJKCompatibilityIdeographs", "AlphabeticPresentationForms", "ArabicPresentationForms-A", "VariationSelectors", "VerticalForms", "CombiningHalfMarks", "CJKCompatibilityForms", "SmallFormVariants", "ArabicPresentationForms-B", "HalfwidthAndFullwidthForms", "Specials"};

    static{
        //*
        DIGIT.setDigit(false);
        WORDCHAR.setWordChar(false);
        SPACE.setSpace(false);
        HSPACE.setHorizontalSpace(false);
        VSPACE.setVerticalSpace(false);

        UDIGIT.setDigit(true);
        UWORDCHAR.setWordChar(true);
        USPACE.setSpace(true);
        UHSPACE.setHorizontalSpace(true);
        UVSPACE.setVerticalSpace(true);

        NONDIGIT.setDigit(false);
        NONDIGIT.setPositive(false);
        NONWORDCHAR.setWordChar(false);
        NONWORDCHAR.setPositive(false);
        NONSPACE.setSpace(false);
        NONSPACE.setPositive(false);
        NONHSPACE.setHorizontalSpace(false);
        NONHSPACE.setPositive(false);
        NONVSPACE.setVerticalSpace(false);
        NONVSPACE.setPositive(false);

        UNONDIGIT.setDigit(true);
        UNONDIGIT.setPositive(false);
        UNONWORDCHAR.setWordChar(true);
        UNONWORDCHAR.setPositive(false);
        UNONSPACE.setSpace(true);
        UNONSPACE.setPositive(false);
        UNONHSPACE.setHorizontalSpace(true);
        UNONHSPACE.setPositive(false);
        UNONVSPACE.setVerticalSpace(true);
        UNONVSPACE.setPositive(false);

        initPosixClasses();
    }

    private static void registerClass(String name, BlockSet cls, ArrayList<String> realm){
        namedClasses.put(name, cls);
        if(!realm.contains(name)) realm.add(name);
    }

    private static void initPosixClasses(){
        BlockSet lower = new BlockSet();
        lower.setRange('a', 'z');
        registerClass("Lower", lower, posixClasses);
        BlockSet upper = new BlockSet();
        upper.setRange('A', 'Z');
        registerClass("Upper", upper, posixClasses);
        BlockSet ascii = new BlockSet();
        ascii.setRange((char) 0, (char) 0x7f);
        registerClass("ASCII", ascii, posixClasses);
        BlockSet alpha = new BlockSet();
        alpha.add(lower);
        alpha.add(upper);
        registerClass("Alpha", alpha, posixClasses);
        BlockSet digit = new BlockSet();
        digit.setRange('0', '9');
        registerClass("Digit", digit, posixClasses);
        BlockSet alnum = new BlockSet();
        alnum.add(alpha);
        alnum.add(digit);
        registerClass("Alnum", alnum, posixClasses);
        BlockSet punct = new BlockSet();
        punct.setChars("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
        registerClass("Punct", punct, posixClasses);
        BlockSet graph = new BlockSet();
        graph.add(alnum);
        graph.add(punct);
        registerClass("Graph", graph, posixClasses);
        registerClass("Print", graph, posixClasses);
        BlockSet blank = new BlockSet();
        blank.setChars(" \t");
        registerClass("Blank", blank, posixClasses);
        BlockSet cntrl = new BlockSet();
        cntrl.setRange((char) 0, (char) 0x1f);
        cntrl.setChar((char) 0x7f);
        registerClass("Cntrl", cntrl, posixClasses);
        BlockSet xdigit = new BlockSet();
        xdigit.setRange('0', '9');
        xdigit.setRange('a', 'f');
        xdigit.setRange('A', 'F');
        registerClass("XDigit", xdigit, posixClasses);
        BlockSet space = new BlockSet();
        space.setChars(" \t\n\r\f\u000b");
        registerClass("Space", space, posixClasses);
    }

    private static void initNames(){
        initNamedCategory("C");
        initNamedCategory("Cn");
        initNamedCategory("Cc");
        initNamedCategory("Cf");
        initNamedCategory("Co");
        initNamedCategory("Cs");

        initNamedCategory("L");
        initNamedCategory("Lu");
        initNamedCategory("Ll");
        initNamedCategory("Lt");
        initNamedCategory("Lm");
        initNamedCategory("Lo");

        initNamedCategory("M");
        initNamedCategory("Mn");
        initNamedCategory("Me");
        initNamedCategory("Mc");

        initNamedCategory("N");
        initNamedCategory("Nd");
        initNamedCategory("Nl");
        initNamedCategory("No");

        initNamedCategory("Z");
        initNamedCategory("Zs");
        initNamedCategory("Zl");
        initNamedCategory("Zp");
        initNamedCategory("Zh");
        initNamedCategory("Zv");

        initNamedCategory("P");
        initNamedCategory("Pd");
        initNamedCategory("Ps");
        initNamedCategory("Pi");
        initNamedCategory("Pe");
        initNamedCategory("Pf");
        initNamedCategory("Pc");
        initNamedCategory("Po");

        initNamedCategory("S");
        initNamedCategory("Sm");
        initNamedCategory("Sc");
        initNamedCategory("Sk");
        initNamedCategory("So");

        initNamedCategory("J");
        initNamedCategory("Js");
        initNamedCategory("Jp");

        initNamedCategory("G");
        initNamedCategory("Gh");
        initNamedCategory("Gv");

        BlockSet bs = new BlockSet();
        bs.setCategory("Cn");
        registerClass("UNASSIGNED", bs, unicodeCategories);
        bs = new BlockSet();
        bs.setCategory("Cn");
        bs.setPositive(false);
        registerClass("ASSIGNED", bs, unicodeCategories);

        for(int i = 0; i < blockDataStarts.length; i++){
            try{
                initNamedBlock(blockDataNames[i], blockDataStarts[i], blockDataEnds[i]);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        initNamedBlock("ALL", 0, 0xffff);

        namesInitialized = true;
        //*/
    }

    private static void initNamedBlock(String name, int first, int last){
        if(first < Character.MIN_VALUE || first > Character.MAX_VALUE)
            throw new IllegalArgumentException("wrong start code (" + first + ") in block " + name);
        if(last < Character.MIN_VALUE || last > Character.MAX_VALUE)
            throw new IllegalArgumentException("wrong end code (" + last + ") in block " + name);
        if(last < first) throw new IllegalArgumentException("end code < start code in block " + name);
        BlockSet bs = namedClasses.get(name);
        if(bs == null){
            bs = new BlockSet();
            registerClass(name, bs, unicodeBlocks);
        }
        bs.setRange((char) first, (char) last);
    }

    private static void initNamedCategory(String name){
        BlockSet bs = new BlockSet();
        bs.setCategory(name);
        registerClass(name, bs, unicodeCategories);
    }

    private static BlockSet getNamedClass(String name){
        if(!namesInitialized) initNames();
        return namedClasses.get(name);
    }
/*
    static void makeICase(Term term, char c) {
        BlockSet bs = new BlockSet();

        //bs.setChar(Character.toLowerCase(c));
        //bs.setChar(Character.toUpperCase(c));
        //bs.setChar(Character.toTitleCase(c));

        bs.setChar(Category.caseFold(c));
        BlockSet.unify(bs, term);
    }*/

    static void makeDigit(Term term, boolean inverse, boolean unicode){
        BlockSet digit = unicode ? inverse ? UNONDIGIT : UDIGIT :
                inverse ? NONDIGIT : DIGIT;
        BlockSet.unify(digit, term);
    }

    static void makeSpace(Term term, boolean inverse, boolean unicode){
        BlockSet space = unicode ? inverse ? UNONSPACE : USPACE :
                inverse ? NONSPACE : SPACE;
        BlockSet.unify(space, term);
    }

    static void makeHSpace(Term term, boolean inverse, boolean unicode){
        BlockSet space = unicode ? inverse ? UNONHSPACE : UHSPACE :
                inverse ? NONHSPACE : HSPACE;
        BlockSet.unify(space, term);
    }

    static void makeVSpace(Term term, boolean inverse, boolean unicode){
        BlockSet space = unicode ? inverse ? UNONVSPACE : UVSPACE :
                inverse ? NONVSPACE : VSPACE;
        BlockSet.unify(space, term);
    }

    static void makeWordChar(Term term, boolean inverse, boolean unicode){
        BlockSet wordChar = unicode ? inverse ? UNONWORDCHAR : UWORDCHAR :
                inverse ? NONWORDCHAR : WORDCHAR;
        BlockSet.unify(wordChar, term);
    }

    static void makeWordBoundary(Term term, boolean inverse, boolean unicode){
        makeWordChar(term, inverse, unicode);
        term.type = unicode ? UBOUNDARY : BOUNDARY;
    }

    static void makeWordStart(Term term, boolean unicode){
        makeWordChar(term, false, unicode);
        term.type = unicode ? UDIRECTION : DIRECTION;
    }

    static void makeWordEnd(Term term, boolean unicode){
        makeWordChar(term, true, unicode);
        term.type = unicode ? UDIRECTION : DIRECTION;
    }

    static void parseGroup(char[] data, int i, int out, Term term, boolean icase, boolean skipspaces,
                           boolean unicode, boolean xml) throws PatternSyntaxException{
        BlockSet sum = new BlockSet();
        BlockSet bs = new BlockSet();
        int mode = ADD;
        char c;
        for(; i < out; ){
            switch(c = data[i++]){
                case '+':
                    mode = ADD;
                    continue;
                case '-':
                    mode = SUBTRACT;
                    continue;
                case '&':
                    mode = INTERSECT;
                    continue;
                case '[':
                    bs.reset();
                    i = parseClass(data, i, out, bs, icase, skipspaces, unicode, xml);
                    switch(mode){
                        case ADD:
                            sum.add(bs);
                            break;
                        case SUBTRACT:
                            sum.subtract(bs);
                            break;
                        case INTERSECT:
                            sum.intersect(bs);
                            break;
                    }
                    continue;
                case ')':
                    throw new PatternSyntaxException("unbalanced class group");
            }
        }
        BlockSet.unify(sum, term);
    }

    static int parseClass(char[] data, int i, int out, Term term, boolean icase, boolean skipspaces,
                          boolean unicode, boolean xml) throws PatternSyntaxException{
        BlockSet bs = new BlockSet();
        i = parseClass(data, i, out, bs, icase, skipspaces, unicode, xml);
        BlockSet.unify(bs, term);
        return i;
    }

    static int parseName(char[] data, int i, int out, Term term, boolean inverse,
                         boolean skipspaces) throws PatternSyntaxException{
        StringBuilder sb = new StringBuilder();
        i = parseName(data, i, out, sb, skipspaces);
        BlockSet bs = getNamedClass(sb.toString());
        if(bs == null) throw new PatternSyntaxException("unknown class: {" + sb + "}");
        BlockSet.unify(bs, term);
        term.inverse = inverse;
        return i;
    }

    /*
     * @param mode add/subtract
     */
    private static int parseClass(char[] data, int i, int out, BlockSet bs,
                                  boolean icase, boolean skipspaces,
                                  boolean unicode, boolean xml) throws PatternSyntaxException{
        char c;
        int prev = -1, oct = 0;
        boolean isFirst = true, setFirst = false, inRange = false;
        BlockSet bs1 = null;
        StringBuilder sb = null;

        for(; i < out; isFirst = setFirst, setFirst = false){
            handle_special:
            switch(c = data[i++]){
                case ']':
                    if(isFirst) break; //treat as normal char
                    if(inRange){
                        bs.setChar('-');
                    }
                    if(prev >= 0){
                        char c1 = (char) prev;
                        if(icase){
                            /*
                            bs.setChar(Character.toLowerCase(c1));
                            bs.setChar(Character.toUpperCase(c1));
                            bs.setChar(Character.toTitleCase(c1));
                            */
                            bs.setChar(Category.caseFold(c1));
                        }else bs.setChar(c1);
                    }
                    return i;

                case '-':
                    if(isFirst) break;
                    //if(isFirst) throw new PatternSyntaxException("[-...] is illegal");
                    if(inRange) break;
                    //if(inRange) throw new PatternSyntaxException("[...--...] is illegal");
                    inRange = true;
                    continue;

                case '[':
                    if(inRange && xml){ //[..-[..]]
                        if(prev >= 0) bs.setChar((char) prev);
                        if(bs1 == null) bs1 = new BlockSet();
                        else bs1.reset();
                        i = parseClass(data, i, out, bs1, icase, skipspaces, unicode, xml);
                        bs.subtract(bs1);
                        inRange = false;
                        prev = -1;
                        continue;
                    }else break;

                case '^':
                    //if(!isFirst) throw new PatternSyntaxException("'^' isn't a first char in a class def");
                    //bs.setPositive(false);
                    //setFirst=true;
                    //continue;
                    if(isFirst){
                        bs.setPositive(false);
                        setFirst = true;
                        continue;
                    }
                    //treat as normal char
                    break;

                case ' ':
                case '\r':
                case '\n':
                case '\t':
                case '\f':
                    if(skipspaces) continue;
                    else break;
                case '\\':
                    BlockSet negativeClass = null;
                    boolean inv = false;
                    switch(c = data[i++]){
                        case 'r':
                            c = '\r';
                            break handle_special;

                        case 'n':
                            c = '\n';
                            break handle_special;

                        case 't':
                            c = '\t';
                            break handle_special;

                        case 'f':
                            c = '\f';
                            break handle_special;

                        case 'u':
                            if(i >= out - 4) throw new PatternSyntaxException("incomplete escape sequence \\uXXXX");
                            c = (char) ((toHexDigit(data[i++]) << 12)
                                    + (toHexDigit(data[i++]) << 8)
                                    + (toHexDigit(data[i++]) << 4)
                                    + toHexDigit(data[i++]));
                            break handle_special;

                        case 'b':
                            c = 8; // backspace
                            break handle_special;

                        case 'x':{   // hex 2-digit number
                            int hex = 0;
                            char d;
                            if((d = data[i++]) == '{'){
                                while(i < out && (d = data[i++]) != '}'){
                                    hex = (hex << 4) + toHexDigit(d);
                                }
                                if(hex > 0xffff || i == out)
                                    throw new PatternSyntaxException("\\x{<out of range or incomplete>}");
                            }else{
                                if(i >= out - 2) throw new PatternSyntaxException("incomplete escape sequence \\xHH");
                                hex = (toHexDigit(d) << 4) + toHexDigit(data[i++]);
                            }
                            c = (char) hex;
                            break handle_special;
                        }
                        case 'o':   // oct 2- or 3-digit number
                            oct = 0;
                            for(; ; ){
                                char d = data[i++];
                                if(d >= '0' && d <= '7'){
                                    oct *= 8;
                                    oct += d - '0';
                                    if(oct > 0xffff){
                                        oct -= d - '0';
                                        oct /= 8;
                                        break;
                                    }
                                }else break;
                            }
                            c = (char) oct;
                            break handle_special;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                            oct = 0;
                            for(; ; ){
                                char d = data[i - 1];
                                if(d >= '0' && d <= '7'){
                                    i++;
                                    oct *= 8;
                                    oct += d - '0';
                                    if(oct > 0xffff){
                                        oct -= d - '0';
                                        oct /= 8;
                                        break;
                                    }
                                }else{
                                    i--;
                                    break;
                                }
                            }
                            c = (char) oct;
                            break handle_special;

                        case 'm':   // decimal number -> char
                            int dec = 0;
                            for(; ; ){
                                char d = data[i++];
                                if(d >= '0' && d <= '9'){
                                    dec *= 10;
                                    dec += d - '0';
                                    if(dec > 0xffff){
                                        dec -= d - '0';
                                        dec /= 10;
                                        break;
                                    }
                                }else break;
                            }
                            c = (char) dec;
                            break handle_special;

                        case 'c':   // ctrl-char
                            c = (char) (data[i++] & 0x1f);
                            break handle_special;

                        //classes;
                        //
                        case 'D':   // non-digit
                            negativeClass = unicode ? UNONDIGIT : NONDIGIT;
                            break;

                        case 'S':   // space
                            negativeClass = unicode ? UNONSPACE : NONSPACE;
                            break;

                        case 'W':   // space
                            negativeClass = unicode ? UNONWORDCHAR : NONWORDCHAR;
                            break;

                        case 'd':   // digit
                            if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\d...]");
                            bs.setDigit(unicode);
                            continue;

                        case 's':   // digit
                            if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\s...]");
                            bs.setSpace(unicode);
                            continue;

                        case 'w':   // digit
                            if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\w...]");
                            bs.setWordChar(unicode);
                            continue;

                        case 'h':   // horizontal whitespace
                            if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\w...]");
                            bs.setHorizontalSpace(unicode);
                            continue;

                        case 'v':   // vertical whitespace
                            if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\w...]");
                            bs.setVerticalSpace(unicode);
                            continue;

                        case 'P':   // \\P{..}
                            if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\P...]");
                            inv = true;
                        case 'p':   // \\p{..}
                            if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\p...]");
                            if(sb == null) sb = new StringBuilder();
                            else sb.setLength(0);
                            i = parseName(data, i, out, sb, skipspaces);
                            BlockSet nc = getNamedClass(sb.toString());
                            if(nc == null) throw new PatternSyntaxException("unknown named class: {" + sb + "}");
                            bs.add(nc, inv);
                            continue;
                        case 'Q':
                        case 'E':
                            throw new PatternSyntaxException("Escaped \\Q\\E literals cannot be inside character classes");
                        default:
                            //other escaped treat as normal
                            break handle_special;
                    }
                    //negativeClass;
                    //\S,\D,\W
                    if(inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\" + c + "...]");
                    bs.add(negativeClass);
                    continue;

                    /*
                case '{':
                    if (inRange) throw new PatternSyntaxException("illegal range: [..." + prev + "-\\w...]");
                    if (sb == null) sb = new StringBuilder();
                    else sb.setLength(0);
                    i = parseName(data, i - 1, out, sb, skipspaces);
                    BlockSet nc = getNamedClass(sb.toString());
                    if (nc == null) throw new PatternSyntaxException("unknown named class: {" + sb + "}");
                    bs.add(nc, false);
                    continue;
                    */
                default:
            }
            //c is a normal char
            if(prev < 0){
                prev = c;
                inRange = false;
                continue;
            }
            if(!inRange){
                char c1 = (char) prev;
                if(icase){
                    /*
                    bs.setChar(Character.toLowerCase(c1));
                    bs.setChar(Character.toUpperCase(c1));
                    bs.setChar(Character.toTitleCase(c1));
                    */
                    bs.setChar(Category.caseFold(c1));
                }else bs.setChar(c1);
                prev = c;
            }else{
                if(prev > c)
                    throw new PatternSyntaxException("illegal range: " + prev + ">" + c);
                char c0 = (char) prev;
                inRange = false;
                prev = -1;
                if(icase){
                    /*
                    bs.setRange(Character.toLowerCase(c0), Character.toLowerCase(c));
                    bs.setRange(Character.toUpperCase(c0), Character.toUpperCase(c));
                    bs.setRange(Character.toTitleCase(c0), Character.toTitleCase(c));
                    */
                    bs.setRange(Category.caseFold(c0), Category.caseFold(c));

                }else bs.setRange(c0, c);
            }
        }
        throw new PatternSyntaxException("unbalanced brackets in a class def");
    }


    private static int parseName(char[] data, int i, int out, StringBuilder sb,
                                 boolean skipspaces) throws PatternSyntaxException{
        char c;
        int start = -1;
        while(i < out){
            switch(c = data[i++]){
                case '{':
                    start = i;
                    continue;
                case '}':
                    return i;
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                case '\f':
                    if(skipspaces) continue;
                    //else pass on

                case 'I':
                    if(start > 0 && start + 1 == i){
                        if(data[i] == 's' || data[i] == 'n'){
                            i++;
                            continue;
                        }
                    }else if(start < 0)
                        throw new PatternSyntaxException("Is or In named class doesn't start with '{'");
                case 'C':
                case 'L':
                case 'M':
                case 'N':
                case 'Z':
                case 'P':
                case 'S':
                case 'J':
                    if(start < 0){
                        sb.append(c);
                        return i;
                    }
                default:
                    if(start < 0) throw new PatternSyntaxException("named class doesn't start with '{'");
                    sb.append(c);
            }
        }
        throw new PatternSyntaxException("wrong class name: " + new String(data, i, out - i));
    }

    private static StringBuilder b0 = new StringBuilder(100), b2 = new StringBuilder(100);

    static String stringValue0(IntBitSet arr){
        b0.setLength(0);
        int c = 0;

        for(; ; ){
            c = arr.nextSetBit(c);
            if(c < 0 || c >= 0xff) break;

            int first = c;

            c = arr.nextClearBit(c);
            if(c <= 0 || c > 0xff) break;

            int last = c - 1;
            if(last == first) b0.append(stringValue(last));
            else{
                b0.append(stringValue(first));
                b0.append('-');
                b0.append(stringValue(last));
            }
            if(c > 0xff) break;
        }
        return b0.toString();
    }
   
   /* Mmm.. what is it? 
   static String stringValueC(boolean[] categories){
      StringBuffer sb=new StringBuffer();
      for(int i=0;i<categories.length;i++){
         if(!categories[i]) continue;
         String name=(String)unicodeCategoryNames.get(new Integer(i));
         sb.append('{');
         sb.append(name);
         sb.append('}');
      }
      return sb.toString();
   }
   */

    static String stringValue2(IntBitSet[] arr){
        b2.setLength(0);
        int c = 0;
        loop:
        for(; ; ){
            boolean marked = false;
            for(; ; ){
                IntBitSet marks = arr[c >> 8];
                if(marks != null && marks.get(c & 255)) break;
                c++;
                if(c > 0xffff) break loop;
            }
            int first = c;
            for(; c <= 0xffff; ){
                IntBitSet marks = arr[c >> 8];
                if(marks == null || !marks.get(c & 255)) break;
                c++;
            }
            int last = c - 1;
            if(last == first) b2.append(stringValue(last));
            else{
                b2.append(stringValue(first));
                b2.append('-');
                b2.append(stringValue(last));
            }
            if(c > 0xffff) break;
        }
        return b2.toString();
    }

    static String stringValue(int c){
        if(c < 32){
            switch(c){
                case '\r':
                    return "\\r";
                case '\n':
                    return "\\n";
                case '\t':
                    return "\\t";
                case '\f':
                    return "\\f";
                default:
                    return "\\x" + c;
            }
        }
        return String.valueOf((char) c);
    }

    static int toHexDigit(char d) throws PatternSyntaxException{
        int val;
        if(d >= '0' && d <= '9') val = d - '0';
        else if(d >= 'a' && d <= 'f') val = 10 + d - 'a';
        else if(d >= 'A' && d <= 'F') val = 10 + d - 'A';
        else throw new PatternSyntaxException("hexadecimal digit expected: " + d);
        return val;
    }

    /*
    public static void main(String[] args) {
        if (!namesInitialized) initNames();
        if (args.length == 0) {
            System.out.println("Class usage: \\p{Class},\\P{Class}");
            printRealm(posixClasses, "Posix classes");
            printRealm(unicodeCategories, "Unicode categories");
            printRealm(unicodeBlocks, "Unicode blocks");
        } else {
            for (int i = 0; i < args.length; i++) {
                System.out.print(args[i]);
                System.out.print(": ");
                System.out.println(namedClasses.containsKey(args[i]) ? "supported" : "not supported");
            }
        }
    }
    */
      /*
      int[][] data=new int[CATEGORY_COUNT][BLOCK_SIZE+2];
      for(int i=Character.MIN_VALUE;i<=Character.MAX_VALUE;i++){
         int cat=Character.getType((char)i);
         data[cat][BLOCK_SIZE]++;
         int b=(i>>8)&0xff;
         if(data[cat][b]==0){
            data[cat][b]=1;
            data[cat][BLOCK_SIZE+1]++;
         }
      }
      for(int i=0;i<CATEGORY_COUNT;i++){
         System.out.print(unicodeCategoryNames.get(new Integer(i))+": ");
         System.out.println(data[i][BLOCK_SIZE]+" chars, "+data[i][BLOCK_SIZE+1]+" blocks, "+(data[i][BLOCK_SIZE]/data[i][BLOCK_SIZE+1])+" chars/block");
      }
      */


    private static void printRealm(ArrayList<String> realm, String name){
        System.out.println(name + ":");
        for(String s : realm){
            System.out.println("  " + s);
        }
    }
}