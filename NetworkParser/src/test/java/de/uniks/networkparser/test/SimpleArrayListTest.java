package de.uniks.networkparser.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import de.uniks.networkparser.list.SimpleList;


public class SimpleArrayListTest
{

   @Test
   public void test()
   {
      ArrayList<TestObject> list = gtTestList();
      SimpleList<TestObject> simpleArrayList = new SimpleList<SimpleArrayListTest.TestObject>();
      for (TestObject testObject : list)
      {
         simpleArrayList.add(testObject);
      }
      for (int i = 0 ; i < simpleArrayList.size() ; i++)
      {
       assertEquals(list.get(i), simpleArrayList.get(i));  
      }
      
      
      for (int i = 0 ; i < simpleArrayList.size() ; i++)
      {
         assertEquals(i, simpleArrayList.getIndex(list.get(i)));
      }
      
      
   }


   private ArrayList<TestObject> gtTestList()
   {
      ArrayList<TestObject> list =  new ArrayList<>();
      TestObject obj0 = new TestObject(Integer.MIN_VALUE);
      list.add(obj0);
      TestObject obj1 = new TestObject(-10);
      list.add(obj1);
      TestObject obj2 = new TestObject(-1);
      list.add(obj2);
      TestObject obj3 = new TestObject(0);
      list.add(obj3);
      TestObject obj4 = new TestObject(1);
      list.add(obj4);
      TestObject obj5 = new TestObject(10);
      list.add(obj5);
      TestObject obj6 = new TestObject(Integer.MAX_VALUE);
      list.add(obj6);
      return list;
   }
   
   
   class TestObject{
      private int hash;

      public TestObject(int hash)
      {
         this.hash = hash;
      }
      
      @Override
      public int hashCode()
      {
         return hash;
      }
      
      
      @Override
      public boolean equals(Object obj)
      {
         return super.equals(obj);
      }
   }

}
