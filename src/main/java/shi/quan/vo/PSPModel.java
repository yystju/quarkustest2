package shi.quan.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSPModel implements PSPNode {
    private Map<String, String> propMap= new HashMap<>();

    private Integer renewableResourceCount;
    private Integer nonRenewableResourceCount;
    private Integer doublyConstrainedResourceCount;

    private List<PSPGeneralInformation> generalInformationList = new ArrayList<>();
    private List<PSPRelationship> relationshipList = new ArrayList<>();

    private PSPResources resources = new PSPResources();
    private PSPAvailabilities availabilities = new PSPAvailabilities();

    public Map<String, String> getPropMap() {
        return propMap;
    }

    public void setPropMap(Map<String, String> propMap) {
        this.propMap = propMap;
    }

    public Integer getRenewableResourceCount() {
        return renewableResourceCount;
    }

    public void setRenewableResourceCount(Integer renewableResourceCount) {
        this.renewableResourceCount = renewableResourceCount;
    }

    public Integer getNonRenewableResourceCount() {
        return nonRenewableResourceCount;
    }

    public void setNonRenewableResourceCount(Integer nonRenewableResourceCount) {
        this.nonRenewableResourceCount = nonRenewableResourceCount;
    }

    public Integer getDoublyConstrainedResourceCount() {
        return doublyConstrainedResourceCount;
    }

    public void setDoublyConstrainedResourceCount(Integer doublyConstrainedResourceCount) {
        this.doublyConstrainedResourceCount = doublyConstrainedResourceCount;
    }

    public List<PSPGeneralInformation> getGeneralInformationList() {
        return generalInformationList;
    }

    public void setGeneralInformationList(List<PSPGeneralInformation> generalInformationList) {
        this.generalInformationList = generalInformationList;
    }

    public List<PSPRelationship> getRelationshipList() {
        return relationshipList;
    }

    public void setRelationshipList(List<PSPRelationship> relationshipList) {
        this.relationshipList = relationshipList;
    }

    public PSPResources getResources() {
        return resources;
    }

    public void setResources(PSPResources resources) {
        this.resources = resources;
    }

    public PSPAvailabilities getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(PSPAvailabilities availabilities) {
        this.availabilities = availabilities;
    }
}
