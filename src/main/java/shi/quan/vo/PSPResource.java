package shi.quan.vo;

import java.util.ArrayList;
import java.util.List;

public class PSPResource implements PSPNode {
    private PSPResources parent;
    private Integer jobId;
    private Integer mode;
    private Integer duration;
    private List<Integer> resourceList = new ArrayList<>();

    public PSPResources getParent() {
        return parent;
    }

    public void setParent(PSPResources parent) {
        this.parent = parent;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<Integer> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Integer> resourceList) {
        this.resourceList = resourceList;
    }
}
