package szz;

import java.util.HashSet;
import java.util.Set;

public class Process {
    public int add;
    public int delete;
    public int modifier;
    public int authorNumber;
    public boolean isBug=false;
    public int currentData;
    public String fileName="";
    public Set<String> emials=new HashSet<String>();

    public Process(int add,int delete, int modifier){
        this.add=add;
        this.delete=delete;
        this.modifier=modifier;
    }

    public Process(){

    }
}
