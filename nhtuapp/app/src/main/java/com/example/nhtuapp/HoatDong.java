package com.example.nhtuapp;

public class HoatDong {
    private String tieuDe;
    private String thoiGian;
    private int anhDaiDien;
    private String noiDung; // <-- THÊM DÒNG NÀY

    // Cập nhật Constructor
    public HoatDong(String tieuDe, String thoiGian, int anhDaiDien, String noiDung) {
        this.tieuDe = tieuDe;
        this.thoiGian = thoiGian;
        this.anhDaiDien = anhDaiDien;
        this.noiDung = noiDung; // <-- THÊM DÒNG NÀY
    }

    // Tạo Getter cho noiDung
    public String getNoiDung() {
        return noiDung;
    }

    // Các getter cũ
    public String getTieuDe() { return tieuDe; }
    public String getThoiGian() { return thoiGian; }
    public int getAnhDaiDien() { return anhDaiDien; }
}