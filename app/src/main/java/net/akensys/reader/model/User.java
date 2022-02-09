package net.akensys.reader.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class User implements Cloneable {
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("login")
    @Expose
    private String login;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("nom")
    @Expose
    private String nom;
    @SerializedName("prenom")
    @Expose
    private String prenom;
    @SerializedName("client")
    @Expose
    private Client client;
    @SerializedName("networks")
    @Expose
    private List<Network> networks = null;
    @SerializedName("expiration_date")
    @Expose
    private String expirationDate;
    @SerializedName("reader_type")
    @Expose
    private Integer readerType;
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
    @SerializedName("role")
    @Expose
    private Role role;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Network> networks) {
        Gson gson = new Gson();
        this.networks = gson.fromJson(gson.toJson(networks), new TypeToken<List<Network>>(){}.getType());
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getReaderType() {
        return readerType;
    }

    public void setReaderType(Integer readerType) {
        this.readerType = readerType;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
