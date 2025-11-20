/*
 * MIT License
 *
 * Copyright (c) 2021 SeeMountainSea
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.shanhai.log.diff;

import lombok.Getter;
import lombok.Setter;

/**
 * 差异项
 * @author Shanhai
 */
@Getter
@Setter
public class DiffItem {
    private String fieldName;
    private String fieldDescription;
    private Object sourceValue;
    private Object targetValue;
    private DiffType diffType;

    public enum DiffType {
        // 新增
        ADDED,
        // 删除
        REMOVED,
        // 修改
        MODIFIED,
        // 无变化
        UNCHANGED
    }

    public DiffItem(String fieldName, String fieldDescription, Object sourceValue,
                    Object targetValue, DiffType diffType) {
        this.fieldName  = fieldName;
        this.fieldDescription  = fieldDescription;
        this.sourceValue  = sourceValue;
        this.targetValue  = targetValue;
        this.diffType  = diffType;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DiffItem{");
        sb.append("fieldName='").append(fieldName).append('\'');
        if (fieldDescription != null && !fieldDescription.isEmpty())  {
            sb.append(",  description='").append(fieldDescription).append('\'');
        }
        sb.append(",  diffType=").append(diffType);
        if (diffType == DiffType.MODIFIED) {
            sb.append(",  sourceValue=").append(sourceValue);
            sb.append(",  targetValue=").append(targetValue);
        } else if (diffType == DiffType.ADDED) {
            sb.append(",  newValue=").append(targetValue);
        } else if (diffType == DiffType.REMOVED) {
            sb.append(",  oldValue=").append(sourceValue);
        }
        sb.append('}');
        return sb.toString();
    }
}
