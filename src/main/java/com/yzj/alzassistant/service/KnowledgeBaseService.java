package com.yzj.alzassistant.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseAddRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseQueryRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseUpdateRequest;
import com.yzj.alzassistant.model.entity.KnowledgeBase;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.KnowledgeBaseVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档 服务层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 添加知识库文档（通过文件上传）
     */
    Long addKnowledgeBaseByFile(MultipartFile file, String category, User loginUser);

    /**
     * 批量添加知识库文档（通过多文件上传）
     */
    List<Long> addKnowledgeBaseByFiles(MultipartFile[] files, String category, User loginUser);

    /**
     * 添加知识库文档
     */
    Long addKnowledgeBase(KnowledgeBaseAddRequest addRequest, User loginUser);

    /**
     * 更新知识库文档
     */
    boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest updateRequest, User loginUser);

    /**
     * 删除知识库文档
     */
    boolean deleteKnowledgeBase(Long id, User loginUser);

    /**
     * 分页查询知识库文档
     */
    Page<KnowledgeBaseVO> listKnowledgeBaseByPage(KnowledgeBaseQueryRequest queryRequest);

    /**
     * 获取知识库文档详情
     */
    KnowledgeBaseVO getKnowledgeBaseById(Long id);

    /**
     * 转换为VO
     */
    KnowledgeBaseVO getKnowledgeBaseVO(KnowledgeBase knowledgeBase);

    /**
     * 转换为VO列表
     */
    List<KnowledgeBaseVO> getKnowledgeBaseVOList(List<KnowledgeBase> knowledgeBaseList);
}
