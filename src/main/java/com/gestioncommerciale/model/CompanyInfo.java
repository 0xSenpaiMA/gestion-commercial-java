package com.gestioncommerciale.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Company information entity
 */
@Entity
@Table(name = "company_info")
public class CompanyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;
    
    @Column
    private String adresse;
    
    @Column
    private String ville;
    
    @Column
    private String pays;
    
    @Column
    private String telephone;
    
    @Column
    private String fax;
    
    @Column
    private String email;
    
    @Column
    private String cnss;
    
    @Column
    private String rc;
    
    @Column(name = "if_number")
    private String ifNumber;
    
    @Column
    private String ice;
    
    @Column
    private String logo;
    
    @Column(name = "code_postal")
    private String codePostal;
    
    @Column(name = "site_web")
    private String siteWeb;
    
    @Column(name = "forme_juridique")
    private String formeJuridique;
    
    @Column
    private String capital;
    
    // Constructors
    public CompanyInfo() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRaisonSociale() { return raisonSociale; }
    public void setRaisonSociale(String raisonSociale) { this.raisonSociale = raisonSociale; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getCnss() { return cnss; }
    public void setCnss(String cnss) { this.cnss = cnss; }
    
    public String getRc() { return rc; }
    public void setRc(String rc) { this.rc = rc; }
    
    public String getIfNumber() { return ifNumber; }
    public void setIfNumber(String ifNumber) { this.ifNumber = ifNumber; }
    
    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }
    
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    
    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }
    
    public String getFormeJuridique() { return formeJuridique; }
    public void setFormeJuridique(String formeJuridique) { this.formeJuridique = formeJuridique; }
    
    public String getCapital() { return capital; }
    public void setCapital(String capital) { this.capital = capital; }
}
