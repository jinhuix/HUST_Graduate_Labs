package com.xjh.utils;

public class Config {
    //map reduce的输入输出
    public static final String CLASS_SUM_MAP_INPUT = "hdfs://host2:9000/CountryTrain";
    public static final String CLASS_SUM_REDUCE_OUTPUT = "hdfs://host2:9000/Result/ClassCount";
    public static final String TERM_REDUCE_OUTPUT = "hdfs://host2:9000/Result/WordCount";
    public static final String PREDICT_MAP_INPUT = "hdfs://host2:9000/CountryTest";
    public static final String PREDICT_REDUCE_OUTPUT = "hdfs://host2:9000/Result/Predict";
    //计算的结果文件
    public static final String CLASS_SUM_LOC = "hdfs://host2:9000/Result/ClassCount/part-r-00000";
    public static final String TERM_LOC = "hdfs://host2:9000/Result/WordCount/part-r-00000";
    //类别
    public static final String AUSTR = "AUSTR";
    public static final String BRAZ = "BRAZ";
    public static final String INDIA = "INDIA";
    public static final String[] CLASS_ARRAY = {"AUSTR", "BRAZ", "INDIA"};
}
