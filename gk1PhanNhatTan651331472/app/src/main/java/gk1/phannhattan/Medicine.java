package gk1.phannhattan;

public class Medicine {
    private String tieuDe;
    private String thoiGian;
    private int anhDaiDien;
    private String noiDung;

    // constructor
    public Medicine(String tieuDe, String thoiGian, int anhDaiDien, String noiDung) {
        this.tieuDe = tieuDe;
        this.thoiGian = thoiGian;
        this.anhDaiDien = anhDaiDien;
        this.noiDung = noiDung;
    }

    public String getNoiDung() {
        return noiDung;
    }
    public String getTieuDe() { return tieuDe; }
    public String getThoiGian() { return thoiGian; }
    public int getAnhDaiDien() { return anhDaiDien; }
}