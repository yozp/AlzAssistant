package com.yzj.alzassistant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseAddRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseQueryRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseUpdateRequest;
import com.yzj.alzassistant.model.entity.KnowledgeBase;
import com.yzj.alzassistant.mapper.KnowledgeBaseMapper;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.KnowledgeBaseVO;
import com.yzj.alzassistant.service.KnowledgeBaseService;
import com.yzj.alzassistant.util.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * 知识库文档 服务层实现。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@Slf4j
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    // 文档存储根目录（相对于项目根目录）
    private static final String DOCUMENT_ROOT_DIR = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "document";

    @Override
    public Long addKnowledgeBaseByFile(MultipartFile file, String category, User loginUser) {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        // 2. 检查文件格式
        if (!DocumentParser.isSupportedFormat(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件格式，仅支持 doc、docx、md、txt、pdf 格式");
        }

        // 3. 保存文件到document目录
        String filePath = saveFileToDocument(file, originalFilename);

        // 4. 获取文件类型
        String fileType = getFileExtension(originalFilename);

        // 5. 先创建知识库记录，状态设为parsing（解析中）
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setTitle(getFileNameWithoutExtension(originalFilename));
        knowledgeBase.setContent(""); // 初始为空，解析完成后更新
        knowledgeBase.setCategory(category);
        knowledgeBase.setFileType(fileType);
        knowledgeBase.setFilePath(filePath);
        knowledgeBase.setSource("文件上传");
        knowledgeBase.setStatus("parsing"); // 解析中
        knowledgeBase.setUserId(loginUser.getId());
        knowledgeBase.setCreateTime(LocalDateTime.now());
        knowledgeBase.setEditTime(LocalDateTime.now());
        knowledgeBase.setUpdateTime(LocalDateTime.now());

        boolean saveResult = this.save(knowledgeBase);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存知识库文档失败");
        }

        Long knowledgeBaseId = knowledgeBase.getId();

        // 6. 异步解析文档内容
        CompletableFuture.runAsync(() -> {
            try {
                String content = DocumentParser.parseDocument(filePath);
                // 更新知识库记录
                KnowledgeBase updateKB = new KnowledgeBase();
                updateKB.setId(knowledgeBaseId);
                updateKB.setContent(content);
                updateKB.setStatus("active"); // 解析完成，设为激活
                updateKB.setUpdateTime(LocalDateTime.now());
                this.updateById(updateKB);
                log.info("文档解析完成: {}", knowledgeBaseId);
            } catch (Exception e) {
                log.error("解析文档失败: {}", filePath, e);
                // 更新状态为失败
                KnowledgeBase updateKB = new KnowledgeBase();
                updateKB.setId(knowledgeBaseId);
                updateKB.setStatus("failed"); // 解析失败
                updateKB.setUpdateTime(LocalDateTime.now());
                this.updateById(updateKB);
            }
        });

        return knowledgeBaseId;
    }

    @Override
    public List<Long> addKnowledgeBaseByFiles(MultipartFile[] files, String category, User loginUser) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        List<Long> ids = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    Long id = addKnowledgeBaseByFile(file, category, loginUser);
                    ids.add(id);
                } catch (Exception e) {
                    log.error("上传文件失败: {}", file.getOriginalFilename(), e);
                    // 继续处理其他文件，不中断整个流程
                }
            }
        }

        if (ids.isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "所有文件上传失败");
        }

        return ids;
    }

    @Override
    public Long addKnowledgeBase(KnowledgeBaseAddRequest addRequest, User loginUser) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtil.copyProperties(addRequest, knowledgeBase);
        if (StrUtil.isBlank(knowledgeBase.getStatus())) {
            knowledgeBase.setStatus("active");
        }
        knowledgeBase.setUserId(loginUser.getId());
        knowledgeBase.setCreateTime(LocalDateTime.now());
        knowledgeBase.setEditTime(LocalDateTime.now());
        knowledgeBase.setUpdateTime(LocalDateTime.now());

        boolean saveResult = this.save(knowledgeBase);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存知识库文档失败");
        }

        return knowledgeBase.getId();
    }

    @Override
    public boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest updateRequest, User loginUser) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        KnowledgeBase knowledgeBase = this.getById(updateRequest.getId());
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "知识库文档不存在");
        }

        BeanUtil.copyProperties(updateRequest, knowledgeBase, "id", "userId", "createTime");
        knowledgeBase.setEditTime(LocalDateTime.now());
        knowledgeBase.setUpdateTime(LocalDateTime.now());

        return this.updateById(knowledgeBase);
    }

    @Override
    public boolean deleteKnowledgeBase(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "知识库文档不存在");
        }

        return this.removeById(id);
    }

    @Override
    public Page<KnowledgeBaseVO> listKnowledgeBaseByPage(KnowledgeBaseQueryRequest queryRequest) {
        QueryWrapper queryWrapper = getQueryWrapper(queryRequest);
        Page<KnowledgeBase> page = this.page(new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize()), queryWrapper);
        Page<KnowledgeBaseVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(getKnowledgeBaseVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public KnowledgeBaseVO getKnowledgeBaseById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "知识库文档不存在");
        }
        return getKnowledgeBaseVO(knowledgeBase);
    }

    @Override
    public KnowledgeBaseVO getKnowledgeBaseVO(KnowledgeBase knowledgeBase) {
        if (knowledgeBase == null) {
            return null;
        }
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        BeanUtil.copyProperties(knowledgeBase, vo);
        return vo;
    }

    @Override
    public List<KnowledgeBaseVO> getKnowledgeBaseVOList(List<KnowledgeBase> knowledgeBaseList) {
        if (knowledgeBaseList == null) {
            return null;
        }
        return knowledgeBaseList.stream()
                .map(this::getKnowledgeBaseVO)
                .collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper getQueryWrapper(KnowledgeBaseQueryRequest queryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (queryRequest.getTitle() != null && StrUtil.isNotBlank(queryRequest.getTitle())) {
            queryWrapper.like("title", queryRequest.getTitle());
        }
        if (queryRequest.getCategory() != null && StrUtil.isNotBlank(queryRequest.getCategory())) {
            queryWrapper.eq("category", queryRequest.getCategory());
        }
        if (queryRequest.getStatus() != null && StrUtil.isNotBlank(queryRequest.getStatus())) {
            queryWrapper.eq("status", queryRequest.getStatus());
        }
        if (queryRequest.getFileType() != null && StrUtil.isNotBlank(queryRequest.getFileType())) {
            queryWrapper.eq("fileType", queryRequest.getFileType());
        }
        // 排序
        if (StrUtil.isNotBlank(queryRequest.getSortField())) {
            if ("ascend".equals(queryRequest.getSortOrder())) {
                queryWrapper.orderBy(queryRequest.getSortField(), true);
            } else {
                queryWrapper.orderBy(queryRequest.getSortField(), false);
            }
        } else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 保存文件到document目录
     */
    private String saveFileToDocument(MultipartFile file, String originalFilename) {
        try {
            // 确保目录存在
            File dir = new File(DOCUMENT_ROOT_DIR);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建目录失败: " + DOCUMENT_ROOT_DIR);
                }
            }

            // 生成唯一文件名（使用时间戳）
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            File targetFile = new File(dir, fileName);
            String filePath = targetFile.getAbsolutePath();

            // 保存文件
            file.transferTo(targetFile);

            // 验证文件是否保存成功
            if (!targetFile.exists()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败，文件不存在: " + filePath);
            }
            if (targetFile.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败，文件大小为0: " + filePath);
            }

            log.info("文件保存成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("保存文件失败: {}", originalFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 获取不带扩展名的文件名
     */
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }
}
