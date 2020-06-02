package shi.quan.common.vo;

public class Trio<One, Two, Three> {
    private One one;
    private Two two;
    private Three three;

    public static <One, Two, Three> Trio<One, Two, Three> trio(One one, Two two, Three three) {
        return new Trio<One, Two, Three>(one, two, three);
    }

    public Trio() {}

    public Trio(One one, Two two, Three three) {
        this.one = one;
        this.two = two;
        this.three = three;
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
