package com.yzj.alzassistant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yzj.alzassistant.config.CosClientConfig;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.manager.CosManager;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseAddRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseQueryRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseUpdateRequest;
import com.yzj.alzassistant.model.entity.KnowledgeBase;
import com.yzj.alzassistant.mapper.KnowledgeBaseMapper;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.KnowledgeBaseVO;
import com.yzj.alzassistant.service.KnowledgeBaseService;
import com.yzj.alzassistant.util.DocumentParser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Override
    public Long addKnowledgeBaseByFile(MultipartFile file, String category, User loginUser) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        if (!DocumentParser.isSupportedFormat(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件格式，仅支持 doc、docx、md、txt、pdf 格式");
        }

        String fileType = getFileExtension(originalFilename);
        String uuid = RandomUtil.randomString(16);
        String suffix = FileUtil.getSuffix(originalFilename);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("/knowledge/%s", uploadFilename);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("knowledge_", "." + suffix);
            file.transferTo(tempFile);

            cosManager.putObject(uploadPath, tempFile);
            String fileUrl = cosClientConfig.getHost() + uploadPath;

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setTitle(getFileNameWithoutExtension(originalFilename));
            knowledgeBase.setContent("");
        knowledgeBase.setCategory(category);
        knowledgeBase.setFileType(fileType);
            knowledgeBase.setFilePath(uploadPath);
            knowledgeBase.setFileUrl(fileUrl);
        knowledgeBase.setSource("文件上传");
            knowledgeBase.setStatus("parsing");
        knowledgeBase.setUserId(loginUser.getId());
        knowledgeBase.setCreateTime(LocalDateTime.now());
        knowledgeBase.setEditTime(LocalDateTime.now());
        knowledgeBase.setUpdateTime(LocalDateTime.now());

        boolean saveResult = this.save(knowledgeBase);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存知识库文档失败");
        }

        Long knowledgeBaseId = knowledgeBase.getId();
            File finalTempFile = tempFile;
        CompletableFuture.runAsync(() -> {
            try {
                    String content = DocumentParser.parseDocument(finalTempFile.getAbsolutePath());
                KnowledgeBase updateKB = new KnowledgeBase();
                updateKB.setId(knowledgeBaseId);
                updateKB.setContent(content);
                    updateKB.setStatus("active");
                updateKB.setUpdateTime(LocalDateTime.now());
                this.updateById(updateKB);
                log.info("文档解析完成: {}", knowledgeBaseId);
            } catch (Exception e) {
                    log.error("解析文档失败: {}", uploadPath, e);
                KnowledgeBase updateKB = new KnowledgeBase();
                updateKB.setId(knowledgeBaseId);
                    updateKB.setStatus("failed");
                updateKB.setUpdateTime(LocalDateTime.now());
                this.updateById(updateKB);
                } finally {
                    deleteTempFile(finalTempFile);
            }
        });

        return knowledgeBaseId;
        } catch (Exception e) {
            log.error("上传文件到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
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

        boolean result = this.removeById(id);
        if (result && StrUtil.isNotBlank(knowledgeBase.getFilePath())) {
            try {
                cosManager.deleteObject(knowledgeBase.getFilePath());
            } catch (Exception e) {
                log.error("删除COS文件失败: {}", knowledgeBase.getFilePath(), e);
            }
        }
        return result;
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

    private void deleteTempFile(File file) {
        if (file == null) {
            return;
                }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("临时文件删除失败: {}", file.getAbsolutePath());
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
