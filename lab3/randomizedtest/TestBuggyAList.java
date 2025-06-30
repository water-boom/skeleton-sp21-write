package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddTreeRemove(){
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> text = new BuggyAList<>();

        correct.addLast(4);
        correct.addLast(5);
        correct.addLast(6);

        text.addLast(4);
        text.addLast(5);
        text.addLast(6);

        assertEquals(correct.size(),text.size());
        assertEquals(correct.removeLast(),text.removeLast());
        assertEquals(correct.removeLast(),text.removeLast());
        assertEquals(correct.removeLast(),text.removeLast());

    }
    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> M = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                M.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size1 = L.size();
                int size2 = M.size();
                assertEquals(size1, size2);
                System.out.println("size: " + size1);
            } else if(operationNumber == 2){
                if(L.size() == 0) continue;
                int last1 = L.getLast();
                int last2 = M.getLast();
                assertEquals(last1,last2);
                System.out.println("getLast("+ last1 + ")" );
            }else if(operationNumber == 3){
                if(L.size() == 0) continue;
                int  last1  = L.removeLast();
                int last2 = M.removeLast();
                assertEquals(last1,last2);
                assertEquals(L.size(),M.size());
                System.out.println("removeLast("+ last1 + ")" );
            }
        }
    }
}
