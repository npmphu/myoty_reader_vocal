package net.akensys.reader.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Network  implements Cloneable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("libelle")
    @Expose
    private String libelle;
    @SerializedName("logo")
    @Expose
    private String logo;
    @SerializedName("couleur_primary")
    @Expose
    private String couleurPrimary;
    @SerializedName("couleur_secondary")
    @Expose
    private String couleurSecondary;
    @SerializedName("couleur_secondary2")
    @Expose
    private String couleurSecondary2;
    @SerializedName("datalogger")
    @Expose
    private Integer datalogger;
    @SerializedName("getdevicelist")
    @Expose
    private Integer getdevicelist;
    @SerializedName("site_ext")
    @Expose
    private Integer siteExt;
    @SerializedName("manage_tour")
    @Expose
    private Integer manageTour;
    @SerializedName("tagname_filter")
    @Expose
    private String tagNameFilter;
    @SerializedName("tag_timeout")
    @Expose
    private Integer tagTimeout;
    @SerializedName("tag_dead")
    @Expose
    private Integer tagDead;
    @SerializedName("color_marker_active")
    @Expose
    private String colorMarkerActive;
    @SerializedName("color_marker_dead")
    @Expose
    private String colorMarkerDead;
    @SerializedName("color_marker_timeout")
    @Expose
    private String colorMarkerTimeout;
    @SerializedName("color_marker_selected")
    @Expose
    private String colorMarkerSelected;
    @SerializedName("dashboard_type")
    @Expose
    private Integer dashboardType;
    @SerializedName("mag_alert")
    @Expose
    private Integer MAGAlert;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCouleurPrimary() {
        return couleurPrimary;
    }

    public void setCouleurPrimary(String couleurPrimary) {
        this.couleurPrimary = couleurPrimary;
    }

    public String getCouleurSecondary() {
        return couleurSecondary;
    }

    public void setCouleurSecondary(String couleurSecondary) {
        this.couleurSecondary = couleurSecondary;
    }

    public String getCouleurSecondary2() {
        return couleurSecondary2;
    }

    public void setCouleurSecondary2(String couleurSecondary2) {
        this.couleurSecondary2 = couleurSecondary2;
    }

    public Integer getDatalogger() {
        return datalogger;
    }

    public void setDatalogger(Integer datalogger) {
        this.datalogger = datalogger;
    }

    public Integer getGetdevicelist() {
        return getdevicelist;
    }

    public void setGetdevicelist(Integer getdevicelist) {
        this.getdevicelist = getdevicelist;
    }

    public Integer getSiteExt() {
        return siteExt;
    }

    public void setSiteExt(Integer siteExt) {
        this.siteExt = siteExt;
    }

    public Integer getManageTour() {
        return manageTour;
    }

    public void setManageTour(Integer manageTour) {
        this.manageTour = manageTour;
    }
    public String getTagNameFilter() {
        return tagNameFilter;
    }

    public void setTagNameFilter(String tagNameFilter) {
        this.tagNameFilter = tagNameFilter;
    }


    public Integer getTagTimeout() {
        return tagTimeout;
    }

    public void setTagTimeout(Integer tagTimeout) {
        this.tagTimeout = tagTimeout;
    }

    public Integer getTagDead() {
        return tagDead;
    }

    public void setTagDead(Integer tagDead) {
        this.tagDead = tagDead;
    }

    public String getColorMarkerActive() {
        return colorMarkerActive;
    }

    public void setColorMarkerActive(String colorMarkerActive) {
        this.colorMarkerActive = colorMarkerActive;
    }

    public String getColorMarkerDead() {
        return colorMarkerDead;
    }

    public void setColorMarkerDead(String colorMarkerDead) {
        this.colorMarkerDead = colorMarkerDead;
    }

    public String getColorMarkerTimeout() {
        return colorMarkerTimeout;
    }

    public void setColorMarkerTimeout(String colorMarkerTimeout) {
        this.colorMarkerTimeout = colorMarkerTimeout;
    }

    public String getColorMarkerSelected() {
        return colorMarkerSelected;
    }

    public void setColorMarkerSelected(String colorMarkerSelected) {
        this.colorMarkerSelected = colorMarkerSelected;
    }

    public Integer getDashboardType() {
        return dashboardType;
    }

    public void setDashboardType(Integer dashboardType) {
        this.dashboardType = dashboardType;
    }

    public Integer getMAGAlert() {
        return MAGAlert;
    }

    public void setMAGAlert(Integer MAGAlert) {
        this.MAGAlert = MAGAlert;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}