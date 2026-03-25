package com.yzj.alzassistant.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 文档解析工具类：word、md、txt、pdf、xls、xlsx 等（本地路径或字节）。
 */
public class DocumentParser {

    /**
     * 解析文档内容（本地路径）
     */
    public static String parseDocument(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }
        String extension = normalizeExtension(getFileExtension(file.getName().toLowerCase()));
        return parseByExtension(file, null, extension);
    }

    /**
     * 解析文档内容（字节 + 扩展名，不含点，如 docx、pdf）
     */
    public static String parseDocument(byte[] data, String extension) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("文件内容为空");
        }
        String ext = normalizeExtension(extension);
        if (StrUtil.isBlank(ext)) {
            throw new IllegalArgumentException("扩展名不能为空");
        }
        return parseByExtension(null, data, ext);
    }

    /**
     * 规范化扩展名：小写、去点前缀、markdown 视为 md（供 URL 后缀校验等使用）
     */
    private static String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        String e = extension.trim().toLowerCase();
        if (e.startsWith(".")) {
            e = e.substring(1);
        }
        return "markdown".equals(e) ? "md" : e;
    }

    /**
     * 规范化扩展名：小写、去点前缀、markdown 视为 md（供 URL 后缀校验等使用）
     */
    public static String normalizeExtensionKey(String extension) {
        return normalizeExtension(extension);
    }

    /**
     * 根据扩展名解析文件
     */
    private static String parseByExtension(File file, byte[] data, String extension) {
        return switch (extension) {
            case "doc" -> file != null ? parseDoc(file) : parseDocFromBytes(data);
            case "docx" -> file != null ? parseDocx(file) : parseDocxFromBytes(data);
            case "md" -> file != null ? parseMarkdown(file) : parseMdTxtFromBytes(data);
            case "txt" -> file != null ? parseTxt(file) : parseMdTxtFromBytes(data);
            case "pdf" -> file != null ? parsePdf(file) : parsePdfFromBytes(data);
            case "xls" -> file != null ? parseXls(file) : parseXlsFromBytes(data);
            case "xlsx" -> file != null ? parseXlsx(file) : parseXlsxFromBytes(data);
            default -> throw new IllegalArgumentException("不支持的文件格式: " + extension);
        };
    }

    /**
     * 解析 DOC 文件
     */
    private static String parseDoc(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            throw new RuntimeException("解析DOC文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 解析 DOC 文件（字节）
     */
    private static String parseDocFromBytes(byte[] data) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
             HWPFDocument document = new HWPFDocument(in);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            throw new RuntimeException("解析DOC失败", e);
        }
    }

    /**
     * 解析 DOCX 文件
     */
    private static String parseDocx(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            throw new RuntimeException("解析DOCX文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 解析 DOCX 文件（字节）
     */
    private static String parseDocxFromBytes(byte[] data) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
             XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            throw new RuntimeException("解析DOCX失败", e);
        }
    }

    /**
     * 解析 Markdown 文件
     */
    private static String parseMarkdown(File file) {
        return FileUtil.readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 解析 TXT 文件
     */
    private static String parseTxt(File file) {
        return FileUtil.readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 解析 Markdown 或 TXT 文件（字节）
     */
    private static String parseMdTxtFromBytes(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 解析 PDF 文件
     */
    private static String parsePdf(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("解析PDF文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 解析 PDF 文件（字节）
     */
    private static String parsePdfFromBytes(byte[] data) {
        try (PDDocument document = Loader.loadPDF(data)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("解析PDF失败", e);
        }
    }

    /**
     * 解析 XLS 文件
     */
    private static String parseXls(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return workbookToPlainText(new HSSFWorkbook(fis));
        } catch (IOException e) {
            throw new RuntimeException("解析XLS文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 解析 XLS 文件（字节）
     */
    private static String parseXlsFromBytes(byte[] data) {
        try (HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data))) {
            return workbookToPlainText(wb);
        } catch (IOException e) {
            throw new RuntimeException("解析XLS失败", e);
        }
    }

    /**
     * 解析 XLSX 文件
     */
    private static String parseXlsx(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            return workbookToPlainText(wb);
        } catch (IOException e) {
            throw new RuntimeException("解析XLSX文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 解析 XLSX 文件（字节）
     */
    private static String parseXlsxFromBytes(byte[] data) {
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data))) {
            return workbookToPlainText(wb);
        } catch (IOException e) {
            throw new RuntimeException("解析XLSX失败", e);
        }
    }

    /**
     * 将 Workbook 转换为纯文本
     */
    private static String workbookToPlainText(Workbook wb) {
        DataFormatter fmt = new DataFormatter();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            if (sheet == null) {
                continue;
            }
            sb.append("【").append(sheet.getSheetName()).append("】\n");
            for (Row row : sheet) {
                if (row == null) {
                    continue;
                }
                boolean any = false;
                for (Cell cell : row) {
                    String t = fmt.formatCellValue(cell);
                    if (StrUtil.isNotBlank(t)) {
                        if (any) {
                            sb.append('\t');
                        }
                        sb.append(t.trim());
                        any = true;
                    }
                }
                sb.append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 检查文件格式是否支持（按扩展名），仅支持 doc、docx、md、txt、pdf、xls、xlsx
     */
    public static boolean isSupportedFormat(String fileName) {
        String extension = normalizeExtension(getFileExtension(fileName.toLowerCase()));
        return StrUtil.equalsAny(extension, "doc", "docx", "md", "txt", "pdf", "xls", "xlsx");
    }

    /**
     * 是否支持的文档扩展名（小写、无点），仅支持 doc、docx、md、txt、pdf、xls、xlsx
     */
    public static boolean isSupportedExtension(String extension) {
        String ext = normalizeExtension(extension);
        return StrUtil.equalsAny(ext, "doc", "docx", "md", "txt", "pdf", "xls", "xlsx");
    }
}
