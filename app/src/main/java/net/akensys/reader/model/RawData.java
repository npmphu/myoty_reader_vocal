package net.akensys.reader.model;

import android.bluetooth.le.ScanResult;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.akensys.reader.util.ElaTag;
import net.akensys.reader.util.Utils;

public class RawData implements Cloneable {
    @SerializedName("tag_id")
    @Expose
    private String tagId;
    @SerializedName("tag_name")
    @Expose
    private String tagName;
    @SerializedName("raw_data")
    @Expose
    private String rawData;

    public RawData(ScanResult scanResult) {
        tagId = ElaTag.getTagId(scanResult);
        tagName = scanResult.getDevice().getName();
        rawData = Utils.toHexadecimalString(scanResult.getScanRecord().getBytes());
    }

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

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
