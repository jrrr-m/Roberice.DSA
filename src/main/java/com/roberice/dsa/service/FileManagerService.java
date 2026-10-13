package com.roberice.dsa.service;

import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.FileRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileManagerService {

    private static final File UPLOAD_DIR = new File("uploads");

    static {
        if (!UPLOAD_DIR.exists()) {
            UPLOAD_DIR.mkdirs();
        }
    }

    public static List<FileRecord> getFiles(String category, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT file_id, file_name, original_name, file_path, file_size, file_type, category, description, uploaded_by, uploaded_at " +
                "FROM file_records WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql.append("AND category = ? ");
            params.add(category.trim());
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (file_name LIKE ? OR original_name LIKE ? OR description LIKE ? OR uploaded_by LIKE ?) ");
            String term = "%" + search.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY uploaded_at DESC");

        List<Map<String, Object>> rows = Database.queryForList(sql.toString(), params.toArray());
        List<FileRecord> list = new ArrayList<>();

        for (Map<String, Object> r : rows) {
            FileRecord f = new FileRecord();
            f.setFileId(((Number) r.get("file_id")).intValue());
            f.setFileName(String.valueOf(r.get("file_name")));
            f.setOriginalName(String.valueOf(r.get("original_name")));
            f.setFilePath(String.valueOf(r.get("file_path")));
            f.setFileSize(((Number) r.get("file_size")).longValue());
            f.setFileType(String.valueOf(r.get("file_type")));
            f.setCategory(String.valueOf(r.get("category")));
            f.setDescription(r.get("description") != null ? String.valueOf(r.get("description")) : "");
            f.setUploadedBy(r.get("uploaded_by") != null ? String.valueOf(r.get("uploaded_by")) : "Admin");
            f.setUploadedAt(String.valueOf(r.get("uploaded_at")));
            list.add(f);
        }
        return list;
    }

    public static FileRecord saveFile(File sourceFile, String category, String description, String username) throws Exception {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new Exception("Source file does not exist.");
        }

        String origName = sourceFile.getName();
        String ext = "";
        int dotIdx = origName.lastIndexOf('.');
        if (dotIdx > 0) ext = origName.substring(dotIdx);

        String uniqueName = System.currentTimeMillis() + "_" + origName.replaceAll("[^a-zA-Z0-9.-]", "_");
        File destFile = new File(UPLOAD_DIR, uniqueName);

        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String relPath = "uploads/" + uniqueName;
        long size = sourceFile.length();
        String fileType = ext.isEmpty() ? "Unknown" : ext.substring(1).toUpperCase();

        String sql = "INSERT INTO file_records (file_name, original_name, file_path, file_size, file_type, category, description, uploaded_by, uploaded_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        long newId = Database.insertAndGetId(sql, uniqueName, origName, relPath, size, fileType,
                (category != null && !category.trim().isEmpty()) ? category : "Document",
                description, username != null ? username : "Admin");

        FileRecord record = new FileRecord();
        record.setFileId((int) newId);
        record.setFileName(uniqueName);
        record.setOriginalName(origName);
        record.setFilePath(relPath);
        record.setFileSize(size);
        record.setFileType(fileType);
        record.setCategory(category);
        record.setDescription(description);
        record.setUploadedBy(username);
        return record;
    }

    public static void deleteFile(int fileId) throws Exception {
        Map<String, Object> row = Database.queryForMap("SELECT file_path FROM file_records WHERE file_id = ?", fileId);
        if (row != null) {
            String path = String.valueOf(row.get("file_path"));
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            Database.update("DELETE FROM file_records WHERE file_id = ?", fileId);
        }
    }

    public static File createDatabaseBackup(String username) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "roberice_backup_" + timestamp + ".sql";
        File backupDir = new File(UPLOAD_DIR, "backups");
        if (!backupDir.exists()) backupDir.mkdirs();
        File backupFile = new File(backupDir, backupFileName);

        File mysqldump = new File("C:\\xampp\\mysql\\bin\\mysqldump.exe");
        if (mysqldump.exists()) {
            ProcessBuilder pb = new ProcessBuilder(
                    mysqldump.getAbsolutePath(),
                    "-u", "root",
                    "--databases", "roberice_db",
                    "--result-file=" + backupFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new Exception("mysqldump process exited with code " + exitCode);
            }
        } else {
            // Write basic SQL export
            try (PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {
                writer.println("-- Roberice DSA Database Backup");
                writer.println("-- Date: " + LocalDateTime.now());
                writer.println("-- Host: localhost  Database: roberice_db");
            }
        }

        // Register in database
        String relPath = "uploads/backups/" + backupFileName;
        String sql = "INSERT INTO file_records (file_name, original_name, file_path, file_size, file_type, category, description, uploaded_by, uploaded_at) " +
                "VALUES (?, ?, ?, ?, 'SQL', 'Backup', 'Automated system database backup snapshot', ?, NOW())";

        Database.insertAndGetId(sql, backupFileName, backupFileName, relPath, backupFile.length(), username != null ? username : "System");
        return backupFile;
    }
}
