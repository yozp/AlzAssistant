package com.yzj.alzassistant.controller;

import com.mybatisflex.core.paginate.Page;
import com.yzj.alzassistant.annotation.AuthCheck;
import com.yzj.alzassistant.common.BaseResponse;
import com.yzj.alzassistant.common.ResultUtils;
import com.yzj.alzassistant.constant.UserConstant;
import com.yzj.alzassistant.exception.ThrowUtils;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseAddRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseQueryRequest;
import com.yzj.alzassistant.model.dto.knowledgeBase.KnowledgeBaseUpdateRequest;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.KnowledgeBaseVO;
import com.yzj.alzassistant.service.KnowledgeBaseService;
import com.yzj.alzassistant.service.RagRebuildService;
import com.yzj.alzassistant.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档 控制层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@RestController
@RequestMapping("/knowledgeBase")
public class KnowledgeBaseController {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    @Resource
    private UserService userService;

    @Resource
    private RagRebuildService ragRebuildService;

    /**
     * 文件上传并解析文档（单文件）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            HttpServletRequest request) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        Long id = knowledgeBaseService.addKnowledgeBaseByFile(file, category, loginUser);
        return ResultUtils.success(id);
    }

    /**
     * 多文件上传并解析文档
     */
    @PostMapping("/upload/multiple")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Long>> uploadMultipleDocuments(
            @RequestPart("files") MultipartFile[] files,
            @RequestParam(value = "category", required = false) String category,
            HttpServletRequest request) {
        ThrowUtils.throwIf(files == null || files.length == 0, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        List<Long> ids = knowledgeBaseService.addKnowledgeBaseByFiles(files, category, loginUser);
        return ResultUtils.success(ids);
    }

    /**
     * 添加知识库文档
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addKnowledgeBase(@RequestBody KnowledgeBaseAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long id = knowledgeBaseService.addKnowledgeBase(addRequest, loginUser);
        return ResultUtils.success(id);
    }

    /**
     * 更新知识库文档
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateKnowledgeBase(@RequestBody KnowledgeBaseUpdateRequest updateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = knowledgeBaseService.updateKnowledgeBase(updateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除知识库文档
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteKnowledgeBase(@RequestBody com.yzj.alzassistant.common.DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = knowledgeBaseService.deleteKnowledgeBase(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询知识库文档
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<KnowledgeBaseVO>> listKnowledgeBaseByPage(@RequestBody KnowledgeBaseQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        Page<KnowledgeBaseVO> page = knowledgeBaseService.listKnowledgeBaseByPage(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 根据id获取知识库文档详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<KnowledgeBaseVO> getKnowledgeBaseById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        KnowledgeBaseVO vo = knowledgeBaseService.getKnowledgeBaseById(id);
        return ResultUtils.success(vo);
    }

    /**
     * 重新构建RAG向量库
     */
    @PostMapping("/rebuild-rag")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> rebuildRag() {
        boolean result = ragRebuildService.rebuildRag();
        return ResultUtils.success(result);
    }

}
