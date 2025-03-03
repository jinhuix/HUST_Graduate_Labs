package com.xjh.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;


// 预测工具类，计算先验概率和条件概率
public class Predition {
    private static Map<String, Double> priors;          // 先验概率
    private static Map<String, Double> termProbability; // 条件概率 <<类别,单词>,概率>
    private static Set<String> termSet;                 // 训练集中单词集合V
    private static Map<String, Double> classTermSum;    // 训练集中三类数据的单词个数

    // 获取先验概率
    public static Map<String, Double> getPriors() {
        return priors;
    }

    // 获取条件概率
    public static Map<String, Double> getTermProbability() {
        return termProbability;
    }

    // 获取训练集中的单词集合
    public static Set<String> getTermSet() {
        return termSet;
    }

    // 获取每个类的单词总数
    public static Map<String, Double> getClassTermSum() {
        return classTermSum;
    }

    //当类被调用的时候计算概率，并保存到类成员变量中
    static {
        try {
            calPriors();
            calTermProbability();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //计算先验概率，注意加log()
    private static void calPriors() throws IOException {
        double totalSum = 0.0;
        List<String> classNames = new ArrayList<>();
        List<Integer> classCounts = new ArrayList<>();

        // 读取类的统计信息
        try (FileSystem fs = FileSystem.get(URI.create(Config.CLASS_SUM_LOC), new Configuration());
                InputStream in = fs.open(new Path(Config.CLASS_SUM_LOC));
                Scanner scanner = new Scanner(in)) {

            while (scanner.hasNext()) {
                String className = scanner.next();
                int classCount = scanner.nextInt();
                classNames.add(className);
                classCounts.add(classCount);
                totalSum += classCount;
            }
        }

        // 计算每个类的先验概率
        for (int i = 0; i < classNames.size(); i++) {
            priors.put(classNames.get(i), Math.log(classCounts.get(i) / totalSum));
        }
    }

    // 计算每个单词出现的概率
    // 最后计算结果，<<类别,单词>,概率>
    private static void calTermProbability() throws IOException {
        Map<String, Integer> termCounts = new HashMap<>();
        double austrSum = 0.0, brazSum = 0.0, indiaSum = 0.0;

        // 读取训练集中的单词统计信息
        try (FileSystem fs = FileSystem.get(URI.create(Config.TERM_LOC), new Configuration());
                InputStream in = fs.open(new Path(Config.TERM_LOC));
                Scanner scanner = new Scanner(in)) {

            while (scanner.hasNext()) {
                String termName = scanner.next();
                int termCount = scanner.nextInt();
                String[] split = termName.split(",");

                switch (split[0]) {
                    case Config.AUSTR: austrSum += termCount; break;
                    case Config.BRAZ: brazSum += termCount; break;
                    case Config.INDIA: indiaSum += termCount; break;
                }

                termSet.add(split[1]); // 添加到单词集合
                termCounts.put(termName, termCount);
            }
        }

        // 计算每个类别的单词总数
        classTermSum.put(Config.AUSTR, austrSum);
        classTermSum.put(Config.BRAZ, brazSum);
        classTermSum.put(Config.INDIA, indiaSum);

        // 计算条件概率
        int vocabularySize = termSet.size();
        for (String word : termSet) {
            calculateWordProbability(Config.AUSTR, word, austrSum, termCounts, vocabularySize);
            calculateWordProbability(Config.BRAZ, word, brazSum, termCounts, vocabularySize);
            calculateWordProbability(Config.INDIA, word, indiaSum, termCounts, vocabularySize);
        }
    }

    // 计算单个单词的条件概率
    private static void calculateWordProbability(String className, String word, double classSum,Map<String, Integer> termCounts, int vocabSize) {
        String key = className + "," + word;
        int wordCount = termCounts.getOrDefault(key, 0);
        double probability = Math.log((wordCount + 1) / (classSum + vocabSize));
        termProbability.put(key, probability);
    }
}
