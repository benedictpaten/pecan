/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

package bp.common.ds;

import java.util.Random;

/**
 * This skiplist implementation was borrowed from Thomas Wenger "The Elegant
 * (and Fast) Skip List" in javapro
 */
public final class SkipList {

    // private data members:
    int myMaxLevel;

    int myLevel;

    SkipListNode myHeader, nilNode;

    float myProbability;

    public static final int NIL_KEY = Integer.MAX_VALUE;

    // 0.25 is a good probability
    final static float GOOD_PROB = (float) 0.25;

    public static final Object NOT_FOUND_OBJ = null;

    public SkipList(final float probability, final int maxLevel) {
        this.myProbability = probability;
        this.myMaxLevel = maxLevel;
        this.myLevel = 0; // level of empty list

        // generate the header of the list:
        this.myHeader = new SkipListNode(this.myMaxLevel, Integer.MIN_VALUE,
                null);

        // append "NIL" element to header:
        this.nilNode = new SkipListNode(this.myMaxLevel, SkipList.NIL_KEY, null);
        for (int i = 0; i <= this.myMaxLevel; i++) {
            this.myHeader.forward[i] = this.nilNode;
        }
    }

    public SkipList() {
        this(Integer.MAX_VALUE);
    }

    public SkipList(final long maxNodes) {
        // see Pugh for math. background
        this(SkipList.GOOD_PROB, (int) Math.ceil(Math.log(maxNodes)
                / Math.log(1 / SkipList.GOOD_PROB)) - 1);
    }

    public void clear() {
        for (int i = 0; i <= this.myMaxLevel; i++) {
			this.myHeader.forward[i] = this.nilNode;
		}
        this.myLevel = 0; // level of empty list
    }

    public void delete(final int searchKey) {
        final SkipListNode[] update = new SkipListNode[this.myMaxLevel + 1];

        // init "cursor" element to header:
        SkipListNode cursor = this.myHeader;

        // find place to insert new node:
        for (int i = this.myLevel; i >= 0; i--) {
            while (cursor.forward[i].key < searchKey) {
                cursor = cursor.forward[i];
            }
            update[i] = cursor;
        }
        cursor = cursor.forward[0];
        // rebuild list without node:
        if (cursor.key == searchKey) {
            for (int i = 0; i <= this.myLevel; i++) {
                if (update[i].forward[i] == cursor) {
                    update[i].forward[i] = cursor.forward[i];
                }
            }
            // correct level of list:
            while ((this.myLevel > 0)
                    && (this.myHeader.forward[this.myLevel].key == SkipList.NIL_KEY)) {
                this.myLevel--;
            }
        }
    }

    protected int generateRandomLevel() {
        int newLevel = 0;
        while ((newLevel < this.myMaxLevel) && (Math.random() < this.myProbability)) {
            newLevel++;
        }
        return newLevel;
    }

    public void insert(final int searchKey, final Object value) {
        final SkipListNode[] update = new SkipListNode[this.myMaxLevel + 1];

        // init "cursor" element to header:
        SkipListNode cursor = this.myHeader;

        // find place to insert new node:
        for (int i = this.myLevel; i >= 0; i--) {
            while (cursor.forward[i].key < searchKey) {
                cursor = cursor.forward[i];
            }
            update[i] = cursor;
        }
        cursor = cursor.forward[0];

        // element with same key:
        if (cursor.key == searchKey) {
            cursor.value = value;
        }
        // or an additional
        // element is inserted:
        else {
            final int newLevel = this.generateRandomLevel();
            // new element has highest level:
            if (newLevel > this.myLevel) {
                for (int i = this.myLevel + 1; i <= newLevel; i++) {
                    update[i] = this.myHeader;
                }
                this.myLevel = newLevel;
            }

            // insert new element:
            cursor = new SkipListNode(newLevel, searchKey, value);
            for (short i = 0; i <= newLevel; i++) {
                cursor.forward[i] = update[i].forward[i];
                update[i].forward[i] = cursor;
            }
        }
    }

