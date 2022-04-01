package com.wwf.shrimp.application.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.wwf.shrimp.application.models.Document;

public class DataUtils {
	
	// Function to remove duplicates from an ArrayList 
    public static ArrayList<Document> removeDuplicates(ArrayList<Document> list) 
    { 
  
        // Create a new LinkedHashSet 
        Set<Document> set = new LinkedHashSet<>(); 
  
        // Add the elements to set 
        set.addAll(list); 
  
        // Clear the list 
        list.clear(); 
  
        // add the elements of set 
        // with no duplicates to the list 
        list.addAll(set); 
  
        // return the list 
        return list; 
    }

}
