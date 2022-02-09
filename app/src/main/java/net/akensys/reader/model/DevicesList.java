package net.akensys.reader.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class DevicesList {

    @SerializedName("client_uuid")
    @Expose
    private String clientUuid;
    @SerializedName("network_id")
    @Expose
    private String networkId;
    @SerializedName("last_update_list")
    @Expose
    private String lastUpdateList;
    @SerializedName("devices_list")
    @Expose
    private List<Device> devices = null;

    public String getClientUuid() {
        return clientUuid;
    }

    public void setClientUuid(String clientUuid) {
        this.clientUuid = clientUuid;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getLastUpdateList() {
        return lastUpdateList;
    }

    public void setLastUpdateList(String lastUpdateList) {
        this.lastUpdateList = lastUpdateList;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        Gson gson = new Gson();
        this.devices = gson.fromJson(gson.toJson(devices), new TypeToken<List<Device>>(){}.getType());;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
