package net.akensys.reader.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScanConfig implements Cloneable {
    @SerializedName("scan_duration_mobile")
    @Expose
    private Integer scanDurationMobile;
    @SerializedName("scan_interval_mobile")
    @Expose
    private Integer scanIntervalMobile;

    public Integer getScanDurationMobile() {
        return scanDurationMobile;
    }

    public void setScanDurationMobile(Integer scanDurationMobile) {
        this.scanDurationMobile = scanDurationMobile;
    }

    public Integer getScanIntervalMobile() {
        return scanIntervalMobile;
    }

    public void setScanIntervalMobile(Integer scanIntervalMobile) {
        this.scanIntervalMobile = scanIntervalMobile;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
