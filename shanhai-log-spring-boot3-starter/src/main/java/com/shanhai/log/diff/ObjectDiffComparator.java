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

import java.lang.reflect.Field;
import java.util.*;

/**
 * 对象差异对比器
 * @author Shanhai
 */
public class ObjectDiffComparator {
    /**
     * 比较两个对象的差异
     */
    public static List<DiffItem> compare(Object sourceObj, Object targetObj) {
        if (sourceObj == null && targetObj == null) {
            return Collections.emptyList();
        }

        if (sourceObj == null || targetObj == null) {
            DiffItem item = new DiffItem("", "", sourceObj, targetObj,
                    sourceObj == null ? DiffItem.DiffType.ADDED : DiffItem.DiffType.REMOVED);
            return Arrays.asList(item);
        }

        return compareObjects(sourceObj, targetObj);
    }

    /**
     * 比较两个对象的所有字段
     */
    private static List<DiffItem> compareObjects(Object sourceObj, Object targetObj) {
        List<DiffItem> diffs = new ArrayList<>();

        // 获取源对象的所有字段
        Map<String, Field> sourceFields = getAllFields(sourceObj.getClass());
        Map<String, Field> targetFields = getAllFields(targetObj.getClass());

        Set<String> allFieldNames = new HashSet<>(sourceFields.keySet());
        allFieldNames.addAll(targetFields.keySet());
        for (String fieldName : allFieldNames) {
            Field sourceField = sourceFields.get(fieldName);
            Field targetField = targetFields.get(fieldName);

            try {
                Object sourceValue = null;
                Object targetValue = null;

                if (sourceField != null) {
                    sourceField.setAccessible(true);
                    sourceValue = sourceField.get(sourceObj);
                }

                if (targetField != null) {
                    targetField.setAccessible(true);
                    targetValue = targetField.get(targetObj);
                }

                if (sourceValue == null && targetValue == null) {
                    continue;
                }
                DiffItem diffItem = compareField(fieldName, sourceField, targetField,
                        sourceValue, targetValue);
                if (diffItem != null && diffItem.getDiffType()  != DiffItem.DiffType.UNCHANGED) {
                    diffs.add(diffItem);
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("无法访问字段: " + fieldName, e);
            }
        }

        return diffs;
    }

    /**
     * 比较单个字段
     */
    private static DiffItem compareField(String fieldName, Field sourceField, Field targetField,
                                         Object sourceValue, Object targetValue) {

        String fieldDescription = getFieldDescription(sourceField, targetField);
        // 判断字段是否存在
        if (sourceField == null) {
            return new DiffItem(fieldName, fieldDescription, null, targetValue, DiffItem.DiffType.ADDED);
        }

        if (targetField == null) {
            return new DiffItem(fieldName, fieldDescription, sourceValue, null, DiffItem.DiffType.REMOVED);
        }

        // 检查是否需要跳过对比
        if (!shouldCompare(sourceField, targetField)) {
            return null;
        }

        // 比较值
        if (sourceValue == null && targetValue == null) {
            return new DiffItem(fieldName, fieldDescription, sourceValue, targetValue, DiffItem.DiffType.UNCHANGED);
        }

        if (sourceValue == null) {
            return new DiffItem(fieldName, fieldDescription, sourceValue, targetValue, DiffItem.DiffType.ADDED);
        }

        if (targetValue == null) {
            return new DiffItem(fieldName, fieldDescription, sourceValue, targetValue, DiffItem.DiffType.REMOVED);
        }

        // 跳过复杂对象
        if (isComplexObject(sourceValue)) {
           return null;
        }

        // 简单值比较
        if (Objects.equals(sourceValue,  targetValue)) {
            return new DiffItem(fieldName, fieldDescription, sourceValue, targetValue, DiffItem.DiffType.UNCHANGED);
        } else {
            return new DiffItem(fieldName, fieldDescription, sourceValue, targetValue, DiffItem.DiffType.MODIFIED);
        }
    }
    /**
     * 获取字段描述
     */
    private static String getFieldDescription(Field sourceField, Field targetField) {
        Field field = sourceField != null ? sourceField : targetField;
        if (field != null) {
            DiffField annotation = field.getAnnotation(DiffField.class);
            if (annotation != null && !annotation.description().isEmpty())  {
                return annotation.description();
            }
        }
        return "";
    }
    /**
     * 判断是否应该参与对比
     */
    private static boolean shouldCompare(Field sourceField, Field targetField) {
        Field field = sourceField != null ? sourceField : targetField;
        if (field != null) {
            DiffField annotation = field.getAnnotation(DiffField.class);
            if (annotation != null) {
                return annotation.compare();
            }
        }
        // 默认参与对比
        return true;
    }

    /**
     * 判断是否为复杂对象
     */
    private static boolean isComplexObject(Object obj) {
        if (obj == null) {
            return false;
        }

        Class<?> clazz = obj.getClass();
        return !(clazz.isPrimitive()  ||
                clazz.getName().startsWith("java.lang")  ||
                clazz.getName().startsWith("java.util")  ||
                clazz.isEnum());
    }
    /**
     * 获取类及其父类的所有字段
     */
    private static Map<String, Field> getAllFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<>();
        while (clazz != null && clazz != Object.class)  {
            for (Field field : clazz.getDeclaredFields())  {
                if (!fields.containsKey(field.getName()))  {
                    fields.put(field.getName(),  field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}