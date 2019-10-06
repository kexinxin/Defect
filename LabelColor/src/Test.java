import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args){
        String pathname = "relation1.txt";
        List<String> fileList=new ArrayList<String>();
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                System.out.println(line);
                String[] lines=line.split(" ");
                if(!fileList.contains(lines[0])) fileList.add(lines[0]);
                if(!fileList.contains(lines[1])) fileList.add(lines[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(fileList.size());
    }
}
