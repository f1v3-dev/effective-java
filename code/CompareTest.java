package code;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.TreeSet;

public class CompareTest {

    public static void main(String[] args) {
        BigDecimal val1 = new BigDecimal("1.0");
        BigDecimal val2 = new BigDecimal("1.00");

        HashSet<BigDecimal> hashSet = new HashSet<>();
        hashSet.add(val1);
        hashSet.add(val2);

        TreeSet<BigDecimal> treeSet = new TreeSet<>();
        treeSet.add(val1);
        treeSet.add(val2);

        System.out.println("Hash Set Size : " + hashSet.size());
        System.out.println("Tree Set Size : " + treeSet.size());

        BigDecimal val3 = new BigDecimal("2.0");

        treeSet.add(val3);

        treeSet.forEach(System.out::println);
    }
}
