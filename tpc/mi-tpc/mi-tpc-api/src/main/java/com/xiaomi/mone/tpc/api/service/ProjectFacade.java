package com.xiaomi.mone.tpc.api.service;

import com.xiaomi.mone.tpc.common.param.*;
import com.xiaomi.mone.tpc.common.vo.NodeUserRelVo;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.mone.tpc.common.vo.ProjectVoV2;
import com.xiaomi.youpin.infra.rpc.Result;

import java.util.List;

public interface ProjectFacade {

    Result<ProjectVoV2> add(ProjectCreateParam param);

    Result edit(ProjectEditParam param);

    Result delete(ProjectDeleteParam param);

    Result<ProjectVoV2> get(ProjectQryParam param);

    Result<List<ProjectVoV2>> search(ProjectQryParam param);
    Result<PageDataVo<ProjectVoV2>> searchByPage(ProjectQryParam param);

    Result<List<ProjectVoV2>> listByCond(ProjectQryParam param);

    Result<List<NodeUserRelVo>> getProjectMembers(ProjectUserQryParam param);

    Result modifyMember(ProjectMemberUpdateParam param);

    Result generateCode(ProjectGenParam param);

    Result<Boolean> topSet(ProjectTopSetParam param);

}