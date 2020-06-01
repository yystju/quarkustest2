package shi.quan.common;

import java.util.Comparator;

// If we could do "typedef QType Quartet<One extends Comparable<One> \\
//        , Two extends Comparable<Two> \\
//        , Three extends Comparable<Three> \\
//        , Four extends Comparable<Four>>;", that will significantly shorten the declaration...
public class Quartet<One extends Comparable<One>
        , Two extends Comparable<Two>
        , Three extends Comparable<Three>
        , Four extends Comparable<Four>> implements Comparable<Quartet<One, Two, Three, Four>> {
    private One one;
    private Two two;
    private Three three;
    private Four four;

    public static <One extends Comparable<One>
            , Two extends Comparable<Two>
            , Three  extends Comparable<Three>
            , Four  extends Comparable<Four>> Quartet<One, Two, Three, Four> quartet(One one, Two two, Three three, Four four) {
        return new Quartet<One, Two, Three, Four>(one, two, three, four);
    }

    public Quartet() {}

    public Quartet(One one, Two two, Three three, Four four) {
        this.one = one;
        this.two = two;
        this.three = three;
        this.four = four;
    }

    @Override
    public int compareTo(Quartet<One, Two, Three, Four> o) {
        return Comparator
                .comparing(Quartet<One, Two, Three, Four>::getOne)
                .thenComparing(Quartet<One, Two, Three, Four>::getTwo)
                .thenComparing(Quartet<One, Two, Three, Four>::getThree)
                .thenComparing(Quartet<One, Two, Three, Four>::getFour)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "(" + one + ", " + two + ", " + three + ", " + four + ')';
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

    public Four getFour() {
        return four;
    }

    public void setFour(Four four) {
        this.four = four;
    }
}
