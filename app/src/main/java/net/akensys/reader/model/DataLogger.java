package net.akensys.reader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataLogger {

    @SerializedName("tag_id")
    @Expose
    private String tagId;
    @SerializedName("tag_name")
    @Expose
    private String tagName;
    @SerializedName("categ_id")
    @Expose
    private Integer categId;
    @SerializedName("log_rst_datetime")
    @Expose
    private String logRstDatetime;
    @SerializedName("log_action")
    @Expose
    private Integer logAction;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Integer getCategId() {
        return categId;
    }

    public void setCategId(Integer categId) {
        this.categId = categId;
    }

    public String getLogRstDatetime() {
        return logRstDatetime;
    }

    public void setLogRstDatetime(String logRstDatetime) {
        this.logRstDatetime = logRstDatetime;
    }

    public Integer getLogAction() {
        return logAction;
    }

    public void setLogAction(Integer logAction) {
        this.logAction = logAction;
    }

}
