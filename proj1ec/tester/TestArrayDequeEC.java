package tester;

import static org.junit.Assert.*;
import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import org.junit.Test;
import student.StudentArrayDeque;


public class TestArrayDequeEC {

    @Test
    public void randomizedTest() {
        StudentArrayDeque<Integer> student = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> test = new ArrayDequeSolution<>();
        StringBuilder ops = new StringBuilder();
        for(int i=0; i<10000; i++) {
            int op = StdRandom.uniform(666);
            if(op == 0){
                int val = StdRandom.uniform(0,100);
                student.addFirst(val);
                test.addFirst(val);
                ops.append(String.format("addFirst(%d)\n", val));
            } else if (op == 1) {
                int val = StdRandom.uniform(0,100);
                student.addLast(val);
                test.addLast(val);
                ops.append(String.format("addLast(%d)\n", val));
            } else if (op == 2) {
                if(student.isEmpty() || test.isEmpty()) {
                    continue;
                }
                Integer expected = test.removeFirst();
                Integer actual = student.removeFirst();
                ops.append("removeFirst()\n");
                assertEquals(ops.toString(), expected, actual);
            } else if (op == 3) {
                if(student.isEmpty() || test.isEmpty()) {
                    continue;
                }
                Integer expected = test.removeLast();
                Integer actual = student.removeLast();
                ops.append("removeLast()\n");
                assertEquals(ops.toString(), expected, actual);
            }

        }
    }
}

