package shi.quan.vo;

import java.util.ArrayList;
import java.util.List;

public class PSPAvailabilities implements PSPNode {
    List<Integer> nameList = new ArrayList<>();
    List<Integer> availabilityList = new ArrayList<>();

    public List<Integer> getNameList() {
        return nameList;
    }

    public void setNameList(List<Integer> nameList) {
        this.nameList = nameList;
    }

    public List<Integer> getAvailabilityList() {
        return availabilityList;
    }

    public void setAvailabilityList(List<Integer> availabilityList) {
        this.availabilityList = availabilityList;
    }
}
