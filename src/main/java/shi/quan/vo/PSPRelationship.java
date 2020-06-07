package shi.quan.vo;

import java.util.ArrayList;
import java.util.List;

public class PSPRelationship implements PSPNode {
    private Integer jobId;
    private Integer modes;
    private Integer successorCount;
    private List<Integer> successorList = new ArrayList<>();

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getModes() {
        return modes;
    }

    public void setModes(Integer modes) {
        this.modes = modes;
    }

    public Integer getSuccessorCount() {
        return successorCount;
    }

    public void setSuccessorCount(Integer successorCount) {
        this.successorCount = successorCount;
    }

    public List<Integer> getSuccessorList() {
        return successorList;
    }

    public void setSuccessorList(List<Integer> successorList) {
        this.successorList = successorList;
    }
}
