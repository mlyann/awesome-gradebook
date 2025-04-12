package org.fp;

import java.util.*;

public class IDGen {
    // Set does not allow duplicates. Guarantees unique IDs
    private static final Set<String> currentIDs = new HashSet<>();
    private static final Random random = new Random();

    public static String getUniqueID(){
        // Loop until unique ID is generated
        while(true){
            String newID = "";
            ArrayList<Integer> digits = new ArrayList<>();
            for(int i = 0; i <= 9; i++){
                digits.add(i);
            }

            Collections.shuffle(digits, random);

            for (int i = 0; i < 8; i++){
                newID += digits.get(i);
            }

            // If id is unique, return it. Else, loop again
            if (currentIDs.add(newID)) {
                return newID;
            }
        }
    }

/*
    // Testing Unique ID gen
    public static void main(String[] args){
        for(int i = 0; i < 20; i++){
            System.out.println("Unique Id: " + org.fp.IDGen.getUniqueID());
        }
    }
 */
}
