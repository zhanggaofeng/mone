package com.xiaomi.mone.tpc.rpc;

import com.xiaomi.mone.dubbo.docs.annotations.ApiModule;
import com.xiaomi.mone.tpc.api.service.ProjectAndOrgFacade;
import com.xiaomi.mone.tpc.common.param.NodeAndOrgQryParam;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.mone.tpc.common.vo.ProjectOrgsVo;
import com.xiaomi.mone.tpc.common.vo.ResultVo;
import com.xiaomi.mone.tpc.login.anno.AuthCheck;
import com.xiaomi.mone.tpc.node.NodeService;
import com.xiaomi.mone.tpc.util.ResultUtil;
import com.xiaomi.youpin.infra.rpc.Result;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/10/26 11:31
 */
@ApiModule(value = "项目及部门管理", apiInterface = ProjectAndOrgFacade.class)
@DubboService(timeout = 10000, group = "${dubbo.group}", version="1.0", retries = 0)
public class ProjectAndOrgFacadeImpl implements ProjectAndOrgFacade {

    @Autowired
    private NodeService nodeService;

    /**
     * 项目列表及部门信息读取
     * @param param
     * @return
     */
    @AuthCheck(authUser = false)
    @Override
    public Result<PageDataVo<ProjectOrgsVo>> list(NodeAndOrgQryParam param) {
        ResultVo<PageDataVo<ProjectOrgsVo>> resultVo = nodeService.projectAndOrgList(param);
        return ResultUtil.build(resultVo);
    }
}
