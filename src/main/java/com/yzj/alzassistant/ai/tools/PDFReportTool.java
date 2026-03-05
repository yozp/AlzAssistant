package com.yzj.alzassistant.ai.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.yzj.alzassistant.config.CosClientConfig;
import com.yzj.alzassistant.constant.FileConstant;
import com.yzj.alzassistant.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 医疗报告 PDF 生成工具。
 * 使用 iText7 生成规范精美的阿尔茨海默症初步评估报告，并上传到 COS。
 */
@Component
@Slf4j
public class PDFReportTool {

    // 主色调蓝色
    private static final DeviceRgb PRIMARY_BLUE = new DeviceRgb(26, 115, 232);
    // 浅蓝色背景
    private static final DeviceRgb LIGHT_BLUE_BG = new DeviceRgb(232, 240, 254);
    // 深色文本
    private static final DeviceRgb DARK_TEXT = new DeviceRgb(32, 33, 36);
    // 分区背景
    private static final DeviceRgb SECTION_BG = new DeviceRgb(26, 115, 232);
    // 表格表头背景
    private static final DeviceRgb TABLE_HEADER_BG = new DeviceRgb(66, 133, 244);
    // 表格交替行背景
    private static final DeviceRgb TABLE_ALT_ROW = new DeviceRgb(245, 248, 255);
    // 警告背景
    private static final DeviceRgb WARNING_BG = new DeviceRgb(255, 243, 224);
    // 边框颜色
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(218, 220, 224);

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Tool("生成阿尔茨海默症初步评估的 PDF 医疗报告。将症状分析、医院信息和建议整合成一份精美的报告文档，上传到云存储并返回下载链接。")
    public String generateMedicalReport(
            @P("患者描述的症状原文") String symptoms,
            @P("AI 对症状的详细分析和评估结论，包括风险等级") String analysis,
            @P("附近推荐医院信息，包含医院名称、地址、电话等") String hospitals,
            @P("专业的就医建议和生活方式建议") String recommendations) {

        log.info("开始生成医疗报告 PDF...");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "medical_report_" + timestamp + ".pdf";
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;

        try {
            FileUtil.mkdir(fileDir);

            PdfFont chineseFont = createChineseFont();
            PdfFont chineseBoldFont = createChineseBoldFont();

            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, PageSize.A4)) {

                document.setMargins(50, 45, 60, 45);
                document.setFont(chineseFont);
                document.setFontSize(11);
                document.setFontColor(DARK_TEXT);

                addTitleSection(document, chineseBoldFont, chineseFont);
                addSeparator(document);
                addSymptomsSection(document, chineseBoldFont, chineseFont, symptoms);
                addAnalysisSection(document, chineseBoldFont, chineseFont, analysis);
                addHospitalSection(document, chineseBoldFont, chineseFont, hospitals);
                addRecommendationsSection(document, chineseBoldFont, chineseFont, recommendations);
                addDisclaimerSection(document, chineseFont);
                addFooter(document, chineseFont);
            }

            String cosKey = "/report/pdf/" + fileName;
            File pdfFile = new File(filePath);
            cosManager.putObject(cosKey, pdfFile);
            String downloadUrl = cosClientConfig.getHost() + cosKey;

