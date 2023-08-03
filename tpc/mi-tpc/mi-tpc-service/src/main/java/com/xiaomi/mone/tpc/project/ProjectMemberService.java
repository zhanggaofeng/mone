package com.xiaomi.mone.tpc.project;

import com.xiaomi.mone.application.api.bo.ProjectMemberVo;
import com.xiaomi.mone.tpc.common.enums.NodeTypeEnum;
import com.xiaomi.mone.tpc.common.enums.NodeUserRelTypeEnum;
import com.xiaomi.mone.tpc.common.enums.OutIdTypeEnum;
import com.xiaomi.mone.tpc.common.enums.ProjectRoleType;
import com.xiaomi.mone.tpc.common.param.NullParam;
import com.xiaomi.mone.tpc.common.param.ProjectMemberQryParam;
import com.xiaomi.mone.tpc.common.param.ProjectMemberUpdateParam;
import com.xiaomi.mone.tpc.common.param.ProjectUserQryParam;
import com.xiaomi.mone.tpc.common.vo.NodeUserRelVo;
import com.xiaomi.mone.tpc.common.vo.ResponseCode;
import com.xiaomi.mone.tpc.common.vo.ResultVo;
import com.xiaomi.mone.tpc.common.vo.UserVo;
import com.xiaomi.mone.tpc.dao.entity.NodeEntity;
import com.xiaomi.mone.tpc.dao.impl.NodeDao;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.node.NodeHelper;
import com.xiaomi.mone.tpc.node.NodeUserHelper;
import com.xiaomi.mone.tpc.node.util.NodeUtil;
import com.xiaomi.mone.tpc.user.UserHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/4 16:19
 */
@Slf4j
@Service
public class ProjectMemberService {

    @Autowired
    private NodeDao nodeDao;
    @Autowired
    private ProjectHelper projectHelper;
    @Autowired
    private NodeHelper nodeHelper;
    @Autowired
    private NodeUserHelper nodeUserHelper;
    @Autowired
    private UserHelper userHelper;

    /**
     * 项目成员列表查询
     * @param param
     * @return
     */
    public ResultVo<Map<Integer, List<NodeUserRelVo>>> list(ProjectMemberQryParam param) {
        NodeEntity nodeEntity = null;
        if (param.getNodeId() != null) {
            nodeEntity = nodeDao.getById(param.getNodeId());
        } else {
            nodeEntity = nodeDao.getOneByOutId(OutIdTypeEnum.PROJECT.getCode(), param.getOutId());
        }
        if (nodeEntity == null || !NodeTypeEnum.PRO_TYPE.getCode().equals(nodeEntity.getType())) {
            ResponseCode.OPER_ILLEGAL.build();
        }
        ProjectUserQryParam qryParam = new ProjectUserQryParam();
        List<Long> projectIds = new ArrayList<>(1);
        projectIds.add(nodeEntity.getOutId());
        qryParam.setProjectIds(projectIds);
        qryParam.setRoleType(param.getRoleType());
        ResultVo<List<NodeUserRelVo>> resultVo = projectHelper.getProjectMembers(qryParam);
        if (resultVo == null || CollectionUtils.isEmpty(resultVo.getData())) {
            return ResponseCode.SUCCESS.build();
        }
        Map<Integer, List<NodeUserRelVo>> relMap = new HashMap<>();
        for (NodeUserRelVo vo : resultVo.getData()) {
            vo.setNodeId(nodeEntity.getId());
            vo.setNodeType(nodeEntity.getType());
            if (!relMap.containsKey(vo.getRoleType())) {
                relMap.put(vo.getRoleType(), new ArrayList<>());
            }
            relMap.get(vo.getRoleType()).add(vo);
        }
        return ResponseCode.SUCCESS.build(relMap);
    }

    public ResultVo update(boolean force, ProjectMemberUpdateParam param) {
        NodeEntity nodeEntity = null;
        if (param.getNodeId() != null) {
            nodeEntity = nodeDao.getById(param.getNodeId());
        } else {
            nodeEntity = nodeDao.getOneByOutId(OutIdTypeEnum.PROJECT.getCode(), param.getOutId());
        }
        if (nodeEntity == null || !NodeTypeEnum.PRO_TYPE.getCode().equals(nodeEntity.getType())) {
            ResponseCode.OPER_ILLEGAL.build();
        }
        if (nodeEntity.getOutId() == null || !OutIdTypeEnum.PROJECT.getCode().equals(nodeEntity.getOutIdType())) {
            ResponseCode.OPER_ILLEGAL.build();
        }
        if (!force && !nodeHelper.isMgrOrSuperMgr(param.getUserId(), nodeEntity)) {
            ResponseCode.NO_OPER_PERMISSION.build();
        }
        Set<String> accSet = new HashSet<>();
        List<UserVo> userVos = new ArrayList<>();
        List<ProjectMemberVo> projectMemberVos = new ArrayList<>();
        if (!StringUtils.isEmpty(param.getUserParams())) {
            for (NullParam userParams : param.getUserParams()) {
                UserVo userVo = userHelper.register(userParams.getAccount(), userParams.getUserType());
                if (userVo == null) {
                    ResponseCode.OPER_ILLEGAL.build();
                }
                String fullAcc = UserUtil.getFullAccount(userParams.getAccount(), userParams.getUserType());
                if (accSet.contains(fullAcc)) {
                    continue;
                }
                accSet.add(fullAcc);
                userVos.add(userVo);
                ProjectMemberVo projectMemberVo = new ProjectMemberVo();
                projectMemberVo.setProjectId(nodeEntity.getOutId());
                projectMemberVo.setRoleType(param.getRoleType());
                projectMemberVo.setUserName(fullAcc);
                projectMemberVos.add(projectMemberVo);
            }
        }
        ResultVo resultVo = projectHelper.saveProjectMembers(param, param.getRoleType(), nodeEntity.getOutId(), projectMemberVos, false);
        NodeUserRelTypeEnum relTypeEnum = ProjectRoleType.getEnum(param.getRoleType()).getNodeUserRelType();
        if (resultVo.success() && relTypeEnum != null) {
            ProjectUserQryParam qryParam = new ProjectUserQryParam();
            List<Long> projectIds = new ArrayList<>(1);
            projectIds.add(nodeEntity.getOutId());
            qryParam.setProjectIds(projectIds);
            ResultVo<List<NodeUserRelVo>> qryResultVo = projectHelper.getProjectMembers(qryParam);
            if (qryResultVo.success()) {
                //同步节点成员
                nodeUserHelper.updateProjectMember(param, qryResultVo.getData(), NodeUtil.toVo(nodeEntity));
            }
        }
        return resultVo;
    }

}