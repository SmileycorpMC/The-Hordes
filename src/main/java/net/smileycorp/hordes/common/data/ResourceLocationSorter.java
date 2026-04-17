package net.smileycorp.hordes.common.data;

import net.minecraft.resources.ResourceLocation;

public class ResourceLocationSorter {

    public static int sort(ResourceLocation loc, ResourceLocation other) {
        String a = loc.toString();
        String b = other.toString();
        int ia = 0, ib = 0;
        int nza, nzb;
        char ca, cb;
        int result;
        while (true) {
            nza = nzb = 0;
            ca = charAt(a, ia);
            cb = charAt(b, ib);
            while (ca == '0') {
                nza++;
                if (!Character.isDigit(charAt(a, ia + 1))) break;
                ca = charAt(a, ia++);
            }
            while (cb == '0') {
                nzb++;
                if (!Character.isDigit(charAt(b, ib + 1))) break;
                cb = charAt(b, ib++);
            }
            if (Character.isDigit(ca) && Character.isDigit(cb))
                if ((result = compareRight(a.substring(ia), b.substring(ib))) != 0) return result;
            if (ca == 0 && cb == 0) return nza - nzb;
            if (ca < cb) return -1;
            else if (ca > cb) return +1;
            ia++;
            ib++;
        }
    }

    private static char charAt(String s, int i) {
        return i >= s.length() ? 0 : Character.toUpperCase(s.charAt(i));
    }

    private static int compareRight(String a, String b) {
        int bias = 0;
        int ia = 0;
        int ib = 0;
        for (;; ia++, ib++) {
            char ca = charAt(a, ia);
            char cb = charAt(b, ib);
            if (!Character.isDigit(ca) && !Character.isDigit(cb)) return bias;
            else if (!Character.isDigit(ca)) return -1;
            else if (!Character.isDigit(cb)) return 1;
            else if (ca < cb) {
                if (bias == 0) bias = -1;
            }
            else if (ca > cb) {
                if (bias == 0) bias = 1;
            }
            else if (ca == 0) return bias;
        }
    }

}
