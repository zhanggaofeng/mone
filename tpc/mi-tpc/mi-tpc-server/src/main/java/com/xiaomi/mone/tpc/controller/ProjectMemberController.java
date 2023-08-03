package com.xiaomi.mone.tpc.controller;

import com.xiaomi.mone.tpc.aop.ArgCheck;
import com.xiaomi.mone.tpc.common.param.ProjectMemberQryParam;
import com.xiaomi.mone.tpc.common.param.ProjectMemberUpdateParam;
import com.xiaomi.mone.tpc.common.vo.NodeUserRelVo;
import com.xiaomi.mone.tpc.common.vo.ResultVo;
import com.xiaomi.mone.tpc.project.ProjectMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 19:41
 */
@Slf4j
@RestController
@RequestMapping(value = "/backend/project/member")
public class ProjectMemberController {

    @Autowired
    private ProjectMemberService projectMemberService;

    @ArgCheck
    @RequestMapping(value = "/list")
    public ResultVo<Map<Integer, List<NodeUserRelVo>>> list(@RequestBody ProjectMemberQryParam param) {
        return projectMemberService.list(param);
    }

    @ArgCheck
    @RequestMapping(value = "/update")
    public ResultVo update(@RequestBody ProjectMemberUpdateParam param) {
        return projectMemberService.update(false, param);
    }


}
