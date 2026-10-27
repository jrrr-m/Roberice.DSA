package com.roberice.pos.db;

import com.roberice.pos.model.FileRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Store Documents and One-Click Database Backups.
 */
public class FileManagerDAO {

    public List<FileRecord> getAllFiles(String categoryFilter) throws SQLException {
        List<FileRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM file_records");
        boolean hasCat = categoryFilter != null && !categoryFilter.equalsIgnoreCase("all") && !categoryFilter.trim().isEmpty();
        if (hasCat) {
            sql.append(" WHERE category = ?");
        }
        sql.append(" ORDER BY uploaded_at DESC, file_id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasCat) {
                ps.setString(1, categoryFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FileRecord f = new FileRecord();
                    f.setFileId(rs.getInt("file_id"));
                    f.setFileName(rs.getString("file_name"));
                    f.setOriginalName(rs.getString("original_name"));
                    f.setFilePath(rs.getString("file_path"));
                    f.setFileSize(rs.getLong("file_size"));
                    f.setFileType(rs.getString("file_type"));
                    f.setCategory(rs.getString("category"));
                    f.setDescription(rs.getString("description"));
                    f.setUploadedBy(rs.getString("uploaded_by"));
                    f.setUploadedAt(DatabaseManager.parseDate(rs.getObject("uploaded_at")));
                    list.add(f);
                }
            }
        }
        return list;
    }

    public boolean addFileRecord(FileRecord rec) throws SQLException {
        String sql = "INSERT INTO file_records (file_name, original_name, file_path, file_size, file_type, category, description, uploaded_by, uploaded_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rec.getFileName());
            ps.setString(2, rec.getOriginalName());
            ps.setString(3, rec.getFilePath());
            ps.setLong(4, rec.getFileSize());
            ps.setString(5, rec.getFileType());
            ps.setString(6, rec.getCategory());
            ps.setString(7, rec.getDescription());
            ps.setString(8, rec.getUploadedBy());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) rec.setFileId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean deleteFileRecord(int fileId) throws SQLException {
        String sql = "DELETE FROM file_records WHERE file_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Triggers a live One-Click Database Backup and saves it as a registered archive.
     */
    public FileRecord triggerBackup(String performedBy) throws Exception {
        File backupDir = new File("backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "roberice_db_backup_" + timeStamp + ".sql";
        File backupFile = new File(backupDir, fileName);

        // Generate SQL dump of current database state
        try (PrintWriter writer = new PrintWriter(new FileWriter(backupFile));
             Connection conn = DatabaseManager.getConnection()) {

            writer.println("-- ========================================================");
            writer.println("-- ROBERICE POS & INVENTORY MANAGEMENT SYSTEM");
            writer.println("-- Automated One-Click System Backup");
            writer.println("-- Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println("-- Triggered By: " + performedBy);
            writer.println("-- Database Engine: " + (DatabaseManager.isMySql() ? "MySQL" : "SQLite"));
            writer.println("-- ========================================================\n");

            // Dump table summaries
            String[] tables = {"product", "inventory", "inventory_batch", "users", "account_requests", "sales_transaction", "transaction_item", "inventory_adjustment"};
            for (String table : tables) {
                writer.println("-- --------------------------------------------------------");
                writer.println("-- Data for table: " + table);
                writer.println("-- --------------------------------------------------------");
                try (Statement s = conn.createStatement();
                     ResultSet rs = s.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();

                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder("INSERT INTO `").append(table).append("` VALUES (");
                        for (int i = 1; i <= cols; i++) {
                            Object val = rs.getObject(i);
                            if (val == null) {
                                sb.append("NULL");
                            } else if (val instanceof Number) {
                                sb.append(val);
                            } else {
                                sb.append("'").append(val.toString().replace("'", "''")).append("'");
                            }
                            if (i < cols) sb.append(", ");
                        }
                        sb.append(");");
                        writer.println(sb.toString());
                    }
                    writer.println();
                } catch (Exception ignored) {}
            }
        }

        FileRecord rec = new FileRecord();
        rec.setFileName(fileName);
        rec.setOriginalName(fileName);
        rec.setFilePath(backupFile.getPath());
        rec.setFileSize(backupFile.length());
        rec.setFileType("application/sql");
        rec.setCategory("Database Backup");
        rec.setDescription("Automated one-click SQL database snapshot archive.");
        rec.setUploadedBy(performedBy);
        addFileRecord(rec);

        return rec;
    }
}