            log.info("医疗报告 PDF 生成并上传成功: {}", downloadUrl);
            return "PDF 医疗报告已生成成功！\n下载链接: " + downloadUrl;

        } catch (Exception e) {
            log.error("生成医疗报告 PDF 失败", e);
            return "PDF 报告生成失败: " + e.getMessage() + "。本地路径: " + filePath;
        }
    }

    /**
     * 创建中文字体。
     */
    private PdfFont createChineseFont() throws Exception {
        return PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
    }

    /**
     * 创建中文字体
     */
    private PdfFont createChineseBoldFont() throws Exception {
        // STSong 没有粗体变体，用同一字体在渲染时通过 setBold() 模拟
        return PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
    }

    /**
     * 添加标题
     */
    private void addTitleSection(Document document, PdfFont boldFont, PdfFont normalFont) {
        Div titleBlock = new Div()
                .setBackgroundColor(PRIMARY_BLUE)
                .setPadding(25)
                .setMarginBottom(5);

        Paragraph title = new Paragraph("阿尔茨海默症初步评估报告")
                .setFont(boldFont).setBold()
                .setFontSize(22)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);
        titleBlock.add(title);

        Paragraph subtitle = new Paragraph("Alzheimer's Disease Preliminary Assessment Report")
                .setFont(normalFont)
                .setFontSize(10)
                .setFontColor(new DeviceRgb(200, 220, 255))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        titleBlock.add(subtitle);

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
        Paragraph dateInfo = new Paragraph("报告生成时间: " + dateStr)
                .setFont(normalFont)
                .setFontSize(9)
                .setFontColor(new DeviceRgb(180, 210, 255))
                .setTextAlignment(TextAlignment.CENTER);
        titleBlock.add(dateInfo);

        document.add(titleBlock);
    }

    /**
     * 添加症状描述
     */
    private void addSymptomsSection(Document document, PdfFont boldFont, PdfFont normalFont, String symptoms) {
        addSectionTitle(document, boldFont, "一、症状描述");

        Div box = createContentBox();
        Paragraph content = new Paragraph(symptoms != null ? symptoms : "未提供症状信息")
                .setFont(normalFont)
                .setFontSize(11)
                .setMultipliedLeading(1.6f);
        box.add(content);
        document.add(box);
    }

    /**
     * 添加症状分析与评估
     */
    private void addAnalysisSection(Document document, PdfFont boldFont, PdfFont normalFont, String analysis) {
        addSectionTitle(document, boldFont, "二、症状分析与评估");

        Div box = createContentBox();
        if (analysis != null && !analysis.isBlank()) {
            String[] paragraphs = analysis.split("\n");
            for (String para : paragraphs) {
                if (para.trim().isEmpty()) continue;
                Paragraph p = new Paragraph(para.trim())
                        .setFont(normalFont)
                        .setFontSize(11)
                        .setMultipliedLeading(1.6f)
                        .setMarginBottom(4);
                box.add(p);
            }
        } else {
            box.add(new Paragraph("暂无分析结果").setFont(normalFont).setFontSize(11));
        }
        document.add(box);
    }

    /**
     * 添加推荐就诊医院
     */
    private void addHospitalSection(Document document, PdfFont boldFont, PdfFont normalFont, String hospitals) {
        addSectionTitle(document, boldFont, "三、推荐就诊医院");

        if (hospitals == null || hospitals.isBlank()) {
            Div box = createContentBox();
            box.add(new Paragraph("暂无医院推荐信息，建议就近前往三甲医院神经内科就诊。")
                    .setFont(normalFont).setFontSize(11));
            document.add(box);
            return;
        }

        // 解析医院信息为表格
        Table table = new Table(UnitValue.createPercentArray(new float[]{5f, 25f, 35f, 20f, 15f}))
                .useAllAvailableWidth()
                .setMarginTop(8)
                .setMarginBottom(15);

        addTableHeader(table, boldFont, "序号", "医院名称", "地址", "电话", "距离");

        String[] lines = hospitals.split("\n");
        int index = 0;
        String currentName = "", currentAddress = "", currentTel = "", currentDistance = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                if (!currentName.isEmpty()) {
                    index++;
                    boolean isAlt = index % 2 == 0;
                    addTableRow(table, normalFont, isAlt,
                            String.valueOf(index), currentName, currentAddress, currentTel, currentDistance);
                    currentName = "";
                    currentAddress = "";
                    currentTel = "";
                    currentDistance = "";
                }
                continue;
            }

            if (line.startsWith("【") && line.contains("】")) {
                currentName = line.substring(line.indexOf("】") + 1).trim();
            } else if (line.contains("地址:") || line.contains("地址：")) {
                currentAddress = line.replaceAll(".*地址[：:]\\s*", "").trim();
            } else if (line.contains("电话:") || line.contains("电话：")) {
                currentTel = line.replaceAll(".*电话[：:]\\s*", "").trim();
            } else if (line.contains("距离:") || line.contains("距离：")) {
                currentDistance = line.replaceAll(".*距离[：:]\\s*", "").trim();
            }
        }
        if (!currentName.isEmpty()) {
            index++;
            boolean isAlt = index % 2 == 0;
            addTableRow(table, normalFont, isAlt,
                    String.valueOf(index), currentName, currentAddress, currentTel, currentDistance);
        }

        if (index > 0) {
            document.add(table);
        } else {
            Div box = createContentBox();
            String[] hospitalLines = hospitals.split("\n");
            for (String hl : hospitalLines) {
                if (!hl.trim().isEmpty()) {
                    box.add(new Paragraph(hl.trim()).setFont(normalFont).setFontSize(11)
                            .setMultipliedLeading(1.5f));
                }
            }
            document.add(box);
        }
    }

    /**
     * 添加专业建议
     */
    private void addRecommendationsSection(Document document, PdfFont boldFont, PdfFont normalFont, String recommendations) {
        addSectionTitle(document, boldFont, "四、专业建议");

        Div box = createContentBox();
        if (recommendations != null && !recommendations.isBlank()) {
            String[] paragraphs = recommendations.split("\n");
            for (String para : paragraphs) {
                if (para.trim().isEmpty()) continue;
                Paragraph p = new Paragraph(para.trim())
                        .setFont(normalFont)
                        .setFontSize(11)
                        .setMultipliedLeading(1.6f)
                        .setMarginBottom(3);
                box.add(p);
            }
        } else {
            box.add(new Paragraph("建议尽早前往医院神经内科进行专业评估。").setFont(normalFont).setFontSize(11));
        }
        document.add(box);
    }

    /**
     * 添加免责声明
     */
    private void addDisclaimerSection(Document document, PdfFont normalFont) {
        Div disclaimerBox = new Div()
                .setBackgroundColor(WARNING_BG)
                .setPadding(15)
                .setMarginTop(20)
                .setBorder(new SolidBorder(new DeviceRgb(255, 183, 77), 1));

        Paragraph disclaimerTitle = new Paragraph("重要声明")
                .setFont(normalFont).setBold()
                .setFontSize(12)
                .setFontColor(new DeviceRgb(230, 81, 0))
                .setMarginBottom(5);
        disclaimerBox.add(disclaimerTitle);

        String disclaimerText = "本报告由 AI 智能系统自动生成，仅供初步参考，不能替代专业医疗诊断和治疗。"
                + "报告中的分析和建议基于用户提供的症状描述，可能存在局限性。"
                + "如有健康问题，请及时咨询专业医生或前往医疗机构就诊。"
                + "最终诊断和治疗方案应由专业医生根据详细检查确定。";

        Paragraph disclaimer = new Paragraph(disclaimerText)
                .setFont(normalFont)
                .setFontSize(9)
                .setFontColor(new DeviceRgb(100, 70, 30))
                .setMultipliedLeading(1.5f);
        disclaimerBox.add(disclaimer);

        document.add(disclaimerBox);
    }

    /**
     * 添加页脚
     */
    private void addFooter(Document document, PdfFont normalFont) {
        Paragraph footer = new Paragraph()
                .setMarginTop(25)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8)
                .setFontColor(new DeviceRgb(150, 150, 150));

        footer.add(new Text("AlzAssistant 阿尔茨海默症智能辅助系统").setFont(normalFont));
        footer.add(new Text("  |  ").setFont(normalFont));
        footer.add(new Text("本报告仅供参考").setFont(normalFont));

        document.add(footer);
    }

    // ---- 辅助方法 ----

    /**
     * 添加分区标题
     */
    private void addSectionTitle(Document document, PdfFont boldFont, String title) {
        Div titleBar = new Div()
                .setBackgroundColor(SECTION_BG)
                .setPadding(8)
                .setPaddingLeft(15)
                .setMarginTop(18)
                .setMarginBottom(8);

        Paragraph titleParagraph = new Paragraph(title)
                .setFont(boldFont).setBold()
                .setFontSize(14)
                .setFontColor(ColorConstants.WHITE);

        titleBar.add(titleParagraph);
        document.add(titleBar);
    }

    /**
     * 创建内容分区
     */
    private Div createContentBox() {
        return new Div()
                .setBackgroundColor(new DeviceRgb(250, 251, 253))
                .setPadding(15)
                .setMarginBottom(5)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f));
    }

    /**
     * 添加分隔线
     */
    private void addSeparator(Document document) {
        document.add(new Paragraph().setMarginTop(5).setMarginBottom(5));
    }

    /**
     * 添加表格表头
     */
    private void addTableHeader(Table table, PdfFont boldFont, String... headers) {
        for (String header : headers) {
            Cell cell = new Cell()
                    .setBackgroundColor(TABLE_HEADER_BG)
                    .setPadding(8)
                    .setBorder(new SolidBorder(ColorConstants.WHITE, 0.5f));
            cell.add(new Paragraph(header).setFont(boldFont).setBold()
                    .setFontSize(10).setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(cell);
        }
    }

    /**
     * 添加表格行
     */
    private void addTableRow(Table table, PdfFont normalFont, boolean isAlternate, String... values) {
        DeviceRgb bgColor = isAlternate ? TABLE_ALT_ROW : new DeviceRgb(255, 255, 255);
        for (String value : values) {
            Cell cell = new Cell()
                    .setBackgroundColor(bgColor)
                    .setPadding(6)
                    .setBorder(new SolidBorder(BORDER_COLOR, 0.3f))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            cell.add(new Paragraph(value != null ? value : "")
                    .setFont(normalFont).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(cell);
        }
    }
}
