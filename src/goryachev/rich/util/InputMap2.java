/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
import java.util.HashMap;
import java.util.List;
import javafx.scene.input.KeyCode;

/**
 * Input Map maps KeyBindings(2) to Action IDs (any object but Runnable or FxAction, typically an enum) and
 * Action IDs to the actual actions (Runnable, FxAction, or lambda).
 * 
 * The input map may not be limited to a keyboard event, so looking up an action from an Action ID for a
 * built-in functionality such as copy, paste, etc. is also permitted.
 *  
 * Example:
 *  
 * Control:
 * - declares an enum action id.
 * - declares public methods that lookup action id, then execute corresponding action (unless disabled)
 * - might declare public FxActions (ex.: copyAction which delegate to action id)
 * Behavior:
 * - maps key bindings to action ids
 * - maps action ids to methods in the behavior
 *  
 *  
 */
public class InputMap2 {
    private final HashMap<Object,Object> map = new HashMap<>();

    public InputMap2() {
    }

    // TODO or make KeyBinding2 class public with a bunch of factory methods
    // TODO should take additional FxAction argument instead of Runnable?
    public void add(Object owner, Runnable function, KeyCode code, KCondition ... modifiers) {
        // TODO check for nulls
        KeyBinding2 k = KeyBinding2.of(code, modifiers);
        if(k != null) {
            map.put(k, function);
            //System.err.println("add " + k); // FIX
        }
        
        List<Object> remove = getRemoveList(owner);
        // FIX this will incorrectly remove a user defined function
        remove.add(k);
    }

    /** returns a Runnable function object for the given Action.  Might return null. */
    public Runnable getFunction(KeyBinding2 k) {
        Object v = map.get(k);
        if (v instanceof Runnable r) {
            return r;
        }
        return null;
    }
    
    // TODO this should return FxAction which app developer can enable/disable
    /*
    public FxAction getAction(Object tag) {
        // map can hold Actions as well
    }
    */

    private List<Object> getRemoveList(Object owner) {
        CompoundKey k = new CompoundKey(owner);
        Object x = map.get(k);
        if (x instanceof List list) {
            return list;
        }

        ArrayList<Object> list = new ArrayList<>();
        map.put(k, list);
        return list;
    }

    public void dispose(Object owner) {
        CompoundKey k = new CompoundKey(owner);
        Object x = map.remove(k);

        if (x instanceof List toRemove) {
            for (Object r : toRemove) {
                // FIX need to check if the entry was set by the user or by the owner
                map.remove(r);
            }
        }
    }
}
