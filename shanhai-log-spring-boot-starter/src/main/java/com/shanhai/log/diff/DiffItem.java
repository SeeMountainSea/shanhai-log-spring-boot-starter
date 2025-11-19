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

import java.util.ArrayList;
import java.util.List;

/**
 * 差异项
 * @author Shanhai
 */
public class DiffItem {
    private String fieldName;
    private String fieldDescription;
    private Object sourceValue;
    private Object targetValue;
    private DiffType diffType;
    // 嵌套差异
    private List<DiffItem> nestedDiffs;

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
        this.nestedDiffs  = new ArrayList<>();
    }

    // Getters and Setters
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName  = fieldName; }

    public String getFieldDescription() { return fieldDescription; }
    public void setFieldDescription(String fieldDescription) { this.fieldDescription  = fieldDescription; }

    public Object getSourceValue() { return sourceValue; }
    public void setSourceValue(Object sourceValue) { this.sourceValue  = sourceValue; }

    public Object getTargetValue() { return targetValue; }
    public void setTargetValue(Object targetValue) { this.targetValue  = targetValue; }

    public DiffType getDiffType() { return diffType; }
    public void setDiffType(DiffType diffType) { this.diffType  = diffType; }

    public List<DiffItem> getNestedDiffs() { return nestedDiffs; }
    public void addNestedDiff(DiffItem diffItem) { this.nestedDiffs.add(diffItem);  }

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

        if (!nestedDiffs.isEmpty())  {
            sb.append(",  nestedDiffs=").append(nestedDiffs);
        }
        sb.append('}');
        return sb.toString();
    }
}
