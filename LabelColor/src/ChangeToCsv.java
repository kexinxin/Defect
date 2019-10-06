import java.io.*;
import java.util.HashMap;

public class ChangeToCsv {
    public static void main(String[] args) throws IOException {
        String inputRelationName="igraph2Relation.txt";
        String inputLabelName="igraph2Label.txt";
        String outLabelName="igraph2LabelChange.txt";
        String outRelationName="igraph2RelationChange.csv";

        int count=0;
        HashMap<String,String> hashMap=new HashMap<String,String>();
        try (FileReader reader = new FileReader(inputLabelName);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                String index=line.split(" ")[0];
                hashMap.put(index,count+"");
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        File writeName = new File(outLabelName); // 相对路径，如果没有则要建立一个新的output.txt文件
        writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
        FileWriter writer = new FileWriter(writeName);
        BufferedWriter out = new BufferedWriter(writer);
        try (FileReader reader = new FileReader(inputLabelName);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                out.write(hashMap.get(line.split(" ")[0])+","+line.split(" ")[1]+"\r\n");
            }
            out.flush(); // 把缓存区内容压入文件
        } catch (IOException e) {
            e.printStackTrace();
        }



        writeName = new File(outRelationName); // 相对路径，如果没有则要建立一个新的output.txt文件
        writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
        writer = new FileWriter(writeName);
        out = new BufferedWriter(writer);
        try (FileReader reader = new FileReader(inputRelationName);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            while ((line = br.readLine()) != null) {
              System.out.println(line);
              out.write(hashMap.get(line.split(" ")[0])+","+hashMap.get(line.split(" ")[1])+"\r\n");
            }
            out.flush(); // 把缓存区内容压入文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
