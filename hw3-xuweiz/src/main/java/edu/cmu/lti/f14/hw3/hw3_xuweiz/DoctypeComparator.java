package edu.cmu.lti.f14.hw3.hw3_xuweiz;

import java.util.Comparator;

/**
 * Description: Compare cosine similarity in the list.
 * 
 * @author Xuwei Zou
 *
 */
public class DoctypeComparator implements Comparator<Doctype> {
/**
 * Override the compare function.
 */
  @Override
  public int compare(Doctype o1, Doctype o2) {
    // TODO Auto-generated method stub
    if(o1.getcossim()>o2.getcossim()){
      return -1;
    }
    else if(o1.getcossim()<o2.getcossim()){
      return 1;
    }
    else{
      if(o1.getrel()==1){
        return -1;
      }
      else{
        return 1;
      }
    }
  }

}
