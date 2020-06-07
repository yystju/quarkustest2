package shi.quan.vo;

public class PSPGeneralInformation implements PSPNode {
    private Integer projectNumber;
    private Integer jobCount;
    private Integer relDate;
    private Integer dueDate;
    private Integer tardCost;
    private Integer mpmTime;

    public Integer getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(Integer projectNumber) {
        this.projectNumber = projectNumber;
    }

    public Integer getJobCount() {
        return jobCount;
    }

    public void setJobCount(Integer jobCount) {
        this.jobCount = jobCount;
    }

    public Integer getRelDate() {
        return relDate;
    }

    public void setRelDate(Integer relDate) {
        this.relDate = relDate;
    }

    public Integer getDueDate() {
        return dueDate;
    }

    public void setDueDate(Integer dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getTardCost() {
        return tardCost;
    }

    public void setTardCost(Integer tardCost) {
        this.tardCost = tardCost;
    }

    public Integer getMpmTime() {
        return mpmTime;
    }

    public void setMpmTime(Integer mpmTime) {
        this.mpmTime = mpmTime;
    }
}
