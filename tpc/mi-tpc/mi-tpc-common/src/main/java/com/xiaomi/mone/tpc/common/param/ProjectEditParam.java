package com.xiaomi.mone.tpc.common.param;

import com.xiaomi.mone.application.api.bo.ProjectVo;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @project: mi-tpc
 * @author: zgf1
 * @date: 2022/3/3 19:52
 */
@Data
@ToString(callSuper = true)
public class ProjectEditParam extends BaseParam implements Serializable {

    private Long parentNodeId;
    private String nodeName;
    private String desc;
    private OrgInfoParam orgParam;
    private ProjectVo projectVo;
    private boolean validateRole = true;


    @Override
    public boolean argCheck() {
        if (projectVo == null) {
            return false;
        }
        if (projectVo.getId() <= 0L) {
            return false;
        }
        if (orgParam != null && !orgParam.argCheck()) {
            return false;
        }
        return true;
    }
}
