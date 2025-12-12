package com.yzj.alzassistant.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 文档解析工具类
 * 支持解析 word、md、txt、pdf 等格式的文档
 */
public class DocumentParser {

    /**
     * 解析文档内容
     *
     * @param filePath 文件路径
     * @return 文档文本内容
     */
    public static String parseDocument(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        String fileName = file.getName().toLowerCase();
        String extension = getFileExtension(fileName);

        return switch (extension) {
            case "doc" -> parseDoc(file);
            case "docx" -> parseDocx(file);
            case "md", "markdown" -> parseMarkdown(file);
            case "txt" -> parseTxt(file);
            case "pdf" -> parsePdf(file);
            default -> throw new IllegalArgumentException("不支持的文件格式: " + extension);
        };
    }

    /**
     * 解析 .doc 文件
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
     * 解析 .docx 文件
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
     * 解析 Markdown 文件
     */
    private static String parseMarkdown(File file) {
        return FileUtil.readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 解析文本文件
     */
    private static String parseTxt(File file) {
        return FileUtil.readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 解析 PDF 文件
     */
    private static String parsePdf(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("解析PDF文件失败: " + file.getAbsolutePath(), e);
        }
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
     * 检查文件格式是否支持
     */
    public static boolean isSupportedFormat(String fileName) {
        String extension = getFileExtension(fileName.toLowerCase());
        return StrUtil.equalsAny(extension, "doc", "docx", "md", "markdown", "txt", "pdf");
    }
}


