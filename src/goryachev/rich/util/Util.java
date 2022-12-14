/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package goryachev.rich.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.css.CssMetaData;
import javafx.css.Styleable;

import goryachev.rich.Marker;

/**
 * Utility methods to be moved to com.sun.javafx hierarchy.
 */
public class Util {
    /**
     * Combines CssMetaData items in one unmodifiable list with the size equal to the number
     * of items it holds (i.e. with no unnecessary overhead).
     * 
     * @param list css metadata items, usually from the parent
     * @param items additional items
     * @return unmodifiable list containing all the items
     */
    public static List<CssMetaData<? extends Styleable, ?>> initStyleables(
            List<CssMetaData<? extends Styleable, ?>> list,
            CssMetaData<? extends Styleable, ?>... items) {

        int sz = list.size() + items.length;
        ArrayList<CssMetaData<? extends Styleable, ?>> rv = new ArrayList<>(sz);
        rv.addAll(list);
        for (CssMetaData<? extends Styleable, ?> p : items) {
            rv.add(p);
        }
        return Collections.unmodifiableList(rv);
    }

    public static String getResourceURL(Class<?> c, String name) {
        String pkg = c.getPackage().getName().replace(".", "/");
        if (pkg.length() != 0) {
            name = "/" + pkg + "/" + name;
        }
        return c.getResource(name).toExternalForm();
    }

    public static <T> T notNull(T item, String name) {
        if(item == null) {
            throw new IllegalArgumentException(name + " must not be null.");
        }
        return item;
    }
    
    public static <T extends Comparable<T>> void isLessThanOrEqual(T min, T max, String nameMin, String nameMax) {
        if(min.compareTo(max) > 0) {
            throw new IllegalArgumentException(nameMin + " must be less or equal to " + nameMax);
        }
    }
}