    public Iterator iterator() {
        return new Iterator();
    }

    /**
     * this class allows simple range queries
     */
    public class Iterator implements java.util.Iterator {
        SkipListNode cursor;

        int stop, cValue = Integer.MIN_VALUE;

        Iterator() {
            this.cursor = SkipList.this.myHeader.forward[0];
            this.stop = Integer.MAX_VALUE;
        }

        public int currentNodeKey() {
            return this.cValue;
        }

        public boolean hasNext() {
            return this.cursor.key < this.stop;
        }

        public Object next() {
            final SkipListNode pCursor = this.cursor;
            this.cValue = pCursor.key;
            this.cursor = this.cursor.forward[0];
            return pCursor.value;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Find element preceding searchKey, which may be header
     */
    private SkipListNode rangeEnd(final int searchKey) {
        // init "cursor" element to header:
        SkipListNode cursor = this.myHeader;

        // find element in list:
        for (int i = this.myLevel; i >= 0; i--) {
            SkipListNode nextNode = cursor.forward[i];
            while (nextNode.key < searchKey) {
                cursor = nextNode;
                nextNode = cursor.forward[i];
            }
        }
        return cursor;
    }

    private SkipListNode rangeStart(final int searchKey) {
        // init "cursor" element to header:
        SkipListNode cursor = this.myHeader;

        // find element in list:
        for (int i = this.myLevel; i >= 0; i--) {
            SkipListNode nextNode = cursor.forward[i];
            while (nextNode.key < searchKey) {
                cursor = nextNode;
                nextNode = cursor.forward[i];
            }
        }
        cursor = cursor.forward[0];
        return cursor;
    }

    public Object search(final int searchKey) {
        // init "cursor" element to header:
        SkipListNode cursor = this.myHeader;

        // find element in list:
        for (int i = this.myLevel; i >= 0; i--) {
            SkipListNode nextNode = cursor.forward[i];
            while (nextNode.key < searchKey) {
                cursor = nextNode;
                nextNode = cursor.forward[i];
            }
        }
        cursor = cursor.forward[0];

        if (cursor.key == searchKey) {
			return cursor.value;
		}
        return SkipList.NOT_FOUND_OBJ;
    }
    
    public Object searchGreaterThanOrEqual(final int searchKey) {
        final SkipListNode cursor = this.rangeStart(searchKey);
        return cursor.value;
    }

    public final Object searchLessThan(final int searchKey) {
        final SkipListNode cursor = this.rangeEnd(searchKey);
        return cursor.value;
    }

    public static class SkipListNode {

        // Constructor:
        // Constructs a new list element
        public SkipListNode(final int level, final int key, final Object value) {
            this.key = key;
            this.value = value;
            this.forward = new SkipListNode[level + 1];
        }

        // accessible attributes:
        int key; // key data

        Object value; // associated value

        // array of forward pointers
        SkipListNode forward[];
    }

    public static void main(final String[] args) {
        final String[] elements = new String[] { " booo ", " ship ",
                " jonah ", " etc " };
        final SkipList sk = new SkipList(100000);
        final Random r = new Random();
        final int[] list = new int[Integer.parseInt(args[0])];
        for (int i = 0; i < Integer.parseInt(args[0]); i++) {
            final int j = r.nextInt(Integer.MAX_VALUE);
            list[i] = j;
            sk.insert(j, j + " "
                    + elements[r.nextInt(elements.length)]);

        }
        System.out.println(sk.toString());
        for (int i = 0; i < Integer.parseInt(args[0]); i++) {
			System.out.println(sk.search(list[i]).toString());
		}
    }

    @Override
	public String toString() {
        final StringBuffer sB = new StringBuffer();
        SkipListNode cursor = this.myHeader;
        while (cursor.key != SkipList.NIL_KEY) {
            if (cursor.value != null) {
				sB.append(cursor.value.toString() + "\n");
			}
            cursor = cursor.forward[0];
        }
        return sB.toString();
    }

}