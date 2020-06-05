package shi.quan.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSPData {
    private Map<String, String> properties = new HashMap<>();

    private Map<String, String> resources = new HashMap<>();

    private String projectInformationHeader = "";
    private List<String> projectInformationRows = new ArrayList<>();

    private String precedenceRelationsHeader = "";
    private List<String> pprecedenceRelationsRows = new ArrayList<>();

    private String reqeustsDurationHeader = "";
    private List<String> reqeustsDurationRows = new ArrayList<>();

    private String resourceAvailabilitiesHeader = "";
    private List<String> resourceAvailabilitiesRows = new ArrayList<>();


    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public String getProjectInformationHeader() {
        return projectInformationHeader;
    }

    public void setProjectInformationHeader(String projectInformationHeader) {
        this.projectInformationHeader = projectInformationHeader;
    }

    public List<String> getProjectInformationRows() {
        return projectInformationRows;
    }

    public void setProjectInformationRows(List<String> projectInformationRows) {
        this.projectInformationRows = projectInformationRows;
    }

    public String getPrecedenceRelationsHeader() {
        return precedenceRelationsHeader;
    }

    public void setPrecedenceRelationsHeader(String precedenceRelationsHeader) {
        this.precedenceRelationsHeader = precedenceRelationsHeader;
    }

    public List<String> getPprecedenceRelationsRows() {
        return pprecedenceRelationsRows;
    }

    public void setPprecedenceRelationsRows(List<String> pprecedenceRelationsRows) {
        this.pprecedenceRelationsRows = pprecedenceRelationsRows;
    }

    public String getReqeustsDurationHeader() {
        return reqeustsDurationHeader;
    }

    public void setReqeustsDurationHeader(String reqeustsDurationHeader) {
        this.reqeustsDurationHeader = reqeustsDurationHeader;
    }

    public List<String> getReqeustsDurationRows() {
        return reqeustsDurationRows;
    }

    public void setReqeustsDurationRows(List<String> reqeustsDurationRows) {
        this.reqeustsDurationRows = reqeustsDurationRows;
    }

    public String getResourceAvailabilitiesHeader() {
        return resourceAvailabilitiesHeader;
    }

    public void setResourceAvailabilitiesHeader(String resourceAvailabilitiesHeader) {
        this.resourceAvailabilitiesHeader = resourceAvailabilitiesHeader;
    }

    public List<String> getResourceAvailabilitiesRows() {
        return resourceAvailabilitiesRows;
    }

    public void setResourceAvailabilitiesRows(List<String> resourceAvailabilitiesRows) {
        this.resourceAvailabilitiesRows = resourceAvailabilitiesRows;
    }
}
