package shi.quan.rcpsp.vo;

public enum PrecedenceDiagrammingConstraint {
    FS("FS", 0), FF("FF",1), SS("SS",2), SF("SF", 4);

    private PrecedenceDiagrammingConstraint(String name, int code) {
        this.name = name;
        this.code = code;
    }

    private int code;
    private String name;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
