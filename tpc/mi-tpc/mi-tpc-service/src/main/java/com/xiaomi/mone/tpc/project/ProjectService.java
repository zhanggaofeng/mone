package com.xiaomi.mone.tpc.project;

import com.xiaomi.mone.application.api.bo.ProjectMemberVo;
import com.xiaomi.mone.application.api.bo.SearchProjectParam;
import com.xiaomi.mone.tpc.cache.Cache;
import com.xiaomi.mone.tpc.cache.enums.ModuleEnum;
import com.xiaomi.mone.tpc.cache.key.Key;
import com.xiaomi.mone.tpc.common.enums.*;
import com.xiaomi.mone.tpc.common.param.*;
import com.xiaomi.mone.tpc.common.util.GsonUtil;
import com.xiaomi.mone.tpc.common.vo.*;
import com.xiaomi.mone.tpc.dao.entity.NodeEntity;
import com.xiaomi.mone.tpc.dao.impl.NodeDao;
import com.xiaomi.mone.tpc.iam.IamHelper;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.mone.tpc.node.NodeFlagHelper;
import com.xiaomi.mone.tpc.node.NodeHelper;
import com.xiaomi.mone.tpc.node.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 17:17
 */
@Slf4j
@Service
public class ProjectService implements ProjectHelper {

    @Reference(interfaceClass = com.xiaomi.mone.application.api.service.ProjectService.class, check =false, group ="${new.project.dubbo.group}", timeout = 60000)
    private com.xiaomi.mone.application.api.service.ProjectService newProjectService;
    @Autowired
    private NodeHelper nodeHelper;
    @Autowired
    private NodeDao nodeDao;
    @Autowired
    private NodeFlagHelper nodeFlagHelper;
    @Autowired
    private IamHelper iamHelper;
    @Value("${def.project.gorup.id}")
    private String defProjectGroupId;
    @Autowired
    private Cache cache;

