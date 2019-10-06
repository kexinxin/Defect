import java.io.*;
import java.util.ArrayList;
import java.util.List;

//用于处理代码度量元的标注
public class DealWithMetrics {
    public static void main(String[] args){
        String outFileName="I:\\tools\\LabelColor\\xml\\atmosphere.csv";
        String bugFile="atmosphere2BugFile";


        List<String> bugfileList=new ArrayList<String>();
        try (FileReader reader = new FileReader(bugFile);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //System.out.println(line);
                bugfileList.add(line.split("/")[line.split("/").length-1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<String> resultList=new ArrayList<String>();
        try (FileReader reader = new FileReader(outFileName);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String result="";
                String[] tokens=line.split(",");
                int isBug=0;
                for(String str:bugfileList){
                    if(str.contains(tokens[0])) isBug=1;
                }
                for(int i=1;i<tokens.length;i++){
                    result+=tokens[i]+",";
                    //System.out.print(tokens[i]+" ");
                }
                result+=isBug+"";
                System.out.println(result);
                resultList.add(result);
                //System.out.println(isBug+"");
                //String index=line.split(" ")[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        String reusltFileName="I:\\tools\\LabelColor\\xml\\atmosphereResult.csv";
        try {
            File writeName = new File(reusltFileName); // 相对路径，如果没有则要建立一个新的output.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                for(String line:resultList){
                    out.write(line+"\r\n");
                }
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
