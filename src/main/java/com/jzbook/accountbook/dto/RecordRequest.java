package com.jzbook.accountbook.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 新增 / 修改记账记录的请求体 */
public class RecordRequest {

    @NotNull(message = "类型不能为空（1 支出，2 收入）")
    @Min(value = 1, message = "类型只能是 1（支出）或 2（收入）")
    @Max(value = 2, message = "类型只能是 1（支出）或 2（收入）")
    private Integer type;

    @NotBlank(message = "分类不能为空")
    @Size(max = 20, message = "分类不能超过 20 个字")
    private String category;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amount;

    /** 不传则默认今天 */
    private LocalDate recordDate;

    @Size(max = 50, message = "备注不能超过 50 个字")
    private String remark;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