    public ResultVo<ProjectVoV2> add(ProjectCreateParam param) {
        NodeEntity parentNode = null;
        if (param.getParentNodeId() != null) {
            parentNode = nodeDao.getById(param.getParentNodeId(), NodeEntity.class);
            if (parentNode == null) {
                return ResponseCode.OPER_FAIL.build();
            }
        } else {
            parentNode = nodeDao.getOneByOutId(param.getParentOutIdType(), param.getParentOutId());
            if (parentNode == null) {
                return ResponseCode.NO_OPER_PERMISSION.build("项目未绑定");
            }
        }
        NodeTypeEnum parentNodeType = NodeTypeEnum.getEnum(parentNode.getType());
        if (!parentNodeType.supportSubNodeType(NodeTypeEnum.PRO_TYPE.getCode())) {
            return ResponseCode.OPER_ILLEGAL.build();
        }
        //项目创建
        com.xiaomi.mone.application.api.bo.Result<com.xiaomi.mone.application.api.bo.ProjectVo> projectResult =
                newProjectService.createProject(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), param.getProjectVo());
        log.info("rpc-createProject调用request={}, response={}", GsonUtil.gsonString(param.getProjectVo()), GsonUtil.gsonString(projectResult));
        if (projectResult == null || projectResult.getData() == null || !projectResult.isSuccess()) {
            return ResponseCode.OPER_FAIL.build(projectResult.getMessage());
        }
        if (!parentNode.getId().equals(defProjectGroupId)) {
            FlagVo flagVo = nodeFlagHelper.getFirstOneByParentId(parentNode.getId(), FlagTypeEnum.IAM.getCode());
            if (flagVo != null) {
                IamResInfoVo iamResInfoVo = iamHelper.createResouce(flagVo.getFlagKey(), projectResult.getData().getId(), projectResult.getData().getName());
                log.info("项目创建,iamId={}下面创建资源{}", flagVo.getFlagKey(), iamResInfoVo);
            }
        }
        NodeAddParam nodeParam = buildNodeAddParam(param, projectResult.getData());
        ResultVo<NodeVo> nodeResult = nodeHelper.realAdd(nodeParam, parentNode);
        if (!nodeResult.success()) {
            //原子性保证，待优化
            log.error("项目创建项目创建成功，节点创建失败; projectId={}", projectResult.getData().getId());
            return ResponseCode.OPER_FAIL.build(nodeResult.getMessage());
        }
        ProjectVoV2 projectVoV2 = new ProjectVoV2();
        projectVoV2.setProjectVo(projectResult.getData());
        projectVoV2.setNodeVo(nodeResult.getData());
        return ResponseCode.SUCCESS.build(projectVoV2);
    }

    private NodeAddParam buildNodeAddParam(ProjectCreateParam param, com.xiaomi.mone.application.api.bo.ProjectVo projectVo) {
        NodeAddParam addParam = new NodeAddParam();
        addParam.setUserId(param.getUserId());
        addParam.setAccount(param.getAccount());
        addParam.setUserType(param.getUserType());
        addParam.setParentNodeId(param.getParentNodeId());
        addParam.setParentOutId(param.getParentOutId());
        addParam.setParentOutIdType(param.getParentOutIdType());
        addParam.setType(NodeTypeEnum.PRO_TYPE.getCode());
        addParam.setOutIdType(OutIdTypeEnum.PROJECT.getCode());
        addParam.setOutId(projectVo.getId());
        if (StringUtils.isNotBlank(param.getNodeName())){
            addParam.setNodeName(param.getNodeName());
        } else {
            addParam.setNodeName(projectVo.getName());
        }
        if (StringUtils.isNotBlank(param.getDesc())){
            addParam.setDesc(param.getDesc());
        } else {
            addParam.setDesc(projectVo.getDesc());
        }
        addParam.setOrgParam(param.getOrgParam());
        return addParam;
    }

    public ResultVo edit(ProjectEditParam param) {
        NodeEntity curNode = nodeDao.getOneByOutId(OutIdTypeEnum.PROJECT.getCode(), param.getProjectVo().getId());
        if (curNode == null) {
            return ResponseCode.OPER_ILLEGAL.build("项目节点不存在");
        }
        //项目创建
        com.xiaomi.mone.application.api.bo.Result<Boolean> projectResult =
                newProjectService.updateProject(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), param.getProjectVo(), param.isValidateRole());
        log.info("rpc-updateProject调用request={}, response={}", GsonUtil.gsonString(param.getProjectVo()), GsonUtil.gsonString(projectResult));
        if (projectResult == null || projectResult.getData() == null || !projectResult.isSuccess()) {
            return ResponseCode.OPER_FAIL.build(projectResult.getMessage());
        }
        //更新节点信息--以项目更新结果为主
        if (StringUtils.isNotBlank(param.getProjectVo().getName()) || param.getOrgParam() != null) {
            NodeEditParam nodeParam = new NodeEditParam();
            nodeParam.setUserId(param.getUserId());
            nodeParam.setAccount(param.getAccount());
            nodeParam.setUserType(param.getUserType());
            nodeParam.setNodeName(param.getProjectVo().getName());
            nodeParam.setDesc(param.getProjectVo().getDesc());
            nodeParam.setOrgParam(param.getOrgParam());
            nodeHelper.realEdit(nodeParam, curNode);
            if (!curNode.getParentId().equals(defProjectGroupId)) {
                FlagVo curFlagVo = nodeFlagHelper.getFirstOneByParentId(curNode.getParentId(), FlagTypeEnum.IAM.getCode());
                if (curFlagVo != null) {
                    boolean exist = false;
                    List<IamResInfoVo> iamResList = iamHelper.qryResouces(curFlagVo.getFlagKey());
                    if (CollectionUtils.isNotEmpty(iamResList)) {
                        exist = iamResList.stream().filter(iamRes -> String.valueOf(param.getProjectVo().getId()).equals(iamRes.getResourceId())).findAny().isPresent();
                    }
                    if (!exist) {
                        IamResInfoVo iamResInfoVo = iamHelper.createResouce(curFlagVo.getFlagKey(), param.getProjectVo().getId(), curNode.getNodeName());
                        log.info("项目编辑,iamId={}下面创建资源{}", curFlagVo.getFlagKey(), iamResInfoVo);
                    }
                }
            }
        }
        if (param.getParentNodeId() != null && !param.getParentNodeId().equals(curNode.getParentId())) {
            NodeMoveParam nodeParam = new NodeMoveParam();
            nodeParam.setUserId(param.getUserId());
            nodeParam.setAccount(param.getAccount());
            nodeParam.setUserType(param.getUserType());
            nodeParam.setFromId(curNode.getId());
            nodeParam.setToId(param.getParentNodeId());
            nodeHelper.move(true, nodeParam);
            if (!curNode.getParentId().equals(defProjectGroupId)) {
                //删除老资源-弱依赖
                FlagVo curFlagVo = nodeFlagHelper.getFirstOneByParentId(curNode.getParentId(), FlagTypeEnum.IAM.getCode());
                if (curFlagVo != null) {
                    iamHelper.deleteResouce(curFlagVo.getFlagKey(), param.getProjectVo().getId());
                    log.info("项目编辑-移动,iamId={}下面删除资源", curFlagVo.getFlagKey());
                }
            }
            if (!param.getParentNodeId().equals(defProjectGroupId)) {
                //添加新资源-弱依赖
                FlagVo newFlagVo = nodeFlagHelper.getFirstOneByParentId(param.getParentNodeId(), FlagTypeEnum.IAM.getCode());
                if (newFlagVo != null) {
                    String projectName = StringUtils.isNotBlank(param.getProjectVo().getName()) ? param.getProjectVo().getName() : param.getNodeName();
                    projectName = StringUtils.isNotBlank(projectName) ? projectName : curNode.getNodeName();
                    IamResInfoVo iamResInfoVo = iamHelper.createResouce(newFlagVo.getFlagKey(), param.getProjectVo().getId(), projectName);
                    log.info("项目编辑-移动,iamId={}下面创建资源{}", newFlagVo.getFlagKey(), iamResInfoVo);
                }
            }
        }
        return ResponseCode.SUCCESS.build();
    }

    public ResultVo delete(ProjectDeleteParam param) {
        NodeEntity curNode = nodeDao.getOneByOutId(OutIdTypeEnum.PROJECT.getCode(), param.getProjectId());
        if (curNode == null) {
            return ResponseCode.OPER_ILLEGAL.build("项目节点不存在");
        }
        //简单校验下是否存在子节点
        List<NodeEntity> subNodes = nodeDao.getByParentId(curNode.getId(), 1);
        if (!CollectionUtils.isEmpty(subNodes)) {
            return ResponseCode.OPER_FAIL.build("项目下有残留数据，请清空后在删除");
        }
        String fullAccount = UserUtil.getFullAccount(param.getAccount(), param.getUserType());
        com.xiaomi.mone.application.api.bo.ProjectVo projectVo = newProjectService.getProjectById(param.getProjectId());
        log.info("rpc-getProjectById调用account={}, projectId={}, response={}", fullAccount, param.getProjectId(), GsonUtil.gsonString(projectVo));
        if (projectVo != null) {
            com.xiaomi.mone.application.api.bo.Result<Boolean> projectResult =
                    newProjectService.deleteProject(fullAccount, param.getProjectId(), param.isValidateRole());
            log.info("rpc-deleteProject调用account={}, projectId={}, response={}", fullAccount, param.getProjectId(), GsonUtil.gsonString(projectResult));
            if (projectResult == null || projectResult.getData() == null || !projectResult.isSuccess()) {
                return ResponseCode.OPER_FAIL.build(projectResult.getMessage());
            }
            if (!curNode.getParentId().equals(defProjectGroupId)) {
                FlagVo flagVo = nodeFlagHelper.getFirstOneByParentId(curNode.getParentId(), FlagTypeEnum.IAM.getCode());
                if (flagVo != null) {
                    iamHelper.deleteResouce(flagVo.getFlagKey(), param.getProjectId());
                    log.info("项目删除,iamId={}下面删除资源", flagVo.getFlagKey());
                }
            }
        }
        if (curNode != null) {
            return nodeHelper.cascadeDelete(true, param, curNode);
        } else {
            return ResponseCode.SUCCESS.build();
        }
    }

    public ResultVo<ProjectVoV2> get(ProjectQryParam param) {
        if (param.getProjectId() == null) {
            return ResponseCode.ARG_ERROR.build();
        }
        com.xiaomi.mone.application.api.bo.ProjectVo projectVo = newProjectService.getProjectById(param.getProjectId());
        if (projectVo == null) {
            return ResponseCode.OPER_FAIL.build("项目不存在");
        }
        NodeEntity curNode = nodeDao.getOneByOutId(OutIdTypeEnum.PROJECT.getCode(), param.getProjectId());
        if (curNode == null) {
            Key key = Key.build(ModuleEnum.NODE_PRO_ADD_LOCK).keys(param.getProjectId());
            if (!cache.get().lock(key)) {
                return ResponseCode.OPER_FAIL.build("存在并发同步操作，请稍后重试");
            }
            try {
                //刷新项目节点
                NodeAddParam addParam = new NodeAddParam();
                addParam.setAccount(param.getAccount());
                addParam.setUserType(param.getUserType());
                addParam.setUserId(param.getUserId());
                addParam.setParentNodeId(Long.valueOf(defProjectGroupId));
                addParam.setNodeName(projectVo.getName());
                addParam.setDesc(projectVo.getDesc());
                addParam.setType(NodeTypeEnum.PRO_TYPE.getCode());
                addParam.setOutId(projectVo.getId());
                addParam.setOutIdType(OutIdTypeEnum.PROJECT.getCode());
                ResultVo<NodeVo> syncResult = nodeHelper.add(true, addParam);
                log.info("项目详情查询，节点同步;syncResult={}", syncResult);
                if (!syncResult.success()) {
                    return ResponseCode.OPER_FAIL.build(syncResult.getMessage());
                }
                curNode = NodeUtil.toEntity(syncResult.getData());
            } finally{
                cache.get().unlock(key);
            }
        }
        ResultVo<NodeVo> nodeResult = nodeHelper.get(param, curNode, param.isNeedParent());
        if (!nodeResult.success()) {
            return ResponseCode.OPER_FAIL.build(nodeResult.getMessage());
        }
        ProjectVoV2 projectVoV2 = new ProjectVoV2();
        projectVoV2.setProjectVo(projectVo);
        projectVoV2.setNodeVo(nodeResult.getData());
        return ResponseCode.SUCCESS.build(projectVoV2);
    }

    public ResultVo<PageDataVo<ProjectVoV2>> searchByPage(ProjectQryParam param) {
        PageDataVo<ProjectVoV2> pageData = param.buildPageDataVo();
        SearchProjectParam projectParam = new SearchProjectParam();
        projectParam.setAllProject(!param.isMy());
        projectParam.setSearch(param.getName());
        projectParam.setProjectType(param.getType());
        projectParam.setPage(pageData.getPage());
        projectParam.setPageSize(pageData.getPageSize());
        projectParam.setNeedTopSort(param.isNeedTopSort());
        com.xiaomi.mone.application.api.bo.Result<com.xiaomi.data.push.common.PageInfo<com.xiaomi.mone.application.api.bo.ProjectVo>> pageDataResult = newProjectService.pageProject(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), projectParam);
        if (pageDataResult == null || pageDataResult.getData() == null || CollectionUtils.isEmpty(pageDataResult.getData().getData())) {
            return ResponseCode.SUCCESS.build(pageData);
        }
        pageData.setTotal(pageDataResult.getData().getTotal());
        List<ProjectVoV2> projectVoV2s =pageDataResult.getData().getData().stream().map(p -> {
            ProjectVoV2 p2 = new ProjectVoV2();
            p2.setProjectVo(p);
            return p2;
        }).collect(Collectors.toList());
        pageData.setList(projectVoV2s);
        return ResponseCode.SUCCESS.build(pageData);
    }

    public ResultVo<Boolean> topProject(ProjectTopSetParam param) {
        com.xiaomi.mone.application.api.bo.Result<Boolean> pageDataResult = newProjectService.topProject(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), param.getProjectId(), param.isTop());
        if (pageDataResult == null || !pageDataResult.isSuccess()) {
            if (pageDataResult != null) {
                return ResponseCode.UNKNOWN_ERROR.build(pageDataResult.getMessage());
            } else {
                return ResponseCode.UNKNOWN_ERROR.build("置顶请求失败");
            }
        }
        return ResponseCode.SUCCESS.build(pageDataResult.getData());
    }

    public ResultVo<List<ProjectVoV2>> listByCond(ProjectQryParam param) {
        List<com.xiaomi.mone.application.api.bo.ProjectVo> projectVos = null;
        if (CollectionUtils.isNotEmpty(param.getNames())) {
            projectVos = newProjectService.getProjectsByNames(param.getNames());
        } else if (CollectionUtils.isNotEmpty(param.getProjectIds())) {
            projectVos = newProjectService.getProjectsByIds(param.getProjectIds());
        } else if (StringUtils.isNotBlank(param.getGitAddr())) {
            projectVos = newProjectService.getProjectByGit(param.getGitAddr());
        }
        if (CollectionUtils.isEmpty(projectVos)) {
            return ResponseCode.SUCCESS.build();
        }
        List<ProjectVoV2> projectVoV2s =projectVos.stream().map(p -> {
            ProjectVoV2 p2 = new ProjectVoV2();
            p2.setProjectVo(p);
            return p2;
        }).collect(Collectors.toList());
        return ResponseCode.SUCCESS.build(projectVoV2s);
    }

    @Override
    public ResultVo<List<NodeUserRelVo>> getProjectMembers(ProjectUserQryParam param) {
        if (CollectionUtils.isEmpty(param.getProjectIds())) {
            return ResponseCode.SUCCESS.build();
        }
        List<com.xiaomi.mone.application.api.bo.ProjectMemberVo> pMemberVos = newProjectService.getProjectMembers(param.getProjectIds(), param.getRoleType());
        if (CollectionUtils.isEmpty(pMemberVos)) {
            return ResponseCode.SUCCESS.build();
        }
        //暂时不需要查询node节点数据
        List<NodeUserRelVo> nodeUserRelVos =pMemberVos.stream().map(pm -> {
            AuthUserVo authUserVo = UserUtil.parseFullAccount(pm.getUserName());
            NodeUserRelVo nodeUserRelVo = new NodeUserRelVo();
            nodeUserRelVo.setAccount(authUserVo.getAccount());
            nodeUserRelVo.setUserType(authUserVo.getUserType());
            nodeUserRelVo.setProjectId(pm.getProjectId());
            nodeUserRelVo.setRoleType(pm.getRoleType());
            if (ProjectRoleType.Owner.getCode().equals(pm.getRoleType())) {
                nodeUserRelVo.setType(NodeUserRelTypeEnum.MANAGER.getCode());
            } else if (ProjectRoleType.Member.getCode().equals(pm.getRoleType())) {
                nodeUserRelVo.setType(NodeUserRelTypeEnum.MEMBER.getCode());
            }
            return nodeUserRelVo;
        }).collect(Collectors.toList());
        return ResponseCode.SUCCESS.build(nodeUserRelVos);
    }

    @Override
    public ResultVo addProjectMembers(BaseParam param, ProjectMemberVo projectMemberVo, boolean validateRole) {
        com.xiaomi.mone.application.api.bo.Result<Boolean> pMemberVos = newProjectService.addProjectMember(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), projectMemberVo, validateRole);
        if (pMemberVos == null || !pMemberVos.isSuccess()) {
            return ResponseCode.OPER_FAIL.build("项目成员添加失败");
        }
        return ResponseCode.SUCCESS.build();
    }

    @Override
    public ResultVo saveProjectMembers(BaseParam param, Integer roleType, Long projectId, List<ProjectMemberVo> memberVos, boolean validateRole) {
        com.xiaomi.mone.application.api.bo.Result<Boolean> pMemberVos = newProjectService.saveProjectMembers(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), roleType,projectId, memberVos,  validateRole);
        if (pMemberVos == null || !pMemberVos.isSuccess()) {
            return ResponseCode.OPER_FAIL.build("项目成员修改失败");
        }
        return ResponseCode.SUCCESS.build();
    }

    @Override
    public ResultVo delProjectMembers(BaseParam param, ProjectMemberVo projectMemberVo, boolean validateRole) {
        com.xiaomi.mone.application.api.bo.Result<Boolean> pMemberVos = newProjectService.deleteProjectMember(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), projectMemberVo.getRoleType(), projectMemberVo.getProjectId(), projectMemberVo.getUserName(), validateRole);
        if (pMemberVos == null || !pMemberVos.isSuccess()) {
            return ResponseCode.OPER_FAIL.build("项目成员删除失败");
        }
        return ResponseCode.SUCCESS.build();
    }

    public ResultVo generateCode(ProjectGenParam param) {
        log.info("ProjectService.generateCode req={}", param);
        try {
            com.xiaomi.mone.application.api.bo.Result<Boolean> proGenResult = newProjectService.generateCode(UserUtil.getFullAccount(param.getAccount(), param.getUserType()), param.getProjectVo());
            log.info("ProjectService.generateCode res={}", proGenResult);
            if (proGenResult == null || !proGenResult.isSuccess()) {
                if (proGenResult != null) {
                    return ResponseCode.OPER_FAIL.build(proGenResult.getMessage());
                }
                return ResponseCode.OPER_FAIL.build();
            }
            return ResponseCode.SUCCESS.build();
        } catch (Throwable e) {
            log.info("ProjectService.generateCode异常 req={}", param, e);
            return ResponseCode.UNKNOWN_ERROR.build("项目生成失败");
        }
    }

}
