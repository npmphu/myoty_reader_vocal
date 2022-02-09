package net.akensys.reader.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Client implements Cloneable {
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("libelle")
    @Expose
    private String libelle;
    @SerializedName("adresse")
    @Expose
    private String adresse;
    @SerializedName("adresse_complementaire")
    @Expose
    private String adresseComplementaire;
    @SerializedName("code_postal")
    @Expose
    private String codePostal;
    @SerializedName("ville")
    @Expose
    private String ville;
    @SerializedName("telephone")
    @Expose
    private String telephone;
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
    @SerializedName("login_logo")
    @Expose
    private String loginLogo;
    @SerializedName("login_background")
    @Expose
    private String loginBackground;
    @SerializedName("login_title")
    @Expose
    private String loginTitle;
    @SerializedName("manage_network")
    @Expose
    private Integer manageNetwork;
    @SerializedName("expiration_date")
    @Expose
    private String expirationDate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getAdresseComplementaire() {
        return adresseComplementaire;
    }

    public void setAdresseComplementaire(String adresseComplementaire) {
        this.adresseComplementaire = adresseComplementaire;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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

    public String getLoginLogo() {
        return loginLogo;
    }

    public void setLoginLogo(String loginLogo) {
        this.loginLogo = loginLogo;
    }

    public String getLoginBackground() {
        return loginBackground;
    }

    public void setLoginBackground(String loginBackground) {
        this.loginBackground = loginBackground;
    }

    public String getLoginTitle() {
        return loginTitle;
    }

    public void setLoginTitle(String loginTitle) {
        this.loginTitle = loginTitle;
    }

    public Integer getManageNetwork() {
        return manageNetwork;
    }

    public void setManageNetwork(Integer manageNetwork) {
        this.manageNetwork = manageNetwork;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

