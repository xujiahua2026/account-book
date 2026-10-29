package com.jzbook.accountbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** AI 一句话记账请求 */
public class AiTextRequest {

    @NotBlank(message = "请说一句话，例如：昨天午饭38块")
    @Size(max = 200, message = "一次最多 200 个字")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
