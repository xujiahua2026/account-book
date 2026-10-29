package com.jzbook.accountbook.service;

import com.jzbook.accountbook.entity.Record;

/**
 * AI 能力：把一句自然语言解析成记账记录
 */
public interface AiService {

    /**
     * 解析用户输入并落库
     * @param userId 用户ID
     * @param text   自然语言，例如"昨天午饭38块"
     * @return 保存后的记录
     */
    Record parseAndSave(Long userId, String text);

    /** 是否已配置密钥 */
    boolean isKeyConfigured();

    /** 密钥长度（排查用，不暴露内容） */
    int getKeyLength();

    /** 密钥是否以 BOM 开头（记事本保存常见问题） */
    boolean hasLeadingBom();

    /** 密钥是否包含空白字符 */
    boolean hasWhitespace();

    /** 密钥是否被引号包裹 */
    boolean hasQuotes();

    /** 当前使用的模型名 */
    String getModel();
}
