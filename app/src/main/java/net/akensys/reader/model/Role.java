package net.akensys.reader.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Role implements Cloneable {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("libelle")
    @Expose
    private String libelle;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
