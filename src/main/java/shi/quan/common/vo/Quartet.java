package shi.quan.common.vo;

public class Quartet<One, Two, Three, Four> {
    private One one;
    private Two two;
    private Three three;
    private Four four;

    public static <One, Two, Three, Four> Quartet<One, Two, Three, Four> quartet(One one, Two two, Three three, Four four) {
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
