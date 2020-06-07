package shi.quan.vo;

import java.util.ArrayList;
import java.util.List;

public class PSPResources implements PSPNode {
    List<Integer> nameList = new ArrayList<>();
    List<PSPResource> resourceList = new ArrayList<>();

    public List<Integer> getNameList() {
        return nameList;
    }

    public void setNameList(List<Integer> nameList) {
        this.nameList = nameList;
    }

    public List<PSPResource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<PSPResource> resourceList) {
        this.resourceList = resourceList;
    }
}
