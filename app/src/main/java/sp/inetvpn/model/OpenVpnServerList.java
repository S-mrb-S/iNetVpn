package sp.inetvpn.model;

/*/===========================================================
  by MehrabSp
//===========================================================*/
public class OpenVpnServerList {
    // {"id":0, "file":"ovpn file", "country":"Germany", "image":"germany"},
    public String ID;
    public String FileContent;
    public String Country;
    public String Image;

    public String GetID() {
        return ID;
    }

    public void SetID(String ID) {
        this.ID = ID;
    }

    public String GetFileContent() {
        return FileContent;
    }

    public void SetFileContent(String FileContent) {
        this.FileContent = FileContent;
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
}
/*/===========================================================
  by MehrabSp
//===========================================================*/