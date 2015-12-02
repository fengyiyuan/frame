package com.feng.core.project.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.Maps;

import freemarker.template.Template;

public class Generator {

    private Properties prop;
    
    public void mybatisGenerate(){
        InputStream configFile = null;
        try {
            List<String> warnings = new ArrayList<String>();
            boolean overwrite = true;
            
            configFile = Generator.class.getResource("config/generatorConfig.xml").openStream();
            
            
            
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configFile);
            
            List<Context> list = config.getContexts();
            for (Context context : list) {
                
                PluginConfiguration pluginConfiguration = new PluginConfiguration();
                pluginConfiguration.setConfigurationType(FengMybatisPlugin.class.getName());
                
                context.addPluginConfiguration(pluginConfiguration);
                
                CommentGeneratorConfiguration commmentConfig = new CommentGeneratorConfiguration();
                commmentConfig.setConfigurationType(FengCommentGenerator.class.getName());
                commmentConfig.addProperty("suppressAllComments", "true");
                commmentConfig.addProperty("suppressDate", "true");
                context.setCommentGeneratorConfiguration(commmentConfig);
                
                
                JDBCConnectionConfiguration jdbcConfig = context.getJdbcConnectionConfiguration();
                String driver = prop.getProperty("mysql.driverClass");
                String url = prop.getProperty("mysql.url");
                String userId = prop.getProperty("mysql.userId");
                String password = prop.getProperty("mysql.password");
                String rootClass = prop.getProperty("javaModel.rootClass");
                
                jdbcConfig.setDriverClass(driver);
                jdbcConfig.setConnectionURL(url);
                jdbcConfig.setUserId(userId);
                jdbcConfig.setPassword(password);
                
                Map<String, String> dbConfigMap = Maps.newHashMap();
                dbConfigMap.put("driverClassName", driver);
                dbConfigMap.put("url", url);
                dbConfigMap.put("username", userId);
                dbConfigMap.put("password", password);
                
                
                DataSource ds = DruidDataSourceFactory.createDataSource(dbConfigMap);
                JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
                List<Map<String, Object>> tableList = jdbcTemplate.queryForList(" SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? ", prop.getProperty("mysql.schema"));
                
                String targetProject = prop.getProperty("targetProject");
                
                String jtp = prop.getProperty("targetPackage.javaModel");
                JavaModelGeneratorConfiguration javaModelConfig = context.getJavaModelGeneratorConfiguration();
                javaModelConfig.setTargetProject(targetProject);
                javaModelConfig.setTargetPackage(jtp);
                
                if(rootClass != null && !"".equals(rootClass)){
                    javaModelConfig.addProperty("rootClass", rootClass);
                }
                
                String stp = prop.getProperty("targetPackage.mapper");
                SqlMapGeneratorConfiguration sqlMapConfig = context.getSqlMapGeneratorConfiguration();
                sqlMapConfig.setTargetProject(targetProject);
                sqlMapConfig.setTargetPackage(stp);
                
                String ctp = prop.getProperty("targetPackage.dao");
                JavaClientGeneratorConfiguration javaClientConfig = context.getJavaClientGeneratorConfiguration();
                javaClientConfig.setTargetProject(targetProject);
                javaClientConfig.setTargetPackage(ctp);
                
                
                List<TableConfiguration> tableConfList = context.getTableConfigurations();
                tableConfList.clear();
                TableConfiguration tableConf = null;
                for(int i = 0 ; i < tableList.size() ; i++){
                    tableConf = new TableConfiguration(context);
                    Map<String, Object> map = tableList.get(i);
                    tableConf.setTableName(map.get("TABLE_NAME").toString());
                    
                    tableConf.setUpdateByExampleStatementEnabled(false);
                    tableConf.setSelectByExampleStatementEnabled(false);
                    tableConf.setUpdateByExampleStatementEnabled(false);
                    tableConf.setDeleteByExampleStatementEnabled(false);
                    tableConf.setCountByExampleStatementEnabled(false);
                    
                    tableConfList.add(tableConf);
                }
                
            }
            
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);
            
            System.out.println(warnings);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            IOUtils.closeQuietly(configFile);
        }
    }
    
    public void fileGenerator(){
        try {
            String targetProject = prop.getProperty("targetProject");
            String basePackage = prop.getProperty("module.base.package");
            String baseFile = targetProject + "/" + basePackage.replaceAll("\\.", "/");
            File file = new File(baseFile);
            if(!file.exists()){
               file.mkdirs();
               System.out.println(baseFile + " created.");
            }else{
               System.out.println(baseFile + " exist, skip.");
            }
            String modules = prop.getProperty("module.names");
            String[] moduleArr = modules.split(",");
            String moduleType = prop.getProperty("module.type");
            String[] moduleTypeArr = moduleType.split(",");
            
            for (int i = 0; i < moduleArr.length; i++) {
                String module = moduleArr[i];
                for (int j = 0; j < moduleTypeArr.length; j++) {
                    String type = moduleTypeArr[j];
                    
                    String path = baseFile + "/" + module + "/" + type;
                    
                    boolean implFlag = true;
                    if("controller".equals(type)){
                        implFlag = false;
                    }
                    makeFile(type, path, basePackage, module, false);
                    if(implFlag){
                        makeFile(type, path + "/impl", basePackage, module, true);
                    }
                }
            }
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void makeFile(String type ,String path, String basePackage, String  module, boolean impl){
        FileWriter fw = null;
        try {
            makeDir(path);
            
            Template template = getTemplate(type + (impl?"Impl":"") + ".ftl");
            Map<String,String> root = Maps.newHashMap();
            
            root.put("modulePackage", basePackage +"." + module + "." + type);
            root.put("moduleName", StringUtils.capitalize(module));
            
            
            File typeFile = new File(path + "/" + StringUtils.capitalize(module) + StringUtils.capitalize(type) + (impl?"Impl":"") + ".java");
            if(!typeFile.exists()){
                typeFile.createNewFile();
                fw = new FileWriter(typeFile);
                template.process(root, fw);
                fw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            IOUtils.closeQuietly(fw);
        }
    }
    
    private void makeDir(String path){
        File tempFile = new File(path);
        if(!tempFile.exists()){
            tempFile.mkdirs();
            System.out.println(path + " created.");
        }else{
            System.out.println(path + " exist, skip.");
        }
    }
    
    
    public Template getTemplate(String name) {
        try {
            freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
            cfg.setDirectoryForTemplateLoading(new File(this.getClass().getResource("ftl").getPath()));
            Template temp = cfg.getTemplate(name);
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void init(){
        InputStream is = null;
        try {
             is = Generator.class.getResource("config/generatorConfig.properties").openStream();
             prop = new Properties();
             prop.load(is);
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            IOUtils.closeQuietly(is);
        }
        
    }
    
    
    public static Generator newInstance(){
        return new Generator();
    }
    
   public static void main(String[] args) {
       Generator instance = Generator.newInstance();
       
       instance.init();
       
       instance.mybatisGenerate();
       
       instance.fileGenerator();
   }
}
