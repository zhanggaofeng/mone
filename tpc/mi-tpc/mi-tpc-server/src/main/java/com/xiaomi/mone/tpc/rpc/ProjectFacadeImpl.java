package com.xiaomi.mone.tpc.rpc;

import com.xiaomi.mone.dubbo.docs.annotations.ApiDoc;
import com.xiaomi.mone.dubbo.docs.annotations.ApiModule;
import com.xiaomi.mone.tpc.aop.ArgCheck;
import com.xiaomi.mone.tpc.api.service.ProjectFacade;
import com.xiaomi.mone.tpc.common.param.*;
import com.xiaomi.mone.tpc.common.vo.*;
import com.xiaomi.mone.tpc.project.ProjectMemberService;
import com.xiaomi.mone.tpc.project.ProjectService;
import com.xiaomi.mone.tpc.util.ResultUtil;
import com.xiaomi.youpin.infra.rpc.Result;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@ApiModule(value = "项目管理", apiInterface = ProjectFacade.class)
@DubboService(timeout = 10000, group = "${dubbo.group}", version="1.0")
public class ProjectFacadeImpl implements ProjectFacade {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMemberService projectMemberService;

    @ApiDoc("项目创建")
    @ArgCheck
    @Override
    public Result<ProjectVoV2> add(ProjectCreateParam param) {
        ResultVo<ProjectVoV2> resultVo = projectService.add(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目编辑")
    @ArgCheck
    @Override
    public Result edit(ProjectEditParam param) {
        ResultVo<ProjectVoV2> resultVo = projectService.edit(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目删除")
    @ArgCheck
    @Override
    public Result delete(ProjectDeleteParam param) {
        ResultVo<ProjectVoV2> resultVo = projectService.delete(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目详情")
    @ArgCheck
    @Override
    public Result<ProjectVoV2> get(ProjectQryParam param) {
        ResultVo<ProjectVoV2> resultVo = projectService.get(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目检索")
    @ArgCheck
    @Override
    public Result<List<ProjectVoV2>> search(ProjectQryParam param) {
        ResultVo<PageDataVo<ProjectVoV2>> resultVo = projectService.searchByPage(param);
        ResultVo<List<ProjectVoV2>> listResultVo = ResponseCode.SUCCESS.build((resultVo == null || resultVo.getData() == null) ? null : resultVo.getData().getList());
        return ResultUtil.build(listResultVo);
    }

    @ApiDoc("项目置顶")
    @ArgCheck
    @Override
    public Result<Boolean> topSet(ProjectTopSetParam param) {
        ResultVo<Boolean> resultVo = projectService.topProject(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目列表查询-分页")
    @ArgCheck
    @Override
    public Result<PageDataVo<ProjectVoV2>> searchByPage(ProjectQryParam param) {
        ResultVo<PageDataVo<ProjectVoV2>> resultVo = projectService.searchByPage(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目查询-根据条件")
    @ArgCheck
    @Override
    public Result<List<ProjectVoV2>> listByCond(ProjectQryParam param) {
        ResultVo<List<ProjectVoV2>> resultVo = projectService.listByCond(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目成员查询")
    @ArgCheck
    @Override
    public Result<List<NodeUserRelVo>> getProjectMembers(ProjectUserQryParam param) {
        ResultVo<List<NodeUserRelVo>> resultVo = projectService.getProjectMembers(param);
        return ResultUtil.build(resultVo);
    }

    @ApiDoc("项目成员修改")
    @ArgCheck
    @Override
    public Result modifyMember(ProjectMemberUpdateParam param) {
        ResultVo resultVo = projectMemberService.update(true, param);
        return ResultUtil.build(resultVo);
    }

    @Override
    public Result generateCode(ProjectGenParam param) {
        ResultVo<List<NodeUserRelVo>> resultVo = projectService.generateCode(param);
        return ResultUtil.build(resultVo);
    }

}
