package com.gold.hamrahvpn.model;

/*/===========================================================
  by MehrabSp --> github.com/MehraB832 && github.com/MehrabSp
  T.me/MehrabSp
//===========================================================*/
public class OpenVpnServerList {
    // {"id":0, "file":0, "city":"Essen","country":"Germany","image":"germany","ip":"51.68.191.75","active":"true","signal":"a"},
    public String ID;
    public String FileID;
    public String City;
    public String Country;
    public String Image;
    public String IP;
    public String Active;
    public String Signal;
    private String ovpn; // url assets
    private String ovpnUserName;
    private String ovpnUserPassword;

//    public OpenVpnServerList() {
//    }
//
//    public OpenVpnServerList(String flagUrl, String ovpn) {
//        this.ovpn = ovpn;
//    }
//
//    public OpenVpnServerList(String ovpn, String ovpnUserName, String ovpnUserPassword) {
//        this.ovpn = ovpn;
//        this.ovpnUserName = ovpnUserName;
//        this.ovpnUserPassword = ovpnUserPassword;
//    }

    public String GetOVPN() {
        return ovpn;
    }

    public void SetOVPN(String ovpn) {
        this.ovpn = ovpn;
    }

    public String GetOVPNUserName() {
        return ovpnUserName;
    }

    public void SetOVPNUserName(String ovpnUserName) {
        this.ovpnUserName = ovpnUserName;
    }

    public String GetOVPNUserPassword() {
        return ovpnUserPassword;
    }

    public void SetOVPNUserPassword(String ovpnUserPassword) {
        this.ovpnUserPassword = ovpnUserPassword;
    }

    public String GetID() {
        return ID;
    }

    public void SetID(String ID) {
        this.ID = ID;
    }

    public String GetFileID() {
        return FileID;
    }

    public void SetFileID(String FileID) {
        this.FileID = FileID;
    }

    public String GetCity() {
        return City;
    }

    public void SetCity(String City) {
        this.City = City;
    }

    public String GetCountry() {
        return Country;
    }

    public void SetCountry(String Country) {
        this.Country = Country;
    }

    public String GetImage() {
        return Image;
    }

    public void SetImage(String Image) {
        this.Image = Image;
    }

    public String GetIP() {
        return IP;
    }

    public void SetIP(String IP) {
        this.IP = IP;
    }

    public String GetActive() {
        return Active;
    }

    public void SetActive(String Active) {
        this.Active = Active;
    }

    public String GetSignal() {
        return Signal;
    }

    public void SetSignal(String Signal) {
        this.Signal = Signal;
    }
}
/*/===========================================================
  by MehrabSp --> github.com/MehraB832 && github.com/MehrabSp
  T.me/MehrabSp
//===========================================================*/