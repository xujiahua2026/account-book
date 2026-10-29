package com.jzbook.accountbook.dto;

import jakarta.validation.constraints.NotNull;

/** 修改记录的请求体：在新增字段的基础上多一个 id */
public class RecordUpdateRequest extends RecordRequest {

    @NotNull(message = "记录 id 不能为空")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
