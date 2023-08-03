package com.xiaomi.mone.tpc.common.param;

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
public class ProjectDeleteParam extends BaseParam implements Serializable {

    private Long projectId;
    private boolean validateRole = true;


    @Override
    public boolean argCheck() {
        if (projectId == null) {
            return false;
        }
        return true;
    }
}
