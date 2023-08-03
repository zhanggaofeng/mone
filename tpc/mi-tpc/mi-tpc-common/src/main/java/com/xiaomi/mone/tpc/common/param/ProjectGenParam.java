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
public class ProjectGenParam extends BaseParam implements Serializable {

    private ProjectVo projectVo;


    @Override
    public boolean argCheck() {
        if (projectVo == null) {
            return false;
        }
        return true;
    }
}
