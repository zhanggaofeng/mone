package com.xiaomi.mone.tpc.api.service;

import com.xiaomi.mone.tpc.common.param.NodeAndOrgQryParam;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.mone.tpc.common.vo.ProjectOrgsVo;
import com.xiaomi.youpin.infra.rpc.Result;

public interface ProjectAndOrgFacade {

    /**
     * 查询项目和部门信息
     * @param param
     * @return
     */
    Result<PageDataVo<ProjectOrgsVo>> list(NodeAndOrgQryParam param);

}