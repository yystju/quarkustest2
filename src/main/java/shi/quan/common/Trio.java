package shi.quan.common;

import java.util.Comparator;

public class Trio<One extends Comparable<One>, Two extends Comparable<Two>, Three  extends Comparable<Three>> implements Comparable<Trio<One, Two, Three>> {
    private One one;
    private Two two;
    private Three three;

    public static <One extends Comparable<One>, Two extends Comparable<Two>, Three  extends Comparable<Three>> Trio<One, Two, Three> trio(One one, Two two, Three three) {
        return new Trio<One, Two, Three>(one, two, three);
    }

    public Trio() {}

    public Trio(One one, Two two, Three three) {
        this.one = one;
        this.two = two;
        this.three = three;
    }

    @Override
    public int compareTo(Trio<One, Two, Three> o) {
        return Comparator
                .comparing(Trio<One, Two, Three>::getOne)
                .thenComparing(Trio<One, Two, Three>::getTwo)
                .thenComparing(Trio<One, Two, Three>::getThree)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "(" + one + ", " + two + ", " + three + ')';
    }

    public One getOne() {
        return one;
    }

    public void setOne(One one) {
        this.one = one;
    }

    public Two getTwo() {
        return two;
    }

    public void setTwo(Two two) {
        this.two = two;
    }

    public Three getThree() {
        return three;
    }

    public void setThree(Three three) {
        this.three = three;
    }
}
