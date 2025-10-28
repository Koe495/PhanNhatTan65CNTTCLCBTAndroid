package com.example.nhtuapp;

public class HoatDong {
    private String tieuDe;
    private String thoiGian;
    private int anhDaiDien; // Dùng int để lưu ID từ drawable

    public HoatDong(String tieuDe, String thoiGian, int anhDaiDien) {
        this.tieuDe = tieuDe;
        this.thoiGian = thoiGian;
        this.anhDaiDien = anhDaiDien;
    }

    // Tạo các hàm Getters
    public String getTieuDe() {
        return tieuDe;
    }
    public String getThoiGian() {
        return thoiGian;
    }
    public int getAnhDaiDien() {
        return anhDaiDien;
    }
}