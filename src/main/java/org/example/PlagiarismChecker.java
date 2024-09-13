package org.example;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlagiarismChecker {

    // 中文分词
    private Set<String> tokenize(String text) {
        List<Term> terms = HanLP.segment(text);
        Set<String> words = new HashSet<>();
        for (Term term : terms) {
            words.add(term.word); // 提取Term中的word字段
        }
        return words; // 返回唯一的单词集合
    }

    // 计算Jaccard相似度
    private double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }

    // 从文件中读取分词结果
    private Set<String> tokenizeFile(String filePath) {
        Set<String> tokens = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                tokens.addAll(tokenize(line)); // 对每一行进行分词
            }
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }
        return tokens;
    }

    public void checkPlagiarism(String originalText, String plagiarizedText, String outputFilePath) {
        Set<String> tokens1 = tokenize(originalText);
        Set<String> tokens2 = tokenize(plagiarizedText);

        double similarity = jaccardSimilarity(tokens1, tokens2);
        double percentage = similarity * 100;

        String result = String.format("原文与抄袭文的相似度: %.2f%%\n", percentage);
        System.out.println(result);

        if (similarity > 0.5) { // 设置阈值为50%
            result += "警告：可能存在抄袭行为！\n";
        } else {
            result += "文本之间没有明显的抄袭行为。\n";
        }

        writeResultsToFile(outputFilePath, result);
    }

    // 将结果写入文件
    private void writeResultsToFile(String filePath, String results) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(results);
            System.out.println("结果已写入 " + filePath);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("请提供原文路径、抄袭文路径和输出文件路径: ");
            return;
        }

        String originalFilePath = args[0];
        String plagiarizedFilePath = args[1];
        String outputFilePath = args[2];

        // 读取原文和抄袭文
        StringBuilder originalText = new StringBuilder();
        StringBuilder plagiarizedText = new StringBuilder();

        try {
            // 读取原文
            BufferedReader originalReader = new BufferedReader(new FileReader(originalFilePath));
            String line;
            while ((line = originalReader.readLine()) != null) {
                originalText.append(line).append("\n");
            }
            originalReader.close();

            // 读取抄袭文
            BufferedReader plagiarizedReader = new BufferedReader(new FileReader(plagiarizedFilePath));
            while ((line = plagiarizedReader.readLine()) != null) {
                plagiarizedText.append(line).append("\n");
            }
            plagiarizedReader.close();
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
            return;
        }

        PlagiarismChecker checker = new PlagiarismChecker();
        checker.checkPlagiarism(originalText.toString(), plagiarizedText.toString(), outputFilePath);
    }
}
