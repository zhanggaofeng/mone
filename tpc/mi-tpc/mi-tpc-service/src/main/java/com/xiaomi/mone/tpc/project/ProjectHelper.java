package com.xiaomi.mone.tpc.project;

import com.xiaomi.mone.application.api.bo.ProjectMemberVo;
import com.xiaomi.mone.tpc.common.param.BaseParam;
import com.xiaomi.mone.tpc.common.param.ProjectUserQryParam;
import com.xiaomi.mone.tpc.common.vo.NodeUserRelVo;
import com.xiaomi.mone.tpc.common.vo.ResultVo;

import java.util.List;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/7/5 19:13
 */
public interface ProjectHelper {

    ResultVo<List<NodeUserRelVo>> getProjectMembers(ProjectUserQryParam param);

    ResultVo addProjectMembers(BaseParam param, ProjectMemberVo projectMemberVo, boolean validateRole);

    ResultVo saveProjectMembers(BaseParam param, Integer roleType, Long projectId, List<ProjectMemberVo> memberVos, boolean validateRole);

    ResultVo delProjectMembers(BaseParam param, ProjectMemberVo projectMemberVo, boolean validateRole);
}
