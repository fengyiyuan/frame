package com.feng.core.project.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.DefaultCommentGenerator;

public class FengCommentGenerator extends DefaultCommentGenerator{

    
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        
        String remarks = introspectedColumn.getRemarks();
        
        if( !"".equals(remarks) ){
            field.addJavaDocLine("/**"); 
            field.addJavaDocLine(" * " + introspectedColumn.getRemarks()); 
            field.addJavaDocLine(" */");
        }
        
    }
    
    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable,
            IntrospectedColumn introspectedColumn) {
    }
    
    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable,
            IntrospectedColumn introspectedColumn) {
    }
    
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        innerClass.addJavaDocLine("/**"); 
        innerClass.addJavaDocLine(" * " + introspectedTable.getFullyQualifiedTable()); 
        innerClass.addJavaDocLine(" */");
    }
    
    // xml 注释
    @Override
    public void addComment(XmlElement xmlElement) {
    }
    
    
    // mapper.java 注释
    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
    }
}
